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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.User;
import com.google.sps.Trip;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/user-trips")
public class UserTripServlet extends HttpServlet {

  // This allows the doGet method to return null.
  private static final String nullReturn = null;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    // If no user is signed in, return null.
    Entity userEntity = AuthServlet.getCurrentUserEntity(AuthServlet.getUserService());
    if (userEntity == null) {
      response.getWriter().println(nullReturn);
      return;
    }

    // Get the list of Trip Entity objects through a Query.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(Trip.TRIP, userEntity.getKey());
    PreparedQuery results = datastore.prepare(query);

    // Iterate over the trip Entity objects, and convert them to Trip objects.
    List<Trip> tripList = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      tripList.add(Trip.buildTripFromEntity(entity));
    }

    // Convert the trip list to JSON, and return JSON. Empty list signals no trips.
    String json = convertTripListToJson(tripList);
    response.getWriter().println(json);
  }

  /**
  * Converts a List of Trips into a JSON string using the Gson library.
  */
  private String convertTripListToJson(List<Trip> tripList) {
    Gson gson = new Gson();
    String json = gson.toJson(tripList);
    return json;
  }

}
