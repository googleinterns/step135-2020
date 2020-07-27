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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.NotFoundException;
import com.google.maps.model.AddressType;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.GeocodedWaypointStatus;
import com.google.maps.model.LatLng;
import com.google.maps.model.Photo;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
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
import com.google.sps.TripDay;
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
 * calendar, calculates optimal route using Maps Java Client,
 * and puts info into datastore to be pulled by maps and calendar
 */
@WebServlet("/calculate-trip")
public class TripServlet extends HttpServlet {

  // Constant for picking route
  private static final int ROUTE_INDEX = 0;

  private static final int PHOTO_SRC_SIZE = 400;

  // Trip attributes needed to store the Trip Entity in datastore.
  private String tripName;
  private String tripDestination;
  private String tripDayOfTravel;
  private String destinationName;
  private String photoSrc;

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
   * Get user input.
   * Generate directionsRequest from user input and parse optimized route.
   * Create and store events in Datastore.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");

    // Retrieve form inputs to define the Trip object.
    this.tripName = request.getParameter(INPUT_TRIP_NAME);
    this.tripDestination = request.getParameter(INPUT_DESTINATION);
    this.tripDayOfTravel = request.getParameter(INPUT_DAY_OF_TRAVEL);
    String[] poiStrings = request.getParameterValues(INPUT_POI_LIST);

    // Populate the destinationName and photoSrc fields using Google Maps API.
    populateDestinationAndPhoto(this.context, this.tripDestination);

    // Store the Trip Entity in datastore with the User Entity as an ancestor.
    Entity tripEntity = storeTripEntity(response, this.tripName, this.destinationName, 
      this.tripDayOfTravel, this.photoSrc, datastore);

    //do post for maps
    DirectionsApiRequest dirRequest = generateDirectionsRequest(this.tripDestination, this.tripDestination, poiStrings, this.context);
    DirectionsResult dirResult = getDirectionsResult(dirRequest);
    List<Integer> travelTimes = getTravelTimes(dirResult);
    List<String> orderedLocationStrings = getOrderedWaypoints(dirResult, poiStrings);

    // put TripDay entity into datastore
    Entity tripDayEntity = putTripDayInDatastore(this.tripDestination, datastore, LocalDate.parse(tripDayOfTravel), tripEntity.getKey());

    List<Entity> locationEntities = TripDay.locationsToEntities(orderedLocationStrings, tripDayEntity.getKey());
    TripDay.storeLocationsInDatastore(locationEntities, this.datastore);
    
    // put Event entities in datastore
    putEventsInDatastore(tripDayEntity, LocalDate.parse(tripDayOfTravel), datastore, orderedLocationStrings, travelTimes);

