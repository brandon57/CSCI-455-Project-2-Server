package server;

import java.net.*;
import java.util.concurrent.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ServerSide {
	
	public static Map<InetAddress, Integer> User_state = new HashMap<>();
	private static Map<InetAddress, String> temp = new HashMap<>();
	public static Map<InetAddress, String> incomingData = Collections.synchronizedMap(temp);
	private static DatagramSocket User_Socket = null;
	private static Scanner input = new Scanner(System.in);
	//Manages the threads
	private static ExecutorService Thread_Manager = Executors.newCachedThreadPool();
	
	
	public static void main(String[] args) throws Exception {		
		
		//Creates a socket
		Startup();
		
		byte[] recievedData = new byte[1024];
		
		Database Fundraisers = new Database();
		
		//Waits for a User to connect then creates a thread for each user
		while(true)
		{
			DatagramPacket New_Connection = new DatagramPacket(recievedData, recievedData.length);
			User_Socket.receive(New_Connection);
			String data = new String(New_Connection.getData(), 0, New_Connection.getLength()).trim();
			//Checks if the data sent if from a Client that has already connected
			if(!User_state.containsKey(New_Connection.getAddress()))
			{
				User_state.put(New_Connection.getAddress(), 0);
				Thread_Manager.execute(new User(User_Socket, New_Connection, data, User_state.get(New_Connection.getAddress()), Fundraisers));
			}
			else
			{
				incomingData.put(New_Connection.getAddress(), data);
			}
			
		}
	}
	
	//This opens up the socket so others can connect
	private static void Startup() throws Exception
	{
		String port = null;
		while(true)
		{
			System.out.println("What is the port number of the server? Your input should be between 0-65535\nYou can exit if you want.");
			port = input.nextLine();
			if(validPort(port) == true)
			{
				break;
			}
			else if(port.equalsIgnoreCase("exit"))
			{
				close();
			}
			else
			{
				System.out.println("That port number is not valid");
			}
		}
		System.out.println("Opening Socket...");
		User_Socket = new DatagramSocket(Integer.valueOf(port));
		System.out.println("Socket is open");
	}
	
	//Checks if the port number the user inputted is valid
	private static boolean validPort(String port)
	{
		Integer temp = 0;
		try
		{
			temp = Integer.valueOf(port);
			if(temp >= 0 && temp <= 65535)
			{
				return true;
			}
		}
		catch(Exception e)
		{
			return false;
		}
		return false;
	}
	
	//Shuts down the server
	private static void close() throws Exception
	{
		if(User_Socket != null)
		{
			User_Socket.close();
		}
		if(input != null)
		{
			input.close();
		}
		Thread_Manager.isShutdown();
		System.out.println("Shuting down...");
		System.exit(0);
	}
}
