package com.bayviewglen.zork;

public class Items {
	Room location;
	int weight;

	public Items(Room startRoom, int itemWeight) {
		location = startRoom;
		weight = itemWeight;
	}

	public void setLocation(Room currentRoom) {
		location = currentRoom;
	}

}