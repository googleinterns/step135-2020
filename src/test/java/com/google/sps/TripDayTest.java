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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.TripDay;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.ArrayList;
import java.util.List;


@RunWith(JUnit4.class)
public final class TripDayTest {

  // Test place IDs
  private static final String TIMES_SQUARE_ID = "ChIJmQJIxlVYwokRLgeuocVOGVU";
  private static final String CENTRAL_PARK_ID = "ChIJ4zGFAZpYwokRGUGph3Mf37k";
  private static final String WORLD_TRADE_ID = "ChIJy7cGfBlawokR5l2e93hsoEA";
  private static final String EMPIRE_STATE_ID = "ChIJtcaxrqlZwokRfwmmibzPsTU";
  private static final String HOTEL_ID = "ChIJ68J3tfpYwokR2HaRoBcB4xg";

  // constants for entity construction
  private static final String LOCATION_ENTITY_TYPE = "location";
  private static final String NAME = "name";
  private static final String ORDER = "order";

  // constants for tripDay entity
  private static final String INPUT_DATE = "2020-07-15";

  private ArrayList<String> locations;

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

  // Test TripDay constructor and get functions
  @Test
  public void testTripDayConstructor() {
    locations = new ArrayList<>();
    locations.add(TIMES_SQUARE_ID);
    locations.add(CENTRAL_PARK_ID);
    locations.add(WORLD_TRADE_ID);
    locations.add(EMPIRE_STATE_ID);

    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, locations);

    Assert.assertEquals(tripDay.getOrigin(), HOTEL_ID);
    Assert.assertEquals(tripDay.getDestination(), HOTEL_ID);
    Assert.assertEquals(tripDay.getLocations(), locations);
  }

  // Test TripDay constructor with null origin
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorNullOrigin() {
    locations = new ArrayList<>();
    locations.add(TIMES_SQUARE_ID);

    TripDay tripDay = new TripDay(null, HOTEL_ID, locations);
  }

  // Test TripDay constructor with null parameter
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorNullDestination() {
    locations = new ArrayList<>();
    locations.add(TIMES_SQUARE_ID);

    TripDay tripDay = new TripDay(HOTEL_ID, null, locations);
  }

  // Test TripDay constructor with null locations
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorNullLocations() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, null);
  }

  // Test the locationsToEntities function
  @Test
  public void testLocationsToEntities() {
    locations = new ArrayList<>();
    locations.add(TIMES_SQUARE_ID);
    locations.add(CENTRAL_PARK_ID);

    // build tripDay Entity (parent)
    Entity tripDayEntity = new Entity("trip-day");
    tripDayEntity.setProperty("origin", HOTEL_ID);
    tripDayEntity.setProperty("destination", HOTEL_ID);
    tripDayEntity.setProperty("date", INPUT_DATE);
    Key testKey = tripDayEntity.getKey();

    List<Entity> actualEntities = TripDay.locationsToEntities(locations, testKey);

    // build manual list of expected entities based on hard coded locations list
    List<Entity> expectedEntities = new ArrayList<>();
    Entity TimesSquareEntity = new Entity(LOCATION_ENTITY_TYPE, testKey);
    TimesSquareEntity.setProperty(NAME, TIMES_SQUARE_ID);
    TimesSquareEntity.setProperty(ORDER, 0);
    expectedEntities.add(TimesSquareEntity);
    Entity CentralParkEntity = new Entity(LOCATION_ENTITY_TYPE, testKey);
    CentralParkEntity.setProperty(NAME, CENTRAL_PARK_ID);
    CentralParkEntity.setProperty(ORDER, 1);
    expectedEntities.add(CentralParkEntity);

    // Check that the entities are generated in the correct order with the correct properties
    Assert.assertEquals(expectedEntities.size(), actualEntities.size());
    Assert.assertEquals(expectedEntities.get(0).getProperty(NAME), actualEntities.get(0).getProperty(NAME));
    Assert.assertEquals(expectedEntities.get(0).getProperty(ORDER), actualEntities.get(0).getProperty(ORDER));
    Assert.assertEquals(expectedEntities.get(1).getProperty(NAME), actualEntities.get(1).getProperty(NAME));
    Assert.assertEquals(expectedEntities.get(1).getProperty(ORDER), actualEntities.get(1).getProperty(ORDER));
  }

  // Test the storing of location entities in datastore.
  @Test
  public void testStoreLocationsInDatastore() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // build tripDay Entity (parent)
    Entity tripDayEntity = new Entity("trip-day");
    tripDayEntity.setProperty("origin", HOTEL_ID);
    tripDayEntity.setProperty("destination", HOTEL_ID);
    tripDayEntity.setProperty("date", INPUT_DATE);

    // put parent entity in datastore and get key
    datastore.put(tripDayEntity);
    Key testKey = tripDayEntity.getKey();

    // build list of location entities
    List<Entity> expectedEntities = new ArrayList<>();
    Entity TimesSquareEntity = new Entity(LOCATION_ENTITY_TYPE, testKey);
    TimesSquareEntity.setProperty(NAME, TIMES_SQUARE_ID);
    TimesSquareEntity.setProperty(ORDER, 0);
    expectedEntities.add(TimesSquareEntity);
    Entity CentralParkEntity = new Entity(LOCATION_ENTITY_TYPE, testKey);
    CentralParkEntity.setProperty(NAME, CENTRAL_PARK_ID);
    CentralParkEntity.setProperty(ORDER, 1);
    expectedEntities.add(CentralParkEntity);

    TripDay.storeLocationsInDatastore(expectedEntities, datastore);

    // query datastore to ensure that correct entities were stored
    Query query = new Query(LOCATION_ENTITY_TYPE, testKey);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> actualEntities = results.asList(FetchOptions.Builder.withDefaults());

    Assert.assertEquals(expectedEntities.size(), actualEntities.size());
    Assert.assertEquals(expectedEntities.get(0), actualEntities.get(0));
    Assert.assertEquals(expectedEntities.get(1), actualEntities.get(1));
  }
}
