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

import com.google.appengine.api.datastore.Entity;
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
  
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  // format (yyyy-MM-dd'T'HH:mm:ss)
  private String strStartTime;
  private String strEndTime;

  // in mins
  private long travelTime;
 
  // class constants
  private static final int HALFHOUR = 30;
  private static final int HOUR = 60;
  private static final int MINUTES_IN_A_DAY = 1440;
  private static final int MIN_POSSIBLE_TIME = 0;

  // event fields for entity
  private static final String NAME = "name";
  private static final String ADDRESS = "end-time";
  private static final String DATE = "date";
  private static final String START_TIME = "start-time";
  private static final String TRAVEL_TIME = "travel-time";

  /**
   * Constructor that takes in time spent at location
   * 
   * @param name name of the location (NOT address)
   * @param address exact address of the POI
   * @param startTime start of activity
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
    this.strStartTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startTime);
    this.strEndTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(endTime);
  }

  /**
   * Constructor that assume one hour default spent at location
   * 
   * @param name name of the location (NOT address)
   * @param address exact address of the POI
   * @param startTime start of activity
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
      throw new IllegalArgumentException("Time cannot be less than "
                                         + MIN_POSSIBLE_TIME);
    }

    if (time >= MINUTES_IN_A_DAY) {
      throw new IllegalArgumentException("Time cannot be more than or equal to"
                                         + MINUTES_IN_A_DAY);
    }
  }

  // build entity from event to be put in datastore off event attributes
  public Entity eventToEntity(String parentKeyID) {
    Entity eventEntity = new Entity("events", parentKeyID);
    eventEntity.setProperty(NAME, this.name);
    eventEntity.setProperty(ADDRESS, this.address);
    eventEntity.setProperty(START_TIME, 
                  DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startTime));
    eventEntity.setProperty(TRAVEL_TIME, Long.toString(this.travelTime));
    return eventEntity;
  } 

  // build event from entity
  public static Event eventFromEntity(Entity eventEntity) {
    String name = (String) entity.getProperty(NAME);
    String address = (String) entity.getProperty(ADDRESS);
    String startDateTimeStr = (String) entity.getProperty(START_TIME);
    String travelTime = (String) entity.getProperty(TRAVEL_TIME);
    Event event = new Event(name, address, LocalDateTime.parse(startDateTimeStr),
                          Integer.parseInt(travelTime));
    return event;
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
