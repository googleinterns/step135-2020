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

  // Add constants necessary to testing retrieval of the current User.
  private static final String EMAIL = "testemail@gmail.com";
  private static final String AUTH_DOMAIN = "gmail.com";
  private static final String LOGOUT_URL = "/_ah/logout?continue=%2F";
  private static final String LOGIN_URL = "/_ah/login?continue=%2F";

  @Before
  public void initDatastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Add the logged-in user to Datastore. Get property names from the User class
   * and test property values from field variables.
   *
   * @return The Key of the User Entity in Datastore.
   */ 
  public static Key addLoggedInUserToDatastore() {
    // Add the logged-in User to Datastore, and get the User Entity Key.
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
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

}
