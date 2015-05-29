package com.bayviewglen.zork;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class Game - the main class of the "Zork" game.
 *
 * Author: Michael Kolling Version: 1.1 Date: March 2000
 * 
 * This class is the main class of the "Zork" application. Zork is a very
 * simple, text based adventure game. Users can walk around some scenery. That's
 * all. It should really be extended to make it more interesting!
 * 
 * To play this game, create an instance of this class and call the "play"
 * routine.
 * 
 * This main class creates and initialises all the others: it creates all rooms,
 * creates the parser and starts the game. It also evaluates the commands that
 * the parser returns.
 */

class Game {
	private Parser parser;
	private Room currentRoom;
	private Room previousRoom;
	private Date endTime;
	private Date pauseTime;

	// This is a MASTER object that contains all of the rooms and is easily
	// accessible.
	// The key will be the name of the room -> no spaces (Use all caps and
	// underscore -> Great Room would have a key of GREAT_ROOM
	// In a hashmap keys are case sensitive.
	// masterRoomMap.get("GREAT_ROOM") will return the Room Object that is the
	// Great Room (assuming you have one).
	private HashMap<String, Character> listOfCharacters = new HashMap<String, Character>();
	private Inventory inventoryItems = new Inventory();
	private Items key, myBackpack, sabaBackpack, USB;
	private Inventory allItems = new Inventory();

	private HashMap<String, Room> masterRoomMap;

