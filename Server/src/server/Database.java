package server;

import java.util.ArrayList;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

//This class is used to store data
//Might get rid of this is I find out reading from a file is easier
public class Database <T> {	
	//Globabl variables
	private LocalDate currentDate = LocalDate.now();
	private DateTimeFormatter date_format;
	private String format = "MM/dd/yyyy";
	//Where the Fundraisers are stored
	private ArrayList<ArrayList<T>> Fundraisers = new ArrayList<ArrayList<T>>();
	
	public <T> Database() throws ParseException
	{
		//Puts in a fundraiser to start with
		set("John's college fund", 1000.0, "10/30/2023");
		set("John's wedding", 10040.0, "11/20/2022");
	}
	
	//Grabs a fund
	synchronized public ArrayList<T> get(int choice)
	{
		
		ArrayList<T> Fundraiser = Fundraisers.get(choice);
		if(Fundraiser == null)
		{
			return null;		
		}
		
		return Fundraiser;
	}
	
	//Creates a new Fundraiser and puts it in the Fundraisers ArrayList
	synchronized public void set(String name, Double target, String deadline)
	{
		LocalDate temp = LocalDate.parse(deadline, date_format.ofPattern(format));
		
		ArrayList<T> Fundraiser = new ArrayList<>()
		{
			{
				add((T) name);
				add((T) (Double) 0.0);
				add((T) target);
				add((T) deadline);
			}
		};
		
		//Checks if the Fundraiser is currently going on or is done
		if(currentDate.isAfter(temp))
		{
			Fundraiser.add((T) (Integer) 1);
		}
		else
		{
			Fundraiser.add((T) (Integer) 0);
		}
		Fundraisers.add(Fundraiser);
	}
	
	//This is what allows a user to donate
	synchronized public Integer donate(Integer choice, Double amount)
	{
		Double updated_donated_amount = 0.0;
		ArrayList<T> Fundraiser;
		//Checks if a fundraiser is still going
		//Or if amount if a negative number
		if(choice <= 0 || amount <= 0)
		{
			return 0;
		}
		int j = 0;
		for(int i = 0; i < Fundraisers.size(); i++)
		{
			Fundraiser = Fundraisers.get(i);
			if(Fundraiser.get(4).equals(0))
			{
				j++;
				if(j == choice)
				{
					try
					{
						updated_donated_amount = ((Double) Fundraiser.get(1)) + amount;
						Fundraiser.set(1, (T) updated_donated_amount);
						return 1;
					}
					catch(Exception e)
					{
						return 0;
					}
				}
			}
		}
		return 0;
	}
	
	//Checks if the date is valid
	synchronized public boolean valid_Date(String date)
	{
		try
		{
			LocalDate temp = LocalDate.parse(date, date_format.ofPattern(format));
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	//Checks how many fundraisers are in the system
	synchronized public int size()
	{
		int result = Fundraisers.size();
		return result;
	}
}
