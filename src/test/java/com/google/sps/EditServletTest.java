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

import static org.mockito.Mockito.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.Trip;
import com.google.sps.data.Event;
import com.google.sps.data.User;
import com.google.sps.servlets.AuthServlet;
import com.google.sps.servlets.EditServlet;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthServlet.class, DatastoreServiceFactory.class})
public final class EditServletTest {

  private EditServlet editServlet;

  // Add constants for testing User class.
  public static final String EMAIL = "testemail@gmail.com";

  // Constants to represent different TripDay attributes.
  private static final String INPUT_DESTINATION = 
    "Space Needle, Broad Street, Seattle, WA, USA";
  private static final String INPUT_DESTINATION_2 = 
    "Woodland Park Zoo, Phinney Avenue North, Seattle, WA, USA";
  private static final String INPUT_DATE = "2020-08-22";
  private static final String INPUT_DATE_2 = "2020-08-23";

  // Constants to represent different Trip attributes.
  private static final String TRIP_NAME = "Trip to California";
  private static final String DESTINATION_NAME = "California";
  private static final String IMAGE_SRC =
    "https://lh3.googleusercontent.com/p/AF1QipM7tbCZOj_5SOft9cYgI7un3bmieieqvdYkCPT5=s1600-w400";
  private static final String TRIP_START_DATE = "2020-08-22";
  private static final String TRIP_END_DATE = "2020-08-23";

  // Constants to represent the different Event Attributes.
  private static final String SPACE_NEEDLE = "Space Needle";
  private static final String SPACE_NEEDLE_ADDRESS = 
    "Space Needle, Broad Street, Seattle, WA, USA";
  private static final LocalDateTime SPACE_NEEDLE_START_TIME = 
      LocalDateTime.of(LocalDate.parse("2020-08-22"), LocalTime.of(11, 30));
  private static final String WOODLAND = "Woodland Park Zoo";
  private static final String WOODLAND_ADDRESS = 
    "Woodland Park Zoo, Phinney Avenue North, Seattle, WA, USA";
  private static final LocalDateTime WOODLAND_START_TIME = 
      LocalDateTime.of(LocalDate.parse("2020-08-22"), LocalTime.of(10, 00));
  private static final int HALF_HOUR = 30;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initEditServlet() {
    editServlet = new EditServlet();
  }

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void initTest() {
    Assert.assertTrue(true);
  }

  @Test
  public void testGetTripFromTripKey() {
    // Add a User to the database.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);

    // Add a Trip to Datastore with the User Entity Key.
    Entity tripEntity = new Entity(Trip.TRIP, userEntity.getKey());
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_START_DATE);
    tripEntity.setProperty(Trip.END_DATE, TRIP_END_DATE);
    datastore.put(tripEntity);

