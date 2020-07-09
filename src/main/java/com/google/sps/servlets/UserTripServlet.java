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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.User;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/user-trips")
public class UserTripServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    // If no user is signed in, return null.
    User user = AuthServlet.getCurrentUser();
    if (user == null) {
      return null;
    }

    // Get the list of trip IDs, and return the relevant trip information.
    List<String> tripIdList = user.getTripIdList();

    // Query database to get all relevant trips.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter emailFilter =
      new FilterPredicate(Trip.TRIP_ID, FilterOperator.IN, tripIdList);
    Query query = new Query(Trip.TRIP).setFilter(emailFilter);
    PreparedQuery results = datastore.prepare(query);

    // Get the list of results. If empty, return null; otherwise, return the trip objects.
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());
    if (listResults.isEmpty()) {
      return null;
    }

    // Iterate over the trip Entity objects, and convert them to Trip objects.
    List<Trip> tripList = new ArrayList<>();
    for (Entity entity : listResults) {
      tripList.add(Trip.buildTripFromEntity());
    }

    // Convert the trip list to JSON, and return JSON.
    String json = convertTripListToJson(tripList);
    return json;
  }

  /**
  * Converts a List of Trips into a JSON string using the Gson library.
  */
  private String convertTripListToJson(List<Trip> tripList) {
    Gson gson = new Gson();
    String json = gson.toJson(tripList);
    return json;
  }

  // Placeholder inner class for the Trip class.
  class Trip {

    public Trip() {}

    public static Trip buildTripFromEntity(Entity entity) {
      return new Trip();
    }

  }

}
