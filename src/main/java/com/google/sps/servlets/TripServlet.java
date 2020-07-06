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
import java.util.Enumeration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/calculate-trip")
public class TripServlet extends HttpServlet {

  // event fields to request
  private static final String NAME = "name";
  private static final String ADDRESS = "end-time";
  private static final String START_TIME = "start-time";
  private static final String END_TIME = "end-time";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    Query query = new Query("input");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Event> events = new ArrayList<>();

    // create the events
    for (Entity entity : results.asIterable()) {
    	String name = (String) entity.getProperty(NAME);
      String address = (String) entity.getProperty(ADDRESS);
      String startTime = (String) entity.getProperty(START_TIME);
      String endTime = (String) entity.getProperty(END_TIME);
      Event e = new Event(name, address, null);
      comments.add(com);
    }   

  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    // Print out params to site to verify retrieval of "start trip" user input.
    Enumeration<String> params = request.getParameterNames(); 
    while (params.hasMoreElements()) {
      String paramName = params.nextElement();
      response.getWriter().println(paramName + ": " + request.getParameter(paramName));
    }
  }


  private void createEvents(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    // hard coded events till user class is done
    String hardCodedName = "Golden Gate Bridge";
    String hardCodedAddress = "Golden Gate Bridge, Golden Gate Bridge, San Francisco, CA" 
    String hardCodedStart = "2020-06-29T09:00:00";
    String hardCodedEnd = "2020-06-29T10:00:00";

  }

}
