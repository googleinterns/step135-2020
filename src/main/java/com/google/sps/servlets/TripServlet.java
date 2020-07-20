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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.NotFoundException;
import com.google.maps.model.AddressType;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.GeocodedWaypointStatus;
import com.google.maps.model.LatLng;
import com.google.maps.model.TrafficModel;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TransitRoutingPreference;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import com.google.gson.Gson;
import com.google.sps.Trip;
import com.google.sps.TripDay;
import com.google.sps.data.Config;
import com.google.sps.data.Event;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet that currently allows for retrieving info from url and put in 
 *  calendar and calculates optimal route using Maps Java Client
 */
@WebServlet("/calculate-trip")
public class TripServlet extends HttpServlet {

  // Constant for picking route
  private static final int ROUTE_INDEX = 0;

  // time class constants
  private static final int HALF_HOUR = 30;
  private static final int ONE_HOUR = 60;
  private static final int NINETY_MINS = 90;
  private static final int SECONDS_IN_MIN = 60;

  // event fields for entity
  private static final String NAME = "name";
  private static final String ADDRESS = "end-time";
  private static final String DATE = "date";
  private static final String START_TIME = "start-time";
  private static final String TRAVEL_TIME = "travel-time";

  // Constants to get form inputs.
  private static final String INPUT_TRIP_NAME = "inputTripName";
  private static final String INPUT_DESTINATION = "inputDestination";
  private static final String INPUT_DAY_OF_TRAVEL = "inputDayOfTravel";
  private static final String INPUT_POI_LIST = "poiList";

  // Datastore and API context
  private DatastoreService datastore;
  private GeoApiContext context;

  /**
   * Initializes datastore and API.
   */
  @Override
  public void init() {
    this.datastore = DatastoreServiceFactory.getDatastoreService();  
    this.context = new GeoApiContext.Builder()
      .apiKey(Config.API_KEY)
      .build();
  }
 
  /**
   * Iterate through the entities and create the events and write them to json.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");
    
    // do get for events
    eventDoGet(response);
  }

  /**
   * Get user input.
   * Generate directionsRequest from user input and parse optimized route.
   * Create and store events in Datastore.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");

    Enumeration<String> params = request.getParameterNames();
    String tripName = request.getParameter(INPUT_TRIP_NAME);
    String origin = request.getParameter(INPUT_DESTINATION);
    String startDate = request.getParameter(INPUT_DAY_OF_TRAVEL);
    String[] poiStrings = request.getParameterValues(INPUT_POI_LIST);

    //do post for maps
    DirectionsApiRequest dirRequest = generateDirectionsRequest(origin, origin, poiStrings, this.context);
    DirectionsResult dirResult = getDirectionsResult(dirRequest);
    List<Integer> travelTimes = getTravelTimes(dirResult);
    List<String> orderedLocationStrings = getOrderedWaypoints(dirResult, poiStrings);

    // TODO: replace testKey with key of TripDay entity
    long test = 123;
    Key testKey = KeyFactory.createKey("test", test);

    List<Entity> locationEntities = TripDay.locationsToEntities(orderedLocationStrings, testKey);
    TripDay.storeLocationsInDatastore(locationEntities, this.datastore);

    // do post for events
    eventDoPost(startDate, orderedLocationStrings, travelTimes); 

    // redirect to trips page
    response.sendRedirect("/trips/");
  }

  /**
   * Iterate through the entities and create the events and write them to json.
   */
  private void eventDoGet(HttpServletResponse response) throws IOException { 
    
    Query query = new Query("events");

    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();

    // create the events
    for (Entity entity : results.asIterable()) {
      events.add(Event.eventFromEntity(entity));
    }   

    response.getWriter().println(convertToJson(events));
  }

  /**
   * Creates the events and puts them into datastore.
   * @param date Trip date
   * @param pois List<String> of pois
   * @param travelTimes List<Integer> of travel times in minutes
   */
  private void eventDoPost(String date, List<String> pois, List<Integer> travelTimes) 
      throws IOException { 
    
    // set startDateTime
    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.of(10, 0));

    int travelTimeIndex = 1;
    // for each poi create the necessary fields
    for (String address : pois) {
      String name = address.split(",")[0];
      Event event = new Event(name, address, startDateTime, HALF_HOUR);
      Entity eventEntity = event.eventToEntity();

      // put entity in datastore   
      datastore.put(eventEntity);

      // sets start time for next event 2 hours after start of prev
      startDateTime = startDateTime.plusMinutes(Long.valueOf(ONE_HOUR + travelTimes.get(travelTimeIndex)));
      travelTimeIndex++;
    }
  }

  /**
   * Generate directionsResult from directionsRequest.
   * @param directionsRequest DirectionsApiRequest object generated from user input
   */
  public static DirectionsResult getDirectionsResult(DirectionsApiRequest directionsRequest) 
      throws IOException {
    // Calculate route and save travelTimes and waypointOrder to two ArrayLists.
    try {
      DirectionsResult dirResult = directionsRequest.await();
      return dirResult;
    } catch (ApiException | InterruptedException e) {
      // If no directions are found or API throws an error.
      throw new IOException(e);
    } 
  }

  /**
   * Generates directionsRequest from user input.
   * @param origin route starting point
   * @param destination route ending point
   * @param poiStrings String array of poi stops along the route
   * @param context API context
   */
  public static DirectionsApiRequest generateDirectionsRequest(String origin, String destination, 
      String[] poiStrings, GeoApiContext context) {

    // Generate directions request
    DirectionsApiRequest directionsRequest = DirectionsApi.newRequest(context)
        .origin(origin)
        .destination(destination)
        .waypoints(poiStrings)
        .optimizeWaypoints(true)
        .mode(TravelMode.DRIVING);

    return directionsRequest;
  }

  /**
   * Gets list of travel times for each route leg from a DirectionsResult object.
   * @param dirResult DirectionsResult object containing optimal route
   */
  public static List<Integer> getTravelTimes(DirectionsResult dirResult) {
    List<Integer> travelTimes = new ArrayList<>();
    // Take the first route, usually the optimal.
    for (DirectionsLeg leg : dirResult.routes[ROUTE_INDEX].legs) {
      int travelTime = (int) leg.duration.inSeconds / SECONDS_IN_MIN;
      travelTimes.add(travelTime);
    }

    return travelTimes;
  }

  /**
   * Gets list of poi addresses in optimized route order from a DirectionsResult object.
   * @param dirResult DirectionsResult object containing optimal route
   * @param pois String array of poi names (in any order)
   */
  public static List<String> getOrderedWaypoints(DirectionsResult dirResult, String[] pois) {
    // Take the first route, usually the optimal.
    int[] waypointOrder = dirResult.routes[ROUTE_INDEX].waypointOrder;

    // Generate an ordered list of location Strings from waypointOrder.
    List<String> orderedLocationStrings = new ArrayList<>();
    for (int i = 0; i < waypointOrder.length; i++) {
      orderedLocationStrings.add(pois[waypointOrder[i]]);
    }

    return orderedLocationStrings;
  }

  /**
   * Converts list of Event objects into a JSON string using the Gson library.
   * @param events List of event objects
   */
  private String convertToJson(List<Event> events) {
    Gson gson = new Gson();
    String json = gson.toJson(events);
    return json;
  }
}
