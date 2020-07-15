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

package com.google.sps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.servlets.TripServlet;
// import java.io.PrintWriter;
// import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public final class TripServletTest {

  // Address to return from inputDestination.
  private static final String INPUT_DESTINATION = 
    "4265 24th Street San Francisco, CA, 94114";
  private static final String INPUT_DATE = "2020-07-15";
  private static final String POI_ONE = "one";
  private static final String POI_TWO = "two";

  // create TripServlet object
  TripServlet tripServlet;

  // Add helper to allow datastore testing in local JUnit tests.
  // See https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting.
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initTripServlet() {
    tripServlet = new TripServlet();
  }

   @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testPutTripDayInDatastore() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("inputDestination")).thenReturn(INPUT_DESTINATION);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity tripDayEntity = tripServlet.putTripDayInDatastore(request, response, datastore, INPUT_DATE);

    Query query = new Query("trip-day");
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    Assert.assertEquals(1, listResults.size());
    Assert.assertEquals(listResults.get(0), tripDayEntity);
  }

  @Test
  public void testPutEventsInDatastore() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("poi-1")).thenReturn(POI_ONE);
    when(request.getParameter("poi-2")).thenReturn(POI_TWO);

    List<String> paramsList = new ArrayList<>();
    paramsList.add("inputDestination");
    paramsList.add("inputDayOfTravel");
    paramsList.add("inputTripName");
    paramsList.add("poi-1");
    paramsList.add("poi-2");
    Enumeration<String> params = Collections.enumeration(paramsList);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity tripDayEntity = new Entity("trip-day");
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", INPUT_DATE);

    datastore.put(tripDayEntity);

    tripServlet.putEventsInDatastore(request, response, params, tripDayEntity, INPUT_DATE, datastore);

    Query query = new Query("event");
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    Assert.assertEquals(2, listResults.size());
  }

}