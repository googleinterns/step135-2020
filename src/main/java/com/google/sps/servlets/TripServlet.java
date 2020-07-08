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

  private static final int HALF_HOUR = 30;

  // event fields to request
  private static final String NAME = "name";
  private static final String ADDRESS = "end-time";
  private static final String DATE = "date";
  private static final String START_TIME = "start-time";
  private static final String TRAVEL_TIME = "travel-time";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    Query query = new Query("events");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();

    // create the events
    for (Entity entity : results.asIterable()) {
    	String name = (String) entity.getProperty(NAME);
      String address = (String) entity.getProperty(ADDRESS);
      String startTime = (String) entity.getProperty(START_TIME);
      String endTime = (String) entity.getProperty(END_TIME);
      Event e = new Event(name, address, startTime, endTime);
      events.add(e);
    }   

    response.setContentType("application/json;");

    response.getWriter().println(convertToJson(events));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    // Print out params to site to verify retrieval of "start trip" user input.
    Enumeration<String> params = request.getParameterNames(); 

    // get date of trip
    String date = request.getParameter("inputDateOfTrvael");

    int count = 0;
    LocalTime startTime = LocalTime.of(10, 0);

    while (params.hasMoreElements()) {
      String p = params.nextElement();
      if (p.contains("poi")) {
        String address = request.getParameter(p);
        String name = address.split(",")[0];
        createEvent(name, address, date, startTime, HALF_HOUR)
      }
    }


    // chris's code to display elements
    while (params.hasMoreElements()) {
      String paramName = params.nextElement();
      response.getWriter().println(paramName + ": " + request.getParameter(paramName));
    }

    createEvents(request, response);
  }

  // creates the events and puts them in datastore
  private void createEvent(String name, String address, String date, int startTime, 
              int travelTime) {

    // create entity that posts events
    Entity eventEntity = new Entity("events");
    eventEntity.setProperty(NAME, name);
    eventEntity.setProperty(ADDRESS, address);
    eventEntity.setProperty(DATE, date);
    eventEntity.setProperty(START_TIME, startTime);
    evetnEntity.setProperty(TRAVEL_TIME, travelTime);

    // put entity in datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventEntity);

    // redirect to home page
    //response.sendRedirect("/");
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
