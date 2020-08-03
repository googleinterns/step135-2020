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

package com.google.sps.data.algorithm;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.google.sps.data.algorithm.Tuple;
import com.google.sps.servlets.TripServlet;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.ArrayList; 
import java.util.List;
import java.util.Map;

/**
 * Solves the traveling Salesman problem by backtracking using DFS
 * Checks opening hours for locations visited
 */
public class TspSolver {

  // class constants
  private static final int START_POS = 0;
  private static final int START_COUNT = 0;
  private static final int START_COST = 0;
  private static final int START_ANS = Integer.MAX_VALUE;
  private static final LocalTime START_TIME = LocalTime.of(10, 0);

  private GeoApiContext context;

  // variables to create before alg
  // DISTANCE SHOULD PROBS BE AN AMOUNT OF TIME RIGHT? YES ANSWER TO MY OWN Q
  int[][] timeMatrix;
  HashMap<Integer, String> placeIdToInt;
  HashMap<Integer, List<LocalTime>> openHours;

  // var to output
  Tuple finalAnswer;
 
  
  public TspSolver(String center, List<String> pois, GeoApiContext context) 
      throws IOException {
    // set GeoApiContext to be same as TripServlet
    this.context = context;

    // variables used for tracking
    int currPos = START_POS;
    int numNodes = pois.size() + 1; // total nodes: #locations & center
    LocalTime currentTime = START_TIME;
    int count = START_COUNT;
    int cost = START_COST;
    Tuple ans = new Tuple(START_ANS, new ArrayList<>());
    boolean[] visited = new boolean[numNodes]; // sets values to "false"  

    // TODO: call functions to set these values and the solver
    createTimeMatrix(center, pois);
    // TODO: create openHours
    
    visited[0] = true;
    this.finalAnswer = solver(currPos, numNodes, currentTime, count, cost, ans, visited);
  }

  private Tuple solver(int currPos, int numNodes, LocalTime currentTime, 
      int count, int cost, Tuple ans, boolean[] visited) {
    /**
     * If last node is reached and shares an edge w/ start node
     * Calculate min of cost and currAns
     * return and keep traversing the graph/matrix
     */
    // QUESTION: DO I ADD THE CENTER TO THE PATH AS WELL? CHECK W/ EHIKA
    if ((count == numNodes) && timeMatrix[currPos][0] > 0) {
      int min = Math.min(ans.getCurrAns(), cost + timeMatrix[currPos][0]);
      ans.setCurrAns(min);
      return ans;
    }

    for (int i = 0; i < numNodes; i++) {
      // if node is unvisited and greater than 0, i.e. not the same node
      if (!visited[i] && timeMatrix[currPos][i] > 0 /**&& CHECK IF IS OPEN!!!!!!*/) {
        visited[i] = true;
        currentTime.plusHours((long) 1); // ADD TRAVELTIME, SHOULD DIST MATRIX BE TIME??
        ans = solver(i, numNodes, currentTime, count + 1, 
            cost + timeMatrix[currPos][i], ans, visited);
        visited[i] = false;
      }
    }
    return ans;
  }

  /**
   * Create time Matrix using DistanceMatrixAPI
   * 
   * @param pois list of pois, 0 is in start and stop point
   */
  private void createTimeMatrix(String center, List<String> pois) 
      throws IOException {
    if (pois == null) {
      throw new IllegalArgumentException("Pois input list is null");
    } else if (pois.isEmpty()) {
      throw new IllegalArgumentException("Pois input list is empty");
    } else if (center == null) {
      throw new IllegalArgumentException("center poi is null");
    }
    // TODO

    List<String> allPois = new ArrayList<>();
    allPois.add(center);
    allPois.addAll(pois);

    // fill timeMatrix diagonal with 0's
    for (int i = 0; i < allPois.size(); i++) {
      timeMatrix[i][i] = 0;
    }

    for (int i = 0; i < allPois.size(); i++) {
      for (int j = i; j < allPois.size(); j++) {
        int travelTime = getTravelTimeMins(allPois.get(i), allPois.get(j));
        timeMatrix[i][j] = travelTime;
        timeMatrix[j][i] = travelTime;
      }
    }
  }

  /**
   * Get travel time in seconds between two locations
   * 
   * @param origin start location
   * @param destination end location
   * @return time in seconds to travel from start to end
   */
  private int getTravelTimeMins(String origin, String destination) 
      throws IOException {
    // generate directions request w/ origin, destination by driving
    DirectionsApiRequest directionsRequest = DirectionsApi.newRequest(this.context)
        .originPlaceId(origin)
        .destinationPlaceId(destination)
        .mode(TravelMode.DRIVING);

    // get directions Result from directionsRequest
    DirectionsResult dirResult = TripServlet.getDirectionsResult(directionsRequest);

    // get the time in minutes of the first the trip
    DirectionsLeg leg  = dirResult.routes[TripServlet.ROUTE_INDEX].legs[0];
    int travelTime = (int) leg.duration.inSeconds / TripServlet.SECONDS_IN_MIN;
    return travelTime;
  }

  /**
   * Populate placeIdToInt
   */ 
  private HashMap<Integer, String> populatePlaceIdToInt(String center, String[] pois) {
    HashMap<Integer, String> placeIdToInt = new HashMap<>();
    placeIdToInt.put(0, center);

    for (int i = 1; i <= pois.length; i++) {
      placeIdToInt.put(i, pois[i]);
    }

    return placeIdToInt;
  }

  // getter functions
  public int[][] getTimeMatrix() {
    return this.timeMatrix;
  }

  public HashMap<Integer, String> getPlaceIdToInt() {
    return this.placeIdToInt;
  }

  public HashMap<Integer, List<LocalTime>> getOpenHours() {
    return this.openHours;
  }
}