	private void initRooms(String fileName) throws Exception {
		masterRoomMap = new HashMap<String, Room>();
		Scanner roomScanner;
		try {
			HashMap<String, HashMap<String, String>> exits = new HashMap<String, HashMap<String, String>>();
			roomScanner = new Scanner(new File(fileName));
			while (roomScanner.hasNext()) {
				Room room = new Room();
				// Read the Name
				String roomName = roomScanner.nextLine();
				room.setRoomName(roomName.split(":")[1].trim());
				// Read the Description
				String roomDescription = roomScanner.nextLine();
				room.setDescription(roomDescription.split(":")[1].replaceAll("<br>", "\n").trim());
				// Read the Exits
				String roomExits = roomScanner.nextLine();
				// An array of strings in the format E-RoomName
				String[] rooms = roomExits.split(":")[1].split(",");
				HashMap<String, String> temp = new HashMap<String, String>();
				for (String s : rooms) {
					temp.put(s.split("-")[0].trim(), s.split("-")[1]);
				}

				exits.put(roomName.substring(10).trim().toUpperCase().replaceAll(" ", "_"), temp);

				// This puts the room we created (Without the exits in the
				// masterMap)
				masterRoomMap.put(roomName.toUpperCase().substring(10).trim().replaceAll(" ", "_"), room);

				// Now we better set the exits.
			}

			for (String key : masterRoomMap.keySet()) {
				Room roomTemp = masterRoomMap.get(key);
				HashMap<String, String> tempExits = exits.get(key);
				for (String s : tempExits.keySet()) {
					// s = direction
					// value is the room.

					String roomName2 = tempExits.get(s.trim());
					Room exitRoom = masterRoomMap.get(roomName2.toUpperCase().replaceAll(" ", "_"));
					roomTemp.setExit(s.trim().charAt(0), exitRoom);

				}

			}

			roomScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the game and initialise its internal map.
	 */
	public Game() {
		try {
			initRooms("data/Rooms.dat");
			currentRoom = masterRoomMap.get("MENTOR_CLASSROOM");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parser = new Parser();

		initializeItems();
	}

	/**
	 * Main play routine. Loops until end of play.
	 */
	public void play() {
		printWelcome();

		Date now = new Date();
		endTime = new Date(now.getTime() + 6000 * 1000L);

		// Enter the main command loop. Here we repeatedly read commands and
		// execute them until the game is over.

		boolean finished = false;
		while (!finished) {

			Command command = parser.getCommand();
			finished = processCommand(command);
			if (new Date().after(endTime)) {
				System.out.println("Oh no Mr.DesLauriers found you! You weren't fast enough!");
				finished = true;
			}
		}
		System.out.println("Thank you for playing.  Good bye.");
	}

	public void initializeItems() {

		key = createItem("key", masterRoomMap.get("SCIENCES_OFFICE"), 1);
		sabaBackpack = createItem("sabaBackpack", masterRoomMap.get("PHYSICS_CLASSROOM"), 6);
		myBackpack = createItem("myBackpack", masterRoomMap.get("MR.AULD'S_OFFICE"), 6);
	}

	/**
	 * Print out the opening message for the player.
	 */
	private void printWelcome() {
		System.out.println();
		System.out.println("Welcome to Escape Mr.Deslauriers!");
		System.out.println("This is an exciting game where you must attempt to hand in an overdue assignment without getting caught be your evil computer science teacher.");
		System.out.println("Type 'help' if you need help.");
		System.out.println();
		describeRoom();
	}

	public void describeRoom() {
		System.out.println(currentRoom.longDescription());
		// print any items + people that are in this room
	}

	/**
	 * Given a command, process (that is: execute) the command. If this command
	 * ends the game, true is returned, otherwise false is returned.
	 */
	private boolean processCommand(Command command) {
		if (command.isUnknown()) {
			System.out.println("I don't know what you mean...");
			return false;
		}

		String commandWord = command.getCommandWord();
		if (commandWord.equals("help"))
			printHelp();
		else if (commandWord.equals("go"))
			goRoom(command);
		else if (commandWord.equals("pause"))
			pauseTime = new Date();
		else if (commandWord.equals("resume")) {
			Date rightNow = new Date();
			endTime = new Date(rightNow.getTime() - pauseTime.getTime() + endTime.getTime());
		} else if (commandWord.equals("grab")) {
			grabItem(command);
		} else if (commandWord.equals("talk")) {
			talkCharacter(command);
		} else if (commandWord.equals("drop")) {
			dropItem(command);
		} else if (commandWord.equals("show")) {
			map(command);
		} else if (commandWord.equals("quit")) {
			if (command.hasSecondWord())
				System.out.println("Quit what?");
			else
				return true; // signal that we want to quit
		} else if (commandWord.equals("eat")) {
			System.out.println("Do you really think you should be eating at a time like this?");
		} else if (commandWord.equals("use")) {
			useItem(command);
		}

		return false;
	}

	private void map(Command command) {
		if (!command.hasSecondWord()) {
			System.out.println("Show what?");
		} else if (command.getSecondWord().equalsIgnoreCase("map")) {
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
			System.out.printf("|%21s|%21s|%21s|\n", " ", " ", " ");
			System.out.printf("|%21s|%21s|%21s|\n", " ", " ", " ");
			System.out.printf("|%21s|%21s|%21s|\n", "Mentor Classroom  ", "Physics Classroom  ", "Sciences Office   ");
			System.out.printf("|%21s|%21s|%21s|\n", " ", " ", " ");
			System.out.printf("|%21s|%21s|%21s|\n", " ", " ", " ");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
			System.out.printf("|%21s|%21s|%21s|\n", " ", " ", " ");
			System.out.printf("|%21s|%21s|%21s|\n", "Hallway 1      ", "Hallway 2      ", "Hallway 3      ");
			System.out.printf("|%21s|%21s|%21s|\n", " ", " ", " ");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
			System.out.printf("%22s|%21s|%21s|\n", " N           ", " ", " ");
			System.out.printf("%22s|%21s|%21s|\n", " |           ", " ", " ");
			System.out.printf("%22s|%21s|%21s|\n", "W -- -- E       ", "Mr. Auld's Office  ", "Tech Office     ");
			System.out.printf("%22s|%21s|%21s|\n", " |           ", " ", " ");
			System.out.printf("%22s|%21s|%21s|\n", " S           ", " ", " ");
			System.out.println("                       - - - - - - - - - - - - - - - - - - - - - - ");
		} else {
			describeRoom();
		}
	}

	// implementations of user commands:

	/**
	 * Print out some help information. Here we print some stupid, cryptic
	 * message and a list of the command words.
	 */
	private void printHelp() {
		System.out.println("You are lost. You are alone. You wander");
		System.out.println("around at Monash Uni, Peninsula Campus.");
		System.out.println();
		System.out.println("Your command words are:");
		parser.showCommands();
	}

	/**
	 * Try to go to one direction. If there is an exit, enter the new room,
	 * otherwise print an error message.
	 */
	private void goRoom(Command command) {
		boolean done = false;
		if (!command.hasSecondWord()) {
			// if there is no second word, we don't know where to go...
			System.out.println("Go where?");
			return;
		}

		String direction = command.getSecondWord();

		if (direction.equalsIgnoreCase("back")) {
			if (previousRoom == null) {
				System.out.println("Go where?");
				done = true;
			} else {
				currentRoom = previousRoom;
				describeRoom();
				done = true;
			}
		}

		while (!done) {

			// Try to leave current room.
			Room nextRoom = currentRoom.nextRoom(direction);

			if (nextRoom == null) {
				System.out.println("There is no door!");
				done = true;
			} else if (nextRoom.getRoomName().equalsIgnoreCase("physics classroom")) {
				if (inventoryItems.contains("key")) {
					previousRoom = currentRoom;
					currentRoom = nextRoom;
					System.out.println("You used the key to get into the physics classroom.");
					describeRoom();
					done = true;
				} else {
					System.out.println("The door is locked! Maybe Mr.Hitchcock has the key...");
					done = true;
				}
			} else {
				previousRoom = currentRoom;
				currentRoom = nextRoom;
				describeRoom();
				done = true;
			}

		}

	}

	private void createCharacter(String name, Room location) {
		Character newCharacter = new Character(name, location);
		listOfCharacters.put(name, newCharacter);
	}

	private Items createItem(String name, Room location, int weight) {
		Items newItem = new Items(name, location, weight);
		allItems.addToInventory(newItem);
		return newItem;
	}

	private void dropItem(Command command) {
		if (!command.hasSecondWord()) {
			System.out.println("Drop what?");
		} else {
			if (!inventoryItems.contains(command.getSecondWord())) {
				System.out.println("You can't drop something you don't have!");
			} else if (currentRoom.getRoomName().equalsIgnoreCase("hallway") && command.getSecondWord().equals("sabaBackpack")) {
				sabaBackpack.setLocation(currentRoom);
				inventoryItems.removeItem(command.getSecondWord());
				System.out.println("Saba: thanks for bringing my backpack! Here's the USB.");
				inventoryItems.addToInventory(createItem("USB", masterRoomMap.get("hallway"), 2));
			} else if (!currentRoom.getRoomName().equalsIgnoreCase("hallway") && command.getSecondWord().equals(sabaBackpack)) {
				System.out.println("Do you really think you should be throwing Saba under the bus like this? She needs her textbook!");
			} else {
				inventoryItems.removeItem(command.getSecondWord());
			}
		}
	}

	private void grabItem(Command command) {
		String itemRoomName = "";
		String currentRoomName = "";
		if (!command.hasSecondWord()) {
			System.out.println("Grab what?");
		} else {
			if (inventoryItems.contains(command.getSecondWord())) {
				System.out.println("Why are you trying to pick up something you already have?");
			} else if (!allItems.contains(command.getSecondWord())) {
				System.out.println("This item doesn't exist!");
			} else if (allItems.contains(command.getSecondWord())) {
				itemRoomName = allItems.getItem(command.getSecondWord()).getLocation().getRoomName();
				currentRoomName = currentRoom.getRoomName();
				if (itemRoomName.equalsIgnoreCase(currentRoomName)) {
					inventoryItems.addToInventory(allItems.getItem(command.getSecondWord()));
				}
			}
		}
	}

	private void talkCharacter(Command command) {
		Scanner keyboard = new Scanner(System.in);

		if (!command.hasSecondWord()) {
			System.out.println("Talk to who?");
		} else {
			String word = command.getSecondWord();
			if (word.equalsIgnoreCase("Mr.Hitchcock") && !(currentRoom.getRoomName().equalsIgnoreCase("sciences office"))) {
				System.out.println("Mr.Hitchcock doesn't seem to be in " + currentRoom.getRoomName());
			} else if (word.equalsIgnoreCase("Mr.Hitchcock")) {
				System.out.println("Mr.Hitchcock: Hello there, you must be looking to get in to the physics classroom. I saw a bag in there this morning and I knew someone would need it.");
				System.out.println("Mr.Hitchcock: Do you want the key for the physics classroom?");
				String decision = keyboard.nextLine();
				if (decision.equalsIgnoreCase("no")) {
					System.out.println("Mr.Hitchcock: I hope you don't need that backpack.");
				} else {
					System.out.println("Mr.Hitchcock: So you want the key? That means it's time for a riddle.");
					System.out
							.println("Mr.Hitchcock: You have been given the task of transporting 3,000 apples 1,000 miles from Appleland to Bananaville. \nYour truck can carry 1,000 apples at a time. \nEvery time you travel a mile towards Bananaville you must pay a tax of 1 apple but you pay nothing when going in the other direction (towards Appleland).");
				}
			}
		}

		keyboard.close();
	}

	private boolean useItem(Command command) {
		if (!command.hasSecondWord()) {
			System.out.println("What are you trying to use?");
		} else if (command.hasSecondWord() && command.getSecondWord().equals("USB") && inventoryItems.contains("USB")) {
			System.out.println("file 2");
			System.out.println("file 3");
			System.out.println("file 8");
			System.out.print("Please pick a file to use: ");
			Scanner keyboard = new Scanner(System.in);
			String fileNumber = keyboard.nextLine();
			if (!fileNumber.equals("3")) {
				System.out
						.println("Oh no! Looks like Saba had some nasty files on her USB and you downloaded them! Wonder what those could have been for... Anyway, you took your computer to the tech office which took 10 minutes! Better hurry up and download the right file so you can finish your project!");
				subtractTime();
			} else {

				return true;
			}
		} else if (command.hasSecondWord() && command.getSecondWord().equals("USB") && !inventoryItems.contains("USB")) {
			System.out.println("How are you planning to use something that you don't have?");
		} else {
			System.out.println("You can't use that item for anything!");
		}
		return false;
	}
}
