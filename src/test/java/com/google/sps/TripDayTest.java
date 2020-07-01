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

import com.google.sps.TripDay;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class TripDayTest {

  private static final String TIMES_SQUARE_ID = "ChIJmQJIxlVYwokRLgeuocVOGVU";
  private static final String CENTRAL_PARK_ID = "ChIJ4zGFAZpYwokRGUGph3Mf37k";
  private static final String WORLD_TRADE_ID = "ChIJy7cGfBlawokR5l2e93hsoEA";
  private static final String EMPIRE_STATE_ID = "ChIJtcaxrqlZwokRfwmmibzPsTU";
  private static final String HOTEL_ID = "ChIJ68J3tfpYwokR2HaRoBcB4xg";

  private ArrayList<String> locations;

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

  // Test TripDay constructor with null parameter
  @Test(expected = IllegalArgumentException.class)
  public void testTripDayConstructorNull() {
    TripDay tripDay = new TripDay(HOTEL_ID, HOTEL_ID, null);
  }

}
