package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.*;
import java.util.ArrayList;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class User<T> implements Runnable {
	
	private DatagramSocket Connection = null;
	private InetAddress IP = null;
	private int port = 0;
	Database<T> Fundraisers;
	private String textFromClient = "";
	private int state = 0;
	private DateTimeFormatter date_format = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	private byte[] sendDatabuf = new byte[2048];
	
	public User(DatagramSocket new_Connection, InetAddress newIP, int newPort, String data, Integer newState, Database<T> database)
	{
		Connection = new_Connection;
		IP = newIP;
		port = newPort;
		//IP = new_connectionPacket.getAddress();
		//port = new_connectionPacket.getPort();
		Fundraisers = database;
		textFromClient = data;
		state = newState;
	}
	
	@Override
	public void run() {
		try
		{
			int current_past = 0;
			
			System.out.println(Thread.currentThread().getName() + ": IP address: " + IP + ", Port number: " + port + ", ACTION: has connected"
			+ ", Local time: " + date_format.format(LocalDateTime.now()));
			sendData("You are connected!\n");
			current_past = 0;
			//textFromClient = null;
//			if(state == 0)
//			{
//				
//			}
			
			//The beginning of the UI
			//This is how the User interacts with the Server
			
			while(true)
			{
				//Keeps the user at either current or past fundrasiers 
				sendData("\n");
				if(current_past == 0 || current_past == 1)
				{
					display_Fundraisers(current_past);
					display_options();
				}
				
				textFromClient = recieveData().toLowerCase();
				switch(textFromClient)
				{
				case "current", "1":
					current_past = 0;
					//ServerSide.Users_connected.replace(IP, 1);
					break;
				case "past", "2":
					current_past = 1;
					//ServerSide.Users_connected.put(IP, 2);
					break;
				case "create", "3":
					//ServerSide.Users_connected.put(IP, 3);
					create();
					break;
				case "donate", "4":
					//ServerSide.Users_connected.put(IP, 4);
					donate();
					break;
				case "refresh", "5":
					System.out.println(Thread.currentThread().getName() + ": IP address: " + IP + ", Port number: " + port + ", ACTION: Refreshed page"
					+ ", Local time: " + date_format.format(LocalDateTime.now()));
					break;
				case "exit", "6":
					exit(0);
					break;
				default:
					current_past = 2;
					sendData("Your input is not valid\n" + "Try again\n");
					sendData("stop");
					break;
				}
			}
		}
		catch(Exception e)
		{
			
		}
		
	}
	
	//This is an easier way to recieve data from a user
	private String recieveData() throws Exception
	{
		LocalTime timeOut = LocalTime.now().plusMinutes(1);
		while(!ServerSide.incomingData.containsKey(IP))
		{
			if(LocalTime.now().equals(timeOut) || LocalTime.now().isAfter(timeOut))
			{
				exit(1);
			}
		}
		return ServerSide.incomingData.remove(IP);
	}
	
	//This is an easier way to send data
	private void sendData(String data) throws Exception
	{
		try
		{
			sendDatabuf = data.getBytes();
			DatagramPacket toClient = new DatagramPacket(sendDatabuf, sendDatabuf.length, IP, port);
			Connection.send(toClient);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("error");
		}
		
	}
	
	//Let's the user quit the program
	private void exit(int type) throws Exception
	{
		String disconnectType = null;
		switch(type)
		{
			case 0:
				disconnectType = ", ACTION: Disconnected";
				ServerSide.incomingData.remove(IP);
				break;
			case 1:
				disconnectType = ", ACTION: Timeout";
				ServerSide.Users_timedout.put(IP, ServerSide.incomingData.remove(IP));
				break;
		}
		ServerSide.Users_connected.remove(IP);
		System.out.println(Thread.currentThread().getName() + ": IP address: " + IP + ", Port number: " + port + disconnectType
		+ ", Local time: " + date_format.format(LocalDateTime.now()));
		sendData("Timeout\n");
		
		while(true)
		{
			Thread.currentThread().wait();
		}
	}
	
	//The UI for how the user donates
	private void donate() throws Exception
	{
		System.out.println(Thread.currentThread().getName() + ": IP address: " + IP + ", Port number: " + port + ", ACTION: Donated"
		+ ", Local time: " + date_format.format(LocalDateTime.now()));
		Integer choice = 0;
		Double donation = 0.0;
		display_Fundraisers(0);
		sendData("Which fundraiser do you want to donate to?\n" + "Your response should be a number\n");
		sendData("stop");
		while(true)
		{
			try
			{
				choice = Integer.valueOf(recieveData());
				if(choice > 0)
				{
					break;
				}
				sendData("Your input is not valid\n");
				sendData("Try again\n");
				sendData("stop");
			}
			catch(Exception e)
			{
				sendData("Your input is not valid\n");
				sendData("Try again\n");
				sendData("stop");
			}
		}
		sendData("How much do you want to donate?\n");
		sendData("stop");
		while(true)
		{
			try
			{
				donation = Double.valueOf(recieveData());
				int temp = BigDecimal.valueOf(donation).scale();
				if(donation > 0 && temp <= 2)
				{
					break;
				}
				sendData("Your input is not valid\n");
				sendData("Try again\n");
				sendData("stop");
			}
			catch(Exception e)
			{
				sendData("Your input is not valid\n");
				sendData("Try again\n");
				sendData("stop");
			}
		}
		if(Fundraisers.donate(choice, donation).equals(0))
		{
			sendData("Your donation couldn't go through :(\n");
			return;
		}
		sendData("Your donation went through! :)\n" + "Thank you!\n");
	}
	
	//Displays the options the user can select
	private void display_options() throws Exception
	{
		sendData("These are the options you have to chose from\n"
		+"1. See current funderaisers\n"
		+"2. See past funderaisers\n"
		+"3. Create a funderaiser\n"
		+"4. Donate to a funderaiser\n"
		+"5. Refresh\n"
		+"6. Exit\n");
		sendData("stop");
	}
	
	
	//Allows the user to Create a new fundraiser
	private void create() throws Exception
	{
		System.out.println(Thread.currentThread().getName() + ": IP address: " + IP + ", Port number: " + port + ", ACTION: creating a fundraiser"
		+ ", Local time: " + date_format.format(LocalDateTime.now()));
		String name;
		Double target = 0.0;
		String deadline;
		sendData("What is the name of your fundraiser?\n");
		sendData("stop");
		name = recieveData();
		sendData("What is your target amount?\n");
		sendData("stop");
		while(true)
		{
			try
			{
				target = Double.valueOf(recieveData());
				int temp = BigDecimal.valueOf(target).scale();
				if(target > 0 && temp <= 2)
				{
					break;
				}
				sendData("Your input is not valid\n");
				sendData("Try again\n");
				sendData("stop");
			}
			catch(Exception e)
			{
				sendData("Your input is not valid\n");
				sendData("Try again\n");
				sendData("stop");
			}
		}
		sendData("What is the deadline? Your input should look like this \"MM/dd/yyyy\"\n");
		sendData("stop");
		while(true)
		{
			deadline = recieveData();
			if(Fundraisers.valid_Date(deadline) == true)
			{
				break;
			}
			else
			{
				sendData("Your input is not valid\n");
				sendData("Try again\n");
				sendData("stop");
			}
		}
		Fundraisers.set(name, target, deadline);
	}
	
	
	//Prints out the fundraisers
	//The input determines if either past or current fundraisers get displayed
	//0 for current and 1 for past
	private void display_Fundraisers(int display) throws Exception
	{
		
		ArrayList<T> temp;
		ArrayList<ArrayList<T>> Past_Fundraisers = new ArrayList<ArrayList<T>>();
		ArrayList<ArrayList<T>> Current_Fundraisers = new ArrayList<ArrayList<T>>();
		
		for(int i = 0; i < Fundraisers.size(); i++)
		{
			if(Fundraisers.get(i).get(4).equals(0))
			{
				Current_Fundraisers.add(Fundraisers.get(i));
			}
			else if(Fundraisers.get(i).get(4).equals(1))
			{
				Past_Fundraisers.add(Fundraisers.get(i));
			}
		}
		String temp1 = "";
		display_border();
		switch(display)
		{
			//Current Fundraisers
			case 0:
				System.out.println(Thread.currentThread().getName() + ": IP address: " + IP + ", Port number: " + port + ", ACTION: Looking at on going fundrasisers"
				+ ", Local time: " + date_format.format(LocalDateTime.now()));
				sendData("Here are on going funderaisers:\n");
				for(int i = 0; i < Current_Fundraisers.size(); i++)
				{
					temp1 = "";
					temp = Current_Fundraisers.get(i);
					for(int j = 0; j < temp.size()-1; j++)
					{
						switch(j) 
						{
							case 0:
								temp1 = temp1 + " " + temp.get(j).toString() + " |";
								break;
							case 1:
								temp1 = temp1 + " raised: $" + temp.get(j).toString() + " |";
								break;
							case 2:
								temp1 = temp1 + " goal: $" + temp.get(j).toString() + " |";
								break;
							case 3:
								temp1 = temp1 + " deadline: " + temp.get(j).toString() + " |";
								break;
						}
					}
					sendData((i+1) + "." + temp1 + "\n");
				}
				break;
			//Old Fundraisers
			case 1:
				System.out.println(Thread.currentThread().getName() + ": IP address: " + IP + ", Port number: " + port + ", ACTION: Looking at past fundrasisers"
				+ ", Local time: " + date_format.format(LocalDateTime.now()));
				sendData("Here are past funderaisers:\n");
				for(int i = 0; i <	Past_Fundraisers.size(); i++)
				{
					temp1 = "";
					temp = Past_Fundraisers.get(i);
					for(int j = 0; j < temp.size()-1; j++)
					{
						switch(j) 
						{
							case 0:
								temp1 = temp1 + " " + temp.get(j).toString() + " |";
								break;
							case 1:
								temp1 = temp1 + " raised: $" + temp.get(j).toString() + " |";
								break;
							case 2:
								temp1 = temp1 + " goal: $" + temp.get(j).toString() + " |";
								break;
							case 3:
								temp1 = temp1 + " ended: " + temp.get(j).toString() + " |";
								break;
						}
					}
					sendData((i+1) + "." + temp1 + "\n");
				}
				break;
			default:
				break;
		}
		display_border();
	}
	
	//Creates a border around the fundraiser tables
	private void display_border() throws Exception
	{
		String sentence = "";
		for(int i = 0; i <= 80; i++)
		{
			sentence = sentence + "-";
		}
		String result = sentence + "\n";
		sendData(result);
	}
	
}
