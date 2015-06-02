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

	public void removeItem(String removeItem) {
		if (!inventory.containsKey(removeItem)) {
			System.out.println("Ummmm...you don't have that item...");
		} else {
			inventory.remove(removeItem);
			// subtract weight of item
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
