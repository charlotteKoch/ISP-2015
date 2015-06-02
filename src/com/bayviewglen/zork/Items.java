package com.bayviewglen.zork;

public class Items {
	private String name;
	private Room location;
	private int weight;

	public Items(String itemName, Room startRoom, int itemWeight) {
		name = itemName;
		location = startRoom;
		weight = itemWeight;
	}

	public void setLocation(Room currentRoom) {
		location = currentRoom;
	}

	public Room getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public int getWeight() {
		return weight;
	}

}