package com.bayviewglen.zork;

import java.util.ArrayList;

public class Inventory {
	private ArrayList<Items> inventory;
	private int weight;
	private int maxWeight;

	public Inventory() {
		inventory = new ArrayList<Items>();
		weight = 0;
		maxWeight = 10;
	}

	public void addToInventory(Items item) {
		boolean check = checkWeight(item);
		if (check) {
			inventory.add(item);
			weight += item.weight;
		} else {
			System.out.println("You only have one back! How do you plan to carry two backpacks?");
		}
	}

	private boolean checkWeight(Items item) {
		boolean canAdd = true;
		int weightNew = weight + item.weight;
		if (weightNew > maxWeight) {
			canAdd = false;
		}
		return canAdd;
	}

	public boolean contains(Items name) {
		if (inventory.contains(name)) {
			return true;
		}
		return false;
	}

	public boolean containsName(String name) {
		for (int x = 0; x < inventory.size(); x++) {
			if (inventory.get(x).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void removeItem(Item removeItem) {
		int length = inventory.size();
		int count = 0;
		for (int x = 0; x < length; x++) {
			if (removeName.equals(inventory.get(x).getName())) {
				inventory.remove(x);
				count++;
			}
		}
		if (count != 0) {
			System.out.println("Ummmm...you don't have that item...");
		}
	}

	public boolean compareLocation(String location) {
		for (int x = 0; x < inventory.size(); x++) {
			if (inventory.get(x).location.getRoomName().equalsIgnoreCase(location)) {
				return true;
			}
		}
		return false;
	}

}
