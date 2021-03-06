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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.Event;
import com.google.sps.data.User;
import com.google.sps.Trip;
import com.google.sps.servlets.CalendarServlet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CalendarServletTest {

  private CalendarServlet calendarServlet;

  // class constants
  private static final String INPUT_DESTINATION = 
      "4265 24th Street San Francisco, CA, 94114";
  private static final String INPUT_DESTINATION2 = "testDest";
  private static final String INPUT_DATE = "2020-07-15";
  private static final String TEST_NAME = "testName";
  private static final String EMPIRE_ADDRESS = "20 W 34th St, New York, NY 10001";
  private static final int HALF_HOUR = 30;
  private static final LocalDateTime DEF_START_TIME = 
      LocalDateTime.of(LocalDate.parse(INPUT_DATE), LocalTime.of(10, 0));
  private static final String EMAIL = "test123@gmail.com";

  // Constants to represent different Trip attributes.
  private static final String TRIP_NAME = "Trip to California";
  private static final String DESTINATION_NAME = "California";
  private static final String IMAGE_SRC =
    "https://lh3.googleusercontent.com/p/AF1QipM7tbCZOj_5SOft9cYgI7un3bmieieqvdYkCPT5=s1600-w400";
  private static final String TRIP_DAY_OF_TRAVEL = "2020-02-29";

  // event constants
  private static final String DOME = "Half Dome Visor";
  private static final String DOME_ADDRESS = "Half Dome Visor";
  private static final LocalDateTime DOME_START_TIME = 
      LocalDateTime.of(LocalDate.parse("2020-07-22"), LocalTime.of(11, 30));
  private static final String YOSEMITE = "Upper Yosemite Fall";
  private static final String YOSEMITE_ADDRESS = "Upper Yosemite Fall";
  private static final LocalDateTime YOS_START_TIME = 
      LocalDateTime.of(LocalDate.parse("2020-07-22"), LocalTime.of(10, 00));

  private static final String DEF_PLACE_ID = "1234";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initCalendarServlet() {
    calendarServlet = new CalendarServlet();
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
  public void testWriteEventsMulTripDays() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);    

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // initialize datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // create user entity
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);
    Key userEntityKey = userEntity.getKey();

    // Add a single Trip to Datastore with the User Entity Key.
    Entity tripEntity = new Entity(Trip.TRIP, userEntityKey);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_DAY_OF_TRAVEL);
    tripEntity.setProperty(Trip.END_DATE, TRIP_DAY_OF_TRAVEL);
    datastore.put(tripEntity);

    // create tripDay entities, needed for put events in datastore
    Entity tripDayEntity1 = new Entity(TripDay.QUERY_STRING, tripEntity.getKey());
    tripDayEntity1.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity1.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity1.setProperty("date", INPUT_DATE);
    datastore.put(tripDayEntity1);

    Entity tripDayEntity2 = new Entity(TripDay.QUERY_STRING, tripEntity.getKey());
    tripDayEntity2.setProperty("origin", INPUT_DESTINATION2);
    tripDayEntity2.setProperty("destination", INPUT_DESTINATION2);
    tripDayEntity2.setProperty("date", INPUT_DATE);
    datastore.put(tripDayEntity2);

    // create event entities and put it into datastore
    Event e1 = new Event(DOME, DOME_ADDRESS, DEF_PLACE_ID, DOME_START_TIME, 
                        HALF_HOUR);
    Entity event1 = e1.eventToEntity(tripDayEntity1.getKey());
    datastore.put(event1);

    Event e2 = new Event(YOSEMITE, YOSEMITE_ADDRESS, DEF_PLACE_ID, YOS_START_TIME, 
                        HALF_HOUR);
    Entity event2 = e2.eventToEntity(tripDayEntity2.getKey());
    datastore.put(event2);

    // run do Get
    calendarServlet.doGetEvents(response, datastore, userEntity, tripEntity.getKey());

    String expectedJson = "[{\"name\":\"Half Dome Visor\",\"address\":\"Half Dome Visor\"," + 
            "\"startTime\":{\"date\":{\"year\":2020,\"month\":7,\"day\":22}," + 
            "\"time\":{\"hour\":11,\"minute\":30,\"second\":0,\"nano\":0}}," + 
            "\"endTime\":{\"date\":{\"year\":2020,\"month\":7,\"day\":22}," +
            "\"time\":{\"hour\":12,\"minute\":30,\"second\":0,\"nano\":0}}," +
            "\"placeId\":\"1234\"," +
            "\"strStartTime\":\"2020-07-22T11:30:00\",\"strEndTime\":\"2020-07-22T12:30:00\"," +
            "\"travelTime\":30}," +
            "{\"name\":\"Upper Yosemite Fall\",\"address\":\"Upper Yosemite Fall\"," +
            "\"startTime\":{\"date\":{\"year\":2020,\"month\":7,\"day\":22}," + 
            "\"time\":{\"hour\":10,\"minute\":0,\"second\":0,\"nano\":0}}," + 
            "\"endTime\":{\"date\":{\"year\":2020,\"month\":7,\"day\":22}," +
            "\"time\":{\"hour\":11,\"minute\":0,\"second\":0,\"nano\":0}}," +
            "\"placeId\":\"1234\"," +
            "\"strStartTime\":\"2020-07-22T10:00:00\",\"strEndTime\":\"2020-07-22T11:00:00\"," +
            "\"travelTime\":30}]";
    
    writer.flush();
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

  @Test
  public void testNoCurrentUser() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);  

    when(request.getParameter("tripKey")).thenReturn("true");

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    calendarServlet.doGet(request, response);

    String expectedJson = "No current User";
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

  @Test
  public void testNoTripKeyPassedIn() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class); 

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // initialize datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);

    calendarServlet.doGet(request, response);

    String expectedJson = "No trip Key";
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }
}
