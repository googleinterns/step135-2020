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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.Config;
import com.google.sps.data.User;
import com.google.sps.servlets.AuthServlet;
import com.google.sps.servlets.UserTripServlet;
import com.google.sps.data.Trip;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@PrepareForTest(UserServiceFactory.class)
public final class UserTripServletTest {

  // Add constants necessary to testing retrieval of the current User.
  private static final String EMAIL = "testemail@gmail.com";
  private static final String AUTH_DOMAIN = "gmail.com";
  private static final String LOGOUT_URL = "/_ah/logout?continue=%2F";
  private static final String LOGIN_URL = "/_ah/login?continue=%2F";

  // Constants to represent different Trip attributes.
  private static final String TRIP_NAME = "Trip to California";
  private static final String DESTINATION_NAME = "California";
  private static final String IMAGE_SRC =
    "https://lh3.googleusercontent.com/p/AF1QipM7tbCZOj_5SOft9cYgI7un3bmieieqvdYkCPT5=s1600-w400";
  private static final String TRIP_DAY_OF_TRAVEL = "2020-02-29";

  // Constants to represent different Trip attributes for a second Trip.
  private static final String TRIP_NAME_2 = "Family Vacation";
  private static final String DESTINATION_NAME_2 = "Island of Hawai'i";
  private static final String IMAGE_SRC_2 = "../images/placeholder_image.png";
  private static final String TRIP_DAY_OF_TRAVEL_2 = "2020-07-17";

  // Create UserTripServlet object.
  private UserTripServlet userTripServlet;

  // Add helper to allow datastore testing in local JUnit tests.
  // See https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting.
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initServlets() {
    userTripServlet = new UserTripServlet();
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
  public void testWriteTripsToFileResponseNoTrip() throws Exception {
    // Add the logged-in User to Datastore, and get the User Entity Key.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);
    Key userEntityKey = userEntity.getKey();

    // Mock request and response.  
    HttpServletRequest requestMock = mock(HttpServletRequest.class);    
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Create writers to pass into the mock response object.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(responseMock.getWriter()).thenReturn(writer);

    // Run the UserServlet writeTripsToFile(...) method with response mock.
    userTripServlet.writeTripsToFile(responseMock, userEntityKey); 

    // Create the expected JSON String outputted from the above function.
    // The tripKey variable was copied from the output.
    String expectedJson = "[]";

    writer.flush(); // Flush the writer.
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

  @Test
  public void testWriteTripsToFileResponseOneTrip() throws Exception {
    // Add the logged-in User to Datastore, and get the User Entity Key.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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

    // Mock request and response.  
    HttpServletRequest requestMock = mock(HttpServletRequest.class);    
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Create writers to pass into the mock response object.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(responseMock.getWriter()).thenReturn(writer);

    // Run the UserServlet writeTripsToFile(...) method with response mock.
    userTripServlet.writeTripsToFile(responseMock, userEntityKey); 

    // Create the expected JSON String outputted from the above function.
    // The tripKey variable was copied from the output.
    String expectedJson = "[{\"tripName\":\"Trip to California\",\"destinationName\"" +
    ":\"California\",\"tripKey\":\"agR0ZXN0chQLEgR1c2VyGAEMCxIEdHJpcBgCDA\"" + 
    ",\"imageSrc\":\"https://lh3.googleusercontent.com/p/AF1QipM7tbCZOj_5SOft9cYg" +
    "I7un3bmieieqvdYkCPT5\\u003ds1600-w400\",\"startDate\":{\"year\":2020,\"month\"" +
    ":2,\"day\":29},\"endDate\":{\"year\":2020,\"month\":2,\"day\":29},\"num" +
    "Days\":1}]";

    writer.flush(); // Flush the writer.
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

    @Test
  public void testDoGetResponseTwoTrips() throws Exception {
    // Add the logged-in User to Datastore, and get the User Entity Key.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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

    // Add a second Trip to Datastore with the User Entity Key.
    Entity tripEntity2 = new Entity(Trip.TRIP, userEntityKey);
    tripEntity2.setProperty(Trip.TRIP_NAME, TRIP_NAME_2);
    tripEntity2.setProperty(Trip.DESTINATION_NAME, DESTINATION_NAME_2);
    tripEntity2.setProperty(Trip.IMAGE_SRC, IMAGE_SRC_2);
    tripEntity2.setProperty(Trip.START_DATE, TRIP_DAY_OF_TRAVEL_2);
    tripEntity2.setProperty(Trip.END_DATE, TRIP_DAY_OF_TRAVEL_2);
    datastore.put(tripEntity2);

    // Mock request and response.  
    HttpServletRequest requestMock = mock(HttpServletRequest.class);    
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Create writers to pass into the mock response object.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(responseMock.getWriter()).thenReturn(writer);

    // Run the UserServlet writeTripsToFile(...) method with response mock.
    userTripServlet.writeTripsToFile(responseMock, userEntityKey); 

    // Create the expected JSON String outputted from the above function.
    // The tripKey variables were copied from the output.
    String expectedJson = "[{\"tripName\":\"Trip to California\",\"destinationName\"" +
    ":\"California\",\"tripKey\":\"agR0ZXN0chQLEgR1c2VyGAEMCxIEdHJpcBgCDA\"" + 
    ",\"imageSrc\":\"https://lh3.googleusercontent.com/p/AF1QipM7tbCZOj_5SOft9cYg" +
    "I7un3bmieieqvdYkCPT5\\u003ds1600-w400\",\"startDate\":{\"year\":2020,\"month\"" +
    ":2,\"day\":29},\"endDate\":{\"year\":2020,\"month\":2,\"day\":29},\"num" +
    "Days\":1},{\"tripName\":\"Family Vacation\",\"destinationName\":\"Island " +
    "of Hawai\\u0027i\",\"tripKey\":\"agR0ZXN0chQLEgR1c2VyGAEMCxIEdHJpcBgDDA\"," +
    "\"imageSrc\":\"../images/placeholder_image.png\",\"startDate\":{\"year\":2020" +
    ",\"month\":7,\"day\":17},\"endDate\":{\"year\":2020,\"month\":7,\"day\":17}" +
    ",\"numDays\":1}]";

    writer.flush(); // Flush the writer.
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }
}
