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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.google.sps.data.Event;
import com.google.sps.data.User;
import com.google.sps.Trip;
import com.google.sps.servlets.AuthServlet;
import com.google.sps.servlets.TripServlet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({PlacesApi.class,FindPlaceFromTextRequest.class,UserServiceFactory.class})
public final class TripServletTest {

  // class constants
  private static final String INPUT_DESTINATION = 
    "4265 24th Street San Francisco, CA, 94114";
  private static final LocalDate INPUT_DATE = LocalDate.parse("2020-07-15");
  private static final String POI_ONE = "one";
  private static final String POI_TWO = "two";

  // create TripServlet object
  private TripServlet tripServlet;
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  // initialize mock objects
  private HttpServletRequest request = mock(HttpServletRequest.class);
  private HttpServletResponse response = mock(HttpServletResponse.class);

  // Constants to pass into PlacesApi methods.
  private static final String TEXT_LOCATION_SEARCH = "Big Island, Hawaii, USA";
  private static final String PLACE_ID = "ChIJWTr3xcHnU3kRNIHX-ZKkVRQ";

  // Add constants necessary to testing retrieval of the current User.
  public static final String EMAIL = "testemail@gmail.com";
  public static final String AUTH_DOMAIN = "gmail.com";
  public static final String LOGOUT_URL = "/_ah/logout?continue=%2F";
  public static final String LOGIN_URL = "/_ah/login?continue=%2F";

  // Add helper to allow datastore testing in local JUnit tests.
  // See https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting.
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initTripServlet() {
    tripServlet = new TripServlet();
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
  public void testPutTripDayInDatastore() throws Exception {
    // set mock object behavior
    when(request.getParameter("inputDestination")).thenReturn(INPUT_DESTINATION);

    // create key
    Key testKey = KeyFactory.createKey("test", ((long) 123));

    // put entity in datastore and query it
    Entity tripDayEntity = tripServlet.putTripDayInDatastore(request, datastore, INPUT_DATE, testKey);
    Query query = new Query(TripDay.QUERY_STRING);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // check size, tripDayEntity is correctly added
    Assert.assertEquals(1, listResults.size());
    Assert.assertEquals(listResults.get(0), tripDayEntity);
  }

  @Test
  public void testPutEventsInDatastore() throws Exception {
    // set mock object behavior
    when(request.getParameter("poi-1")).thenReturn(POI_ONE);
    when(request.getParameter("poi-2")).thenReturn(POI_TWO);

    // manually create params list
    List<String> paramsList = new ArrayList<>();
    paramsList.add("inputDestination");
    paramsList.add("inputDayOfTravel");
    paramsList.add("inputTripName");
    paramsList.add("poi-1");
    paramsList.add("poi-2");
    Enumeration<String> params = Collections.enumeration(paramsList);

    // initialize datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // create tripDay entity, needed for put events in datastore
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING);
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", INPUT_DATE.toString());
    datastore.put(tripDayEntity);

    // put entities in datastore and query them
    List<Entity> eventEntities = tripServlet.putEventsInDatastore(request, response, params, tripDayEntity, INPUT_DATE, datastore);
    Query query = new Query(Event.QUERY_STRING);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // check that size is correct, added in correct order, entities match
    Assert.assertEquals(2, listResults.size());
    Assert.assertEquals(listResults.get(0), eventEntities.get(0));
    Assert.assertEquals(listResults.get(1), eventEntities.get(1));
  }

   @Test
  public void testFullDoPost() {
    // TODO: Adam to add full integration test
  }

  public void testGetPlaceIdFromTextSearchCandidatesPresent() throws Exception {
    // Mock the GeoApiContext object to be passed into PlacesApi methods,
    // and the FindPlaceFromTextRequest.
    GeoApiContext mockGeoApiContext = mock(GeoApiContext.class);
    FindPlaceFromTextRequest findPlaceRequest = 
      PowerMockito.mock(FindPlaceFromTextRequest.class);

    // Create the FindPlaceFromText object with a valid place ID.
    FindPlaceFromText findPlaceResult = new FindPlaceFromText();
    findPlaceResult.candidates = new PlacesSearchResult[1];
    findPlaceResult.candidates[0] = new PlacesSearchResult();
    findPlaceResult.candidates[0].placeId = PLACE_ID;

    // Have the findPlaceRequest.await() method return the FindPlaceFromText
    // object using PowerMockito, as await() is a final method.
    PowerMockito.when(findPlaceRequest.await()).thenReturn(findPlaceResult);

    // Mock the PlacesApi object.
    PowerMockito.mockStatic(PlacesApi.class);
    when(PlacesApi.findPlaceFromText(any(), anyString(), any()))
      .thenReturn(findPlaceRequest);

    // Run the getPlaceIdFromTextSearch(...) method to test the result.
    String placeIdResult = tripServlet.getPlaceIdFromTextSearch(mockGeoApiContext, 
      TEXT_LOCATION_SEARCH);

    // Confirm that the method returns the correct place ID.
    Assert.assertEquals(PLACE_ID, placeIdResult);
  }

