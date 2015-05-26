/*
 *******************************************************************************
 *  UDPClient.java
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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * An UDP client used to continuously ping the two successors of a peer
 * 
 * @author Tian Jiang
 * @version 1.0
 */
public class UDPClient extends Thread{
	// The sequence number of pinging message
	int seq = 0;
	// The acknowledged number of pinging message from successor1
	int ackN1 = 0;
	// The acknowledged number of pinging message from successor2
	int ackN2 = 0;
	
	// Constructor
	public UDPClient(){
		
	}
	
	/**
	 * An run method used to continuously ping the two successors of a peer
	 *
	 */
	@Override
	public void run(){		
		try {
			// Get the IP address of the local host
			InetAddress IPAddress = InetAddress.getLocalHost();
		
			// The data space used to store the request and response
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
				
			// The client socket used to send request to successor1 and successor2
			DatagramSocket clientSocket1 = new DatagramSocket();
			DatagramSocket clientSocket2 = new DatagramSocket();

			// Set the timeout period
			clientSocket1.setSoTimeout(5*1000);
			clientSocket2.setSoTimeout(5*1000);
			
			// The data packet used to received the response from the successors
			DatagramPacket receivePacket1 = new DatagramPacket(receiveData, receiveData.length);
			DatagramPacket receivePacket2 = new DatagramPacket(receiveData, receiveData.length);
			
			
			while(PeerInfo.run){
				++seq;
				
				// Pass the peer number, sequences number and p2 to the server of successor1.
				// P2 means for successor1, this peer is its predecessor2.
				String request1 = String.valueOf(PeerInfo.peer) + ":" + String.valueOf(seq) + ":p2\n";
				sendData = request1.getBytes();
				DatagramPacket sendPacket1 = new DatagramPacket(sendData, sendData.length, IPAddress, PeerInfo.successor1+50000);
				clientSocket1.send(sendPacket1);
				
				// Pass the peer number, sequences number and p1 to the server of successor2.
				// P1 means for successor2, this peer is its predecessor1.
				String request2 = String.valueOf(PeerInfo.peer) + ":" + String.valueOf(seq) + ":p1\n";
				sendData = request2.getBytes();
				DatagramPacket sendPacket2 = new DatagramPacket(sendData, sendData.length, IPAddress, PeerInfo.successor2+50000);
				clientSocket2.send(sendPacket2);
						
				try{	
					clientSocket1.receive(receivePacket1);
					String response1 = new String(receivePacket1.getData());
					
					if(response1.contains(String.valueOf(seq))){
					    ackN1 = seq;
						System.out.print("A ping response message was received from Peer " + PeerInfo.successor1 + ".\n");
					}
				}
				catch(SocketTimeoutException e1){
					/* If the unacknowledged number equals or larger than 3, 
					it can be claimed that the first successor is no longer alive. 
					The server will make the second successor its first successor.
					Then the server will ask another successor for the information of its first successor.
					Finally make this new successor being its second successor. */
					int uAckN1 = seq - ackN1;
					if(uAckN1 >= 3){
						System.out.print("Peer " + String.valueOf(PeerInfo.successor1) + " is no longer alive.\n");
					
						PeerInfo.successor1 = PeerInfo.successor2;
						System.out.print("My first successor is now peer " + String.valueOf(PeerInfo.successor1) +".\n");
						// create socket which connects to server
						Socket clientSocket = new Socket("localhost", PeerInfo.successor1+50000);

						// write to server
						DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
						outToServer.writeBytes("forceQuit\n");
					
						BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						String response = inFromServer.readLine();	
					
						// close client socket
						clientSocket.close();
					
						PeerInfo.successor2 = Integer.parseInt(response.trim());
						System.out.print("My second successor is now peer " + String.valueOf(PeerInfo.successor2) +".\n");
						
						seq = 0;
					}
				}
				
				try{
					clientSocket2.receive(receivePacket2);
					String response2 = new String(receivePacket2.getData());
					if(response2.contains(String.valueOf(seq))){
						ackN2 = seq;
						System.out.print("A ping response message was received from Peer " + PeerInfo.successor2 + ".\n");
					}
				}
				catch(SocketTimeoutException e2){
					/* If the unacknowledged number equals or larger than 3, 
					it can be claimed that the second successor is no longer alive. 
					The server will make the thread sleep for 5 seconds in order 
					to wait for the updating of its first successor. 
					Then server will ask the first successor for the information of its first successor.
					Finally make this new successor being its second successor. */
					int uAckN2 = seq - ackN2;
					if(uAckN2 >= 3){
						Thread.sleep(5*1000);
						
						System.out.print("Peer " + String.valueOf(PeerInfo.successor2) + " is no longer alive.\n");
					
						System.out.print("My first successor is now peer " + String.valueOf(PeerInfo.successor1) +".\n");
						// create socket which connects to server
						Socket clientSocket = new Socket("localhost", PeerInfo.successor1+50000);

						// write to server
						DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
						outToServer.writeBytes("forceQuit\n");
					
						BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						String response = inFromServer.readLine();	
					
						// close client socket
						clientSocket.close();
					
						PeerInfo.successor2 = Integer.parseInt(response.trim());
						System.out.print("My second successor is now peer " + String.valueOf(PeerInfo.successor2) +".\n");
						
						seq = 0;
					}
				}
				
				// ping every 10 seconds
				Thread.sleep(10*1000);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}