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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.maps.errors.ApiException;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.LatLng;
import com.google.maps.model.Photo;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.sps.data.Config;
import com.google.sps.Trip;
import com.google.sps.data.Event;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet that currently allows for retrieving info from url and put in 
 *  calendar
 */
@WebServlet("/calculate-trip")
public class TripServlet extends HttpServlet {

  // Create the GeoApiContext object.
  private GeoApiContext context;
  private int photoSrcSize = 400;

  // Constants to get form inputs.
  private static final String INPUT_TRIP_NAME = "inputTripName";
  private static final String INPUT_DESTINATION = "inputDestination";
  private static final String INPUT_DAY_OF_TRAVEL = "inputDayOfTravel";

  // Trip attributes needed to store the Trip Entity in datastore.
  private String tripName;
  private String tripDestination;
  private String tripDayOfTravel;
  private String destinationName;
  private String photoSrc;

  // time class constants
  private static final int HALF_HOUR = 30;
  private static final int NINETY_MINS = 90;

  // event fields for entity
  private static final String NAME = "name";
  private static final String ADDRESS = "end-time";
  private static final String DATE = "date";
  private static final String START_TIME = "start-time";
  private static final String TRAVEL_TIME = "travel-time";

  @Override
  public void init() {
    this.context = new GeoApiContext.Builder()
      .apiKey(Config.API_KEY)
      .build();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");
    
    // do get for events
    eventDoGet(response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");

    // Retrieve form inputs to define the Trip object.
    this.tripName = request.getParameter(INPUT_TRIP_NAME);
    this.tripDestination = request.getParameter(INPUT_DESTINATION);
    this.tripDayOfTravel = request.getParameter(INPUT_DAY_OF_TRAVEL);

    // Populate the destinationName and photoSrc fields using Google Maps API.
    populateDestinationAndPhoto(context, tripDestination);

    // Store the Trip Entity in datastore with the User Entity as an ancestor.
    storeTripEntity(response, this.tripName, this.destinationName, 
      this.tripDayOfTravel, this.photoSrc);

    /**
     * TODO: Remaining code for storing Event and TripDay objects should 
     * go here, below the above code, as the Trip has to be set first in order
     * to maintain Entity hierarchy / ancestor paths. 
     * 
     * Below methods can also use the field variables fetched from request in 
     * the above code.
     */

    // do post for events
    eventDoPost(request, response); 

    // Redirect to the "/trips/" page to show the trip that was added.
    response.sendRedirect("/trips/");
  }

  /**
   * Get the place ID of the text search. Return null if no place ID matches
   * the search.
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
   * Make the servlet cleaner
   * Iterate through the entities and create the events and write them to json
   */
  private void eventDoGet(HttpServletResponse response) throws IOException {
    Query query = new Query("events");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();

    // create the events
    for (Entity entity : results.asIterable()) {
      events.add(Event.eventFromEntity(entity));
    }   

    response.getWriter().println(convertToJson(events));
  }

  /**
   * Make the servlet cleaner.
   * Searches through the parameters and creates the events and puts them into
   * datastore.
   */
  private void eventDoPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException { 
    
    // Print out params to site to verify retrieval of "start trip" user input.
    Enumeration<String> params = request.getParameterNames();

    // get date of trip
    String date = request.getParameter("inputDayOfTravel");

    // set startDateTime
    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.of(10, 0));

    // search through all the parameters looking for pois
    while (params.hasMoreElements()) {
      String p = params.nextElement();

      /** 
       * for each poi create the necessary fields, this will change
       * as we are able to pull from the maps backend api
       */
      if (p.contains("poi")) {
        String address = request.getParameter(p);
        String name = address.split(",")[0];
        Event event = new Event(name, address, startDateTime, HALF_HOUR);
        Entity eventEntity = event.eventToEntity();

        // put entity in datastore
        DatastoreService datastore = 
                                DatastoreServiceFactory.getDatastoreService();      
        datastore.put(eventEntity);

        // sets start time for next event 2 hours after start of prev
        startDateTime = startDateTime.plusMinutes(Long.valueOf(NINETY_MINS));
      }
    }
  }

  /**
   * Get the PlaceDetails object from the place ID.
   */
  public PlaceDetails getPlaceDetailsFromPlaceId(GeoApiContext context, String placeId)
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
  public void populateDestinationAndPhoto(GeoApiContext context, String tripDestination)
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
        this.photoSrc = getUrlFromPhotoReference(this.photoSrcSize, photoObject.photoReference);
      }
    }
  }

  /**
   * Store the Trip Entity in datastore with the User Entity as an ancestor.
   * Return the Trip Entity object.
   */
  public Entity storeTripEntity(HttpServletResponse response, String tripName, 
    String destinationName, String tripDayOfTravel, String photoSrc) throws IOException {
    // Get User Entity. If user not logged in, redirect to homepage.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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
  public String getUrlFromPhotoReference(int maxWidth, String photoReference) {
    final String baseUrl = "https://maps.googleapis.com/maps/api/place/photo?";
    return baseUrl + "maxwidth=" + maxWidth + "&photoreference=" + 
      photoReference + "&key=" + Config.API_KEY;
  }

  /**
   * Converts list of Event objects into a JSON string using the Gson library.
   */
  private String convertToJson(List<Event> events) {
    Gson gson = new Gson();
    String json = gson.toJson(events);
    return json;
  }
}
