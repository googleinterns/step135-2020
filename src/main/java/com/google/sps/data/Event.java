// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
package com.google.sps.data;
 
/**
 * Class that creates an event specific to a POI 
 */
public class Event {
 
  // inputs
  private String name;
  private String address;
  private String date;
  private int startTime;
  private int endTime;
  private int travelTime;

  // fields to be saved for parsing
  private String strStartTime;
  private String strEndTime;
 
  // class constants
  private final static int HALFHOUR = 30;
  private final static int HOUR = 60;
  private final static int MINUTES_IN_A_DAY = 1440;
  private final static int MIN_POSSIBLE_TIME = 0;

  /**
   * Constructor that takes in time spent at location
   * 
   * @param name date from user input travel day. Cannot be null.
   * @param address time to to start or end event. Must be within bounds
   * @param date day of travel
   * @param startTime start of activing
   * @param travelTime time spent traveling to next location
   * @param timeAtLocation time spent at POI
   */
  public Event(String name, String address, String date, int startTime, 
              int travelTime, int timeAtLocation) {
    this.name = name;
    this.address = address;
    this.date = date;
    this.startTime = startTime;
    this.travelTime = travelTime;
    this.endTime = calculateEndTime(timeAtLocation);

    createCalendarTimes();
  }
 
  /**
   * Constructor that assume one hour default spent at location
   * 
   * @param name date from user input travel day. Cannot be null.
   * @param address time to to start or end event. Must be within bounds
   * @param date day of travel
   * @param startTime start of activing
   * @param travelTime time spent traveling to next location
   */
  public Event(String name, String address, String date, int startTime, int travelTime) {
    this.name = name;
    this.address = address;
    this.date = date;
    this.startTime = startTime;
    this.travelTime = travelTime;
    this.endTime = calculateEndTime(HOUR);

    createCalendarTimes();
  }

  // function that sets start and end Time with correct string format
  private void createCalendarTimes() {
    this.strStartTime = createStrTime(this.date, this.startTime);
    this.strEndTime = createStrTime(this.date, this.endTime);
  }

  /**
   * function that builds string w correct format for calendar-script.js
   * 
   * @param date date from user input travel day. Cannot be null.
   * @param time time to to start or end event. Must be within bounds
   */
  private static String createStrTime(String date, int time) {
    if (date == null) {
      throw new NullPointerException("Date cannot be null");
    }

    if (time < MIN_POSSIBLE_TIME) {
      throw new IllegalArgumentException("Time cannot be less than 0");
    }

    if (time >= MINUTES_IN_A_DAY) {
      throw new IllegalArgumentException("Time cannot be more than a 24hrs");
    }

    String output = "";
    output += date + "T" + Integer.toString(time).substring(0, 2) + ":" + 
      Integer.toString(time).substring(2, 4) + ":00";
    return output;
  }

  /**
   * function that calculates endTime given start and timeSpent
   * 
   * @param timeAtLocation time that the user spends at a POI
   */
  private int calculateEndTime(int timeAtLocation) {
    return this.startTime + Integer.parseInt(convertToFormat(timeAtLocation));
  }

  /**
   * function that converts mintues into hhmm format
   * 
   * @param time time in minutes
   */
  public static String convertToFormat(int time) {
    if (time < MIN_POSSIBLE_TIME) {
      throw new IllegalArgumentException("Time cannot be less than 0");
    }

    if (time >= MINUTES_IN_A_DAY) {
      throw new IllegalArgumentException("Time cannot be more than a 24hrs");
    }

    String numHours = Integer.toString(time / 60);
    String numMins = Integer.toString(time % 60);

    // add 0's to front of number if neccessary
    if (Integer.parseInt(numHours) < 10) {
      numHours = "0" + numHours;
    } 

    if (Integer.parseInt(numMins) < 10) {
      numMins = "0" + numMins;
    }

    return numHours + numMins;
  }

  // getter functions
  public String getName() {
    return this.name;
  }

  public String getAddress() {
    return this.address;
  }

  public String getDate() {
    return this.date;
  }

  public int getStartTime() {
    return this.startTime;
  } 

  public int getEndTime() {
    return this.endTime;
  }

  public int getTravelTime() {
    return this.travelTime;
  }

  public String getStrStartTime() {
    return this.strStartTime;
  } 

  public String getStrEndTime() {
    return this.strEndTime;
  }

}
