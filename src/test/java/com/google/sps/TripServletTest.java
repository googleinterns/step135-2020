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
import com.google.sps.data.Config;
import com.google.sps.servlets.TripServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.mockito.ArgumentMatchers;

import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.Duration;
import com.google.maps.model.TravelMode;


// @RunWith(JUnit4.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest(DirectionsApi.class)
public final class TripServletTest {

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

    DirectionsApiRequest testRequest = TripServlet.generateDirectionsRequest(origin, waypoints, mockGeoApiContext);

    verify(mockRequest).origin(origin);
    verify(mockRequest).destination(origin);
    verify(mockRequest).waypoints(waypoints);
    verify(mockRequest).optimizeWaypoints(true);
    verify(mockRequest).mode(TravelMode.DRIVING);
  }

  @Test
  public void getOrderedWaypointsTest() {
    // Manually create directions result object
    DirectionsResult dirResult = new DirectionsResult();
    dirResult.routes = new DirectionsRoute[1];
    dirResult.routes[0] = new DirectionsRoute();
    dirResult.routes[0].waypointOrder = new int[]{ 1, 2, 0 };
    
    // Manually create user input
    List<String> pois = new ArrayList<>();
    pois.add("Alki Beach, Seattle, WA, USA");
    pois.add("MoPOP, 5th Avenue North, Seattle, WA, USA");
    pois.add("Space Needle, Broad Street, Seattle, WA, USA");
    
    List<String> orderedWaypoints = TripServlet.getOrderedWaypoints(dirResult, pois);

    List<String> expectedWaypointOrder = new ArrayList<>();
    expectedWaypointOrder.add("MoPOP, 5th Avenue North, Seattle, WA, USA");
    expectedWaypointOrder.add("Space Needle, Broad Street, Seattle, WA, USA");
    expectedWaypointOrder.add("Alki Beach, Seattle, WA, USA");

    Assert.assertEquals(orderedWaypoints, expectedWaypointOrder);
  }

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
    
    List<Integer> travelTimes  = TripServlet.getTravelTimes(dirResult);

    List<Integer> expectedTravelTimes = new ArrayList<>();
    expectedTravelTimes.add(18);
    expectedTravelTimes.add(1);
    expectedTravelTimes.add(25);
    expectedTravelTimes.add(35);

    Assert.assertEquals(travelTimes, expectedTravelTimes);
  }

  // public void testServlet() {
  //   HttpServletRequest mockRequest = mock(HttpServletRequest.class);
  //   HttpServletResponse mockResponse = mock(HttpServletResponse.class);

  //   when(mockRequest.getParameter("inputTripName")).thenReturn("My Trip");
  //   when(mockRequest.getParameter("inputDestination")).
  //       thenReturn("The Westin Bellevue, Bellevue Way Northeast, Bellevue, WA, USA");
  //   when(mockRequest.getParameter("inputDayOfTravel")).thenReturn("2020-07-10");
  //   when(mockRequest.getParameterNames()).thenReturn(new String[]{ "poi-1", "poi-2", "poi-3"});
  //   when(mockRequest.getParameter("poi-1")).thenReturn("MoPOP, 5th Avenue North, Seattle, WA, USA");
  //   when(mockRequest.getParameter("poi-2")).thenReturn("Space Needle, Broad Street, Seattle, WA, USA");
  //   when(mockRequest.getParameter("poi-3")).thenReturn("Alki Beach, Seattle, WA, USA");

  //   PowerMockito.mockStatic(DirectionsApi.class);

  //   GeoApiContext distCalcer = new GeoApiContext.Builder()
	// 	    .apiKey(Config.API_KEY)
	// 	    .build();
    
  //   DirectionsApiRequest mockDirectionsApiRequest = mock(DirectionsApiRequest.class);
  //   when(DirectionsApi.newRequest(distCalcer)).thenReturn(mockDirectionsApiRequest);


  // }

}


