package com.marist.mscs721;

import com.google.gson.Gson;

import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class RoomScheduler {
	protected static Scanner keyboard = new Scanner(System.in);
	private static String jsonDir = "C:/Users/genti/GG_HOME_FOLDER/marist/Spring_2018/mscs721/homework/hw_1/json_files/";
    public static final String DEVIDER = "---------------------";
	public static void main(String[] args) {
		Boolean end = false;
		ArrayList<Room> rooms = new ArrayList<>();
        while (!end) {
            switch (mainMenu()) {
                case 1:
                    System.out.println(addRoom(rooms));
                    break;
                case 2:
                    System.out.println(removeRoom(rooms));
                    break;
                case 3:
                    System.out.println(scheduleRoom(rooms));
                    break;
                case 4:
                    System.out.println(listSchedule(rooms));
                    break;
                case 5:
                    System.out.println(listRooms(rooms));
                    break;
                case 6:
                    System.out.println(exportRooms(rooms));
                    break;
                case 7:
                    System.out.println(importRooms(rooms));
                    break;
                default:
                    System.out.println("Error");
            }

        }

	}

	/**
     * listSchedule
     *
     * List the schedule for the room
     *
     * @param: ArrayList roomList
     * @return: String
     * */
	protected static String listSchedule(ArrayList<Room> roomList) {
		String roomName = getRoomName();
		System.out.println(roomName + " Schedule");
		System.out.println(DEVIDER);
		
		for (Meeting m : getRoomFromName(roomList, roomName).getMeetings()) {
			System.out.println(m.toString());
		}

		return "";
	}

	/**
	 * mainMenu
     *
	 * Display the menu and user selection
	 *
	 * @return int
	 */
	protected static int mainMenu() {
		System.out.println("Main Menu:");
		System.out.println("  1 - Add a room");
		System.out.println("  2 - Remove a room");
		System.out.println("  3 - Schedule a room");
		System.out.println("  4 - List Schedule");
		System.out.println("  5 - List Rooms");
        System.out.println("  6 - Export Rooms");
        System.out.println("  7 - Import Rooms");
		System.out.println("Enter your selection: ");

		/**
		 * Make sure we have an int
		 * Display the menu until we get an integer
		 * Let the user know that they need to input an int
		 * Display the menu again
		 */
		while(!keyboard.hasNextInt()){
			System.out.println("Error: Make sure you enter an integer.");
			System.out.println("Main Menu:");
			System.out.println("  1 - Add a room");
			System.out.println("  2 - Remove a room");
			System.out.println("  3 - Schedule a room");
			System.out.println("  4 - List Schedule");
			System.out.println("  5 - List Rooms");
            System.out.println("  6 - Export Rooms");
            System.out.println("  7 - Import Rooms");
			System.out.println("Enter your selection: ");
			keyboard.next();
		}
		return keyboard.nextInt();
	}

	/**
     * addRoom
     *
     * Adding the room for user.
     *
     * @param  roomList the list of rooms
     * @return String
     */
	protected static String addRoom(ArrayList<Room> roomList) {
		System.out.println("Add a room:");
		String name = getRoomName();
		System.out.println("Room capacity?");
		int capacity = keyboard.nextInt();

		Room newRoom = new Room(name, capacity);
		roomList.add(newRoom);

		return "Room '" + newRoom.getName() + "' added successfully!";
	}

	/**
     * exportRooms
     *
     * Export rooms to json
     *
     * @param: roomList the list of rooms
     * @return String
     */
	protected static String exportRooms(ArrayList<Room> roomList){
        System.out.println("Export rooms");
        System.out.println(DEVIDER);
        Gson gs = new Gson();
        String json;
        for (Room room : roomList) {
            json = gs.toJson(room);
            System.out.println("Json: " + " - " + json);

            try (PrintWriter out = new PrintWriter(jsonDir+room.getName()+".json")) {
                out.println(json);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println(DEVIDER);
	    return "";
    }

    /**
     * importRooms
     *
     * Import rooms from json
     *
     * @param roomList the list of rooms
     * @return String
     */
    protected static String importRooms(ArrayList<Room> roomList){
        Gson gson = new Gson();
        FileReader fr = null;
        File folder = new File(jsonDir);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                try
                {
                    fr = new FileReader(jsonDir+listOfFiles[i].getName());
                    Room room = gson.fromJson(fr, Room.class);
                    roomList.add(room);
                    System.out.println("Room " + "'" +room.getName()+ "'" + " imported successfully!");
                }
                catch (FileNotFoundException fe)
                {
                    System.out.println("File not found");
                }finally {
                    try {
                        fr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
                //@TODO print error
            }
        }
        return "";
    }

	/**
     * removeRoom
     *
     * Removing romm from the list
     *
     * @param roomList
     * @return String
     */
	protected static String removeRoom(ArrayList<Room> roomList) {
		System.out.println("Remove a room:");
		roomList.remove(findRoomIndex(roomList, getRoomName()));

		return "Room removed successfully!";
	}

	/**
     * listRooms
     *
     * @param roomList the list of rooms
     * @return String
     */
	protected static String listRooms(ArrayList<Room> roomList) {
		System.out.println("Room Name - Capacity");
		System.out.println(DEVIDER);
		for (Room room : roomList) {
			System.out.println(room.getName() + " - " + room.getCapacity());
		}

		System.out.println(DEVIDER);

		return roomList.size() + " Room(s)";
	}

	/**
     * scheduleRoom
     *
     * Adding room schedule to the list
     *
     * @param roomList the list of rooms
     * @return String
     **/
	protected static String scheduleRoom(ArrayList<Room> roomList) {
		System.out.println("Schedule a room:");
		String name = getRoomName();
		//Start Date
		System.out.println("Start Date? (yyyy-mm-dd):");
		String startDate = keyboard.next();
		String dateString = getDate(startDate);
		//Start Time
		System.out.println("Start Time? (HH:mm)");
		String startTime = keyboard.next();
		//startTime = startTime + ":00"; //Don't need this when using time library
		String timeString = getTime(startTime);

		//End Data
		System.out.println("End Date? (yyyy-mm-dd):");
		String endDate = keyboard.next();
        String dateStringEnd = getDate(endDate);
		//End Time
		System.out.println("End Time? (HH:mm)");
		String endTime = keyboard.next();
		//endTime = endTime + ":00.0";
        String timeStringEnd = getTime(endTime);

        //Current meeting timestamps
		Timestamp startTimestamp = Timestamp.valueOf(dateString + " " + timeString + ":00");
		Timestamp endTimestamp = Timestamp.valueOf(dateStringEnd + " " + timeStringEnd + ":00");

		System.out.println("Subject?");
		String subject = keyboard.nextLine();//There is a problem here
		Room curRoom = getRoomFromName(roomList, name);
		//Get all already set meetings
        //@// TODO: 1/30/2018 Check for time conflict with current meeting to be added
		ArrayList<Meeting> me = curRoom.getMeetings();
		if (me.isEmpty()){
            if(endTimestamp.before(startTimestamp)){
                //error
                System.out.println("Stop time cannot be before start time");
                return "Error: Time conflict!";
            }
        }
        for (Meeting mt : me) {
            //System.out.println("StartMeeting: " + meetingStart.getTime() + " EndMeeting: " + meetingEnd.getTime());
            if(endTimestamp.before(startTimestamp)){
              //error
                System.out.println("Stop time cannot be before start time");
                return "Error: Time conflict!";
            }else if(startTimestamp.after(mt.getStartTime()) && startTimestamp.before(mt.getStopTime())){
              //conflict
                System.out.println("Start time is in the middle of existing");
                return "Error: Time conflict!";
            }else if(endTimestamp.after(mt.getStartTime()) && endTimestamp.before(mt.getStopTime())){
              //conflict
                System.out.println("End time is in the middle of existing");
                return "Error: Time conflict!";
            }else if(startTimestamp.before(mt.getStartTime()) && endTimestamp.after(mt.getStopTime())){
              //conflict
                System.out.println("Start time is good but End time is bigger than existing stop time");
                return "Error: Time conflict!";
            }
        }

		Meeting meeting = new Meeting(startTimestamp, endTimestamp, subject);
		curRoom.addMeeting(meeting);

		return "Successfully scheduled meeting!";
	}

	/**
     * getDate
     *
     * Get the date and check for error
     *
     * @param input date
     * @return String
     */
	protected static String getDate(String input){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(input, formatter);
            System.out.println(date.toString());
            return date.toString();
        }
        catch (DateTimeParseException exc) {
            System.out.println("String " + input + " is not parsable!");
            return "false";
            //throw exc;      // Rethrow the exception.
        }
    }

    /**
     * getDate
     *
     * Get the date and check for error
     *
     * @param input
     * @return String
     */
    protected static String getTime(String input){
        try {
            LocalTime time = LocalTime.parse(input);
            System.out.println(time.toString());
            return time.toString();
        }
        catch (DateTimeParseException exc) {
            System.out.println("String " + input + " is not parsable!");
            return "false";
            //throw exc;      // Rethrow the exception.
        }
    }

    /**
     * getDate
     *
     * Get the date and check for error
     *
     * @param input
     * @return String
     *
     * @// TODO: 1/30/2018 Dont need this for now
     */
    protected static String getTimeStamp(String input){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime date = LocalDateTime.parse(input, formatter);
            System.out.println(date.toString());

            Timestamp ts = Timestamp.valueOf(date);
            System.out.println(ts.getTime());

            return ts.toString();
        }
        catch (DateTimeParseException exc) {
            System.out.println("String " + input + " is not parsable!");
            return "false";
            //throw exc;      // Rethrow the exception.
        }
    }

	/**
     * getRoomFromName
     *
     * Get a specific room, searching by name
     *
     * @param roomList the list of rooms
     * @param  name name of the room
     * @return Room
     */
	protected static Room getRoomFromName(ArrayList<Room> roomList, String name) {
		return roomList.get(findRoomIndex(roomList, name));
	}

	/**
     * findRoomIndex
     *
     * Get specific room, searching by the index in the list
     *
     * @param roomList the list of rooms
     * @param roomName room name
     * @return int
     */
	protected static int findRoomIndex(ArrayList<Room> roomList, String roomName) {
		int roomIndex = 0;

		for (Room room : roomList) {
			if (room.getName().compareTo(roomName) == 0) {
				break;
			}
			roomIndex++;
		}

		return roomIndex;
	}

	/**
     * getRoomName
     *
     * Get the room name
     *
     * @return String
     */
	protected static String getRoomName() {
		System.out.println("Room Name?");
		return keyboard.next();
	}

}