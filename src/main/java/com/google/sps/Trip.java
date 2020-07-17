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

package com.google.sps;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Trip is the class for storing a single trip (could be multiple days).
 */
public class Trip {

  // Trip cannot be longer than a month (31 days)
  private static final int MAX_NUM_DAYS = 31;

  // Field attributes for the Trip class.
  private String tripName;
  private String destinationName;
  private String tripKey;
  private String imageSrc;
  private LocalDate startDate;
  private LocalDate endDate;
  private int numDays;

  // Constants to get and put the Entity objects in Datastore.
  private static final String TRIP = "trip";
  private static final String TRIP_NAME = "trip_name";
  private static final String DESTINATION_NAME = "destination_name";
  private static final String TRIP_KEY = "trip_key";
  private static final String IMAGE_SRC = "image_src";
  private static final String START_DATE = "start_date";
  private static final String END_DATE = "end_date";

  /**
   * Creates a new Trip.
   *
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param destinationName The name of the destination the user is heading to.
   * @param tripKey The key of the trip as created by the Trip Entity.
   * @param imageSrc The image source / URL to represent the trip.
   * @param startDate The start date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   * @param endDate The end date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   */
  public Trip(String tripName, String destinationName, String tripKey, 
    String imageSrc, String startDate, String endDate) {

    if (tripName == null) {
      throw new IllegalArgumentException("tripName cannot be null");
    }
    
    if (destinationName == null) {
      throw new IllegalArgumentException("destinationName cannot be null");
    }
    
    if (tripKey == null) {
      throw new IllegalArgumentException("tripKey cannot be null");
    }
    
    if (imageSrc == null) {
      throw new IllegalArgumentException("imageSrc cannot be null");
    }

    if (startDate == null) {
      throw new IllegalArgumentException("startDate cannot be null");
    }

    if (endDate == null) {
      throw new IllegalArgumentException("endDate cannot be null");
    }

    this.tripName = tripName;
    this.destinationName = destinationName;
    this.tripKey = tripKey;
    this.imageSrc = imageSrc;
    this.startDate = getLocalDate(startDate);
    this.endDate = getLocalDate(endDate);

    int numDays = calcNumDays(this.startDate, this.endDate);
    if (numDays <= 0 || numDays > MAX_NUM_DAYS) {
      throw new IllegalArgumentException("numDays must be an integer between 1 and 31, inclusive.");
    }
    this.numDays = numDays;
  }

  /**
   * Creates a new Trip that is only one day by default. 
   * This constructor will be used for the MVP.
   * 
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param destinationName The name of the destination the user is heading to.
   * @param tripKey The key of the trip as created by the Trip Entity.
   * @param imageSrc The image source / URL to represent the trip.
   * @param startDate The start date for the trip. Must be non-null.
   */
  public Trip(String tripName, String destinationName, String tripKey, 
    String imageSrc, String startDate) {

    this(tripName, destinationName, tripKey, imageSrc, startDate, startDate);
  }

  /**
   * Builds and return an Entity object of type "Trip".
   * The Trip Entity must have a User Entity as its parent (ancestor).
   */
  public static Entity buildEntity(String tripName, String destinationName, 
    String imageSrc, String startDate, String endDate, Key userEntityParentKey) {

    Entity tripEntity = new Entity(TRIP, userEntityParentKey);
    tripEntity.setProperty(TRIP_NAME, tripName);
    tripEntity.setProperty(DESTINATION_NAME, destinationName);
    tripEntity.setProperty(IMAGE_SRC, imageSrc);
    tripEntity.setProperty(START_DATE, startDate);
    tripEntity.setProperty(END_DATE, endDate);
    return tripEntity;
  }

  /**
   * Returns the human-readable name for this trip.
   */
  public String getTripName() {
    return this.tripName;
  }

  /**
   * Returns the name of the destination the user is heading to.
   */
  public String getDestinationName() {
    return this.destinationName;
  }

  /**
   * Returns the key of the trip as created by the Trip Entity.
   */
  public String getTripKey() {
    return this.tripKey;
  }

  /**
   * Returns the image source / URL to represent the trip.
   */
  public String getImageSrc() {
    return this.imageSrc;
  }

  /**
   * Returns the start date for this trip.
   */
  public LocalDate getStartDate() {
    return this.startDate;
  }

  /**
   * Returns the end date for this trip.
   */
  public LocalDate getEndDate() {
    return this.endDate;
  }

  /**
   * Returns the number of days in this trip.
   */
  public int getNumDays() {
    return this.numDays;
  }

  /**
   * Returns LocalDate representation of a String if it is in the yyyy-MM-dd date format.
   * Otherwise, throws an IllegalArgumentException
   * @param inDate The String date representation.
   */
  private static LocalDate getLocalDate(String inDate) {
    try {
      return LocalDate.parse(inDate);
    } catch (DateTimeParseException pe) {
      throw new IllegalArgumentException("Invalid date format. Must be in yyyy-MM-dd date format.");
    }
  }

  /**
   * Calculates number of days between two dates.
   * @param startDate The LocalDate representation of start date.
   * @param endDate The LocalDate representation of end date.
   */
  private static int calcNumDays(LocalDate startDate, LocalDate endDate) {
    int numDays = ((int) ChronoUnit.DAYS.between(startDate, endDate)) + 1;
    return numDays;
  }
}
