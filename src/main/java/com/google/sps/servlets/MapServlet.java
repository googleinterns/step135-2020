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
import com.google.sps.data.Event;
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
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // get current user
    Entity userEntity = AuthServlet.getCurrentUserEntity();

    // if no user Entity return home
    if (request.getParameter("tripKey") == null ){
      response.getWriter().println("No trip Key");
      response.sendRedirect("/trips/");
    } else if (userEntity == null) {
      response.getWriter().println("No current User");
      response.sendRedirect("/");
    } else { 
      String stringTripKey = request.getParameter("tripKey");
      Key tripKey = KeyFactory.stringToKey(stringTripKey);

      // gets the locations from datastore and writes them to .../get-map
      doGetMap(response, datastore, userEntity, tripKey);
    }
  }

  /**
   * Gets the events and prints them to writer, necessary break up for testing
   */
  public void doGetMap(HttpServletResponse response, 
      DatastoreService datastore, Entity userEntity, Key tripEntityKey) throws IOException {
  
    Filter tripKeyFilter =
      new FilterPredicate("__key__", FilterOperator.EQUAL, tripEntityKey);
    Query tripQuery = new Query(Trip.TRIP, userEntity.getKey());
    tripQuery.setFilter(tripKeyFilter);

    PreparedQuery tripResults = datastore.prepare(tripQuery);
    Entity tripEntity = tripResults.asSingleEntity();

    Query tripDayQuery = new Query(TripDay.QUERY_STRING, tripEntity.getKey());
    PreparedQuery tripDayResults = datastore.prepare(tripDayQuery);

    int tripDayIndex = 0;

    Entity tripDayEntity = tripDayResults.asList(FetchOptions.Builder.withLimit(10)).get(tripDayIndex);

    List<String> locations = new ArrayList<>();
    locations.add((String) tripDayEntity.getProperty("origin"));

    Query locationsQuery = new Query(TripDay.LOCATION_ENTITY_TYPE, tripDayEntity.getKey());
    locationsQuery.addSort(TripDay.ORDER);
    PreparedQuery locationResults = datastore.prepare(locationsQuery); 
    for (Entity locationEntity : locationResults.asIterable()) {
      locations.add((String) locationEntity.getProperty(TripDay.NAME));
    }
    response.getWriter().println(convertToJson(locations));
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
