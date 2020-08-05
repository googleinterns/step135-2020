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
 * Checks opening hours for locations visited
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
  HashMap<Integer, String> intToPlaceId;
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
    System.out.println(intOfWeek);
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
    Tuple cost = new Tuple(START_COST, new ArrayList<>());
    Tuple ans = new Tuple(START_ANS, new ArrayList<>());
    boolean[] visited = new boolean[numNodes]; // sets values to "false"  

    // TODO: call functions to set these values and the solver
    createTimeMatrix(center, pois);
    populateIntMapAndOpenHours(center, pois);

    
    visited[0] = true;
    this.finalAnswer = solverHelper(currPos, numNodes, currentTime, count, cost, ans, visited);
  }

  public Tuple solverHelper(int currPos, int numNodes, LocalTime currentTime, 
      int count, Tuple cost, Tuple ans, boolean[] visited) {
    System.out.println("\n" + "currCost: " + cost.toString() + " currCount: " + count);
    /**
     * If last node is reached and shares an edge w/ start node
     * Calculate min of cost and currAns
     * return and keep traversing the graph/matrix
     */
    if ((count == numNodes-1) && timeMatrix[currPos][0] > 0) {
      System.out.println("in return statement");
      if (ans.getCurrAns() < cost.getCurrAns() + timeMatrix[currPos][0]) {
        return ans;
      } else {
        cost.incCurrAns(timeMatrix[currPos][0]);
        return cost;
      }
    }

    for (int i = 0; i < numNodes; i++) {

      boolean isOpen = (boolean) checkIsOpenSetTime(currentTime, i,
          currPos).get(0);
      int timeToAdd = (int) checkIsOpenSetTime(currentTime, i,
          currPos).get(1);
      
      System.err.println("Open: " + isOpen);
      System.err.println("visited node: " + visited[i]);
      System.err.println("currPos: " + currPos + " i: " + i + " timeMat: " + timeMatrix[currPos][i]);
      System.err.println("placeId: " + intToPlaceId.get(i));
      System.err.println("CurrentTime: " + currentTime.toString());
      System.err.println("openHours: " + openHours.get(i) + "\n");

      // if node is unvisited and greater than 0, i.e. not the same node
      if (!visited[i] && timeMatrix[currPos][i] > 0 && isOpen) {
        System.err.println("in Here");
        visited[i] = true;
        LocalTime timePostUpdate = currentTime.plusMinutes((long) timeToAdd);
        //System.err.println("currenTime: in if statement " + timePostUpdate);
        List<Integer> costPath = cost.getCurrPath();
        List<Integer> updatePath = new ArrayList<>();
        updatePath.addAll(costPath);
        updatePath.add(i);
        int newTupleCost = cost.getCurrAns() + timeToAdd - HOUR;
        Tuple updateTuple = new Tuple(newTupleCost, updatePath);
        System.out.println("recursive call next");
        ans = solverHelper(i, numNodes, timePostUpdate, count + 1, 
            updateTuple, ans, visited);
        System.out.println("moveing on");
        visited[i] = false;
      }
    }
    return ans;
  }

  /**
   * Check if a location is open when traveled there. Options are:
   * 1. Arrival before open time: 
   * 2. Arrival during open hours
   * 3. Arrival after closed 
   * 4. Open Hours are not available
   * 
   * @return List<Object> at index 0 will return a boolean, index 1
   * will return the amount of time to add to current time. 
   * 1. If before open time then add the amount of time till it opens
   *   plus time spent (one hour).
   * 2. If during opening hours or open hours are not available return 
   *  travel time plus one hour
   * 2. If arrival after closed return [false, 0]
   */
  private List<Object> checkIsOpenSetTime(LocalTime currentTime, int i,
      int currPos) {
    List<Object> output = new ArrayList<>();
    boolean isOpen;
      if (openHours.get(i) != null) {
        LocalTime open = openHours.get(i).open.time;
        LocalTime close = openHours.get(i).close.time;
        
        // edge case where close time is next day at 0:00
        if (close.compareTo(LocalTime.parse("00:00")) == 0) {
          close = LocalTime.of(23, 59);
        } else {
          close.minusMinutes(HOUR);
        }  

        // set time after travelTime
        LocalTime afterDrive = currentTime.plusMinutes(timeMatrix[currPos][i]);

        /**
         * check if arrival time is before open time, during,
         * or after closing time
         */ 
        if (afterDrive.compareTo(open) < 0) {
          int timeTillOpen = (int) afterDrive.until(open, ChronoUnit.MINUTES);
          output.add(true);
          output.add(timeTillOpen + timeMatrix[currPos][i] + HOUR);
        } else if (isOpen = (afterDrive.compareTo(open) >= 0) && 
            (afterDrive.compareTo(close) <= 0)) {
          output.add(true);
          output.add(timeMatrix[currPos][i] + HOUR);
        } else {
          output.add(false);
          output.add(0);
        }
      } else {
        output.add(true);
        output.add(timeMatrix[currPos][i] + HOUR);
      }

      return output;
  }

  /**
   * Create time Matrix using DirectionsRequest
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
   * Sets the openHours for each placeId (as int), as an OpeningHours.Period[]
   * If can't find opening hours set to be null (this is handled as all day by 
   * solver)
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
   * if day in openingHours array is the same as the day searched for 
   * check if open and close time are not null
   */
  private void checkDayIsPresentInHours(OpeningHours openingHours, int index) {
      int adjustedWeekIndex;
      // periods is numbered Sunday - Saturday, WeekdayText is Monday - Sunday
      if (intOfWeek == 0) {
        adjustedWeekIndex = 6;
      } else {
        adjustedWeekIndex = intOfWeek - 1;
      }
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

  // getter functions
  public int[][] getTimeMatrix() {
    return this.timeMatrix;
  }

  public HashMap<Integer, String> getPlaceIdToInt() {
    return this.intToPlaceId;
  }

  public HashMap<Integer, OpeningHours.Period> getOpenHours() {
    return this.openHours;
  }

  public Tuple getFinalAnswer() {
    return this.finalAnswer;
  }

  /**
   * these functions are exclusively used for testing the recursive part of 
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
