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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.ArrayList; 
import java.util.List;
import java.util.Map;

/**
 * Solves the traveling Salesman problem by backtracking using DFS
 * Ensures most efficient route taking into account Opening Hours.
 * 
 * Note: `setIntToPlaceId()`, `setOpenHours()`, & `setTimeMatrix()` are used
 *    only for testing purposes and should be removed after mocking of Google 
 *    API objects is implemented in TspSolverTest.java
 */
public class TspSolver {

  // static list of weekdays
  private static final String[] WEEKDAYS = {"Monday", "Tuesday", "Wednesday", 
      "Thursday", "Friday", "Saturday", "Sunday"};

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
  Map<Integer, String> intToPlaceId;
  HashMap<Integer, OpeningHours.Period> openHours;
  int intOfWeek; // Sunday is 0, Saturday is 6

  // var to output
  Tuple finalAnswer;
 
  /**
   * Initializes context and intOfWeek, must call solver then getFinalAnswer(). 
   * 
   * @param context geoContext from TripServlet
   * @param intOfWeek day of week as an int (Sunday:0 - Saturday:6) from TripDay
   */
  public TspSolver(GeoApiContext context, int intOfWeek) {
    if (context == null) {
      throw new IllegalArgumentException("GeoApiContext is null");
    } else if (intOfWeek < 0 || intOfWeek > 6) {
      throw new IllegalArgumentException("intOfWeek not in valid range (0-6)");
    }

    // set class constants from TripServlet
    this.context = context;
    this.intOfWeek = intOfWeek;
  }

  /**
   * Finds the optimal path respecting open hours for the given POIs. After 
   * this method has returned the optimal path can be retrieved with 
   * getFinalAnswer() method
   * 
   * @param center central location
   * @param pois list of pois to visit
   */
  public void solver(String center, List<String> pois) throws IOException {
    if (pois == null) {
      throw new IllegalArgumentException("pois input list is null");
    } else if (pois.isEmpty()) {
      throw new IllegalArgumentException("pois input list is empty");
    } else if (center == null) {
      throw new IllegalArgumentException("center poi is null");
    } else if (center.isEmpty()) {
      throw new IllegalArgumentException("center poi is empty string");
    }

    // variables used for tracking
    LocalTime currentTime = START_TIME;
    List<Integer> startList = new ArrayList<>();
    startList.add(START_POS);
    Tuple cost = new Tuple(START_COST, startList);
    Tuple ans = new Tuple(START_ANS, new ArrayList<>());
    // total nodes: #locations + center & sets values to "false"
    boolean[] visited = new boolean[pois.size() + 1];

    // call function to set timeMatrix, intToPlaceId, & openHours
    createTimeMatrix(center, pois);
    populateIntMapAndOpenHours(center, pois);

    // set central visited node to visted
    visited[0] = true;

    // call recursive helper fuction to get final Answer
    this.finalAnswer = solverHelper(currentTime, 
        cost, ans, visited);
  }

  /**
   * Recursive helper function that is used to solve computational part of
   * algorithm. Called in global `solver()` method
   *
   * @param currPos represents the placeId of where the alg currently is
   * @param numNodes total number of locations to visit (includes center)
   * @param currentTime time if path at this point were taken 
   * @param count number of pois visited during this path
   * @param cost Tuple of current path (time cost, List of pois)
   * @param ans the current best solution found
   * @param array of which pois have been visited by this path
   */
  public Tuple solverHelper( LocalTime currentTime, 
      Tuple cost, Tuple ans, boolean[] visited) {

    int count = cost.getCurrPath().size();
    int currPos = cost.getCurrPath().get(count-1);
    int numNodes = visited.length;
    /**
     * If last node is reached and shares an edge w/ start node
     * Calculate min of cost and currAns
     * return and keep traversing the graph/matrix
     */
    if ((count == numNodes) && timeMatrix[currPos][0] > 0) {
      if (ans.getCurrAns() < cost.getCurrAns() + timeMatrix[currPos][0]) {
        return ans;
      } else {
        cost.incCurrAns(timeMatrix[currPos][0]);
        return cost;
      }
    }

    /**
     * Backtracking Step: loop through the time Matrix from currPos, increase
     * count by 1 and update (int) and (List) value of cost
     */
    for (int i = 0; i < numNodes; i++) {
      // set open boolean and time to add to currenTime
      boolean isOpen = travelPlusWaitTime(currentTime, i, currPos) != -1;
      int timeToAdd = travelPlusWaitTime(currentTime, i, currPos);
      
      // if node is unvisited and greater than 0, i.e. not the same node
      if (!visited[i] && timeMatrix[currPos][i] > 0 && isOpen) {
        // mark ith node as visited
        visited[i] = true;

        // update cost
        LocalTime timePostUpdate = currentTime.plusMinutes((long) timeToAdd);
        List<Integer> costPath = cost.getCurrPath();
        List<Integer> updatePath = new ArrayList<>();
        updatePath.addAll(costPath);
        updatePath.add(i);
        int newTupleCost = cost.getCurrAns() + timeToAdd - HOUR;
        Tuple updateTuple = new Tuple(newTupleCost, updatePath);

        // recursive call
        ans = solverHelper(timePostUpdate,
            updateTuple, ans, visited);

        // mark ith node as unvisited
        visited[i] = false;
      }
    }
    return ans;
  }