    // Run getTripFromTripKey(...), with the Trip and User present in datastore.
    Entity tripEntityReturn = 
      editServlet.getTripFromTripKey(userEntity, tripEntity.getKey(), datastore);

    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(tripEntity, tripEntityReturn);
  }

  @Test
  public void testGetTripFromTripKeyNotUnderUserNull() {
    // Add a User to the database.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);

    // Add a Trip to Datastore without the User Entity Key.
    Entity tripEntity = new Entity(Trip.TRIP);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_START_DATE);
    tripEntity.setProperty(Trip.END_DATE, TRIP_END_DATE);
    datastore.put(tripEntity);

    /**
     * Run getTripFromTripKey(...), with the Trip and User present in datastore,
     * but the Trip not under the User.
     */
    Entity tripEntityReturn = 
      editServlet.getTripFromTripKey(userEntity, tripEntity.getKey(), datastore);

    // Confirm that the Entity return is null.
    Assert.assertNull(tripEntityReturn);
  }

  @Test
  public void testGetTripFromTripKeyInvalidTripKeyNull() {
    // Add a User to the database.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);

    // Add a Trip to Datastore with the User Entity Key.
    Entity tripEntity = new Entity(Trip.TRIP, userEntity.getKey());
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_START_DATE);
    tripEntity.setProperty(Trip.END_DATE, TRIP_END_DATE);
    datastore.put(tripEntity);

    /**
     * Run getTripFromTripKey(...), with the Trip and User present in datastore,
     * but an invalid Trip Key.
     */
    Entity tripEntityReturn = editServlet.getTripFromTripKey(userEntity, 
      KeyFactory.createKey("INVALID_KIND", "INVALID_NAME"), datastore);

    // Confirm that the Entity return is null.
    Assert.assertNull(tripEntityReturn);
  }

  @Test
  public void testGetTripDaysFromTripMultipleBackwardOrder() throws Exception {
    // Add a single Trip to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripEntity = new Entity(Trip.TRIP);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_START_DATE);
    tripEntity.setProperty(Trip.END_DATE, TRIP_END_DATE);
    datastore.put(tripEntity);

    // Add a two TripDays to Datastore, with the later date TripDay first.
    Entity tripDayEntity1 = new Entity(TripDay.QUERY_STRING, tripEntity.getKey());
    tripDayEntity1.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity1.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity1.setProperty("date", INPUT_DATE_2);
    datastore.put(tripDayEntity1);

    Entity tripDayEntity2 = new Entity(TripDay.QUERY_STRING, tripEntity.getKey());
    tripDayEntity2.setProperty("origin", INPUT_DESTINATION_2);
    tripDayEntity2.setProperty("destination", INPUT_DESTINATION_2);
    tripDayEntity2.setProperty("date", INPUT_DATE);
    datastore.put(tripDayEntity2);

    /**
     * Run getTripDaysFromTrip(...), with the Trip and TripDay present in 
     * datastore. Test that the method returns the TripDays in the right order.
     */
    List<Entity> tripDayEntityReturn = 
      editServlet.getTripDaysFromTrip(tripEntity.getKey(), datastore);

    // Confirm that the TripDay Entity List is returned correctly, in order by date.
    List<Entity> tripDayEntityList = new ArrayList<>();
    tripDayEntityList.add(tripDayEntity2);
    tripDayEntityList.add(tripDayEntity1);
    Assert.assertEquals(tripDayEntityList, tripDayEntityReturn);
  }

  @Test
  public void testGetTripDaysFromTripSingle() throws Exception {
    // Add a single Trip to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripEntity = new Entity(Trip.TRIP);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_START_DATE);
    tripEntity.setProperty(Trip.END_DATE, TRIP_END_DATE);
    datastore.put(tripEntity);

    // Add a one TripDay to Datastore.
    Entity tripDayEntity1 = new Entity(TripDay.QUERY_STRING, tripEntity.getKey());
    tripDayEntity1.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity1.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity1.setProperty("date", INPUT_DATE_2);
    datastore.put(tripDayEntity1);

    /**
     * Run getTripDaysFromTrip(...), with the Trip and TripDay present in 
     * datastore.
     */
    List<Entity> tripDayEntityReturn = 
      editServlet.getTripDaysFromTrip(tripEntity.getKey(), datastore);

    // Confirm that the TripDay Entity List is returned correctly.
    List<Entity> tripDayEntityList = new ArrayList<>();
    tripDayEntityList.add(tripDayEntity1);
    Assert.assertEquals(tripDayEntityList, tripDayEntityReturn);
  }

  @Test
  public void testGetTripDaysFromTripNonePresent() throws Exception {
    // Add a single Trip to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripEntity = new Entity(Trip.TRIP);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_START_DATE);
    tripEntity.setProperty(Trip.END_DATE, TRIP_END_DATE);
    datastore.put(tripEntity);

    /**
     * Run getTripDaysFromTrip(...), with the Trip and TripDay present in 
     * datastore.
     */
    List<Entity> tripDayEntityReturn = 
      editServlet.getTripDaysFromTrip(tripEntity.getKey(), datastore);

    // Confirm that the TripDay Entity List is returned as an empty list.
    List<Entity> tripDayEntityList = new ArrayList<>();
    Assert.assertEquals(tripDayEntityList, tripDayEntityReturn);
  }

  @Test
  public void testGetTripDaysFromTripNotUnderTrip() throws Exception {
    // Add a single Trip to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripEntity = new Entity(Trip.TRIP);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_START_DATE);
    tripEntity.setProperty(Trip.END_DATE, TRIP_END_DATE);
    datastore.put(tripEntity);

    // Add a one TripDay to Datastore without the Trip Entity Key.
    Entity tripDayEntity1 = new Entity(TripDay.QUERY_STRING);
    tripDayEntity1.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity1.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity1.setProperty("date", INPUT_DATE_2);
    datastore.put(tripDayEntity1);

    /**
     * Run getTripDaysFromTrip(...), with the Trip and TripDay present in 
     * datastore.
     */
    List<Entity> tripDayEntityReturn = 
      editServlet.getTripDaysFromTrip(tripEntity.getKey(), datastore);

    // Confirm that the TripDay Entity List is returned correctly.
    List<Entity> tripDayEntityList = new ArrayList<>();
    Assert.assertEquals(tripDayEntityList, tripDayEntityReturn);
  }

    @Test
  public void testGetTripDaysFromTripInvalidKey() throws Exception {
    // Add a single Trip to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripEntity = new Entity(Trip.TRIP);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_START_DATE);
    tripEntity.setProperty(Trip.END_DATE, TRIP_END_DATE);
    datastore.put(tripEntity);

    // Add a one TripDay to Datastore without the Trip Entity Key.
    Entity tripDayEntity1 = new Entity(TripDay.QUERY_STRING, tripEntity.getKey());
    tripDayEntity1.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity1.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity1.setProperty("date", INPUT_DATE_2);
    datastore.put(tripDayEntity1);

    /**
     * Run getTripDaysFromTrip(...), with the Trip and TripDay present in 
     * datastore.
     */
    List<Entity> tripDayEntityReturn = editServlet.getTripDaysFromTrip(
      KeyFactory.createKey("INVALID_KIND", "INVALID_NAME"), datastore);

    // Confirm that the TripDay Entity List is returned correctly.
    List<Entity> tripDayEntityList = new ArrayList<>();
    Assert.assertEquals(tripDayEntityList, tripDayEntityReturn);
  }

  @Test
  public void testGetEventsFromTripDayMultipleBackwardOrder() throws Exception {
    // Add a TripDay to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING);
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", INPUT_DATE);
    datastore.put(tripDayEntity);

    // Add two Events to Datastore, in backwards order.
    Event e1 = new Event(SPACE_NEEDLE, SPACE_NEEDLE_ADDRESS, SPACE_NEEDLE_START_TIME, HALF_HOUR);
    Entity event1 = e1.eventToEntity(tripDayEntity.getKey());
    datastore.put(event1);

    Event e2 = new Event(WOODLAND, WOODLAND_ADDRESS, WOODLAND_START_TIME, HALF_HOUR);
    Entity event2 = e2.eventToEntity(tripDayEntity.getKey());
    datastore.put(event2);

    /**
     * Run getEventsFromTripDay(...), with the TripDay and Events present in 
     * datastore. Test that the method returns the Events in the right order.
     */
    List<Event> eventEntityReturn = 
      editServlet.getEventsFromTripDay(tripDayEntity.getKey(), datastore);

    // Confirm that the Event Entity List is returned correctly, in order by date.
    Assert.assertEquals(2, eventEntityReturn.size());
    Assert.assertEquals(WOODLAND, eventEntityReturn.get(0).getName());
    Assert.assertEquals(WOODLAND_ADDRESS, eventEntityReturn.get(0).getAddress());
    Assert.assertEquals(WOODLAND_START_TIME, eventEntityReturn.get(0).getStartTime());
    Assert.assertEquals(HALF_HOUR, eventEntityReturn.get(0).getTravelTime());
    Assert.assertEquals(SPACE_NEEDLE, eventEntityReturn.get(1).getName());
    Assert.assertEquals(SPACE_NEEDLE_ADDRESS, eventEntityReturn.get(1).getAddress());
    Assert.assertEquals(SPACE_NEEDLE_START_TIME, eventEntityReturn.get(1).getStartTime());
    Assert.assertEquals(HALF_HOUR, eventEntityReturn.get(1).getTravelTime());
  }

  @Test
  public void testGetEventsFromTripDaySingle() throws Exception {
    // Add a TripDay to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING);
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", INPUT_DATE);
    datastore.put(tripDayEntity);

    // Add one Event to Datastore.
    Event e1 = new Event(SPACE_NEEDLE, SPACE_NEEDLE_ADDRESS, SPACE_NEEDLE_START_TIME, HALF_HOUR);
    Entity event1 = e1.eventToEntity(tripDayEntity.getKey());
    datastore.put(event1);

    /**
     * Run getEventsFromTripDay(...), with the TripDay and Events present in 
     * datastore.
     */
    List<Event> eventEntityReturn = 
      editServlet.getEventsFromTripDay(tripDayEntity.getKey(), datastore);

    // Confirm that the Event Entity List is returned correctly.
    Assert.assertEquals(1, eventEntityReturn.size());
    Assert.assertEquals(SPACE_NEEDLE, eventEntityReturn.get(0).getName());
    Assert.assertEquals(SPACE_NEEDLE_ADDRESS, eventEntityReturn.get(0).getAddress());
    Assert.assertEquals(SPACE_NEEDLE_START_TIME, eventEntityReturn.get(0).getStartTime());
    Assert.assertEquals(HALF_HOUR, eventEntityReturn.get(0).getTravelTime());
  }

  @Test
  public void testGetEventsFromTripDayNonePresent() throws Exception {
    // Add a TripDay to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING);
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", INPUT_DATE);
    datastore.put(tripDayEntity);

    /**
     * Run getEventsFromTripDay(...), with the TripDay and Events present in 
     * datastore.
     */
    List<Event> eventEntityReturn = 
      editServlet.getEventsFromTripDay(tripDayEntity.getKey(), datastore);

    // Confirm that the Event Entity List is returned correctly, in order by date.
    List<Event> eventEntityList = new ArrayList<>();
    Assert.assertEquals(eventEntityList, eventEntityReturn);
  }

  @Test
  public void testGetEventsFromTripDayInvalidKey() throws Exception {
    // Add a TripDay to Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING);
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", INPUT_DATE);
    datastore.put(tripDayEntity);

    // Add one Event to Datastore.
    Event e1 = new Event(SPACE_NEEDLE, SPACE_NEEDLE_ADDRESS, SPACE_NEEDLE_START_TIME, HALF_HOUR);
    Entity event1 = e1.eventToEntity(tripDayEntity.getKey());
    datastore.put(event1);

    /**
     * Run getEventsFromTripDay(...), with the TripDay and Events present in 
     * datastore.
     */
    List<Event> eventEntityReturn = editServlet.getEventsFromTripDay(
        KeyFactory.createKey("INVALID_KIND", "INVALID_NAME"), datastore);

    // Confirm that the Event Entity List is returned correctly.
    List<Event> eventEntityList = new ArrayList<>();
    Assert.assertEquals(0, eventEntityReturn.size());
  }

  // TODO (chris): Add integration tests to confirm the doGet(...) function.
}
