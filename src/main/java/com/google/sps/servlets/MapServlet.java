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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.sps.Trip;
import com.google.sps.TripDay;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/get-map")
public class MapServlet extends HttpServlet {

  private final String TRIP_KEY_PARAM = "tripKey";

  /**
   * Checks for invalid cases (no user or tripKey).
   * Gets the locations from datastore and prints them to writer.
   */  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");

    // Initialize Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Get current user.
    Entity userEntity = AuthServlet.getCurrentUserEntity();

    // Get tripKey.
    String stringTripKey = request.getParameter(TRIP_KEY_PARAM);

    // if no tripKey return to trips page.
    if (stringTripKey == null) {
      response.getWriter().println("No trip Key");
      response.sendRedirect("/trips/");
  
    // Redirects to sign in page if no user is signed in
    // Otherwise redirects to create trips page
    } else if (userEntity == null) {
      response.getWriter().println("No current User");
      response.sendRedirect("/");
    } else { 
      Key tripKey = KeyFactory.stringToKey(stringTripKey);

      // Gets the locations from datastore and writes them to .../get-map
      String result = doGetMap(response, datastore, userEntity, tripKey);
      response.getWriter().println(result);
    }
  }

  /**
   * Gets the locations and prints them to writer.
   */
  public String doGetMap(HttpServletResponse response, 
      DatastoreService datastore, Entity userEntity, Key tripEntityKey) throws IOException {
  
    // Get trip Entity based on trip key.
    // Trip entity is needed to ensure that tripKey corresponds to a Trip under the current user.
    Filter tripKeyFilter =
      new FilterPredicate("__key__", FilterOperator.EQUAL, tripEntityKey);
    Query tripQuery = new Query(Trip.TRIP, userEntity.getKey());
    tripQuery.setFilter(tripKeyFilter);
    PreparedQuery tripResults = datastore.prepare(tripQuery);
    Entity tripEntity = tripResults.asSingleEntity();

    // If no trip is found then redirect home
    if (tripEntity == null) {
      response.sendRedirect("/");
      return "No trip found";
    } 

    // Get TripDay associated with the Trip.
    // TODO(eshika): change to select the desired tripDay for multiday trips.
    Query tripDayQuery = new Query(TripDay.QUERY_STRING, tripEntity.getKey());
    PreparedQuery tripDayResults = datastore.prepare(tripDayQuery);
    Entity tripDayEntity = tripDayResults.asSingleEntity();

    // Add origin as the first location.
    List<String> locations = new ArrayList<>();
    locations.add((String) tripDayEntity.getProperty(TripDay.ORIGIN));

    // Add rest of POIs to locations list and write to writer.
    Query locationsQuery = new Query(TripDay.LOCATION_ENTITY_TYPE, tripDayEntity.getKey());
    locationsQuery.addSort(TripDay.ORDER);
    PreparedQuery locationResults = datastore.prepare(locationsQuery);
    for (Entity locationEntity : locationResults.asIterable()) {
      // Gets location names as Strings (these names include the full address needed for routing)
      locations.add((String) locationEntity.getProperty(TripDay.NAME));
    }

    return convertToJson(locations);
  }

  /**
   * Converts list of location strings into a JSON string using the Gson library.
   */
  private String convertToJson(List<String> locations) {
    Gson gson = new Gson();
    String json = gson.toJson(locations);
    return json;
  }
}
