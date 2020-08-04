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
import com.google.sps.data.User;
import com.google.sps.servlets.AuthServlet;
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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthServlet.class, DatastoreServiceFactory.class})
public final class MapServletTest {

  private MapServlet mapServlet;

  // User constants
  private static final String EMAIL = "test123@gmail.com";
  private static final String EMAIL2 = "test456@gmail.com";

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
   * Integration test: Tests that doGet writes the correct JSON string containing locations in order
   */
  //@Test
  public void testDoGet() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);    

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // Initialize and mock datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PowerMockito.mockStatic(DatastoreServiceFactory.class);
    when(DatastoreServiceFactory.getDatastoreService()).thenReturn(datastore);

    // Create user entity.
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);
    Key userEntityKey = userEntity.getKey();

    // Mock authServlet such that user is currently logged in.
    PowerMockito.mockStatic(AuthServlet.class);
    PowerMockito.when(AuthServlet.getCurrentUserEntity()).thenReturn(userEntity);

    // Add a single Trip to Datastore with the User Entity Key.
    Entity tripEntity = new Entity(Trip.TRIP, userEntityKey);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, INPUT_DESTINATION);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_DAY_OF_TRAVEL);
    tripEntity.setProperty(Trip.END_DATE, TRIP_DAY_OF_TRAVEL);
    datastore.put(tripEntity);

    // Pass tripKey as query parameter 
    String tripKeyString = KeyFactory.keyToString(tripEntity.getKey());
    when(request.getParameter("tripKey")).thenReturn(tripKeyString);

    // create tripDay entity
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING, tripEntity.getKey());
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", TRIP_DAY_OF_TRAVEL);
    datastore.put(tripDayEntity);

    // create location entities and put it into datastore
    Entity domeEntity = new Entity(TripDay.LOCATION_ENTITY_TYPE, tripDayEntity.getKey());
    domeEntity.setProperty(TripDay.NAME, DOME_ADDRESS);
    domeEntity.setProperty(TripDay.ORDER, 1);
    datastore.put(domeEntity);

    Entity yosemiteEntity = new Entity(TripDay.LOCATION_ENTITY_TYPE, tripDayEntity.getKey());
    yosemiteEntity.setProperty(TripDay.NAME, YOSEMITE_ADDRESS);
    yosemiteEntity.setProperty(TripDay.ORDER, 0);
    datastore.put(yosemiteEntity);

    // run do Get
    mapServlet.doGet(request, response);

    // even though DomeEntity is added first, its order property is 1 so it should appear 
    // after YosemiteEntity in the JSON.
    String expectedJson = "[\"" + INPUT_DESTINATION + "\",\""+YOSEMITE_ADDRESS+"\",\""+DOME_ADDRESS+"\"]";
    
    writer.flush();
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

  /* 
   * Tests that doGetMap constructs the expected JSON string containing locations in order
   */
  //@Test
  public void testDoGetMap() throws Exception {
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
    Entity domeEntity = new Entity(TripDay.LOCATION_ENTITY_TYPE, tripDayEntity.getKey());
    domeEntity.setProperty(TripDay.NAME, DOME_ADDRESS);
    domeEntity.setProperty(TripDay.ORDER, 1);
    datastore.put(domeEntity);

    Entity yosemiteEntity = new Entity(TripDay.LOCATION_ENTITY_TYPE, tripDayEntity.getKey());
    yosemiteEntity.setProperty(TripDay.NAME, YOSEMITE_ADDRESS);
    yosemiteEntity.setProperty(TripDay.ORDER, 0);
    datastore.put(yosemiteEntity);

    // run do Get
    String result = mapServlet.doGetMap(response, datastore, userEntity, tripEntity.getKey(), "");

    // even though DomeEntity is added first, its order property is 1 so it should appear 
    // after YosemiteEntity in the JSON.
    String expectedJson = "[\"" + INPUT_DESTINATION + "\",\""+YOSEMITE_ADDRESS+"\",\""+DOME_ADDRESS+"\"]";
    
    Assert.assertEquals(expectedJson, result);
  }

  /* 
   * Tests doGet when there is no current user
   */
  @Test
  public void testNoCurrentUser() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);  

    // Mock such that the servlet thinks the tripKey exists
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
    when(request.getParameter("tripKey")).thenReturn(null);

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // initialize datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    mapServlet.doGet(request, response);

    String expectedJson = "No trip Key";
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

  /* 
   * Tests doGet when the trip key is for a different user's trip
   */
  @Test
  public void testWrongTripKey() throws Exception {
    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);

    // initialize datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // create user entities
    Entity userEntity1 = new Entity(User.USER);
    userEntity1.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity1);
    Key userEntityKey1 = userEntity1.getKey();

    Entity userEntity2 = new Entity(User.USER);
    userEntity2.setProperty(User.USER_EMAIL, EMAIL2);
    datastore.put(userEntity2);

    // Add a single Trip to Datastore with the user1's Entity Key.
    Entity tripEntity = new Entity(Trip.TRIP, userEntityKey1);
    tripEntity.setProperty(Trip.TRIP_NAME, TRIP_NAME);
    tripEntity.setProperty(Trip.DESTINATION_NAME, INPUT_DESTINATION);
    tripEntity.setProperty(Trip.IMAGE_SRC, IMAGE_SRC);
    tripEntity.setProperty(Trip.START_DATE, TRIP_DAY_OF_TRAVEL);
    tripEntity.setProperty(Trip.END_DATE, TRIP_DAY_OF_TRAVEL);
    datastore.put(tripEntity);

    // Mock request such that tripKey provided is user1's trip
    // Mock response to return mock writer
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class); 
    String tripKeyString = KeyFactory.keyToString(tripEntity.getKey());
    when(request.getParameter("tripKey")).thenReturn(tripKeyString);
    when(response.getWriter()).thenReturn(writer);

    // Mock authServlet such that user2 is currently logged in
    PowerMockito.mockStatic(AuthServlet.class);
    PowerMockito.when(AuthServlet.getCurrentUserEntity()).thenReturn(userEntity2);

    mapServlet.doGet(request, response);

    String expectedJson = "No trip found";
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }
}
