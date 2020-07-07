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

import com.google.sps.Trip;
import com.google.sps.TripDay;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.ArrayList;
import java.util.Arrays;

@RunWith(JUnit4.class)
public final class TripTest {
  private static final String TRIP_NAME = "Trip to California";
  private static final String START_DATE = "2020-02-29";
  private static final String END_DATE = "2020-03-05";
  private static final int NUM_DAYS = 6;
  
  private ArrayList<TripDay> tripDays;

  private static final String TIMES_SQUARE_ID = "ChIJmQJIxlVYwokRLgeuocVOGVU";
  private static final String CENTRAL_PARK_ID = "ChIJ4zGFAZpYwokRGUGph3Mf37k";
  private static final String WORLD_TRADE_ID = "ChIJy7cGfBlawokR5l2e93hsoEA";
  private static final String EMPIRE_STATE_ID = "ChIJtcaxrqlZwokRfwmmibzPsTU";
  private static final String HOTEL_ID = "ChIJ68J3tfpYwokR2HaRoBcB4xg";

  private ArrayList<String> locations = new ArrayList<>(Arrays.asList(TIMES_SQUARE_ID, CENTRAL_PARK_ID,
                                                                    WORLD_TRADE_ID, EMPIRE_STATE_ID));

  // Test the trip constructor which allows for multiple days
  @Test
  public void testTripConstructorMultiDay() {
    TripDay tripDay1 = new TripDay(HOTEL_ID, HOTEL_ID, locations);
    TripDay tripDay2 = new TripDay(HOTEL_ID, HOTEL_ID, locations);
    
    tripDays = new ArrayList<>();
    tripDays.add(tripDay1);
    tripDays.add(tripDay2);

    Trip trip = new Trip(TRIP_NAME, START_DATE, END_DATE, tripDays);

    Assert.assertEquals(trip.getTripName(), TRIP_NAME);
    Assert.assertEquals(trip.getStartDate(), START_DATE);
    Assert.assertEquals(trip.getEndDate(), END_DATE);
    Assert.assertEquals(trip.getNumDays(), NUM_DAYS);
    Assert.assertEquals(trip.getTripDays(), tripDays);
  }

  // Test the Trip constructor for a single day (MVP version)
  @Test
  public void testTripConstructorSingleDay() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, locations);
    
    tripDays = new ArrayList<>();
    tripDays.add(tripDay);

    Trip trip = new Trip(TRIP_NAME, START_DATE, tripDays);

    Assert.assertEquals(trip.getTripName(), TRIP_NAME);
    Assert.assertEquals(trip.getStartDate(), START_DATE);
    Assert.assertEquals(trip.getEndDate(), START_DATE);
    Assert.assertEquals(trip.getNumDays(), 1);
    Assert.assertEquals(trip.getTripDays(), tripDays);
    
  }

  // Test multiple day constructor with null parameter
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayNull() {
    Trip trip = new Trip(TRIP_NAME, START_DATE, END_DATE, null);
  }

  // Test multiple day constructor with wrong date format
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayInvalidDate() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, locations);
    
    tripDays = new ArrayList<>();
    tripDays.add(tripDay);

    Trip trip = new Trip(TRIP_NAME, "06-28-2020", END_DATE, tripDays);
  }

  // Test multiple day constructor with over a month
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayLongTrip() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, locations);
    
    tripDays = new ArrayList<>();
    tripDays.add(tripDay);

    Trip trip = new Trip(TRIP_NAME, "2020-07-04", "2020-08-09", tripDays);
  }

  // Test single day constructor with null parameter
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorSingleDayNull() {
    Trip trip = new Trip(TRIP_NAME, START_DATE, null);
  }

  // Test single day constructor with invalid date format 
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorSingleDayInvalidStart() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, locations);
    
    tripDays = new ArrayList<>();
    tripDays.add(tripDay);

    Trip trip = new Trip(TRIP_NAME, "Dec 9 2020", tripDays);
  }

}
