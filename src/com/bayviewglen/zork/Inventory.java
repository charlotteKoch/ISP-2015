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
		inventory.add(item);
		boolean check = checkWeight(item);
		if (check) {
			weight += item.weight;
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

}
