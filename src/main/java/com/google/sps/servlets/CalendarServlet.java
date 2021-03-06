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

/**
 * Servlet that gets events from datastore and writes them to json
 * used from frontend to create events
 */
@WebServlet("/get-calendar")
public class CalendarServlet extends HttpServlet {

  /**
   * Adam TODO: put functionality into a Utility class!
   */

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

      // gets the events from datastore and writes them to .../get-calendar
      doGetEvents(response, datastore, userEntity, tripKey);
    }
  }

  /**
   * Gets the events and prints them to writer, necessary break up for testing
   */
  public void doGetEvents(HttpServletResponse response, 
      DatastoreService datastore, Entity userEntity, Key tripEntityKey) throws IOException {
  
    Filter tripKeyFilter =
      new FilterPredicate("__key__", FilterOperator.EQUAL, tripEntityKey);
    Query tripQuery = new Query(Trip.TRIP, userEntity.getKey());
    tripQuery.setFilter(tripKeyFilter);

    PreparedQuery tripResults = datastore.prepare(tripQuery);
    Entity tripEntity = tripResults.asSingleEntity();

    Query tripDayQuery = new Query(TripDay.QUERY_STRING, tripEntity.getKey());
    PreparedQuery tripDayResults = datastore.prepare(tripDayQuery);

    List<Event> events = new ArrayList<>();

    // iterate through all the TripDays and get events for each one
    for (Entity tripDayEntity : tripDayResults.asIterable()) {
      Query eventsQuery = new Query(Event.QUERY_STRING, tripDayEntity.getKey());
      PreparedQuery eventResults = datastore.prepare(eventsQuery);

      for (Entity eventEntity : eventResults.asIterable()) {
        events.add(Event.eventFromEntity(eventEntity));
      }
    }
    response.getWriter().println(convertToJson(events));
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


