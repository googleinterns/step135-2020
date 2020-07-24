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
    String stringTripKey = request.getParameter("tripKey");
    Key tripKey = KeyFactory.stringToKey(stringTripKey);

    // get current user
    Entity userEntity = AuthServlet.getCurrentUserEntity();


    if (userEntity == null) {
      response.sendRedirect("/");
    }

    Filter tripKeyFilter =
      new FilterPredicate("__key__", FilterOperator.EQUAL, tripKey);
    Query tripQuery = new Query("trip", userEntity.getKey());
    tripQuery.setFilter(tripKeyFilter);

    PreparedQuery tripResults = datastore.prepare(tripQuery);
    Entity tripEntity = tripResults.asSingleEntity();

    if (tripEntity == null) {
      throw new IllegalArgumentException("Trip does not exist");
    }

    Query tripDayQuery = new Query("trip-day", tripEntity.getKey());
    PreparedQuery tripDayResults = datastore.prepare(tripDayQuery);

    List<Entity> tripDayEntities = tripDayResults.asList(FetchOptions.Builder.withDefaults());
    // For MVP: only one tripDay per trip, so get first tripDay.
    Entity tripDayEntity = tripDayEntities.get(0);

    String origin = (String) tripDayEntity.getProperty("origin");
    Query locationsQuery = new Query("location", tripDayEntity.getKey());
    PreparedQuery locationsResults = datastore.prepare(locationsQuery);
    List<Entity> locationsEntities = locationsResults.asList(FetchOptions.Builder.withDefaults());
    
    List<String> locations = new ArrayList<>();
    locations.add(origin);
    for (Entity locationEntity : locationsEntities) {
      locations.add((String) locationEntity.getProperty("name"));
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
