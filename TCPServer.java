/*
 *******************************************************************************
 *  TCPServer.java
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
import java.io.*;
import java.net.*;

/**
 * An TCP server used to deal with user's request 
 * 
 * @author Tian Jiang
 * @version 1.0
 */
public class TCPServer extends Thread{
	// Constructor
	public TCPServer(){
		
	}

	/**
	 * An run method used to deal with user's request
	 *
	 */
	@Override
	public void run() {

		try {
			//open TCP server
			ServerSocket welcomeSocket = new ServerSocket(PeerInfo.peer+50000);
		
			while (PeerInfo.run){

				// accept connection from connection queue
				Socket connectionSocket = welcomeSocket.accept();

				// create read stream to get input
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				String request = inFromClient.readLine();
				
				// if it is a file request, first circulates the location of the files
				// if it is stored in current peer, send a response message to the peer who requests the file
				// if not, forwards the request to the first successor of current peer
				if(request.contains("request")){
					String[] info = request.split(":");
					int hashCode = Integer.parseInt(info[1]) % 256;
					
					// because it is a circle of a P2P network, so there are two cases to calculate the location
					// 1. if the predecessor2 is smaller than the current peer, and the hash value is between these 
					// two numbers, the file can be claimed to stored in current peer. For example, peer 30 asks file
					// 0040 to peer 50.
					// 2. if the predecessor2 is larger than the current peer, then there are two sub situation:
					// the hash value is larger than predecessor2 or the hash value is smaller that current peer
					// if these cases are true, the file can be claimed to stored in current peer. For example, 
					// peer 121 asks file 255 to peer 10 or peer 121 asks file 9 to peer 10.
					// otherwise, it can be claimed that the file is not stored here.
					if(PeerInfo.peer > PeerInfo.predecessor2){
						if(hashCode <= PeerInfo.peer && hashCode > PeerInfo.predecessor2)
							atHere(info[1], Integer.valueOf(info[2]));
						else
							forwarding(info[1], PeerInfo.successor1, request);
					}
					else{
						if(hashCode > PeerInfo.predecessor2 || hashCode <= PeerInfo.peer)
							atHere(info[1], Integer.valueOf(info[2]));
						else 
							forwarding(info[1], PeerInfo.successor1, request);

					}
				}
				// if it is a quit notice
				else if(request.contains("quit")){
					String[] preInfo = request.split(":");
					int peer = Integer.parseInt(preInfo[1]);
					int s1 = Integer.parseInt(preInfo[2]);
					int s2 = Integer.parseInt(preInfo[3]);
					
					System.out.print("Peer " + peer + " will depart from the network.\n");
					// if the departing peer is the first successor of the current peer, then make the
					// two successors of the departing peer its successors.
					if(PeerInfo.successor1 == peer){
						PeerInfo.successor1 = s1;
						PeerInfo.successor2 = s2;
					}
					// if the departing peer is the second successor of the current peer, then make the
					// first successor of the departing peer its second successor.
					else if(PeerInfo.successor2 == peer){
						PeerInfo.successor2 = s1;
					}
					System.out.print("My first successor is now peer " + PeerInfo.successor1 + ".\n");
					System.out.print("My second successor is now peer " + PeerInfo.successor2 + ".\n");
					
					// send success response to departing peer
					DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream());  
				    String response = "success\n";  
				    outToClient.writeBytes(response);
				}
				// if receives a response from a peer who has the requested file
				else if(request.contains("file")){
					String[] peerHasFile = request.split(":");
					System.out.print("Received a response massage from peer " + peerHasFile[0] + ", which has the file " + peerHasFile[1] +".\n");
				}
				// if receives a request from peer whose successor is force quit, 
				// send the number of the first successor to it.
				else if(request.contains("forceQuit")){
					DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream());  
					String response = String.valueOf(PeerInfo.successor1) + "\n";  
				    outToClient.writeBytes(response);
				}
			} 
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	private void forwarding(String fileName, int succ1, String request) throws Exception{
		System.out.print("File " + fileName + " is not Stored here.\n");
		System.out.print("File request message has been forwarded to my successor.\n");
		
		// create socket which connects to server
		Socket clientSocket = new Socket("localhost", succ1+50000);
		
		// write to server
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes(String.valueOf(request) + '\n');
		
		// close client socket
		clientSocket.close();
	}
	
	private void atHere(String fileName, int requester) throws Exception{
		// process input
		System.out.print("File " + fileName + " is here.\n");
		
		// create socket which connects to server
		Socket clientSocket = new Socket("localhost", requester+50000);
		
		// write to server
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes(String.valueOf(PeerInfo.peer) + ":" + fileName + ":" + "file\n");

		// close client socket
		clientSocket.close();
	}
}