    // Redirect to the "/trips/" page to show the trip that was added.
    response.sendRedirect("/trips/");
  }

  /**
   * Get the place ID of the text search. Return null if no place ID matches
   * the search.
   * 
   * @param context The entry point for making requests against the Google Geo 
   * APIs (googlemaps.github.io/google-maps-services-java/v0.1.2/javadoc/com/google/maps/GeoApiContext.html).
   * @param textSearch The text query to be entered in the findPlaceFromText(...)
   * API call. Must be non-null.
   */ 
  public String getPlaceIdFromTextSearch(GeoApiContext context, String textSearch) 
    throws IOException {

    FindPlaceFromTextRequest findPlaceRequest = PlacesApi.findPlaceFromText(context, 
      textSearch, FindPlaceFromTextRequest.InputType.TEXT_QUERY);

    try {
      FindPlaceFromText findPlaceResult = findPlaceRequest.await();

      // Return place ID of the first candidate result.
      if (findPlaceResult.candidates != null) {
        return findPlaceResult.candidates[0].placeId;
      }
      
      // No candidate is given, so return null.
      return null;
    } catch(ApiException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  /**
   * put TripDay into Datastore
   * @return tripDay entity, needed for event creation
   */
  public Entity putTripDayInDatastore(String origin, DatastoreService datastore, LocalDate date, Key tripEntityKey) 
      throws IOException {
    TripDay tripDay = new TripDay(origin, origin, new ArrayList<>(), date);
    Entity tripDayEntity = tripDay.buildEntity(tripEntityKey);
    datastore.put(tripDayEntity);
    return tripDayEntity;  
  }

  /**
   * Make the servlet cleaner.
   * Searches through the parameters and creates the events and puts them into
   * datastore with associated tripDayEntity as a parent
   */
  public List<Entity> putEventsInDatastore(Entity tripDayEntity, LocalDate date, DatastoreService datastore,
      List<String> pois, List<Integer> travelTimes)
      throws IOException { 

    // entities to return, needed for testing
    List<Entity> eventEntities = new ArrayList<>();    

    // set startDateTime
    LocalDateTime startDateTime = LocalDateTime.of(date, LocalTime.of(10, 0));

    int travelTimeIndex = 1;
    // for each poi create the necessary fields
    for (String address : pois) {
      String name = address.split(",")[0];
      Event event = new Event(name, address, startDateTime, HALF_HOUR);
      Entity eventEntity = event.eventToEntity(tripDayEntity.getKey());
      eventEntities.add(eventEntity);

      // put entity in datastore   
      datastore.put(eventEntity);

      // sets start time for next event one hour and travel time after start of prev
      startDateTime = startDateTime.plusMinutes(Long.valueOf(ONE_HOUR + travelTimes.get(travelTimeIndex)));
      travelTimeIndex++;
    }
    return eventEntities;
  }

  /**
   * Get the PlaceDetails object from the place ID.
   */
  private PlaceDetails getPlaceDetailsFromPlaceId(GeoApiContext context, String placeId)
    throws IOException {

    PlaceDetailsRequest placeDetailsRequest = PlacesApi.placeDetails(context, 
      placeId);
    try {
      return placeDetailsRequest.await();
    } catch(ApiException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  /**
   * Populate the destinationName and photoSrc fields using the Google Maps API.
   */
  private void populateDestinationAndPhoto(GeoApiContext context, String tripDestination)
    throws IOException {

    // Get place ID from search of trip destination. Get photo and destination 
    // if not null; otherwise, use a placeholder photo and destination.
    String destinationPlaceId = getPlaceIdFromTextSearch(context, this.tripDestination);
    if (destinationPlaceId == null) {
      this.destinationName = tripDestination;
      this.photoSrc = "../images/placeholder_image.png";
    } else {
      PlaceDetails placeDetailsResult = getPlaceDetailsFromPlaceId(context, destinationPlaceId);

      // Get the name of the location from the place details result.
      this.destinationName = placeDetailsResult.name;

      // Get a photo of the location from the place details result.
      if (placeDetailsResult.photos == null) {
        this.photoSrc = "../images/placeholder_image.png";
      } else {
        Photo photoObject = placeDetailsResult.photos[0];
        this.photoSrc = getUrlFromPhotoReference(PHOTO_SRC_SIZE, photoObject.photoReference);
      }
    }
  }

  /**
   * Store the Trip Entity in datastore with the User Entity as an ancestor.
   * Return the Trip Entity object.
   * 
   * @param response The HttpServletResponse used to redirect to homepage if
   * no user is logged in. 
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param destinationName The name of the destination the user is heading to.
   * This destination should be verified by the Google Maps API.
   * @param tripDayOfTravel The date of the trip. Must be in yyyy-MM-dd date format.
   * @param photoSrc The image source / URL to represent the trip. This is 
   * typically retrieved using the Places API to get a photo from the destination
   * name, but can also be the placeholder image source if no photo exists.
   */
  public Entity storeTripEntity(HttpServletResponse response, String tripName, 
    String destinationName, String tripDayOfTravel, String photoSrc, DatastoreService datastore) throws IOException {
    // Get User Entity. If user not logged in, redirect to homepage.
    Entity userEntity = AuthServlet.getCurrentUserEntity();
    if (userEntity == null) {
      response.sendRedirect("/");
      return null;
    }

    // Put Trip Entity into datastore.
    Entity tripEntity = Trip.buildEntity(tripName, destinationName, photoSrc,
      tripDayOfTravel, tripDayOfTravel, userEntity.getKey());
    datastore.put(tripEntity);
    return tripEntity;
  }

  /**
   * Get a URL to show the photo from the photoreference.
   * See https://developers.google.com/places/web-service/photos#place_photo_requests
   * for more info.
   * 
   * @param maxWidth This is the maximum width of the image.
   * @param photoReference This is the photo reference String stored in the 
   * Google Maps Photo object; this is used to retrieve the actual photo URL.
   */
  private String getUrlFromPhotoReference(int maxWidth, String photoReference) {
    final String baseUrl = "https://maps.googleapis.com/maps/api/place/photo?";
    return baseUrl + "maxwidth=" + maxWidth + "&photoreference=" + 
      photoReference + "&key=" + Config.API_KEY;
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
