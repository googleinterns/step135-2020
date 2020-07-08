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

import com.google.sps.data.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
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

  // files for event 1
  private static final String GOLDEN_GATE_PARK = "GGPark";
  private static final String ADDRESS =  "4265 24th Street San Francisco, CA, 94114";

  private static final LocalDateTime DEF_LDT = LocalDateTime.of(LocalDate.parse("2020-06-25"), LocalTime.of(10, 0));

  @Test(expected = IllegalArgumentException.class)
  public void testcheckTravelTimeMinPossibleTime() {
    Event e = new Event (GOLDEN_GATE_PARK, ADDRESS, DEF_LDT, -1, HOUR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testcheckTravelTimeGreaterThanFullDay() {
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

    Assert.assertEquals("2020-06-25T10:00:00", Event.getProperDateFormat(
                                                            e.getStartTime()));
    Assert.assertEquals("2020-06-25T11:20:00", Event.getProperDateFormat(
                                                              e.getEndTime()));
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

    Assert.assertEquals("2020-06-25T10:00:00", Event.getProperDateFormat(
                                                            e.getStartTime()));
    Assert.assertEquals("2020-06-25T11:00:00", Event.getProperDateFormat(
                                                              e.getEndTime()));
  }
}
