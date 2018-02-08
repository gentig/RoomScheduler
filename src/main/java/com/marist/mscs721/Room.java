package com.marist.mscs721;

import com.sun.javafx.beans.IDProperty;

import java.util.ArrayList;
import java.util.UUID;

public class Room {

	private String ID;
	private String name;
	private int capacity;
	private ArrayList<Meeting> meetings;
	
	
	public Room(String newName, int newCapacity) {
		setName(newName);
		setCapacity(newCapacity);
		setMeetings(new ArrayList<Meeting>());
		this.ID = UUID.randomUUID().toString();
	}

	public void addMeeting(Meeting newMeeting) {
		this.getMeetings().add(newMeeting);
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getCapacity() {
		return capacity;
	}


	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}


	public ArrayList<Meeting> getMeetings() {
		return meetings;
	}


	public void setMeetings(ArrayList<Meeting> meetings) {
		this.meetings = meetings;
	}

	public String getID(){
		return ID;
	}
}
