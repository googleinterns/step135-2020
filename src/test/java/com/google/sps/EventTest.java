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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class EventTest {

  // class constants
  private final static int HOUR_AT_CURRENT_POI = 60;

  // files for event 1
  private static final String GOLDENGATEPARK = "GGPark";
  private static final String ADDRESS =  "4265 24th Street San Francisco, CA, 94114";
  private static final String DATE = "2020-06-25";
  private static final int START_TIME = 1000;
  private static final int DURATION_IN_MIN = 80;
  private static final String STR_START_TIME = "2020-06-25T10:00:00";
  private static final String STR_MANUAL_END_TIME = "2020-06-25T11:20:00";
  private static final String STR_DEF_END_TIME = "2020-06-25T11:00:00";

  
  @Test
  public void testConstructorManualTimeSpent() {
    Event e1 = new Event(GOLDENGATEPARK, ADDRESS, DATE, START_TIME, DURATION_IN_MIN);

    Assert.assertEquals(GOLDENGATEPARK, e1.getName());
    Assert.assertEquals(ADDRESS, e1.getAddress());
    Assert.assertEquals(DATE, e1.getDate());
    Assert.assertEquals(START_TIME, e1.getStartTime());
    Assert.assertEquals(e1.calculateEndTime(DURATION_IN_MIN), e1.getEndTime());

    Assert.assertEquals(STR_START_TIME, e1.getStrStartTime());
    Assert.assertEquals(STR_MANUAL_END_TIME, e1.getStrEndTime());
  }

   @Test
  public void testConstructorDefaultTimeSpent() {
    Event e1 = new Event(GOLDENGATEPARK, ADDRESS, DATE, START_TIME);

    Assert.assertEquals(GOLDENGATEPARK, e1.getName());
    Assert.assertEquals(ADDRESS, e1.getAddress());
    Assert.assertEquals(DATE, e1.getDate());
    Assert.assertEquals(START_TIME, e1.getStartTime());
    //Assert.assertEquals(1100, e1.getEndTime());

    Assert.assertEquals(STR_START_TIME, e1.getStrStartTime());
    //Assert.assertEquals(STR_DEF_END_TIME, e1.getStrEndTime());
  }
}