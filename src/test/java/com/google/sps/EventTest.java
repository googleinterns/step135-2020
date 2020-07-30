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
import com.google.sps.data.Event;
import com.google.sps.data.TripDay;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class EventTest {

  // time class constants
  private final static int HOUR = 60;
  private final static int HALF_HOUR = 30;
  private final static int NINETY_MIN = 90;
  private final static int HOUR_FIFTY_FIVE = 115;
  private final static int THREE_HOURS = 180;

  // constants for tripDay entity
  private static final String INPUT_DESTINATION = 
      "4265 24th Street San Francisco, CA, 94114";
  private static final String INPUT_DATE = "2020-07-15";

  // constants for event
  private static final String GOLDEN_GATE_PARK = "GGPark";
  private static final String ADDRESS =  "4265 24th Street San Francisco, CA, 94114";

  private static final LocalDateTime DEF_LDT = LocalDateTime.of(LocalDate.parse("2020-06-25"), LocalTime.of(10, 0));

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

   @Before
  public void setUp() {
    helper.setUp();
  }

   @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTravelTimeBelowMinPossible() {
    Event e = new Event (GOLDEN_GATE_PARK, ADDRESS, DEF_LDT, -1, HOUR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckTravelTimeGreaterThanFullDay() {
    Event e = new Event (GOLDEN_GATE_PARK, ADDRESS, DEF_LDT, 1440, HOUR);
  }

  @Test
  public void testConstructorManualTimeSpent() {
    // clarifying inputs
    int timeAtLocation = 80;
    LocalDateTime manualEndTime = LocalDateTime.of(
                          LocalDate.parse("2020-06-25"), LocalTime.of(11, 20));

    Event e = new Event (GOLDEN_GATE_PARK, ADDRESS, DEF_LDT, HALF_HOUR, 
                        timeAtLocation);

    Assert.assertEquals(GOLDEN_GATE_PARK , e.getName());
    Assert.assertEquals(ADDRESS, e.getAddress());
    Assert.assertEquals(DEF_LDT, e.getStartTime());
    Assert.assertEquals(manualEndTime, e.getEndTime());
    Assert.assertEquals(HALF_HOUR, e.getTravelTime());

    Assert.assertEquals("2020-06-25T10:00:00", 
              DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(e.getStartTime()));
    Assert.assertEquals("2020-06-25T11:20:00", 
              DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(e.getEndTime()));
  }

   @Test
  public void testConstructorDefaultTimeSpent() {
    // clarifying inputs
    LocalDateTime manualEndTime = LocalDateTime.of(
                          LocalDate.parse("2020-06-25"), LocalTime.of(11, 0));

    Event e = new Event (GOLDEN_GATE_PARK, ADDRESS, DEF_LDT, HALF_HOUR);

    Assert.assertEquals(GOLDEN_GATE_PARK , e.getName());
    Assert.assertEquals(ADDRESS, e.getAddress());
    Assert.assertEquals(DEF_LDT, e.getStartTime());
    Assert.assertEquals(manualEndTime, e.getEndTime());
    Assert.assertEquals(HALF_HOUR, e.getTravelTime());

    Assert.assertEquals("2020-06-25T10:00:00", 
              DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(e.getStartTime()));
    Assert.assertEquals("2020-06-25T11:00:00", 
              DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(e.getEndTime()));
  }

   @Test
  public void testEventToEntityCorrectParent() {
    // initialize datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // build tripDay Entity
    Entity tripDayEntity = new Entity(TripDay.QUERY_STRING);
    tripDayEntity.setProperty("origin", INPUT_DESTINATION);
    tripDayEntity.setProperty("destination", INPUT_DESTINATION);
    tripDayEntity.setProperty("date", INPUT_DATE);
    datastore.put(tripDayEntity);

    // build eventEntity using fcn
    Event e = new Event (GOLDEN_GATE_PARK, ADDRESS, DEF_LDT, HALF_HOUR);
    Entity eventEntity = e.eventToEntity(tripDayEntity.getKey());
    datastore.put(eventEntity);

    // get results from datastore w/ tripDay entity as parent
    Query query = new Query(Event.QUERY_STRING, tripDayEntity.getKey());
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    Assert.assertEquals(eventEntity, listResults.get(0));
  }

   @Test
  public void testEventFromEntity() {
    // create eventEntity
    Entity eventEntity = new Entity(Event.QUERY_STRING);
    eventEntity.setProperty("name", GOLDEN_GATE_PARK);
    eventEntity.setProperty("address", ADDRESS);
    eventEntity.setProperty("start-time", 
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(DEF_LDT));
    eventEntity.setProperty("travel-time", Integer.toString(HALF_HOUR));

    // create event w same fields for comparison
    Event expected = new Event (GOLDEN_GATE_PARK, ADDRESS, DEF_LDT, HALF_HOUR);
    Event actual = Event.eventFromEntity(eventEntity);
 
    Assert.assertEquals(expected.getName(), actual.getName());
    Assert.assertEquals(expected.getAddress(), actual.getAddress());
    Assert.assertEquals(expected.getStartTime(), actual.getStartTime());
    Assert.assertEquals(expected.getEndTime(), actual.getEndTime());
    Assert.assertEquals(expected.getTravelTime(), actual.getTravelTime());
  }
}
