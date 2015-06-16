package com.bayviewglen.zork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

class Game implements java.io.Serializable {
	private Parser parser;
	private Room currentRoom;
	private Room previousRoom;
	private Date endTime;
	private Date pauseTime;
	private Date scienceTime;

	// This is a MASTER object that contains all of the rooms and is easily
	// accessible.
	// The key will be the name of the room -> no spaces (Use all caps and
	// underscore -> Great Room would have a key of GREAT_ROOM
	// In a hashmap keys are case sensitive.
	// masterRoomMap.get("GREAT_ROOM") will return the Room Object that is the
	// Great Room (assuming you have one).
	private HashMap<String, Character> listOfCharacters = new HashMap<String, Character>();
	private Inventory inventoryItems = new Inventory();
	private Items myBackpack, sabaBackpack;
	private Inventory allItems = new Inventory();
	private int scienceCounter = 0;
	private boolean finished = false;
	private boolean givenSabaBackpack = false;
	private boolean gottenMyBackpack = false;
	private boolean solvedRiddle = false;

	private HashMap<String, Room> masterRoomMap;

	private void initializeItems() {
		sabaBackpack = createItem("backpack", masterRoomMap.get("PHYSICS_CLASSROOM"), 6);
		myBackpack = createItem("myBackpack", masterRoomMap.get("MR.AULD'S_OFFICE"), 6);
	}

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

