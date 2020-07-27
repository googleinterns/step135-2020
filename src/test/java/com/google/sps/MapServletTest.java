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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.Trip;
import com.google.sps.data.User;
import com.google.sps.servlets.MapServlet;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class MapServletTest {

  private MapServlet mapServlet;

  // User constants
  private static final String EMAIL = "test123@gmail.com";

  // Constants to represent different Trip attributes.
  private static final String TRIP_NAME = "Trip to California";
  private static final String INPUT_DESTINATION = 
      "4265 24th Street San Francisco, CA, 94114";
  private static final String IMAGE_SRC =
    "https://lh3.googleusercontent.com/p/AF1QipM7tbCZOj_5SOft9cYgI7un3bmieieqvdYkCPT5=s1600-w400";
  private static final String TRIP_DAY_OF_TRAVEL = "2020-02-29";

  // location constants
  private static final String DOME_ADDRESS = "Half Dome Visor";
  private static final String YOSEMITE_ADDRESS = "Upper Yosemite Fall";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initMapServlet() {
    mapServlet = new MapServlet();
  }

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /* 
   * Tests that doGet constructs the expected JSON string containing locations in order
   */
  @Test
  public void testWriteLocations() throws Exception {
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
    tripEntity.setProperty(Trip.DESTINATION_NAME, INPUT_DESTINATION);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_DAY_OF_TRAVEL);
    tripEntity.setProperty(Trip.END_DATE, TRIP_DAY_OF_TRAVEL);
    datastore.put(tripEntity);

    // create tripDay entity
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING, tripEntity.getKey());
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", TRIP_DAY_OF_TRAVEL);
    datastore.put(tripDayEntity);

    // create location entities and put it into datastore
    Entity DomeEntity = new Entity(TripDay.LOCATION_ENTITY_TYPE, tripDayEntity.getKey());
    DomeEntity.setProperty(TripDay.NAME, DOME_ADDRESS);
    DomeEntity.setProperty(TripDay.ORDER, 1);
    datastore.put(DomeEntity);

    Entity YosemiteEntity = new Entity(TripDay.LOCATION_ENTITY_TYPE, tripDayEntity.getKey());
    YosemiteEntity.setProperty(TripDay.NAME, YOSEMITE_ADDRESS);
    YosemiteEntity.setProperty(TripDay.ORDER, 0);
    datastore.put(YosemiteEntity);

    // run do Get
    mapServlet.doGetMap(response, datastore, userEntity, tripEntity.getKey());

    // even though DomeEntity is added first, its order property is 1 so it should appear 
    // after YosemiteEntity in the JSON.
    String expectedJson = "[\"" + INPUT_DESTINATION + "\",\""+YOSEMITE_ADDRESS+"\",\""+DOME_ADDRESS+"\"]";
    
    writer.flush();
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

  /* 
   * Tests doGet when there is no current user
   */
  @Test
  public void testNoCurrentUser() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);  

    when(request.getParameter("tripKey")).thenReturn("true");

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    mapServlet.doGet(request, response);

    String expectedJson = "No current User";
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

  /* 
   * Tests doGet when there is no trip key
   */
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

    // create user entity
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);

    mapServlet.doGet(request, response);

    String expectedJson = "No trip Key";
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }
}
