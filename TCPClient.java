/*
 *******************************************************************************
 *  TCPClient.java
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
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An TCP client used to deal with user's request 
 * 
 * @author Tian Jiang
 * @version 1.0
 */
public class TCPClient extends Thread{
	private String fileName;
	
	// Constructor
	public TCPClient(){
		
	}
	/**
	 * An run method used to deal with user's request 
	 *
	 */
	@Override
	public void run() {	
		try {	
			while (PeerInfo.run){
				// get the input from user
				BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
				String request = inFromUser.readLine();
		
				// check the format
				Pattern p1 = Pattern.compile("request [0-9]{4}");
				Pattern p2 = Pattern.compile("quit");
				Matcher m1 = p1.matcher(request);
				Matcher m2 = p2.matcher(request);
				if(!m1.matches() && !m2.matches()){
					System.out.print("The input can only be these two types: \n");
					System.out.print("1.request xxxx(4 digits file name) \n");
					System.out.print("2.quit \n");
					continue;
				}
				
				// if users want to request files
				if(request.contains("request")){				
					//get the file name
					char[] c = request.toCharArray();
					char[] c1 = new char[4];
		
					int j = 0;
					for(int i = 0; i < c.length; ++i)
						if(Character.isDigit(c[i])){
							c1[j] = c[i];
							++j;
						}
		
					fileName = String.copyValueOf(c1);
					
					// pass the file name and the peer number to the server of successor1
					request = "request" + ":" + fileName + ":" + String.valueOf(PeerInfo.peer);	
			
					// create socket which connects to server
					Socket clientSocket = new Socket("localhost", PeerInfo.successor1+50000);

					// write to server
					DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
					outToServer.writeBytes(String.valueOf(request) + '\n');
						
					System.out.print("File request message for " + fileName +" has been sent to my successor.\n");
			
					// close client socket
					clientSocket.close();	
				}
				// if users want to quit
				else if(request.contains("quit")){
					// give the two successors number to its two predecessors 
					// the the two predecessors can update its peer record after the departing of this peer
					
					String preInfo = "quit" + ":" + String.valueOf(PeerInfo.peer) + ":" + String.valueOf(PeerInfo.successor1) + ":" + String.valueOf(PeerInfo.successor2);

					// notify the predecessor1
					Socket clientSocket = new Socket("localhost", PeerInfo.predecessor1+50000);
					DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
					outToServer.writeBytes(preInfo + '\n');
					// get response from predecessor1
					BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String response = inFromServer.readLine();
					clientSocket.close();
										
					// notify the predecessor2
					clientSocket = new Socket("localhost", PeerInfo.predecessor2+50000);
					outToServer = new DataOutputStream(clientSocket.getOutputStream());
					outToServer.writeBytes(preInfo + '\n');
					// get response from predecessor2
					inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String response1 = inFromServer.readLine();
					clientSocket.close();
					
					if(response.contains("success") && response1.contains("success")){
						PeerInfo.run = false;
						System.out.print("You could quit now.\n");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
