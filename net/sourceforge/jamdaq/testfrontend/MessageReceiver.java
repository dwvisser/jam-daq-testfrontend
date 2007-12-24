package net.sourceforge.jamdaq.testfrontend;

import jam.comm.Constants;
import jam.comm.PacketTypes;
import jam.global.GoodThread;
import jam.sort.RingBuffer;

import java.awt.Frame;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Feb 16, 2004
 */
public class MessageReceiver extends GoodThread {
	private transient final DatagramSocket socket;
	private transient final Frame frame;
	private transient final MessageSender sender;
	private transient Future<?> eventGenerator;
	private transient final Console console;

	/**
	 * Creates a new message receiver.
	 * 
	 * @param frame
	 *            the GUI frame of this application
	 * @param port
	 *            that we receive on
	 * @param addr
	 *            that we receive at
	 * @param sender
	 *            used to send messages and data
	 * @throws SocketException
	 *             if we can't bind to the socket
	 * @throws UnknownHostException
	 *             if the host is invalid
	 */
	MessageReceiver(Frame frame, Console console, DatagramSocket localSocket,
			MessageSender sender) throws SocketException, UnknownHostException {
		super();
		this.console = console;
		this.frame = frame;
		this.sender = sender;
		this.socket = localSocket;
	}

	public void run() {
		final DatagramPacket packet = new DatagramPacket(
				new byte[RingBuffer.BUFFER_SIZE], RingBuffer.BUFFER_SIZE);
		while (checkState()) {
			try {
				socket.receive(packet);
				final ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData());
				final int status = byteBuffer.getInt();
				if (status == PacketTypes.OK_MESSAGE.intValue()) {
					final String message = this.unPackMessage(byteBuffer);
					this.console.messageOutln("Recieved: " + message);
					if (message == "START") {
						this.eventGenerator = this.sender
								.startSendingEventData();
					} else if ("STOP" == message) {
						if (null != this.eventGenerator) {
							if (!this.eventGenerator.cancel(true)) {
								LOGGER.severe("Couldn't stop sending events.");
							}
							this.eventGenerator = null;
						}
					}
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), getClass()
						.getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Unpack a datagram with a message. Message packets have an ASCII character
	 * array terminated with \0.
	 * 
	 * @param buffer
	 *            packet contents passed in readable form
	 * @return the string contained in the message
	 */
	private String unPackMessage(final ByteBuffer buffer) {
		final StringBuilder rval = new StringBuilder();
		char next;
		do {
			next = (char) buffer.get();
			rval.append(next);
		} while (next != '\0');
		final int len = rval.length() - 1;
		if (len > Constants.MAX_MESSAGE_SIZE) {// exclude null
			final IllegalArgumentException exception = new IllegalArgumentException(
					"Message length, " + len + ", greater than max allowed, "
							+ Constants.MAX_MESSAGE_SIZE + ".");
			LOGGER.throwing("VMECommunication", "unPackMessage", exception);
			throw exception;
		}
		return rval.substring(0, len);
	}

}
