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
import com.google.maps.model.OpeningHours;
import com.google.maps.model.PlaceDetails;
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
  private static final int HOUR = 60;

  private GeoApiContext context;

  // variables to create before alg
  int[][] timeMatrix;
  HashMap<Integer, String> placeIdToInt;
  HashMap<Integer, OpeningHours.Period> openHours;
  int intOfWeek; // Sunday is 0, Saturday is 6

  // var to output
  Tuple finalAnswer;
 
  /**
   * Initializes context and intOfWeek, must call solver then getFinalAnswer(). Done for testing purposes
   * 
   * @param context geoContext from TripServlet
   * @param intOfWeek day of week as an int (Sunday:0 through Saturday:6) from TripDay
   */
  public TspSolver(GeoApiContext context, int intOfWeek) {
    // set class constants from TripServlet
    this.context = context;
    this.intOfWeek = intOfWeek;
  }

  /**
   * Solves the problem, initializes various variables needed to solve, sets finalAnswer
   * 
   * @param center central location
   * @param pois list of pois to visit
   */
  public void solver(String center, List<String> pois) throws IOException {
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
    populateIntMapAndOpenHours(center, pois);

    
    visited[0] = true;
    this.finalAnswer = solverHelper(currPos, numNodes, currentTime, count, cost, ans, visited);
  }

  private Tuple solverHelper(int currPos, int numNodes, LocalTime currentTime, 
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
      LocalTime open = openHours.get(i).open.time;
      LocalTime close = openHours.get(i).close.time;
      close.minusMinutes(HOUR);
    
      // check if location is open at currentTime along path
      boolean isOpen = (currentTime.compareTo(open) >= 0) && 
          (currentTime.compareTo(close) <= 0);

      // if node is unvisited and greater than 0, i.e. not the same node
      if (!visited[i] && timeMatrix[currPos][i] > 0 && isOpen) {
        visited[i] = true;
        currentTime.plusHours((long) 1); // ADD TRAVELTIME, SHOULD DIST MATRIX BE TIME??
        currentTime.plusMinutes((long) timeMatrix[currPos][i]);
        ans = solverHelper(i, numNodes, currentTime, count + 1, 
            cost + timeMatrix[currPos][i], ans, visited);
        visited[i] = false;
      }
    }
    return ans;
  }

  /**
   * Create time Matrix using DistanceMatrixAPI
   * 
   * @param center central location
   * @param pois list of pois other than central location
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

    timeMatrix = new int[allPois.size()][allPois.size()];
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
   * Populate placeIdToInt and call setOpenHours for each placeId

   */ 
  private HashMap<Integer, String> populateIntMapAndOpenHours(String center, 
      List<String> pois) throws IOException{
    HashMap<Integer, String> placeIdToInt = new HashMap<>();
    placeIdToInt.put(0, center);
    setOpenHours(0, center);

    for (int i = 0; i < pois.size(); i++) {
      placeIdToInt.put(i+1, pois.get(i));
      setOpenHours(i+1, pois.get(i));
    }

    return placeIdToInt;
  }

  /**
   * Sets the openHours for each placeId (as int), as an OpeningHours.Period[]
   * 
   * @param placeId placeId to search openHours for
   * @param index index to put the openHours in the HashMap
   */
  private void setOpenHours(int index, String placeId) throws IOException {
    PlaceDetails placeDetails = TripServlet.getPlaceDetailsFromPlaceId(this.context, placeId);
    this.openHours.put(index, placeDetails.openingHours.periods[this.intOfWeek]);
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

  // getter functions
  public int[][] getTimeMatrix() {
    return this.timeMatrix;
  }

  public HashMap<Integer, String> getPlaceIdToInt() {
    return this.placeIdToInt;
  }

  public HashMap<Integer, OpeningHours.Period> getOpenHours() {
    return this.openHours;
  }

  public Tuple getFinalAnswer() {
    return this.finalAnswer;
  }
}
