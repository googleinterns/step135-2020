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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.sps.data.Event;
import com.google.sps.TripDay;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/get-calendar")
public class TripServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // will add logic so that the Trip is gotten first and then all the tripDays
    Query tripDayQuery = new Query("trip-day");
    PreparedQuery tripDayResults = datastore.prepare(tripDayQuery);

    List<Event> events = new ArrayList<>();

    for (Entity tripDayEntity : tripDayResults.asIterable()) {
      Query eventsQuery = new Query("event", tripDayEntity.getKey());
      PreparedQuery eventResults = datastore.prepare(eventsQuery);

      for (Entity eventEntity : eventREsults.asIterable()) {
        events.add(Event.eventFromEntity(entity));
      }
    }
    
    response.getWriter().println(convertToJson(events));
  }

  /**
   * Make the servlet cleaner
   * Iterate through the entities and create the events and write them to json
   *
  private void eventDoGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException { 
    Query query = new Query("event");

    
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();

    // create the events
    for (Entity entity : results.asIterable()) {
      events.add(Event.eventFromEntity(entity));
    }   

    response.getWriter().println(convertToJson(events));
   }

  private void getTripDayEntity(DatastoreService datastore) {
    Query query = new Query("trip-day");
    PreparedQuery tripDayResults = datastore.prepare(query);

    List<TripDay> tripDays = new ArrayList<>();
    
    // create tripDays, for MVP will be just one
    for (Entity entity : tripDayResults.asIterable()) {
      tripDays.add(TripDay.tripDayFromEntity(entity));
    }*/
  }