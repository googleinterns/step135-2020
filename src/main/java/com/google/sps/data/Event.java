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
 
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class that creates an event specific to a POI 
 */
public class Event {
 
  // inputs
  private String name;
  private String address;
  private String date;
  // format: HHMM
  private int startTime;
  private int endTime;

  //in mins
  private int travelTime;

  // fields to be saved for parsing
  private String strStartTime;
  private String strEndTime;
 
  // class constants
  private static final int HALFHOUR = 30;
  private static final int HOUR = 60;
  private static final int MINUTES_IN_A_DAY = 1440;
  private static final int MIN_POSSIBLE_TIME = 0;

  /**
   * Constructor that takes in time spent at location
   * 
   * @param name name of the location (NOT address)
   * @param address exact address of the POI
   * @param date date from user input travel day. Cannot be null.
   * @param startTime start of activity (HHMM format)
   * @param travelTime time spent traveling to next location (minutes). 
   *        Null if last location of the day.
   * @param timeAtLocation time spent at POI (minutes)
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
   * @param name name of the location (NOT address)
   * @param address exact address of the POI
   * @param date date from user input travel day. Cannot be null.
   * @param startTime start of activity (HHMM format)
   * @param travelTime time spent traveling to next location (minutes)
   *        Null if last location of the day.
   */
  public Event(String name, String address, String date, int startTime, int travelTime) {
    this(name, address, date, startTime, travelTime, HOUR);
  }

  // function that sets start and end Time with correct string format
  private void createCalendarTimes() {
    try {
      this.strStartTime = createStrTime(this.date, this.startTime);
      this.strEndTime = createStrTime(this.date, this.endTime);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * function that builds string w correct format for calendar-script.js
   * 
   * @param date date from user input travel day. Cannot be null.
   * @param time time to to start or end event. Must be within bounds
   */
  private static String createStrTime(String date, int time) throws Exception {
    if (date == null) {
      throw new NullPointerException("Date cannot be null");
    }

    if (time < MIN_POSSIBLE_TIME) {
      throw new IllegalArgumentException("Time cannot be less than " + MIN_POSSIBLE_TIME);
    }

    if (time >= MINUTES_IN_A_DAY) {
      throw new IllegalArgumentException("Time cannot be more than " + MINUTES_IN_A_DAY);
    }

    String output = "";
    output += date + "T" + Integer.toString(time).substring(0, 2) + ":" + 
      Integer.toString(time).substring(2, 4) + ":00";

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      Date d = sdf.parse(output);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return output;
  }

  /**
   * function that calculates endTime given start and timeSpent
   * 
   * @param timeAtLocation time that the user spends at a POI
   */
  private int calculateEndTime(int timeAtLocation) {
    int minsStartTime = (this.startTime / 100) * 60 + (this.startTime % 100);

    return Integer.parseInt(convertToFormat(timeAtLocation + minsStartTime));
  }

  /**
   * function that converts minutes into hhmm format
   * 
   * @param time time in minutes
   */
  public static String convertToFormat(int time) {
    if (time < MIN_POSSIBLE_TIME) {
      throw new IllegalArgumentException("Time cannot be less than " + MIN_POSSIBLE_TIME);
    }

    if (time >= MINUTES_IN_A_DAY) {
      throw new IllegalArgumentException("Time cannot be more than " + MINUTES_IN_A_DAY);
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
