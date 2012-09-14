package console.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.SecureRandom;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class UDPReceiver {
	
	public static final int theport = 8888;
	int port;
	DatagramSocket serverSocket;
	
	public UDPReceiver() throws SocketException{
		port = theport;
		serverSocket = new DatagramSocket(port);
	}
	
	public void listenForever() throws IOException{
		byte[] receiveData = new byte[1024];
        //byte[] sendData = new byte[1024];
        System.out.println("Listening...");
        while(true)
           {
              DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
              serverSocket.receive(receivePacket);
              String sentence = new String( receivePacket.getData());
              sentence = sentence.substring(0,sentence.indexOf("\n"));//cut off string at new line
              System.out.println("RECEIVED: " + sentence + " from :");
              InetAddress IPAddress = receivePacket.getAddress();
              int port = receivePacket.getPort();
              System.out.println(IPAddress + " / "+ port);
              /*String capitalizedSentence = sentence.toUpperCase();
              sendData = capitalizedSentence.getBytes();
              DatagramPacket sendPacket =
              new DatagramPacket(sendData, sendData.length, IPAddress, port);
              serverSocket.send(sendPacket);*/
           }
	}
	
	public static void main(String args[]) throws Exception
	      {
	         
	            UDPReceiver bip = new UDPReceiver();
	           // bip.testhex();
	            bip.listenForever();
	            
	      }
	
	public void testhex(){
		SecureRandom saltGenerator = new SecureRandom();
    	HexBinaryAdapter hexConverter = new HexBinaryAdapter();
    	byte[] salt = new byte[8];
    	
    	saltGenerator.nextBytes(salt);
    	//if (salt==null)
    		System.out.println("salt is \n");
    		for(byte b :salt)
    			System.out.println(b);
    	String saltHex = hexConverter.marshal(salt);
    //	config.addProperty("up2p.password.salt", saltHex,
			//"Hex string of the salt bytes used for user authentication.");
    	System.out.println("Saved salt:"+ saltHex);
	}
	
	}