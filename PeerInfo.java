/*
 *******************************************************************************
 *  PeerInfo.java
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
public class PeerInfo {
	// peer informations which shared by the hole system
	public static int peer = 0;
	public static int successor1 = 0;
	public static int successor2 = 0;
	public static int predecessor1 = 0;
	public static int predecessor2 = 0;
	// used as conditions of the while loops in the UDP* and TCP* files
	public static boolean run = true;
}
