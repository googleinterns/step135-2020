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
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.sps.data.Event;
import com.google.sps.Trip;
import com.google.sps.TripDay;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/get-edit-content")
public class EditServlet extends HttpServlet {

  private final String TRIP_KEY_PARAM = "tripKey";
  private final String KEY_FILTER_NAME = "__key__";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {

    response.setContentType("application/json;");

    // Initialize datastore object.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Get current user, redirect to homepage if no user exists.
    Entity userEntity = AuthServlet.getCurrentUserEntity();
    if (userEntity == null) {
      response.sendRedirect("/");
      return;
    }

    // Get tripKey, redirect to trips page if trip key is invalid.
    String stringTripKey = request.getParameter(TRIP_KEY_PARAM);
    if (stringTripKey == null) {
      response.getWriter().println("Trip key is not present.");
      response.sendRedirect("/trips/");
      return;
    }
    Key tripKey = KeyFactory.stringToKey(stringTripKey);

    // If no trip is returned, redirect to trips page.
    Entity tripEntity = getTripFromTripKey(userEntity, tripKey, datastore);
    if (tripEntity == null) {
      response.sendRedirect("/trips/");
    }

    // Construct part of the trip as JSON.

    // Get the Trip Day list; if none are returned, redirect to trips page.
    List<Entity> tripDayList = getTripDaysFromTrip(tripEntity.getKey(), datastore);
    if (tripDayList == null || tripDayList.isEmpty()) {
      response.sendRedirect("/trips/");
    }

    // Construct the trip days and events after

    // Get the events, and put them in a map relating the date to the Event list.
    Map<String, List<Event>> dateEventMap = new HashMap<>();
    for (Entity tripDay : tripDayList) {
      List<Event> eventList = getEventsFromTripDay(tripDay.getKey(), datastore);
      // TODO: I don't know if this is necessary.
      if (eventList == null) {
        eventList = new ArrayList<>();
      }
      Collections.sort(eventList);

      // Create the dateString and add the event list.
      LocalDate localDate = LocalDate.parse((String) tripDay.getProperty(TripDay.DATE));
      String dateString = 
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, localDate.getDayOfWeek().toString())
        + ", " + localDate.getMonthValue() + "/" + localDate.getDayOfMonth() + "/" 
        + localDate.getYear();
      dateEventMap.put(dateString, eventList);
    }

    // Create a custom object to hold the Trip, TripDay, and Event information.
    // Use this object to create a JSON response which is returned.
    EditTrip editTripObject = 
      new EditTrip(Trip.buildTripFromEntity(tripEntity), dateEventMap);
    String json = convertToJson(editTripObject);
    response.getWriter().println(json);
  }

  /**
   * Get the Trip Entity from Datastore using the current user as the ancestor
   * and the trip key. If no trip exists, this will return null, meaning the 
   * trip is not under the current user or the trip does not exist.
   * 
   * @param userEntity The Entity object of the current user.
   * @param tripKey The Key of the trip being searched for.
   * @param datastore The datastore object to use when searching for the trip.
   * @return The Trip Entity that is under the current user and matches the Trip Key.
   */
  private Entity getTripFromTripKey(Entity userEntity, Key tripKey, 
    DatastoreService datastore) {

    // Construct Filter and Query to get all trips with the current user ancestor.
    Filter tripKeyFilter =
      new FilterPredicate(KEY_FILTER_NAME, FilterOperator.EQUAL, tripKey);
    Query tripQuery = new Query(Trip.TRIP, userEntity.getKey());
    tripQuery.setFilter(tripKeyFilter);
    PreparedQuery tripResults = datastore.prepare(tripQuery);

    // Only one Trip Entity can match the provided key.
    Entity tripEntity = tripResults.asSingleEntity();
    
    // If no Trip Entity is present, this will return null.
    return tripEntity;
  }

  /**
   * Get the TripDay Entity objects from datastore that are under the passed-in
   * Trip Entity Key.
   * 
   * @param tripKey The Key of the trip used as an ancestor of the TripDay objects.
   * @param datastore The datastore object to use when searching for the TripDay objects.
   * @return The List of TripDay Entity objects under the passed-in Trip Key.
   */
  private List<Entity> getTripDaysFromTrip(Key tripKey, DatastoreService datastore) {
    // Construct Query to get all TripDays with the Trip ancestor.
    Query tripDayQuery = new Query(TripDay.QUERY_STRING, tripKey);
    PreparedQuery tripDayResults = datastore.prepare(tripDayQuery);

    // Return the list of TripDay Entity objects under the provided Trip.
    return tripDayResults.asList(FetchOptions.Builder.withDefaults());
  }

  /**
   * Get the Event objects from datastore that are under the passed-in TripDay 
   * Entity Key.
   * 
   * @param tripDayKey The Key of the TripDay used as an ancestor of the Event objects.
   * @param datastore The datastore object to use when searching for the Event objects.
   * @return The List of Event objects under the passed-in TripDay Key.
   */
  private List<Event> getEventsFromTripDay(Key tripDayKey, DatastoreService datastore) {
    // Construct Query to get all Events with the TripDay ancestor.
    Query eventQuery = new Query(Event.QUERY_STRING, tripDayKey);
    PreparedQuery eventResults = datastore.prepare(eventQuery);

    // Return the list of Event Entity objects under the provided TripDay.
    List<Entity> eventEntityList = eventResults.asList(FetchOptions.Builder.withDefaults());

    // Convert Event Entity list to an Event List.
    List<Event> eventList = new ArrayList<>();
    for (Entity eventEntity : eventEntityList) {
      eventList.add(Event.eventFromEntity(eventEntity));
    }
    return eventList;
  }

  /**
   * Converts an object to String JSON form.
   */
  private String convertToJson(Object object) {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    return json;
  }

  /**
   * A custom class used to hold the Trip and Event information.
   */
  private class EditTrip {
    private Trip trip;
    private Map<String, List<Event>> dateEventMap;

    public EditTrip(Trip trip, Map<String, List<Event>> dateEventMap) {
      this.trip = trip;
      this.dateEventMap = dateEventMap;
    }
  }
}
