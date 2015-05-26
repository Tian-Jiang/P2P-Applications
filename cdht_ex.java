/*
 *******************************************************************************
 *  cdht_ex.java
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
public class cdht_ex {
	public static void main(String[] args) throws Exception {
		PeerInfo.peer = Integer.parseInt(args[0]);
		PeerInfo.successor1 = Integer.parseInt(args[1]); 
		PeerInfo.successor2 = Integer.parseInt(args[2]);
	
		UDPServer us = new UDPServer();
		Thread t1 = new Thread(us);
		t1.start();
	
		UDPClient uc = new UDPClient();
		Thread t2 = new Thread(uc);		
		t2.start();
		
		TCPServer ts = new TCPServer();
		Thread t3 = new Thread(ts);
		t3.start();
		
		TCPClient rf = new TCPClient();
		Thread t4 = new Thread(rf);
		t4.start();
	}
}
