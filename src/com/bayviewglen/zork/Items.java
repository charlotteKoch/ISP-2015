package com.bayviewglen.zork;

public class Items {
	Room location;
	int weight;
	
	public void setLocation(Room currentRoom)
	{
		location = currentRoom;
	}
	
	public void setWeight(int itemWeight)
	{
		weight = itemWeight;
	}
}