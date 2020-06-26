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

  // fiels to be saved as json for parsing
  private String strStartTime;
  private String strEndTime;
 
  private final static int HALFHOUR_TILL_NEXT_POI = 30;
  private final static int HOUR_AT_CURRENT_POI = 60;
  private final static int MINUTES_IN_A_DAY = 1440;
  private final static int MIN_POSSIBLE_TIME = 1;

  
  // Constructor that takes in time spent at location
  public Event(String name, String address, String date, int startTime, int timeAtLocation) {
    this.name = name;
    this.address = address;
    this.date = date;
    this.startTime = startTime;
    this.endTime = calculateEndTime(timeAtLocation);

    createCalendarTimes();
  }
 
  // Constructor that takes in endTime
  public Event(String name, String address, String date, int startTime) {
    this.name = name;
    this.address = address;
    this.date = date;
    this.startTime = startTime;
    this.endTime = calculateEndTime(HOUR_AT_CURRENT_POI);

    createCalendarTimes();
  }
 
  // function that calculates endTime given start and timeSpen
  public int calculateEndTime(int timeAtLocation) {
    return this.startTime + convertToFormat(timeAtLocation);
  }

  // function that sets start and end Time with correct string format
  private void createCalendarTimes() {
    this.strStartTime = createStrTime(this.date, this.startTime);
    this.strEndTime = createStrTime(this.date, this.endTime);
  }

  // function that builds string w correct format for calendar-script.js
  private static String createStrTime(String date, int time) {
    String output = "";
    output += date + "T" + Integer.toString(time).substring(0, 2) + ":" + 
      Integer.toString(time).substring(2, 4) + ":00";
    return output;
  }

  // function that converts mintues into hhmm format
  private static int convertToFormat(int timeAtLocation) {
    if (timeAtLocation < MIN_POSSIBLE_TIME) {
      throw new IllegalArgumentException("Time cannot be less than 0");
    }

    if (timeAtLocation >= MINUTES_IN_A_DAY) {
      throw new IllegalArgumentException("Time cannot be more than a 24hrs");
    }

    return Integer.parseInt("" + 
      (timeAtLocation / 60) + (timeAtLocation % 60));
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

  public String getStrStartTime() {
    return this.strStartTime;
  } 

  public String getStrEndTime() {
    return this.strEndTime;
  }
}
