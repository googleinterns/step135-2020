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

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Trip is the class for storing a single trip (could be multiple days).
 */
public class Trip {
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private String tripName;
  private String startDate;
  private String endDate;
  private int numDays;
  private List<TripDay> tripDays;

  /**
   * Creates a new Trip.
   *
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param startDate The start date for the trip. Must be non-null.
   * @param endDate The end date for the trip. Must be non-null.
   * @param numDays The number of days for the trip. 
   * @param tripDays The list of tripDays. Must be non-null.
   */
  public Trip(String tripName, String startDate, String endDate, List<TripDay> tripDays) {
    if (tripName == null) {
      throw new IllegalArgumentException("tripName cannot be null");
    }

    if (startDate == null) {
      throw new IllegalArgumentException("startDate cannot be null");
    }

    if (endDate == null) {
      throw new IllegalArgumentException("endDate cannot be null");
    }

    if (tripDays == null) {
      throw new IllegalArgumentException("tripDays cannot be null. Use empty array instead.");
    }

    if (numDays < 0 || numDays > 31) {
      throw new IllegalArgumentException("numDays must be an integer between 1 and 31.");
    }

    if (!isValidDate(startDate)) {
      throw new IllegalArgumentException("Invalid startDate format.");
    }

    if (!isValidDate(endDate)) {
      throw new IllegalArgumentException("Invalid endDate format.");
    }

    this.tripName = tripName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.numDays = calcNumDays(this.startDate, this.endDate);
    this.tripDays = new ArrayList<>();
    this.tripDays.addAll(tripDays);
  }

  /**
   * Creates a new Trip that is only one day by default. 
   * This constructor will be used for the MVP.
   * 
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param startDate The start date for the trip. Must be non-null.
   * @param tripDays The list of tripDays. Must be non-null.
   */
  public Trip(String tripName, String startDate, List<TripDay> tripDays) {
    if (tripName == null) {
      throw new IllegalArgumentException("tripName cannot be null");
    }

    if (startDate == null) {
      throw new IllegalArgumentException("startDate cannot be null");
    }

    if (tripDays == null) {
      throw new IllegalArgumentException("tripDays cannot be null. Use empty array instead.");
    }

    this.tripName = tripName;
    this.startDate = startDate;

    // For MVP: trips will only be one day
    this.endDate = this.startDate;
    this.numDays = 1;

    this.tripDays = new ArrayList<>();
    this.tripDays.addAll(tripDays);
  }

  /**
   * Returns the human-readable name for this trip.
   */
  public String getTripName() {
    return this.tripName;
  }

  /**
   * Returns the start date for this trip.
   */
  public String getStartDate() {
    return this.startDate;
  }

  /**
   * Returns the end date for this trip.
   */
  public String getEndDate() {
    return this.endDate;
  }

  /**
   * Returns the number of days in this trip.
   */
  public int getNumDays() {
    return this.numDays;
  }

  /**
   * Returns an List<TripDay> of TripDays for this trip.
   */
  public List<TripDay> getTripDays() {
    return this.tripDays;
  }

  private static boolean isValidDate(String inDate) {
    DATE_FORMAT.setLenient(false);
    try {
        DATE_FORMAT.parse(inDate.trim());
    } catch (ParseException pe) {
        return false;
    }
    return true;
  }

  private static int calcNumDays(String startDateString, String endDateString) {
    try {
	       Date startDate = myFormat.parse(startDateString);
	       Date endDate = myFormat.parse(endDateString);
	       long difference = endDate.getTime() - startDate.getTime();
         // Convert milliseconds to days
	       float numDays = (difference / (1000*60*60*24));
	       return numDays;
	 } catch (Exception e) {
	       throw new IllegalArgumentException("Invalid startDate or endDate.");
	 }
  }
}
