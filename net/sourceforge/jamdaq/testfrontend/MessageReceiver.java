package net.sourceforge.jamdaq.testfrontend;

import jam.global.GoodThread;
import jam.FrontEndCommunication;
import jam.sort.NetDaemon;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 16, 2004
 */
public class MessageReceiver extends GoodThread {
	private final DatagramSocket socket;
	private final Frame frame;
	
	MessageReceiver(Frame f, int port, String addr) throws SocketException, 
	UnknownHostException {
		super();
		frame=f;
		socket=new DatagramSocket(port, InetAddress.getByName(addr));
	}
	
	public void run(){
		DatagramPacket packet=new DatagramPacket(
		new byte[NetDaemon.BUFFER_SIZE],NetDaemon.BUFFER_SIZE);
		while (checkState()){
			try{
				socket.receive(packet);
				final DataInput di=new DataInputStream(new ByteArrayInputStream(packet.getData()));
				final int status=di.readInt();
				if (status==FrontEndCommunication.OK) {
					unpackMessage(di);
				} else if (status==FrontEndCommunication.CNAF){
				} else if (status==FrontEndCommunication.SCALER_INTERVAL){
				} else if (status==FrontEndCommunication.VME_ADDRESSES){
				} else {
					System.err.println(getClass().getName()+": packet with unknown message type received");
				}
			} catch (IOException e){
				JOptionPane.showMessageDialog(frame,e.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Unpack a datagram with a message. Message packets have an ASCII 
	 * character array terminated with \0.
	 * 
	 * @param messageDis packet contents passed in readable form
	 * @return the string contained in the message
	 * @throws JamException if there's a problem
	 */
	private String unpackMessage(DataInput di) throws IOException {
		final StringBuffer rval=new StringBuffer();
		char ascii;
		while( (ascii=(char)di.readByte()) != '\0' ) {
			rval.append(ascii);
		}
		return rval.toString();
	}

}
