package com.marist.mscs721;

//Using Gson library for JSON
import com.google.gson.Gson;
//Log4j for debugging
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.*;
//Using java8 nio for file manipulation
import java.nio.file.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class RoomScheduler {
    final static Logger logger = Logger.getLogger(RoomScheduler.class);
	private static Scanner keyboard = new Scanner(System.in);
	//jsonDir is a path outside the project. Not used for now. I might use it later
    //private static String jsonDir = System.getProperty("user.home").replace("\\","/") + "/GG_HOME_FOLDER/marist/Spring_2018/mscs721/homework/hw_1/json_files/";
    public static final String DEVIDER = "---------------------";
    //Compare two timestams
    public static CompareTimestamp compare;

    /**
     * main
     *
     *Initialize program
     * @param args command line arguments
     * @throws IOException throws exception from functions
     */
	public static void main(String[] args) throws IOException {
		Boolean end = false;
		ArrayList<Room> rooms = new ArrayList<>();
		//logger.info("Importing rooms if they are available. We can use JSON files to persist the data" +
        //        "so we import any available rooms first.");
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
                    System.out.println("Wrong menu selection");
            }

        }

	}

	/**
     * listSchedule
     *
     * List the schedule for the room
     *
     * @param roomList a list of rooms
     * @return String
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
		while(keyboard.hasNext()){
		    if(keyboard.hasNextInt()) {
                return keyboard.nextInt();
            } else{
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
		}
		return 0;
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
	    int capacity = 0;
		System.out.println("Add a room:");
		String name = getRoomName();
		System.out.println("Room capacity?");
		if(keyboard.hasNextInt()){
		    capacity = keyboard.nextInt();
		    /**
             * Avoid negative capacity
             * @TODO: 2/24/2018 Fix magic numbers like 0
             */
            if(capacity <= 0){
                logger.error("Capacity should be bigger than zero");
                return "Error";
            }
        }else {
            logger.error("Error: Integer cannot be larger than " + Integer.MAX_VALUE);
            keyboard.next();//consume
            return "Error";
        }
        //If room exists delete it and update it with the new one
        if(roomExists(name,roomList)){
		    logger.error("Room " + name + " already exists. An update will be performed");
		    /**
             * @TODO: 2/24/2018  Ask user if they want to perform this step
             *
             * NOTE: This removes the room silently
             */
		    roomList.remove(findRoomIndex(roomList,name));
        }
		Room newRoom = new Room(name, capacity);
		roomList.add(newRoom);
		return "Room '" + newRoom.getName() + "' added successfully!";
	}

	/**
     * exportRooms
     *
     * Export rooms to json. Files.write operation uses StandartOpenOption CREATE, TRUNCATE_EXISTING
     * So the existing file will be replace with the new file
     *
     * @param roomList a list of rooms
     * @return String
     * @throws IOException cannot export json files
     */
	protected static String exportRooms(ArrayList<Room> roomList) throws IOException{
	    if(!isListEmpty(roomList)) {
            System.out.println(DEVIDER);
            Gson gs = new Gson();
            ArrayList<String> list = new ArrayList<>();
            String jsonRoom, roomName = "";
            for (Room room : roomList) {
                jsonRoom = gs.toJson(room, Room.class);
                list.add(jsonRoom);
            }
            for (String jsonString : list) {
                roomName = gs.fromJson(jsonString, Room.class).getName();
                try {
                    //Passing bytes to the Files.write fixes the problem with iterable argument that we need here
                    Files.write(Paths.get("jsonfiles/" + roomName + ".json"), jsonString.getBytes());
                } catch (IOException e) {
                    logger.trace("Cannot write to JSON file...", e);
                }
            }
            System.out.println(DEVIDER);
            return "";
        }
        logger.error("No rooms to export.");
	    return "";
    }

    /**
     * importRooms
     *
     * Import rooms from json
     *
     * @param roomList the list of rooms
     * @return String
     * @throws IOException cannot import json file/s
     */
    protected static String importRooms(ArrayList<Room> roomList) throws IOException{
        Gson gson = new Gson();
        //Path pathToDir = Paths.get(jsonDir);//path to dir of Json files //original @see jsonDir field comment
        Path pathToDir = Paths.get("jsonFiles");//"jsonFiles" is directory of all Json files
        JsonFiles jsonFiles = new JsonFiles();//callback object for walking directories and files
        Files.walkFileTree(pathToDir , jsonFiles);
        List<Path> allJsonFilesInPath = JsonFiles.getPaths();//get all paths from JsonFiles
        //check if we have rooms to import
        if(!isListEmpty(allJsonFilesInPath)) {
            for (Path pth : allJsonFilesInPath) {
                try (BufferedReader reader = Files.newBufferedReader(pth)) {
                    Room room = gson.fromJson(reader, Room.class);
                    if(roomExists(room.getName(),roomList)){
                        logger.error("Room " + room.getName() + " already exists. An update will be performed");
                        //Delete existing, to be replaced by the import
                        roomList.remove(findRoomIndex(roomList,room.getName()));
                    }
                    roomList.add(room);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "";
        }
        logger.error("No rooms were imported. Missing import files");
        return "";
    }

	/**
     * removeRoom
     *
     * Removing room from the list
     *
     * @param roomList a list of rooms
     * @return String
     */
	protected static String removeRoom(ArrayList<Room> roomList) {
	    String roomName = "";
	    if(!isListEmpty(roomList)) {
            System.out.println("Remove a room:");
            roomName = getRoomName();
            if(roomExists(roomName,roomList)) {
                roomList.remove(findRoomIndex(roomList, roomName));
                return "Room removed successfully!";
            }
            logger.error("No rooms with the name " + roomName + " found");
            return "Error";
        }
        logger.error("List of rooms is empty, no rooms to remove");
        return "Error";
	}

	/**
     * listRooms
     *
     * @param roomList the list of rooms
     * @return String
     */
	private static String listRooms(ArrayList<Room> roomList) {
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
	private static String scheduleRoom(ArrayList<Room> roomList) {
        //Compare interface for comparing timestamps
        compare = (start,end)-> start.after(end);
        String subject = "";
        if(!isListEmpty(roomList)) {
            System.out.println("Schedule a room:");
            //Check if room exists
            String name = getRoomName();
            if(!roomExists(name,roomList)){
                logger.error("Room " + name + " does not exists");
                return "Error";
            }
            //Start Date
            System.out.println("Start Date? (yyyy-mm-dd):");
            String startDate = keyboard.next();
            String dateString = getDate(startDate);
            //Check if date is correct
            if(0 == dateString.compareTo("false")){
                return "Error";
            }
            //Start Time
            System.out.println("Start Time? (HH:mm)");
            String startTime = keyboard.next();
            String timeString = getTime(startTime);
            //Check if time is correct
            if(0 == timeString.compareTo("false")){
                return "Error";
            }
            //End Date
            System.out.println("End Date? (yyyy-mm-dd):");
            String endDate = keyboard.next();
            String dateStringEnd = getDate(endDate);
            if(0 == dateStringEnd.compareTo("false")){
                return "Error";
            }
            //End Time
            System.out.println("End Time? (HH:mm)");
            String endTime = keyboard.next();
            String timeStringEnd = getTime(endTime);
            if(0 == timeStringEnd.compareTo("false")){
                return "Error";
            }
            //Current meeting timestamps
            Timestamp startTimestamp = Timestamp.valueOf(dateString + " " + timeString + ":00");//we need seconds for timestamp
            Timestamp endTimestamp = Timestamp.valueOf(dateStringEnd + " " + timeStringEnd + ":00");//we need seconds for timestamp

            //Adding subject
            System.out.println("Subject?");
            keyboard.nextLine();//consume "\n" left from previous next or nextInt
            if(keyboard.hasNextLine()) {
                //subject = keyboard.nextLine();//There is a problem here cannot get scanner for subject, fix it
                subject = keyboard.nextLine();
                if(subject.length() > 200){
                    logger.error("No more than 200 characters for the subject allowed");
                    return "Error";
                }
            }
            Room curRoom = getRoomFromName(roomList, name);
            //Get all already set meetings
            //@// TODO: 1/30/2018 Check for time conflict with current meeting to be added
            ArrayList<Meeting> me = curRoom.getMeetings();
            //Check if time is in the past
            if(timeInThePast(startTimestamp,endTimestamp)){
                logger.error("Time in the past");
                return "Error";
            }
            if(!me.isEmpty()){
                String answer = checkMeetingConflict(me,startTimestamp,endTimestamp);
                return "Error " + answer;
            }
            Meeting meeting = new Meeting(startTimestamp, endTimestamp, subject);
            curRoom.addMeeting(meeting);
            return "Successfully scheduled meeting!";
	    }
	    logger.error("Cannot schedule meeting. No rooms available");
	    return "";
	}

	/**
     * roomExists
     *
     * @param name name of the room
     * @param roomList list of rooms to search
     * @return boolean
     */
    private static boolean roomExists(String name,ArrayList<Room> roomList) {
        for (Room rm : roomList) {
            if( 0 == name.compareTo(rm.getName())){
                return true;
            }
        }
        return false;
    }

    /**
     * timeInThePast
     *
     * @param start start timestamp
     * @param end end timestamp
     *
     * @return boolean true if time in the past, false otherwise
     */
	private static Boolean timeInThePast(Timestamp start, Timestamp end){
	    //set the interface
	    compare = (first,second)-> first.after(second);
        //Check start time is not in the past
        if(!compare.compare(start,new Timestamp(System.currentTimeMillis()))){
            logger.error("Start time cannot be in the past");
            return true;
        }
        //Check end time is not in the past
        if(!compare.compare(end,new Timestamp(System.currentTimeMillis()))){
            logger.error("End time cannot be in the past");
            return true;
        }
        return false;
    }

    /**
     * checkMeetingConflict
     *
     * Check if there is a conflict with before adding a meeting
     *
     * @param meetings a list of meetings for a room
     * @param startTimestamp start time of the meeting
     * @param endTimestamp end time of the meeting
     * @return String
     */
    private static String checkMeetingConflict(ArrayList<Meeting> me, Timestamp startTimestamp, Timestamp endTimestamp){
        String answer = "";
        for (Meeting mt : me) {
                //System.out.println("StartMeeting: " + meetingStart.getTime() + " EndMeeting: " + meetingEnd.getTime());
            if (endTimestamp.before(startTimestamp)) {
                //error
                System.out.println("Stop time cannot be before start time");
                answer = "conflict";
                break;
            } else if (startTimestamp.after(mt.getStartTime()) && startTimestamp.before(mt.getStopTime())) {
                //conflict
                System.out.println("Start time is in the middle of existing");
                answer="conflict";
                break;
            } else if (endTimestamp.after(mt.getStartTime()) && endTimestamp.before(mt.getStopTime())) {
                //conflict
                System.out.println("End time is in the middle of existing");
                answer="conflict";
            } else if (startTimestamp.before(mt.getStartTime()) && endTimestamp.after(mt.getStopTime())) {
                //conflict
                System.out.println("Start time is good but End time is bigger than existing stop time");
                answer="conflict";
            }else{
                System.out.println("Unknown error");
                answer= "unknown";
                break;
            }
        }
        return answer;
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
            return date.format(formatter);
        }
        catch (DateTimeParseException exc) {
            logger.error("String " + input + " is not parsable!" + exc);
            return "false";
        }
    }

    /**
     * getTime
     *
     * Get the time and check for error
     *
     * @param input time as string
     * @return String
     */
    protected static String getTime(String input){
        try {
            LocalTime time = LocalTime.parse(input);
            System.out.println(time.toString());
            return time.toString();
        }
        catch (DateTimeParseException exc) {
            logger.error("String " + input + " is not parsable!" + exc);
            return "false";
        }
    }

    /**
     * getTimeStamp
     *
     * Get the timeStamp and check for error
     *
     * @param input datetime formated as string
     * @return String
     *
     * @TODO 2/18/2018 Finish this
     */
    protected static String getTimeStamp(String input){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime date = LocalDateTime.parse(input, formatter);
            Timestamp ts = Timestamp.valueOf(date);
            return ts.toString();
        }
        catch (DateTimeParseException exc) {
            logger.error("String " + input + " is not parsable!");
            return "false";
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

	/**
     * checkForRoom
     *
     * This function checks if there is at least one room
     * in a list of rooms
     *
     * @param roomList A list of Rooms
     *
     * @return boolean true or false
     */
    private static Boolean isListEmpty(List list){
        if (list.isEmpty()){
            logger.info("List is empty");
            return true;
        }
        return false;
    }
}
