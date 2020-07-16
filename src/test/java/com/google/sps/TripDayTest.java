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

import com.google.appengine.api.datastore.Entity;
import com.google.sps.TripDay;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class TripDayTest {

  // locations and strings to be added
  private ArrayList<String> locations;
  private static final String TIMES_SQUARE_ID = "ChIJmQJIxlVYwokRLgeuocVOGVU";
  private static final String CENTRAL_PARK_ID = "ChIJ4zGFAZpYwokRGUGph3Mf37k";
  private static final String WORLD_TRADE_ID = "ChIJy7cGfBlawokR5l2e93hsoEA";
  private static final String EMPIRE_STATE_ID = "ChIJtcaxrqlZwokRfwmmibzPsTU";
  private static final String HOTEL_ID = "ChIJ68J3tfpYwokR2HaRoBcB4xg";

  private static final String DATE = "2020-07-08";

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

    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, locations, DATE);

    Assert.assertEquals(tripDay.getOrigin(), HOTEL_ID);
    Assert.assertEquals(tripDay.getDestination(), HOTEL_ID);
    Assert.assertEquals(tripDay.getLocations(), locations);
    Assert.assertEquals(tripDay.getDate(), DATE);
  }

  // Test TripDay constructor with null origin
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorNullOrigin() {
    locations = new ArrayList<>();
    locations.add(TIMES_SQUARE_ID);

    TripDay tripDay = new TripDay(null, HOTEL_ID, locations, DATE);
  }

  // Test TripDay constructor with null parameter
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorNullDestination() {
    locations = new ArrayList<>();
    locations.add(TIMES_SQUARE_ID);

    TripDay tripDay = new TripDay(HOTEL_ID, null, locations, DATE);
  }

  // Test TripDay constructor with null locations
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorNullLocations() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, null, DATE);
  }

  // Test TripDay null date
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorNullDate() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, locations, null);
  }

  // Test TripDay empty date
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorEmptyDate() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, locations, "");
  }

   @Test
  public void testBuildEntityAndFromEntity() {
    // create TripDay
    locations = new ArrayList<>();
    TripDay tripDayOriginal = new TripDay(HOTEL_ID, HOTEL_ID, locations, DATE);

    // create entity from TripDay
    Entity tripDayEntity = tripDayOriginal.buildEntity();

    // convert back to TripDay object 
    TripDay tripDayAfterMethods = TripDay.tripDayFromEntity(tripDayEntity);

    Assert.assertEquals(tripDayOriginal.getOrigin(), tripDayAfterMethods.getOrigin());
    Assert.assertEquals(tripDayOriginal.getDestination(), tripDayAfterMethods.getDestination());
    Assert.assertEquals(tripDayOriginal.getLocations(), tripDayAfterMethods.getLocations());
    Assert.assertEquals(tripDayOriginal.getDate(), tripDayAfterMethods.getDate());
  }
}
