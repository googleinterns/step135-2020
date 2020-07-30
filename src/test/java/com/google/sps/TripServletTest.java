
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
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.Duration;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.TravelMode;
import com.google.sps.data.Event;
import com.google.sps.data.Trip;
import com.google.sps.data.TripDay;
import com.google.sps.data.User;
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
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DirectionsApi.class, DirectionsApiRequest.class,
                 PlacesApi.class,FindPlaceFromTextRequest.class,UserServiceFactory.class})
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

  // Test directionsRequest generation with mocks.
  @Test
  public void generateDirectionsRequestTest() {
    PowerMockito.mockStatic(DirectionsApi.class);
    DirectionsApiRequest mockRequest = mock(DirectionsApiRequest.class);
    when(DirectionsApi.newRequest(any())).thenReturn(mockRequest);
    when(mockRequest.origin(anyString())).thenReturn(mockRequest);
    when(mockRequest.destination(anyString())).thenReturn(mockRequest);
    when(mockRequest.waypoints(ArgumentMatchers.<String>any())).thenReturn(mockRequest);
    when(mockRequest.optimizeWaypoints(anyBoolean())).thenReturn(mockRequest);
    when(mockRequest.mode(any())).thenReturn(mockRequest);

    GeoApiContext mockGeoApiContext = mock(GeoApiContext.class);

    String[] waypoints = new String[]{ "MoPOP, 5th Avenue North, Seattle, WA, USA",
                                    "Space Needle, Broad Street, Seattle, WA, USA",
                                    "Alki Beach, Seattle, WA, USA"};
    
    String origin = "The Westin Bellevue, Bellevue Way Northeast, Bellevue, WA, USA";

    DirectionsApiRequest request = TripServlet.generateDirectionsRequest(origin, origin, waypoints, mockGeoApiContext);

    // Verify that proper methods and parameters are called to generate directionsRequest.
    verify(mockRequest).origin(origin);
    verify(mockRequest).destination(origin);
    verify(mockRequest).waypoints(waypoints);
    verify(mockRequest).optimizeWaypoints(true);
    verify(mockRequest).mode(TravelMode.DRIVING);
  }

  // Test waypoint order parsing from a DirectionsResult object.
  @Test
  public void getOrderedWaypointsTest() {
    // Manually create directions result object.
    DirectionsResult dirResult = new DirectionsResult();
    dirResult.routes = new DirectionsRoute[1];
    dirResult.routes[0] = new DirectionsRoute();
    dirResult.routes[0].waypointOrder = new int[]{ 1, 2, 0 };
    
    // Manually create user input.
    String[] pois = new String[]{"Alki Beach, Seattle, WA, USA",
                                  "MoPOP, 5th Avenue North, Seattle, WA, USA",
                                  "Space Needle, Broad Street, Seattle, WA, USA"};

    List<String> orderedWaypoints = TripServlet.getOrderedWaypoints(dirResult, pois);

    List<String> expectedWaypointOrder = new ArrayList<>();
    expectedWaypointOrder.add("MoPOP, 5th Avenue North, Seattle, WA, USA");
    expectedWaypointOrder.add("Space Needle, Broad Street, Seattle, WA, USA");
    expectedWaypointOrder.add("Alki Beach, Seattle, WA, USA");

    Assert.assertEquals(orderedWaypoints, expectedWaypointOrder);
  }

  // Test travel time parsing from a DirectionsResult object.
  @Test
  public void getTravelTimesTest() {
    // Manually create directions result object
    DirectionsResult dirResult = new DirectionsResult();
    dirResult.routes = new DirectionsRoute[1];
    dirResult.routes[0] = new DirectionsRoute();
    dirResult.routes[0].legs = new DirectionsLeg[4];
    for (int i = 0; i < dirResult.routes[0].legs.length; i++) {
      dirResult.routes[0].legs[i] = new DirectionsLeg();
      dirResult.routes[0].legs[i].duration = new Duration();
    }
    dirResult.routes[0].legs[0].duration.inSeconds = 1080;
    dirResult.routes[0].legs[1].duration.inSeconds = 60;
    dirResult.routes[0].legs[2].duration.inSeconds = 1500;
    dirResult.routes[0].legs[3].duration.inSeconds = 2100;
    
    List<Integer> actualTravelTimes  = TripServlet.getTravelTimes(dirResult);

    List<Integer> expectedTravelTimes = new ArrayList<>();
    expectedTravelTimes.add(18);
    expectedTravelTimes.add(1);
    expectedTravelTimes.add(25);
    expectedTravelTimes.add(35);

    Assert.assertEquals(actualTravelTimes, expectedTravelTimes);
  }

  // Test getting DirectionsResult object from DirectionsRequest
  @Test
  public void getDirectionsResultTest() throws Exception {
    DirectionsApiRequest mockRequest = PowerMockito.mock(DirectionsApiRequest.class);

    // Construct directionsResult object
    DirectionsResult expectedResult = new DirectionsResult();
    expectedResult.routes = new DirectionsRoute[1];
    expectedResult.routes[0] = new DirectionsRoute();
    expectedResult.routes[0].waypointOrder = new int[]{ 1, 2, 0 };

    PowerMockito.when(mockRequest.await()).thenReturn(expectedResult);

    DirectionsResult actualResult = TripServlet.getDirectionsResult(mockRequest);

    Assert.assertEquals(expectedResult.routes[0].waypointOrder, actualResult.routes[0].waypointOrder);
  }

  @Test
  public void doPostTest() {
    //TODO (eshika): add an integration test 
  }

  @Test
  public void testPutTripDayInDatastore() throws Exception {
    // create key
    Key testKey = KeyFactory.createKey("test", ((long) 123));

    // put entity in datastore and query it
    Entity tripDayEntity = tripServlet.putTripDayInDatastore(INPUT_DESTINATION, datastore, INPUT_DATE, testKey);
    Query query = new Query(TripDay.QUERY_STRING);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // check size, tripDayEntity is correctly added
    Assert.assertEquals(1, listResults.size());
    Assert.assertEquals(listResults.get(0), tripDayEntity);
  }

  @Test
  public void testPutEventsInDatastore() throws Exception {
    // Manually create list of ordered locations
    List<String> orderedLocations = new ArrayList<>();
    orderedLocations.add("MoPOP, 5th Avenue North, Seattle, WA, USA");
    orderedLocations.add("Space Needle, Broad Street, Seattle, WA, USA");
    orderedLocations.add("Alki Beach, Seattle, WA, USA");

    // Manually create list of travelTimes
    List<Integer> travelTimes = new ArrayList<>();
    travelTimes.add(18);
    travelTimes.add(1);
    travelTimes.add(25);
    travelTimes.add(35);

    // initialize datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // create tripDay entity, needed for put events in datastore
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING);
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", INPUT_DATE.toString());
    datastore.put(tripDayEntity);

    // put entities in datastore and query them
    List<Entity> eventEntities = tripServlet.putEventsInDatastore(tripDayEntity, INPUT_DATE, datastore, orderedLocations, travelTimes);
    Query query = new Query(Event.QUERY_STRING);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // check that size is correct, added in correct order, entities match
    Assert.assertEquals(3, listResults.size());
    Assert.assertEquals(listResults.get(0), eventEntities.get(0));
    Assert.assertEquals(listResults.get(1), eventEntities.get(1));
    Assert.assertEquals(listResults.get(2), eventEntities.get(2));
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