  /**
   * Check if a location is or will be open. Possibilities:
   * 1. Arrival before open time 
   * 2. Arrival during open hours
   * 3. Arrival after closed 
   * 4. Open Hours are not available
   * 
   * @return int of timeToAdd or (-1) if location is not open:
   *      - cases 1, 2, 4: amount of time added by action
   *      - case 3: -1
   * 
   * Details per case:
   * Case 1: the amount of time till it opens plus time spent (one hour)
   * Case 2 & 4: travel time plus time spent (one hour)
   * Case 3: (-1) to signify after closing
   */
  private int travelPlusWaitTime(LocalTime departureTime, int targetPoi,
      int departPoi) {
    // list of length 2 to return
    List<Object> output = new ArrayList<>();

    int timeAdded; // int to be set returned
      if (openHours.get(targetPoi) != null) {
        LocalTime open = openHours.get(targetPoi).open.time;
        LocalTime close = openHours.get(targetPoi).close.time;
        
        // edge case where close time is next day at 0:00
        if (close.compareTo(LocalTime.parse("00:00")) == 0) {
          close = LocalTime.of(23, 59);
        } else {
          close.minusMinutes(HOUR);
        }  

        // set time after travelTime
        LocalTime afterDrive = departureTime.plusMinutes(timeMatrix[departPoi][targetPoi]);

        /**
         * check if arrival time is before open time, during,
         * or after closing time
         */ 
        if (afterDrive.compareTo(open) < 0) {
          timeAdded = (int) afterDrive.until(open, ChronoUnit.MINUTES) + 
              timeMatrix[departPoi][targetPoi] + HOUR;
        } else if ((afterDrive.compareTo(open) >= 0) && 
            (afterDrive.compareTo(close) <= 0)) {
          timeAdded = timeMatrix[departPoi][targetPoi] + HOUR;
        } else {
          timeAdded = -1;
        }
      } else {
        timeAdded = timeMatrix[departPoi][targetPoi] + HOUR;
      }
      
      // return timeAdded
      return timeAdded;
  }

  /**
   * Create time Matrix using DirectionsRequests (DirectionsAPI)
   * 
   * @param center central location
   * @param pois list of pois other than central location
   */
  private void createTimeMatrix(String center, List<String> pois) 
      throws IOException {

    // create allPois to hold `center` and all other `pois`
    List<String> allPois = new ArrayList<>();
    allPois.add(center);
    allPois.addAll(pois);

    // create timeMatrix and fill diagonal with 0's
    timeMatrix = new int[allPois.size()][allPois.size()];
    for (int i = 0; i < allPois.size(); i++) {
      timeMatrix[i][i] = 0;
    }

    // fill (i, j) and (j, i) indices with travel time from i to j
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
   *
   * @param center central location
   * @param pois list of pois to visit
   */ 
  private void populateIntMapAndOpenHours(String center, 
      List<String> pois) throws IOException{
    this.openHours = new HashMap<>();
    this.intToPlaceId = new HashMap<>();
    this.intToPlaceId.put(0, center);
    setOpenHours(0, center);

    for (int i = 0; i < pois.size(); i++) {
      this.intToPlaceId.put(i+1, pois.get(i));
      setOpenHours(i+1, pois.get(i));
    }
  }

  /**
   * Sets the openHours for each placeId (as int), as an OpeningHours.Period
   * If can't find opening hours set to be null (represents open all day)
   * 
   * @param placeId placeId to search openHours for
   * @param index index to put the openHours in the HashMap
   */
  private void setOpenHours(int index, String placeId) throws IOException {
    PlaceDetails placeDetails = TripServlet.getPlaceDetailsFromPlaceId(this.context, placeId);

    if (placeDetails.openingHours == null) { 
      this.openHours.put(index, null);    
    } else if (placeDetails.openingHours.periods.length != 7) {
      checkDayIsPresentInHours(placeDetails.openingHours, index);
    } else {
      this.openHours.put(index, placeDetails.openingHours.periods[this.intOfWeek]);
    } 
  }

  /**
   * If day in openingHours array is the same as the day searched for 
   * check if open and close time are not null. If no hours found put null
   * into openHours (represents open all day)
   * 
   * @param openingHours openHours for placeId on desired day
   * @param index index to put result in OpenHours Map
   */
  private void checkDayIsPresentInHours(OpeningHours openingHours, int index) {
      // periods is numbered Sunday - Saturday, WeekdayText is Monday - Sunday
      int adjustedWeekIndex = (intOfWeek + 6) % 7;

      // string representation of day searched
      String desiredDayString = WEEKDAYS[adjustedWeekIndex];
      for (int i = 0; i < openingHours.periods.length; i++) {
        // get the day that the ith index in opening hours corresponds to
        String day = openingHours.weekdayText[i].split(":")[0];

        // check neither open nor closing time are null
        boolean open = openingHours.periods[i].open != null;
        boolean close = openingHours.periods[i].close != null;

        if (day.equals(desiredDayString) && open && close) {
          this.openHours.put(index, openingHours.periods[i]);
          return;
        } 
      }
    this.openHours.put(index, null);
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
   * Getter functions. All but `getFinalAnswer()` are used for testing
   */
  public int[][] getTimeMatrix() {
    return this.timeMatrix;
  }

  public Map<Integer, String> getPlaceIdToInt() {
    return this.intToPlaceId;
  }

  public HashMap<Integer, OpeningHours.Period> getOpenHours() {
    return this.openHours;
  }

  public Tuple getFinalAnswer() {
    return this.finalAnswer;
  }

  /**
   * These functions are exclusively used for testing the recursive part of 
   * the algorithm
   */
  public void setTimeMatrix(int[][] timeMatrix) {
    this.timeMatrix = timeMatrix;
  }

  public void setOpenHours(HashMap<Integer, OpeningHours.Period> openHours) {
    this.openHours = openHours;
  }

  public void setIntToPlaceId(HashMap<Integer, String> intToPlaceId) {
    this.intToPlaceId = intToPlaceId;
  }
}
