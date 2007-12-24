package net.sourceforge.jamdaq.testfrontend;

import jam.global.JamException;
import jam.sort.RingBuffer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class for sending messages back to Jam.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Feb 16, 2004
 */
public class MessageSender {

	private transient final DatagramSocket socket;
	private transient final Console console;
	private transient final Counter events, buffers;

	/**
	 * Creates a new message sender.
	 * 
	 * @param console
	 *            for outputting messages to the screen
	 * @param port
	 *            that we send on
	 * @param addr
	 *            that we send from
	 * @param jamport
	 *            that we send to
	 * @param jamaddr
	 *            that we send to
	 * @throws SocketException
	 *             if we can't bind to the socket
	 * @throws UnknownHostException
	 *             if an address is invalid
	 */
	MessageSender(Counter events, Counter buffers, Console console,
			DatagramSocket localSocket) throws SocketException,
			UnknownHostException {
		super();
		this.events = events;
		this.buffers = buffers;
		this.console = console;
		this.socket = localSocket;
	}

	/**
	 * Method which is used to send all packets containing a string to the VME
	 * crate.
	 * 
	 * @param status
	 *            one of OK, SCALER, ERROR, CNAF, COUNTER, VME_ADDRESSES or
	 *            SCALER_INTERVAL
	 * @param message
	 *            string to send
	 * @throws JamException
	 *             if there's a problem
	 */
	private void send(final int status, final byte[] message,
			final boolean terminate) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream(
				message.length + 5);
		final DataOutputStream dos = new DataOutputStream(output);
		dos.writeInt(status);// 4-byte int
		dos.write(message);
		if (terminate) {
			dos.write(0);// 8-bit null termination
		}
		final byte[] byteMessage = output.toByteArray();
		dos.close();
		try {// create and send packet
			final DatagramPacket packetMessage = new DatagramPacket(
					byteMessage, byteMessage.length, socket
							.getRemoteSocketAddress());
			socket.send(packetMessage);
		} catch (IOException e) {
			console
					.errorOutln(getClass().getName()
							+ ".send(): "
							+ "Jam encountered a network communication error attempting to send a packet.");
		}
	}

	public void sendMessage(final String message) throws IOException {
		// send(FrontEndCommunication.OK,message.getBytes(US_ASCII),true);
	}

	public void sendError(final String message) throws IOException {
		// send(FrontEndCommunication.ERROR,message.getBytes(US_ASCII),true);
	}

	public void sendCounters(final int[] values) throws IOException {
		// sendIntegers(FrontEndCommunication.COUNTER,values);
	}

	public void sendScalers(final int[] values) throws IOException {
		// sendIntegers(FrontEndCommunication.SCALER,values);
	}

	public void sendIntegers(final int status, final int[] values)
			throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream(
				4 * (values.length + 1));
		final DataOutputStream dos = new DataOutputStream(bytes);
		for (int i = 0; i < values.length; i++) {
			dos.writeInt(values[i]);
		}
		dos.close();
		send(status, bytes.toByteArray(), false);
	}

	public Future<?> startSendingEventData() {
		Future<?> result = null;
		try {
			final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
					1);
			final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
					200L, TimeUnit.MILLISECONDS, queue);
			final EventGenerator eventGenerator = new EventGenerator(events,
					buffers, console, InetAddress.getLocalHost(), 10205);
			result = executor.submit(eventGenerator);
		} catch (SocketException se) {
			console.errorOutln(se.getMessage());
		} catch (UnknownHostException uhe) {
			console.errorOutln(uhe.getMessage());
		}
		return result;
	}

	static class EventGenerator implements Runnable {
		private static final byte[] buffer = RingBuffer.freshBuffer();
		private static final byte[] parameter = { (byte) 0x80, 0x01 };
		private static final byte[] eventEnd = { (byte) 0xff, (byte) 0xff };
		private static final byte[] bufferPad = { (byte) 0xff, (byte) 0xf0 };
		private transient final Random random = new Random();
		private transient final InetAddress address;
		private transient final int port;
		private transient final DatagramSocket socket;
		private transient final Console console;
		private transient final Counter eventCounter, bufferCounter;

		EventGenerator(Counter eventCounter, Counter bufferCounter,
				Console console, InetAddress address, int port)
				throws SocketException {
			this.eventCounter = eventCounter;
			this.bufferCounter = bufferCounter;
			this.console = console;
			this.address = address;
			this.port = port;
			this.socket = new DatagramSocket(port, address);
		}

		private void fillBuffer() {
			final int histLength = 128;
			final int eventLength = 3 * parameter.length;
			final int count = buffer.length / eventLength;
			int currentIndex = 0;
			for (int i = 0; i < count; i++) {
				System.arraycopy(parameter, 0, buffer, currentIndex,
						parameter.length);
				currentIndex += parameter.length;
				buffer[currentIndex] = 0x00;
				currentIndex++;
				buffer[currentIndex] = (byte) random.nextInt(histLength);
				currentIndex++;
				System.arraycopy(eventEnd, 0, buffer, currentIndex,
						eventEnd.length);
				currentIndex += eventEnd.length;
				eventCounter.increment();
			}
			while (currentIndex < buffer.length) {
				System.arraycopy(bufferPad, 0, buffer, currentIndex,
						bufferPad.length);
				currentIndex += bufferPad.length;
			}
		}

		public void run() {
			while (true) {
				fillBuffer();
				try {// create and send packet
					final DatagramPacket packetMessage = new DatagramPacket(
							buffer, buffer.length, this.address, this.port);
					socket.send(packetMessage);
					bufferCounter.increment();
					Thread.sleep(100);
				} catch (IOException e) {
					console
							.errorOutln(getClass().getName()
									+ ".send(): "
									+ "Jam encountered a network communication error attempting to send a packet.");
				} catch (InterruptedException ie) {
					console.messageOutln("Event generator interrupted.");
				}
			}
		}
	}

}