		initializeCharacters();
		initializeItems();
	}

	/**
	 * Main play routine. Loops until end of play.
	 */
	public void play() {

		File saveFile = new File("data/saveFile.dat");

		if (saveFile.length() != 0) {
			try {
				FileInputStream f_in = new FileInputStream("data/saveFile.dat");
				ObjectInputStream ois = new ObjectInputStream(f_in);
				Object[] objectArray = (Object[]) ois.readObject();
				currentRoom = (Room) objectArray[0];
				inventoryItems = (Inventory) objectArray[1];
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {

			printWelcome();

			Date now = new Date();
			endTime = new Date(now.getTime() + 6000 * 1000L);
		}

		// Enter the main command loop. Here we repeatedly read commands and
		// execute them until the game is over.

		Command command = parser.getCommand();
		finished = processCommand(command);

		while (!finished) {
			command = parser.getCommand();
			finished = processCommand(command);
			if (new Date().after(endTime)) {
				System.out.println("Oh no Mr.DesLauriers found you! You weren't fast enough!");
				finished = true;
			}
		}
		saveGame();
		System.out.println("Thank you for playing.  Good bye.");
	}

	private void saveGame() {
		// TODO Auto-generated method stub

	}

	public void initializeCharacters() {
		createCharacter("Saba", masterRoomMap.get("HALLWAY"));
		createCharacter("Hitchcock", masterRoomMap.get("SCIENCES_OFFICE"));
		createCharacter("Auld", masterRoomMap.get("Mr.AULD'S_OFFICE"));
	}

	/**
	 * Print out the opening message for the player.
	 */
	private void printWelcome() {
		System.out.println();
		System.out.println("Welcome to Escape Mr.DesLauriers!");
		System.out.println("This is an exciting game where you must attempt to hand in an overdue assignment without being caught by your evil computer science teacher.");
		System.out.println("Your game will be saved automatically.");
		System.out.println("Mentor group just ended and you need to get some files from Saba before you can finish your assignment.");
		System.out.println("Your mentor just told you Mr.DesLauriers is looking for you...if he finds you now you'll get 0 on the assignment.");
		System.out.println("Make sure you hand it in before he finds you!");
		System.out.println();
		System.out.println("Your command words are: ");
		parser.showCommands();
		System.out.println();
		System.out.println("The existing items are: backpack, myBackpack, USB, key\n");
		System.out.println("Here is a map of the school:");
		printMap();
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
			if (command.hasSecondWord()) {
				System.out.println("Quit what?");
			} else {
				try {
					FileOutputStream f_out = new FileOutputStream("data/saveFile.dat");
					ObjectOutputStream gamestate = new ObjectOutputStream(f_out);
					Object[] laziness = { currentRoom, inventoryItems };
					gamestate.writeObject(laziness);
					gamestate.close();
					System.out.println("Game Saved.");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return true;
			}
			; // signal that we want to quit
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
			printMap();
		} else {
			describeRoom();
		}
	}

	public void printMap() {
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
	}

	// implementations of user commands:

	/**
	 * Print out some help information. Here we print some stupid, cryptic
	 * message and a list of the command words.
	 */
	private void printHelp() {
		System.out.println("You are trying to finish your computer science project before Mr.DesLauriers catches you. Be fast, you don't have much time!");
		System.out.println();
		System.out.print("Your command words are: " + "yes  " + "nevermind  ");
		parser.showCommands();
		System.out.println("The existing items are: backpack, myBackpack, USB, key");
	}

	/**
	 * Try to go to one direction. If there is an exit, enter the new room,
	 * otherwise print an error message.
	 */
	private void goRoom(Command command) {
		if (!command.hasSecondWord()) {
			// if there is no second word, we don't know where to go...
			System.out.println("Go where?");
			return;
		}

		String direction = command.getSecondWord();

		if (direction.equalsIgnoreCase("back")) {
			if (previousRoom == null) {
				System.out.println("Go where?");
			} else {
				currentRoom = previousRoom;
				describeRoom();
				return;
			}
		}

		// Try to leave current room.
		Room nextRoom = currentRoom.nextRoom(direction);

		if (nextRoom == null) {
			System.out.println("There is no door!");
		} else if (nextRoom.getRoomName().equalsIgnoreCase("physics classroom")) {
			if (inventoryItems.contains("key")) {
				previousRoom = currentRoom;
				currentRoom = nextRoom;
				System.out.println("You used the key to get into the physics classroom.");
				describeRoom();
			} else {
				System.out.println("The door is locked! Maybe Mr.Hitchcock has the key...");
			}
		} else if (nextRoom.getRoomName().equalsIgnoreCase("mr.auld's office")) {
			if (givenSabaBackpack) {
				previousRoom = currentRoom;
				currentRoom = nextRoom;
				describeRoom();
			} else {
				System.out.println("Mr.Auld looks busy right now. Maybe you should get Saba her backpack first.");
			}

		} else if (nextRoom.getRoomName().equalsIgnoreCase("sciences office")) {
			if (scienceCounter == 0) {
				nextRoom = currentRoom;
				System.out.println("Oh no! It looks like Mr.DesLauriers is in his office. If you go in he might catch you...maybe you should wait 20 seconds.");
				Date now = new Date();
				scienceTime = new Date(now.getTime() + 20 * 1000);
				scienceCounter++;
			} else if (!new Date().after(scienceTime)) {
				System.out.println("Oh no! Mr.DesLauriers caught you.");
				System.out.println("The game is now over. Thanks for playing!");
				System.exit(0);
			} else {
				previousRoom = currentRoom;
				currentRoom = nextRoom;
				describeRoom();
				scienceCounter++;
			}
		} else {
			previousRoom = currentRoom;
			currentRoom = nextRoom;
			describeRoom();
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
			} else if (currentRoom.getRoomName().equalsIgnoreCase("HALLWAY 1") && command.getSecondWord().equalsIgnoreCase("backpack")) {
				sabaBackpack.setLocation(currentRoom);
				inventoryItems.removeItem(command.getSecondWord());
				allItems.removeItem(command.getSecondWord());
				System.out.println("Saba: Thanks for bringing my backpack! Here's the USB.");
				System.out.println("");
				System.out
						.println("You look for your backpack in the hallway, which is where you left it, but it's not there! \nYou need your computer to use the USB. Hmm...who would've taken your backpack?");
				inventoryItems.addToInventory(createItem("USB", masterRoomMap.get("HALLWAY 1"), 2));
				givenSabaBackpack = true;
			} else if (!currentRoom.getRoomName().equalsIgnoreCase("HALLWAY 1") && command.getSecondWord().equals("backpack")) {
				System.out.println("Do you really think you should be throwing Saba under the bus like this? She needs her textbook!");
			} else if (command.getSecondWord().equalsIgnoreCase("key")) {
				System.out.println("You really shouldn't be dropping that key!");
			} else {
				inventoryItems.removeItem(command.getSecondWord());
				System.out.println("You have dropped the " + command.getSecondWord() + ".");
				allItems.getItem(command.getSecondWord()).setLocation(currentRoom);
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
					System.out.println("You have picked up the " + command.getSecondWord() + ".");
				} else {
					System.out.println("This item is not in the same room as you!");
				}
			}
		}
	}

	private void talkCharacter(Command command) {
		if (!command.hasSecondWord()) {
			System.out.println("Talk to who?");
		} else {
			String word = command.getSecondWord();
			if (word.equalsIgnoreCase("Mr.Hitchcock") && !(currentRoom.getRoomName().equalsIgnoreCase("sciences office"))) {
				System.out.println("Mr.Hitchcock doesn't seem to be in " + currentRoom.getRoomName());
			} else if (word.equalsIgnoreCase("Mr.Hitchcock") && !solvedRiddle) {
				System.out.println("Mr.Hitchcock: Hello there, you must be looking to get in to the physics classroom. I saw a bag in there this morning and I knew someone would need it.");
				System.out.println("Mr.Hitchcock: Do you want the key for the physics classroom?");
				Scanner keyboard = new Scanner(System.in);

				String decision = keyboard.nextLine();

				if (decision.equalsIgnoreCase("no")) {
					System.out.println("Mr.Hitchcock: I hope you don't need that backpack.");
				} else if (decision.equalsIgnoreCase("yes")) {
					System.out.println("Mr.Hitchcock: So you want the key? That means it's time for a riddle.");
					System.out.println("Mr.Hitchcock: What is right hand rule 0?");
					System.out.println("Mr.Hitchcock: If you can't figure out the answer just say nevermind. I won't give you the key but you can come back and try again later. \n>");
					String answer = keyboard.nextLine();
					while (!answer.equalsIgnoreCase("use your right hand")) {
						System.out.println("Mr.Hitchcock: That's not it... \n>");
						answer = keyboard.nextLine();
						if (answer.equalsIgnoreCase("nevermind")) {
							System.out.println("Mr.Hitchcock: I hope you don't need that backpack.");
							return;
						}
					}
					System.out.println("Mr.Hitchcock: You finally got the answer! Here's the key to the classroom. Have a great day!");
					inventoryItems.addToInventory(createItem("key", masterRoomMap.get("SCIENCES_CLASSROOM"), 1));
					solvedRiddle = true;
				} else {
					System.out.println("Mr.Hitchcock: If you want to say something to me come back and talk to me.");
				}
			} else if (word.equalsIgnoreCase("Mr.Hitchcock")) {
				System.out.println("Don't you have a project to finish or something?");
			} else if (word.equalsIgnoreCase("saba") && !(currentRoom.getRoomName().equalsIgnoreCase("hallway 1"))) {
				System.out.println("Saba doesn't seem to be in " + currentRoom.getRoomName());
			} else if (word.equalsIgnoreCase("saba")) {
				if (!givenSabaBackpack && !inventoryItems.contains("backpack")) {
					System.out
							.println("Saba: Hey, you need that file for your project right? I left my backpack in the physics classroom and it has my USB in it. If you bring me my backpack I can give you the files.");
				} else if (!givenSabaBackpack) {
					System.out.println("Saba: Can you put down my backpack please?");
				} else {
					System.out.println("Saba: You should use the USB to finish your project. Good luck!");
				}
			} else if (word.equalsIgnoreCase("mr.auld") && !(currentRoom.getRoomName().equalsIgnoreCase("mr.auld's office"))) {
				System.out.println("Mr.Auld doesn't seem to be in " + currentRoom.getRoomName());
			} else if (word.equalsIgnoreCase("mr.auld")) {
				if (!gottenMyBackpack) {
					System.out
							.println("Mr.Auld: You must be here to pick up yor backpack. I am disappointed that you left your bag in the hall, especially because it's a fire hazard. \nHere's your backpack but please don't do it again!");
					inventoryItems.addToInventory(myBackpack);
					gottenMyBackpack = true;
				} else {
					System.out.println("You again? What do you want? I am a very busy man!");
				}
			}
		}
	}

	private boolean useItem(Command command) {
		if (!command.hasSecondWord()) {
			System.out.println("What are you trying to use?");
		} else if (command.getSecondWord().equalsIgnoreCase("USB") && !inventoryItems.contains("myBackpack")) {
			System.out.println("How can you use the USB without your computer? You need your backpack to get your computer.");
		} else if (command.hasSecondWord() && command.getSecondWord().equalsIgnoreCase("USB") && inventoryItems.contains("USB")) {
			System.out.println("file 2");
			System.out.println("file 3");
			System.out.println("file 8");
			System.out.print("Please pick a file to use: ");
			Scanner keyboard = new Scanner(System.in);
			String fileNumber = keyboard.nextLine();
			if (!fileNumber.equals("3") && !fileNumber.equals("file 3")) {
				System.out
						.println("Oh no! Looks like Saba had some nasty files on her USB and you downloaded them! Wonder what those could have been for... \nAnyway, you took your computer to the tech office which took 10 minutes! \nBetter hurry up and download the right file so you can finish your project!");
				subtractTime();
			} else {
				System.out.println("Congratulations! You uploaded the files to gitHub before Mr.DesLauriers caught you. Thanks for playing! ");
				System.exit(0);
			}
		} else if (command.hasSecondWord() && command.getSecondWord().equalsIgnoreCase("USB") && !inventoryItems.contains("USB")) {
			System.out.println("How are you planning to use something that you don't have?");
		} else {
			System.out.println("You can't use that item for anything!");
		}
		return false;
	}

	private void subtractTime() {
		long ending = endTime.getTime();
		endTime = new Date(ending - (60 * 10 * 1000));
	}
}
