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

  // temporary vars
  private LocalDateTime startDateTime;
  private static int count = 0;

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
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");
    
    // do get for events
    eventDoGet(request, response);
  }

  // function to set time, need to do only once
  public void setDateTime(String date) {
    if (startDateTime == null) {
      startDateTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.of(10, 0));
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    response.setContentType("application/json;");

    // do post for events
    eventDoPost(request, response);
    
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

    // set startDateTime, will be removed
      setDateTime(date);

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

      // redirect to home page
      response.sendRedirect("/");
    }
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
