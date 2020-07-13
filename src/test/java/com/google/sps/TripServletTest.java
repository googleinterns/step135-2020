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
import com.google.sps.data.Config;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;


@RunWith(JUnit4.class)
public final class TripServletTest {

  @Test
  public void testServlet() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    when(mockRequest.getParameter("inputTripName")).thenReturn("My Trip");
    when(mockRequest.getParameter("inputDestination")).
        thenReturn("The Westin Bellevue, Bellevue Way Northeast, Bellevue, WA, USA");
    when(mockRequest.getParameter("inputDayOfTravel")).thenReturn("2020-07-10");
    when(mockRequest.getParameterNames()).thenReturn(new String[]{ "poi-1", "poi-2", "poi-3"});
    when(mockRequest.getParameter("poi-1")).thenReturn("MoPOP, 5th Avenue North, Seattle, WA, USA");
    when(mockRequest.getParameter("poi-2")).thenReturn("Space Needle, Broad Street, Seattle, WA, USA");
    when(mockRequest.getParameter("poi-3")).thenReturn("Alki Beach, Seattle, WA, USA");

    DirectionsApi mockDirectionsApi = mock(DirectionsApi.class);

    GeoApiContext distCalcer = new GeoApiContext.Builder()
		    .apiKey(Config.API_KEY)
		    .build();
    
    when(mockDirectionsApi.newRequest(distCalcer)).thenReturn(new DirectionsApiRequest() )
  }

}


