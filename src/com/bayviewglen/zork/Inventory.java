package com.bayviewglen.zork;

import java.util.HashMap;

public class Inventory {
	private HashMap<String, Items> inventory;
	private int weight;
	private int maxWeight;

	public Inventory() {
		inventory = new HashMap<String, Items>();
		weight = 0;
		maxWeight = 10;
	}

	public void addToInventory(Items item) {
		boolean check = checkWeight(item);
		if (check) {
			inventory.put(item.getName(), item);
			weight += item.getWeight();
		} else if (!check) {
			System.out.println("You only have one back! How do you plan to carry two backpacks?");
		}
	}

	private boolean checkWeight(Items item) {
		boolean canAdd = true;
		int weightNew = weight + item.getWeight();
		if (weightNew > maxWeight) {
			canAdd = false;
		}
		return canAdd;
	}

	public void removeItem(String itemName) {
		if (!inventory.containsKey(itemName)) {
			System.out.println("Ummmm...you don't have that item...");
		} else {
			Items item = inventory.get(itemName);
			weight -= item.getWeight();
			inventory.remove(itemName);
		}
	}

	public boolean contains(String item) {
		return inventory.containsKey(item);
	}

	public Items getItem(String item) {
		Items getItem = inventory.get(item);
		return getItem;
	}
}
