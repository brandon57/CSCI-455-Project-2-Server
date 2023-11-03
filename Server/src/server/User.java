package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.*;
import java.util.ArrayList;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class User<T> implements Runnable {
	
	private Socket Connection = null;
	Database<T> Fundraisers;
	private DataOutputStream toClient = null;
	private BufferedReader fromClient = null;
	private String textFromClient = "";
	private DateTimeFormatter date_format = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	
	public User(Socket new_Connection, Database<T> database)
	{
		Connection = new_Connection;
		Fundraisers = database;
	}
	
	@Override
	public void run() {
		try
		{
			System.out.println(Thread.currentThread().getName() + ": IP address: " + Connection.getInetAddress() + ", Port number: " + Connection.getPort() + ", ACTION: has connected"
			+ ", Local time: " + date_format.format(LocalDateTime.now()));
			
			toClient = new DataOutputStream(Connection.getOutputStream());
			fromClient = new BufferedReader(new InputStreamReader(Connection.getInputStream()));
			toClient.writeBytes("You are connected!\n");
			
			//The beginning of the UI
			//This is how the User interacts with the Server
			int current_past = 0;
			while(true)
			{
				//Keeps the user at either current or past fundrasiers 
				toClient.writeBytes("\n");
				display_Fundraisers(current_past);
				
				display_options();
				textFromClient = fromClient.readLine().toLowerCase();
				switch(textFromClient)
				{
				case "current":
				case "1":
					current_past = 0;
					break;
				case "past":
				case "2":
					current_past = 1;
					break;
				case "create":
				case "3":
					create();
					break;
				case "donate":
				case "4":
					donate();
					break;
				case "refresh":
				case "5":
					System.out.println(Thread.currentThread().getName() + ": IP address: " + Connection.getInetAddress() + ", Port number: " + Connection.getPort() + ", ACTION: Refreshed page"
					+ ", Local time: " + date_format.format(LocalDateTime.now()));
					break;
				case "exit":
				case "6":
					exit();
					break;
				default:
					toClient.writeBytes("Your input is not valid\n" + "Try again\n");
					break;
				}
				
				
			}
		}
		catch(Exception e)
		{
			
		}
		
	}
	
	//Let's the user quit the program
	private void exit() throws Exception
	{
		System.out.println(Thread.currentThread().getName() + ": IP address: " + Connection.getInetAddress() + ", Port number: " + Connection.getPort() + ", ACTION: Disconnected"
		+ ", Local time: " + date_format.format(LocalDateTime.now()));
		toClient.writeBytes("^^&^&^&\n");
		toClient.close();
		fromClient.close();
		Connection.close();
	}
	
	//The UI for how the user donates
	private void donate() throws Exception
	{
		System.out.println(Thread.currentThread().getName() + ": IP address: " + Connection.getInetAddress() + ", Port number: " + Connection.getPort() + ", ACTION: Donated"
		+ ", Local time: " + date_format.format(LocalDateTime.now()));
		Integer choice = 0;
		Double donation = 0.0;
		display_Fundraisers(0);
		toClient.writeBytes("Which fundraiser do you want to donate to?\n" + "Your response should be a number" + " \n");		
		toClient.writeBytes(" \n");
		while(true)
		{
			try
			{
				choice = Integer.valueOf(fromClient.readLine());
				if(choice > 0)
				{
					break;
				}
				toClient.writeBytes("Your input is not valid\n");
				toClient.writeBytes("Try again\n" + " \n");
			}
			catch(Exception e)
			{
				toClient.writeBytes("Your input is not valid\n");
				toClient.writeBytes("Try again\n" + " \n");
			}
		}
		
		toClient.writeBytes("How much do you want to donate?\n" + " \n");
		while(true)
		{
			try
			{
				donation = Double.valueOf(fromClient.readLine());
				int temp = BigDecimal.valueOf(donation).scale();
				if(donation > 0 && temp <= 2)
				{
					break;
				}
				toClient.writeBytes("Your input is not valid\n");
				toClient.writeBytes("Try again\n" + " \n");
			}
			catch(Exception e)
			{
				toClient.writeBytes("Your input is not valid\n");
				toClient.writeBytes("Try again\n" + " \n");
			}
		}
		if(Fundraisers.donate(choice, donation).equals(0))
		{
			toClient.writeBytes("Your donation couldn't go through :(\n");
			return;
		}
		toClient.writeBytes("Your donation went through! :)\n" + "Thank you!\n");
	}
	
	//Displays the options the user can select
	private void display_options() throws Exception
	{
		toClient.writeBytes("These are the options you have to chose from\n"
		+"1. See current funderaisers\n"
		+"2. See past funderaisers\n"
		+"3. Create a funderaiser\n"
		+"4. Donate to a funderaiser\n"
		+"5. Refresh\n"
		+"6. Exit\n" + " \n");
	}
	
	
	//Allows the user to Create a new fundraiser
	private void create() throws Exception
	{
		System.out.println(Thread.currentThread().getName() + ": IP address: " + Connection.getInetAddress() + ", Port number: " + Connection.getPort() + ", ACTION: creating a fundraiser"
		+ ", Local time: " + date_format.format(LocalDateTime.now()));
		String name;
		Double target = 0.0;
		String deadline;
		
		toClient.writeBytes("What is the name of your fundraiser?\n" + " \n");
		name = fromClient.readLine();
		
		toClient.writeBytes("What is your target amount?\n" + " \n");
		while(true)
		{
			try
			{
				target = Double.valueOf(fromClient.readLine());
				int temp = BigDecimal.valueOf(target).scale();
				if(target > 0 && temp <= 2)
				{
					break;
				}
				toClient.writeBytes("Your input is not valid\n");
				toClient.writeBytes("Try again\n" + " \n");
			}
			catch(Exception e)
			{
				toClient.writeBytes("Your input is not valid\n");
				toClient.writeBytes("Try again\n" + " \n");
			}
		}
		
		toClient.writeBytes("What is the deadline? Your input should look like this \"MM/dd/yyyy\"\n" + " \n");
		while(true)
		{
			deadline = fromClient.readLine();
			if(Fundraisers.valid_Date(deadline) == true)
			{
				break;
			}
			else
			{
				toClient.writeBytes("Your input is not valid\n");
				toClient.writeBytes("Try again\n" + " \n");
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
				System.out.println(Thread.currentThread().getName() + ": IP address: " + Connection.getInetAddress() + ", Port number: " + Connection.getPort() + ", ACTION: Looking at on going fundrasisers"
				+ ", Local time: " + date_format.format(LocalDateTime.now()));
				toClient.writeBytes("Here are on going funderaisers:\n");
				//display();
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
					toClient.writeBytes((i+1) + "." + temp1 + "\n");
					toClient.flush();
				}
				break;
			//Old Fundraisers
			case 1:
				System.out.println(Thread.currentThread().getName() + ": IP address: " + Connection.getInetAddress() + ", Port number: " + Connection.getPort() + ", ACTION: Looking at past fundrasisers"
				+ ", Local time: " + date_format.format(LocalDateTime.now()));
				toClient.writeBytes("Here are past funderaisers:\n");
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
					toClient.writeBytes((i+1) + "." + temp1 + "\n");
					toClient.flush();
				}
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
		toClient.writeBytes(result);
		toClient.flush();
	}
	
}
