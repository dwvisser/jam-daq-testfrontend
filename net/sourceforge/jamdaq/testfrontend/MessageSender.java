package net.sourceforge.jamdaq.testfrontend;

import jam.FrontEndCommunication;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Class for sending messages back to Jam.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 16, 2004
 */
public class MessageSender {

	private static final String US_ASCII="US-ASCII";
	private final DatagramSocket socket;
	private final Console console;
	private final InetAddress jamAddress;
	private final int jamPort;
	
	MessageSender(Console c, int port, String addr, int jamport, 
	String jamaddr) throws SocketException, UnknownHostException {
		super();
		console=c;
		jamPort=jamport;
		jamAddress=InetAddress.getByName(jamaddr);
		socket=new DatagramSocket(port, InetAddress.getByName(addr));
	}
		
	/**
	 * Method which is used to send all packets containing a string to
	 * the VME crate.
	 *
	 * @param status one of OK, SCALER, ERROR, CNAF, COUNTER, 
	 * VME_ADDRESSES or SCALER_INTERVAL
	 * @param message string to send
	 * @throws JamException if there's a problem
	 */
	private void send(int status, byte [] message, boolean terminate) throws Exception {
		final ByteArrayOutputStream ba=new ByteArrayOutputStream(message.length+5);
		final DataOutputStream dos=new DataOutputStream(ba);
		dos.writeInt(status);//4-byte int
		dos.write(message);
		if (terminate){
			dos.write(0);//8-bit null termination
		}
		final byte [] byteMessage = ba.toByteArray();
		dos.close();
		try {//create and send packet
			final DatagramPacket packetMessage=new DatagramPacket(byteMessage, byteMessage.length, jamAddress, jamPort);
			socket.send(packetMessage);
		} catch (IOException e) {
			console.errorOutln(getClass().getName()+".send(): "+
				"Jam encountered a network communication error attempting to send a packet.");
		}
	}
	
	
	void sendMessage(String message) throws Exception{
		send(FrontEndCommunication.OK,message.getBytes(US_ASCII),true);
	}
	
	void sendError(String message) throws Exception{
		send(FrontEndCommunication.ERROR,message.getBytes(US_ASCII),true);
	}
	
	void sendCounters(int [] values) throws Exception {
		sendIntegers(FrontEndCommunication.COUNTER,values);
	}
	
	void sendScalers(int [] values) throws Exception {
		sendIntegers(FrontEndCommunication.SCALER,values);
	}
	
	void sendIntegers(int status, int [] values) throws Exception {
		final ByteArrayOutputStream bytes=new ByteArrayOutputStream(
		4*(values.length+1));
		final DataOutputStream dos = new DataOutputStream(bytes);
		for (int i=0; i<values.length; i++){
			dos.writeInt(values[i]);
		}
		dos.close();
		send(status,bytes.toByteArray(),false);
	}
}