  @Test
  public void testGetPlaceIdFromTextSearchCandidatesNull() throws Exception {
    // Mock the GeoApiContext object to be passed into PlacesApi methods,
    // and the FindPlaceFromTextRequest.
    GeoApiContext mockGeoApiContext = mock(GeoApiContext.class);
    FindPlaceFromTextRequest findPlaceRequest = 
      PowerMockito.mock(FindPlaceFromTextRequest.class);

    // Create the FindPlaceFromText object with no place ID.
    FindPlaceFromText findPlaceResult = new FindPlaceFromText();

    // Have the findPlaceRequest.await() method return the FindPlaceFromText
    // object using PowerMockito, as await() is a final method.
    PowerMockito.when(findPlaceRequest.await()).thenReturn(findPlaceResult);

    // Mock the PlacesApi object.
    PowerMockito.mockStatic(PlacesApi.class);
    when(PlacesApi.findPlaceFromText(any(), anyString(), any()))
      .thenReturn(findPlaceRequest);

    // Run the getPlaceIdFromTextSearch(...) method to test the result.
    String placeIdResult = tripServlet.getPlaceIdFromTextSearch(mockGeoApiContext, 
      TEXT_LOCATION_SEARCH);

    // Confirm that the method returns null.
    Assert.assertNull(placeIdResult);
  }

  @Test
  public void testStoreTripEntityLoggedIn() throws Exception {
    // Mock response.      
    HttpServletResponse responseMock = mock(HttpServletResponse.class);
    
    // Create Trip Entity properties.
    final String tripName = "Family Vacation";
    final String destinationName = "Island of Hawai'i";
    final String tripDayOfTravel = "2020-07-17";
    final String photoSrc = "../images/placeholder_image.png";

    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    // This is the User object from Google Appengine (full path given to avoid
    // confusion with local User.java file).
    when(userServiceMock.getCurrentUser()).thenReturn(
        new com.google.appengine.api.users.User(EMAIL, AUTH_DOMAIN));
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGOUT_URL);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);

    // Run storeTripEntity(...), with the User logged in (so trip is stored).
    Entity tripEntityReturn = tripServlet.storeTripEntity(responseMock,
      tripName, destinationName, tripDayOfTravel, photoSrc, datastore);

    // Retrieve the datastore results.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(Trip.TRIP);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Check whether the proper Entity and count were returned.
    Assert.assertEquals(1, listResults.size());
    Assert.assertEquals(tripName, listResults.get(0).getProperty(Trip.TRIP_NAME));
    Assert.assertEquals(destinationName, listResults.get(0).getProperty(Trip.DESTINATION_NAME));
    Assert.assertEquals(tripDayOfTravel, listResults.get(0).getProperty(Trip.START_DATE));
    Assert.assertEquals(tripDayOfTravel, listResults.get(0).getProperty(Trip.END_DATE));
    Assert.assertEquals(photoSrc, listResults.get(0).getProperty(Trip.IMAGE_SRC));
    
    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(listResults.get(0), tripEntityReturn);
  }

  @Test
  public void testStoreTripEntityLoggedInCheckUser() throws Exception {
    // Mock response.      
    HttpServletResponse responseMock = mock(HttpServletResponse.class);
    
    // Create Trip Entity properties.
    final String tripName = "Family Vacation";
    final String destinationName = "Island of Hawai'i";
    final String tripDayOfTravel = "2020-07-17";
    final String photoSrc = "../images/placeholder_image.png";

    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    // This is the User object from Google Appengine (full path given to avoid
    // confusion with local User.java file).
    when(userServiceMock.getCurrentUser()).thenReturn(
        new com.google.appengine.api.users.User(EMAIL, AUTH_DOMAIN));
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGOUT_URL);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);

    // Run storeTripEntity(...), with the User logged in (so trip is stored).
    tripServlet.storeTripEntity(responseMock, tripName, destinationName, 
      tripDayOfTravel, photoSrc, datastore);

    // Retrieve the datastore results.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(Trip.TRIP);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Check that the Trip Entity has the User as an ancestor / parent.
    Key userAncestorKey = listResults.get(0).getParent();
    Entity userEntity = datastore.get(userAncestorKey);
    Assert.assertEquals(User.USER, userEntity.getKind());
    Assert.assertEquals(EMAIL, userEntity.getProperty(User.USER_EMAIL));
  }

  @Test
  public void testStoreTripEntityNotLoggedIn() throws Exception {
    // Mock response.      
    HttpServletResponse responseMock = mock(HttpServletResponse.class);
    
    // Create Trip Entity properties.
    final String tripName = "Family Vacation";
    final String destinationName = "Island of Hawai'i";
    final String tripDayOfTravel = "2020-07-17";
    final String photoSrc = "../images/placeholder_image.png";

    // Mock UserService methods as logged-out user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(false);
    when(userServiceMock.createLoginURL(AuthServlet.redirectUrl)).thenReturn(LOGIN_URL);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);

    // Run storeTripEntity(...), with the User not logged in (so nothing is stored).
    Entity tripEntityReturn = tripServlet.storeTripEntity(responseMock,
      tripName, destinationName, tripDayOfTravel, photoSrc, datastore);

    // Retrieve the datastore results.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Check whether the proper Entity and count were returned.
    Assert.assertEquals(0, listResults.size());
    Assert.assertNull(tripEntityReturn);
  }
}
