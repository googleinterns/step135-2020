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

  @Test
  public void testConstructorTimeSpent() {
    String goldenGatePark = "GGPark";
    String address =  "4265 24th Street San Francisco, CA, 94114";
    Event e = new Event(goldenGatePark, address, "2020-06-25", 1000, 80);

    Assert.assertEquals(goldenGatePark, e.getName());
    Assert.assertEquals(address, e.getAddress());
    Assert.assertEquals("2020-06-25", e.getDate());
    Assert.assertEquals(1000, e.getStartTime());
    Assert.assertEquals(1120, e.getEndTime());

    Assert.assertEquals("2020-06-25T10:00:00", e.getStrStartTime());
    Assert.assertEquals("2020-06-25T11:20:00", e.getStrEndTime());
  }
}