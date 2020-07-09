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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

/**
 * Trip is the class for storing a single trip (could be multiple days).
 */
public class Trip {
  // Trip cannot be longer than a month (31 days)
  private static final int MAX_NUM_DAYS = 31;

  private String tripName;
  private String tripId;
  private LocalDate startDate;
  private LocalDate endDate;
  private int numDays;
  private List<TripDay> tripDays;

  /**
   * Creates a new Trip.
   *
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param tripId The 16-digit alphanumeric String for the trip. Must be non-null.
   * @param startDate The start date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   * @param endDate The end date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   * @param tripDays The list of tripDays. Must be non-null.
   */
  public Trip(String tripName, String tripId, String startDate, String endDate, 
    List<TripDay> tripDays) {

    if (tripName == null) {
      throw new IllegalArgumentException("tripName cannot be null");
    }

    if (tripId == null) {
      throw new IllegalArgumentException("tripId cannot be null");
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

    // Set field attributes.
    this.tripName = tripName;
    this.tripId = tripId;
    this.startDate = getLocalDate(startDate);
    this.endDate = getLocalDate(endDate);
    
    // Duplicate tripDays to avoid modifying original parameter
    this.tripDays = new ArrayList<>();
    this.tripDays.addAll(tripDays);

    int numDays = calcNumDays(this.startDate, this.endDate);
    if (numDays <= 0 || numDays > MAX_NUM_DAYS) {
      throw new IllegalArgumentException("numDays must be an integer between 1 and 31, inclusive.");
    }
    this.numDays = numDays;
  }

    /**
   * Creates a new Trip.
   *
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param startDate The start date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   * @param endDate The end date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   * @param tripDays The list of tripDays. Must be non-null.
   */
  public Trip(String tripName, String startDate, String endDate, List<TripDay> tripDays) {
    this(tripName, createTripId(), startDate, endDate, tripDays);
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
    this(tripName, createTripId(), startDate, startDate, tripDays);
  }

  // Generates a random 16-digit alphanumeric.
  private String createTripId() {
    RandomStringGenerator generator = new RandomStringGenerator.Builder()
      .withinRange('0', 'z')
      .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
      .build();

    return generator.generate(16);
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
   * Returns a List<TripDay> copy of TripDays for this trip.
   */
  public List<TripDay> getTripDays() {
    List<TripDay> tripDaysCopy = new ArrayList<>();
    tripDaysCopy.addAll(this.tripDays);
    return tripDaysCopy;
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
