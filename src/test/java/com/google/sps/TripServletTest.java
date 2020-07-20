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

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.Duration;
import com.google.maps.model.TravelMode;
import com.google.sps.servlets.TripServlet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DirectionsApi.class, DirectionsApiRequest.class})
public final class TripServletTest {

  // Test directionsRequest generation with mocks.
  @Test
  public void generateDirectionsRequestTest() {
    PowerMockito.mockStatic(DirectionsApi.class);
    DirectionsApiRequest mockRequest = mock(DirectionsApiRequest.class);
    when(DirectionsApi.newRequest(any())).thenReturn(mockRequest);
    when(mockRequest.origin(anyString())).thenReturn(mockRequest);
    when(mockRequest.destination(anyString())).thenReturn(mockRequest);
    when(mockRequest.waypoints(ArgumentMatchers.<String>any())).thenReturn(mockRequest);
    when(mockRequest.optimizeWaypoints(anyBoolean())).thenReturn(mockRequest);
    when(mockRequest.mode(any())).thenReturn(mockRequest);

    GeoApiContext mockGeoApiContext = mock(GeoApiContext.class);

    String[] waypoints = new String[]{ "MoPOP, 5th Avenue North, Seattle, WA, USA",
                                    "Space Needle, Broad Street, Seattle, WA, USA",
                                    "Alki Beach, Seattle, WA, USA"};
    
    String origin = "The Westin Bellevue, Bellevue Way Northeast, Bellevue, WA, USA";

    DirectionsApiRequest request = TripServlet.generateDirectionsRequest(origin, origin, waypoints, mockGeoApiContext);

    // Verify that proper methods and parameters are called to generate directionsRequest.
    verify(mockRequest).origin(origin);
    verify(mockRequest).destination(origin);
    verify(mockRequest).waypoints(waypoints);
    verify(mockRequest).optimizeWaypoints(true);
    verify(mockRequest).mode(TravelMode.DRIVING);
  }

  // Test waypoint order parsing from a DirectionsResult object.
  @Test
  public void getOrderedWaypointsTest() {
    // Manually create directions result object.
    DirectionsResult dirResult = new DirectionsResult();
    dirResult.routes = new DirectionsRoute[1];
    dirResult.routes[0] = new DirectionsRoute();
    dirResult.routes[0].waypointOrder = new int[]{ 1, 2, 0 };
    
    // Manually create user input.
    String[] pois = new String[]{"Alki Beach, Seattle, WA, USA",
                                  "MoPOP, 5th Avenue North, Seattle, WA, USA",
                                  "Space Needle, Broad Street, Seattle, WA, USA"};

    List<String> orderedWaypoints = TripServlet.getOrderedWaypoints(dirResult, pois);

    List<String> expectedWaypointOrder = new ArrayList<>();
    expectedWaypointOrder.add("MoPOP, 5th Avenue North, Seattle, WA, USA");
    expectedWaypointOrder.add("Space Needle, Broad Street, Seattle, WA, USA");
    expectedWaypointOrder.add("Alki Beach, Seattle, WA, USA");

    Assert.assertEquals(orderedWaypoints, expectedWaypointOrder);
  }

  // Test travel time parsing from a DirectionsResult object.
  @Test
  public void getTravelTimesTest() {
    // Manually create directions result object
    DirectionsResult dirResult = new DirectionsResult();
    dirResult.routes = new DirectionsRoute[1];
    dirResult.routes[0] = new DirectionsRoute();
    dirResult.routes[0].legs = new DirectionsLeg[4];
    for (int i = 0; i < dirResult.routes[0].legs.length; i++) {
      dirResult.routes[0].legs[i] = new DirectionsLeg();
      dirResult.routes[0].legs[i].duration = new Duration();
    }
    dirResult.routes[0].legs[0].duration.inSeconds = 1080;
    dirResult.routes[0].legs[1].duration.inSeconds = 60;
    dirResult.routes[0].legs[2].duration.inSeconds = 1500;
    dirResult.routes[0].legs[3].duration.inSeconds = 2100;
    
    List<Integer> actualTravelTimes  = TripServlet.getTravelTimes(dirResult);

    List<Integer> expectedTravelTimes = new ArrayList<>();
    expectedTravelTimes.add(18);
    expectedTravelTimes.add(1);
    expectedTravelTimes.add(25);
    expectedTravelTimes.add(35);

    Assert.assertEquals(actualTravelTimes, expectedTravelTimes);
  }

  // Test getting DirectionsResult object from DirectionsRequest
  @Test
  public void getDirectionsResultTest() throws Exception {
    DirectionsApiRequest mockRequest = PowerMockito.mock(DirectionsApiRequest.class);

    // Construct directionsResult object
    DirectionsResult expectedResult = new DirectionsResult();
    expectedResult.routes = new DirectionsRoute[1];
    expectedResult.routes[0] = new DirectionsRoute();
    expectedResult.routes[0].waypointOrder = new int[]{ 1, 2, 0 };

    PowerMockito.when(mockRequest.await()).thenReturn(expectedResult);

    DirectionsResult actualResult = TripServlet.getDirectionsResult(mockRequest);

    Assert.assertEquals(expectedResult.routes[0].waypointOrder, actualResult.routes[0].waypointOrder);
  }
}
