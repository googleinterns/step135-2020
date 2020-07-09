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

package com.google.sps.servlets;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.NotFoundException;
import com.google.maps.model.AddressType;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.GeocodedWaypointStatus;
import com.google.maps.model.LatLng;
import com.google.maps.model.TrafficModel;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TransitRoutingPreference;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import com.google.maps.errors.ApiException;
import com.google.maps.DirectionsApiRequest;

import com.google.sps.Trip;
import com.google.sps.TripDay;

import java.util.ArrayList;
import java.util.List;

@WebServlet("/calculate-trip")
public class TripServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    // Print out params to site to verify retrieval of "start trip" user input.
    Enumeration<String> params = request.getParameterNames();

    String tripName = request.getParameter("inputTripName");
    String origin = request.getParameter("inputDestination");
    String startDate = request.getParameter("inputDayofTravel");
    List<String> pois = new ArrayList<>(); 
    while (params.hasMoreElements()) {
      String paramName = params.nextElement();
      pois.add(request.getParameter(paramName));
    }

   	GeoApiContext distCalcer = new GeoApiContext.Builder()
		    .apiKey("AIzaSyCmQyeeWI_cV0yvh1SuXYGoLej3g_D9NbY")
		    .build();

    DirectionsApiRequest directionsRequest = DirectionsApi.newRequest(distCalcer)
        .origin(origin)
        .destination(origin)
        .waypoints("Charlestown,MA", "Lexington,MA")
        .optimizeWaypoints(true)
        .mode(TravelMode.DRIVING);

    try {
      DirectionsResult dirResult = directionsRequest.await();
    } catch (ApiException | InterruptedException e) {
      throw new IOException(e);
    } 

    TripDay newTripDay = new TripDay(origin, origin, pois);
    List<TripDay> newTripDays = new ArrayList<>();
    newTripDays.add(newTripDay);
    Trip newTrip = new Trip(tripName, startDate, newTripDays);
  }

}
