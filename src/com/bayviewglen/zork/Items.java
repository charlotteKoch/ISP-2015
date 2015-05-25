package com.bayviewglen.zork;

public class Items {
	String name;
	Room location;
	int weight;

	public Items(String itemName, Room startRoom, int itemWeight) {
		name = itemName;
		location = startRoom;
		weight = itemWeight;
	}

	public void setLocation(Room currentRoom) {
		location = currentRoom;
	}

	public String getName() {
		return name;
	}

}