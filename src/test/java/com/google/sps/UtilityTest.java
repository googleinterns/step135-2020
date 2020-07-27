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
 
package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.Event;
import com.google.sps.data.User;
import com.google.sps.Trip;
import org.junit.Before;

public class UtilityTest {

  // Datastore field variable, used to place and retrieve items to / from Datastore.
  private static DatastoreService datastore;

  // Add helper to allow datastore testing in local JUnit tests.
  // See https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting.
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initDatastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Add the logged-in user to Datastore. Get property names from the User class
   * and test property values from field variables.
   *
   * @param email The email of the logged-in user.
   * @return The Key of the User Entity in Datastore.
   */ 
  public static Key addLoggedInUserToDatastore(String email) {
    // Add the logged-in User to Datastore, and get the User Entity Key.
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, email);
    datastore.put(userEntity);
    Key userEntityKey = userEntity.getKey();
    return userEntityKey;
  }

  /**
   * Add a Trip Entity to Datastore under the passed-in Key of the User Entity.
   *
   * @param userEntityKey The Key from the User Entity in Datastore. Must be 
   * non-null.
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param destinationName The name of the destination the user is heading to.
   * @param tripKey The key of the trip as created by the Trip Entity.
   * @param imageSrc The image source / URL to represent the trip.
   * @param startDate The start date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   * @param endDate The end date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   */
  public static Entity addTripEntityToDatastoreUnderUser(Key userEntityKey,
    String tripName, String destinationName, String imageSrc, String startDate, 
    String endDate) {

    // Add a single Trip to Datastore with the User Entity Key.
    Entity tripEntity = new Entity(Trip.TRIP, userEntityKey);
    tripEntity.setProperty(Trip.TRIP_NAME, tripName);
    tripEntity.setProperty(Trip.DESTINATION_NAME, destinationName);
    tripEntity.setProperty(Trip.IMAGE_SRC, imageSrc);
    tripEntity.setProperty(Trip.START_DATE, startDate);
    tripEntity.setProperty(Trip.END_DATE, endDate);
    datastore.put(tripEntity);
    return tripEntity;
  }

  /**
   * Create the mock of the UserService object of the logged-in user.
   */
  public static UserService createUserServiceMockLoggedIn(String email, 
    String logoutUrl, String authDomain) {

    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    // This is the User object from Google Appengine (full path given to avoid
    // confusion with local User.java file).
    when(userServiceMock.getCurrentUser()).thenReturn(
        new com.google.appengine.api.users.User(email, auth_domain));
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(logoutUrl);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);
    return userServiceMock;
  }

  /**
   * Create the mock of the UserService object of the logged-out user.
   */
  public static UserService createUserServiceMockLoggedOut(String loginUrl) {
    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(false);
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(loginUrl);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);
    return userServiceMock;
  }

  /**
   * Create the TripDay Entity and put it in Datastore.
   */
  public static TripDay putTripDayInDatastore(String inputDestination, 
    String inputDate) {
    // Create the TripDay Entity and put it in Datastore.
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING);
    tripDayEntity.setProperty("origin", inputDestination);
    tripDayEntity.setProperty("destination", inputDestination);
    tripDayEntity.setProperty("date", inputDate);
    datastore.put(tripDayEntity);
    return tripDayEntity;
  }

  /**
   * Create the Event Entity and put it in Datastore.
   */
  public static Event putEventInDatastore(String name, String address, 
    String startTime, String travelTime) {
    // Create the Event Entity and put it in Datastore.
    Entity eventEntity = new Entity(Event.QUERY_STRING);
    eventEntity.setProperty("name", name);
    eventEntity.setProperty("address", address);
    eventEntity.setProperty("start-time", 
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startTime));
    eventEntity.setProperty("travel-time", travelTime);
    datastore.put(eventEntity);
    return eventEntity;
  }

}
