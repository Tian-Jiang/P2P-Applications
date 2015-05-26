/*
 *******************************************************************************
 *  UDPServer.java
 *
 *  Copyright(c) COMP9331 CSE UNSW.
 *
 *< Content of program >
 *  System name: P2P Application
 *< Functional overview >
 *  A simple P2P application
 *< Update histroy >
 *  2012/4/16    version1.0    Tian Jiang
 *******************************************************************************
 */
import java.io.IOException;
import java.net.*;

/**
 * An UDP server used to response the pinging messages 
 * 
 * @author Tian Jiang
 * @version 1.0
 */
public class UDPServer extends Thread{
	// Constructor
	public UDPServer(){
		
	}
	
	/**
	 * An run method used to response the pinging messages
	 *
	 */
	@Override
	public void run(){
		try {
			// open UDP server 
			DatagramSocket serverSocket = new DatagramSocket(PeerInfo.peer+50000);
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];

			while (PeerInfo.run){
				// When receives request, establish the connection
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
				serverSocket.receive(receivePacket);
				String s = new String(receivePacket.getData());
				
				// retrieve the information from request
				String temp[] = s.split(":");
				String peerN = temp[0];		

				System.out.print("A ping request message was received from Peer " + peerN + ".\n");
				
				if(temp[2].contains("p1"))
					PeerInfo.predecessor1 = Integer.parseInt(peerN.trim());
				else if(temp[2].contains("p2"))
					PeerInfo.predecessor2 = Integer.parseInt(peerN.trim());

				// response for the request message by send back the sequence number
				String response = temp[1];
				sendData = response.getBytes();
				int port = receivePacket.getPort();
				InetAddress IPAddress = receivePacket.getAddress();	
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    
				serverSocket.send(sendPacket);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
} 