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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;

/**
 * Class that creates an event specific to a POI 
 */
public class Event {
 
  // inputs
  private String name;
  private String address;
  // format: HHMM
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  //in mins
  private long travelTime;
 
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
   * @param startTime start of activity (YYYY-MM-DD'T'HH:MM:SS)
   * @param travelTime time spent traveling to next location (minutes). 
   *        Null if last location of the day.
   * @param timeAtLocation time spent at POI (minutes)
   */
  public Event(String name, String address, LocalDateTime startTime, 
              int travelTime, int timeAtLocation) {
    this.name = name;
    this.address = address;
    this.startTime = startTime;
    this.endTime = startTime.plusMinutes(Long.valueOf(timeAtLocation));
    this.travelTime = Long.valueOf(travelTime);
    checkTravelTime(this.travelTime);
  }

  /**
   * Constructor that takes in time spent at location
   * 
   * @param name name of the location (NOT address)
   * @param address exact address of the POI
   * @param startTime start of activity (YYYY-MM-DD'T'HH:MM:SS)
   * @param travelTime time spent traveling to next location (minutes). 
   *        Null if last location of the day.
   */
  public Event(String name, String address, LocalDateTime startTime, 
              int travelTime) {
    this(name, address, startTime, travelTime, HOUR);
  }

  /**
   * checks that user inputs travelTime is valid
   * 
   * @param time amount of time spent traveling (minutes)
   */
  private static void checkTravelTime(long time) {
    if (time < MIN_POSSIBLE_TIME) {
      throw new IllegalArgumentException("Time cannot be less than " + MIN_POSSIBLE_TIME);
    }

    if (time >= MINUTES_IN_A_DAY) {
      throw new IllegalArgumentException("Time cannot be more than or equal to" + MINUTES_IN_A_DAY);
    }
  }

  public static String getProperDateFormat(LocalDateTime ldt) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    String output = ldt.format(dtf);
    return output;
  }

  // getter functions
  public String getName() {
    return this.name;
  }

  public String getAddress() {
    return this.address;
  }

  public LocalDateTime getStartTime() {
    return this.startTime;
  } 

  public LocalDateTime getEndTime() {
    return this.endTime;
  }

  public long getTravelTime() {
    return this.travelTime;
  }
}
