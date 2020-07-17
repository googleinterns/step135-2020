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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.Trip;
import com.google.sps.TripDay;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TripTest {

  // Constants for creating Key of Trip Entity ancestor.
  private static final String PARENT_KEY_KIND = "trip_entity_parent_kind";
  private static final String PARENT_KEY_NAME = "trip_entity_parent_name";

  // Cosntants to represent different Trip attributes.
  private static final String TRIP_NAME = "Trip to California";
  private static final String DESTINATION_NAME = "California";
  private static final String TRIP_KEY = 
    "aglub19hcHBfaWRyIgsSBHVzZXIYgICAgICAsAkMCxIEdHJpcBiAgICAgIDwCAw";
  private static final String IMAGE_SRC =
    "https://lh3.googleusercontent.com/p/AF1QipM7tbCZOj_5SOft9cYgI7un3bmieieqvdYkCPT5=s1600-w400";
  private static final String START_DATE_STRING = "2020-02-29";
  private static final String END_DATE_STRING = "2020-03-05";
  private static final int NUM_DAYS = 6;
  private static final LocalDate START_DATE = LocalDate.parse(START_DATE_STRING);
  private static final LocalDate END_DATE = LocalDate.parse(END_DATE_STRING);

  // Add helper to allow datastore testing in local JUnit tests.
  // See https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting.
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
      helper.setUp();
  }

  @After
  public void tearDown() {
      helper.tearDown();
  }

  // Test the trip constructor which allows for multiple days
  @Test
  public void testTripConstructorMultiDay() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      START_DATE_STRING, END_DATE_STRING);

    Assert.assertEquals(trip.getTripName(), TRIP_NAME);
    Assert.assertEquals(trip.getDestinationName(), DESTINATION_NAME);
    Assert.assertEquals(trip.getTripKey(), TRIP_KEY);
    Assert.assertEquals(trip.getImageSrc(), IMAGE_SRC);
    Assert.assertEquals(trip.getStartDate(), START_DATE);
    Assert.assertEquals(trip.getEndDate(), END_DATE);
    Assert.assertEquals(trip.getNumDays(), NUM_DAYS);
  }

  // Test the Trip constructor for a single day (MVP version)
  @Test
  public void testTripConstructorSingleDay() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      START_DATE_STRING);

    Assert.assertEquals(trip.getTripName(), TRIP_NAME);
    Assert.assertEquals(trip.getDestinationName(), DESTINATION_NAME);
    Assert.assertEquals(trip.getTripKey(), TRIP_KEY);
    Assert.assertEquals(trip.getImageSrc(), IMAGE_SRC);
    Assert.assertEquals(trip.getStartDate(), START_DATE);
    Assert.assertEquals(trip.getNumDays(), 1);
  }

  // Test the trip build entity
  @Test
  public void testTripBuildEntity() {
    Entity tripEntity = Trip.buildEntity(TRIP_NAME, DESTINATION_NAME, IMAGE_SRC, 
      START_DATE_STRING, END_DATE_STRING, KeyFactory.createKey(PARENT_KEY_KIND, PARENT_KEY_NAME));

    Assert.assertEquals(TRIP_NAME, tripEntity.getProperty(Trip.TRIP_NAME));
    Assert.assertEquals(DESTINATION_NAME, tripEntity.getProperty(Trip.DESTINATION_NAME));
    Assert.assertEquals(IMAGE_SRC, tripEntity.getProperty(Trip.IMAGE_SRC));
    Assert.assertEquals(START_DATE_STRING, tripEntity.getProperty(Trip.START_DATE));
    Assert.assertEquals(END_DATE_STRING, tripEntity.getProperty(Trip.END_DATE));
  }

  // Test the trip build trip from entity
  @Test
  public void testBuildTripFromEntity() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripEntity = Trip.buildEntity(TRIP_NAME, DESTINATION_NAME, IMAGE_SRC, 
      START_DATE_STRING, END_DATE_STRING, KeyFactory.createKey(PARENT_KEY_KIND, 
      PARENT_KEY_NAME));
    datastore.put(tripEntity);
    Trip trip = Trip.buildTripFromEntity(tripEntity);

    Assert.assertEquals(trip.getTripName(), TRIP_NAME);
    Assert.assertEquals(trip.getDestinationName(), DESTINATION_NAME);
    Assert.assertEquals(trip.getImageSrc(), IMAGE_SRC);
    Assert.assertEquals(trip.getStartDate(), START_DATE);
    Assert.assertEquals(trip.getEndDate(), END_DATE);
  }

  // Test multiple day constructor with null trip name
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayNullTripName() {
    Trip trip = new Trip(null, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      START_DATE_STRING, END_DATE_STRING);
  }

  // Test multiple day constructor with null destination name
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayNullDestinationName() {
    Trip trip = new Trip(TRIP_NAME, null, TRIP_KEY, IMAGE_SRC, 
      START_DATE_STRING, END_DATE_STRING);
  }

  // Test multiple day constructor with null trip key
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayNullTripKey() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, null, IMAGE_SRC, 
      START_DATE_STRING, END_DATE_STRING);
  }

  // Test multiple day constructor with null image source
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayNullImageSrc() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, null, 
      START_DATE_STRING, END_DATE_STRING);
  }

  // Test multiple day constructor with null start date
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayNullStartDate() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      null, END_DATE_STRING);
  }

  // Test multiple day constructor with null end date
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayNullEndDate() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      START_DATE_STRING, null);
  }

  // Test multiple day constructor with wrong start date format
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayInvalidStartDate() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      "06-28-2020", END_DATE_STRING);
  }

  // Test multiple day constructor with wrong end date format
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayInvalidEndDate() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      START_DATE_STRING, "06/28/2020");
  }

  // Test multiple day constructor with over a month
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayLongTrip() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      "2020-07-04", "2020-08-09");
  }

  // Test multiple day constructor with end date before start date
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayNegativeNumDays() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      "2020-07-04", "2020-06-09");
  }

  // Test multiple day constructor with 0 numDays
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorMultiDayZeroNumDays() {
    // A trip with the same start and end date is considered to have 1 day,
    // so a zero day trip would have the start date one day after the end date
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      "2020-07-04", "2020-07-03");
  }

  // Test single day constructor with null trip name
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorSingleDayNullTripName() {
    Trip trip = new Trip(null, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      START_DATE_STRING);
  }

  // Test single day constructor with null start date
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorSingleDayNullStartDate() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      null);
  }

  // Test single day constructor with invalid date format 
  @Test(expected = IllegalArgumentException.class)
  public void testTripConstructorSingleDayInvalidStart() {
    Trip trip = new Trip(TRIP_NAME, DESTINATION_NAME, TRIP_KEY, IMAGE_SRC, 
      "Dec 9 2020");
  }
}
