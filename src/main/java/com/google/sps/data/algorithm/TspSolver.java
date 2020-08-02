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

import com.google.sps.data.algorithm.Tuple;
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

  private static final int START_POSITION = 0;
  private static final int START_COUNT = 0;
  private static final LocalTime START_Time = LocalTime.of(10, 0);

  // variables to create before alg
  // DISTANCE SHOULD PROBS BE AN AMOUNT OF TIME RIGHT? YES ANSWER TO MY OWN Q
  Double[][] distanceMatrix;
  HashMap<Integer, String> placeIdToInt;
  HashMap<Integer, List<LocalTime>> openHours;

 
  
  public TspSolver(String center, String[] pois) {

    // variables used for tracking
    int currPos;
    int numNodes;
    LocalTime currentTime;
    int count;
    int cost;
    Tuple ans;
    boolean[] visited;

    // TODO: call functions to set these values and the solver

  }

  private Tuple solver(int currPos, int numNodes, LocalTime currentTime, 
      int count, int cost, Tuple ans, boolean[] visited) {
    /**
     * If last node is reached and shares an edge w/ start node
     * Calculate min of cost and currAns
     * return and keep traversing the graph/matrix
     */
    // QUESTION: DO I ADD THE CENTER TO THE PATH AS WELL? CHECK W/ EHIKA
    if (count == numNodes) && distanceMatrix[currPos][0] > 0) {
      int min = Math.min(ans.getCurrAns(), cost + distanceMatrix[currPos][0]]);
      ans.setCurrAns(min);
      return ans;
    }

    for (int i = 0; i < numNodes; i++) {
      // if node is unvisited and greater than 0, i.e. not the same node
      if (!visited[i] && graph[currPos][i] > 0 /**&& CHECK IF IS OPEN!!!!!!*/) {
        visited[i] = true;
        currenTime.plusHours((long) 1); // ADD TRAVELTIME, SHOULD DIST MATRIX BE TIME??
        ans = solver(i, numNodes, currentTime, count + 1, cost + graph[currPos][i], 
            ans, visited);
        visited[i] = false;
      }
    }
    return ans;
  }

  /**
   * Create Distance Matrix using DistanceMatrixAPI
   * 
   * @param pois list of pois, 0 is in start and stop point
   */
  private void createDistanceMatrix(ArrayList<String> pois) {
    if (pois == null) {
      throw new IllegalArgumentException("Pois input list is null");
    } else if (pois.isEmpty()) {
      throw new IllegalArgumentException("Pois input list is empty");
    }
    // TODO
  }

  /**
   * Create empty visited array
   */
  private void createVisitedAllFalse(int size) {
    this.visited = new boolean[size];
  }

  /**
   * Calculate number of locations
   */
  private void setNumberOfLocations(int size) {
    this.numNodes = size;
  }

  /**
   * Populate placeIdToInt
   */ 
  private void populatePlaceIdToInt(String center, String[] pois) {
    this.placeIdToInt = new HashMap<>();
    this.placeIdToInt.put(0, center);

    for (int i = 1; i <= pois.length; i++) {
      this.placeIdToInt.put(i, pois[i]);
    }
  }

  // getter functions
  public Double[][] getDistanceMatrix() {
    return this.distanceMatrix;
  }

  public HashMap<Integer, String> getPlaceIdToInt() {
    return this.placeIdToInt;
  }

  public boolean[] getVisited() {
    return this.visited;
  }

  public HashMap<Integer, List<LocalTime>> getOpenHours() {
    return this.openHours;
  }

  public int getCurrPos() {
    return this.currPos;
  }

  public int getNumNodes() {
    return this.numNodes;
  }

  public LocalTime getCurrentTime() {
    return this.currentTime;
  }

  public int getCount() {
    return this.count;
  }

  public int getCost() {
    return this.cost;
  }

  public Tuple getAns() {
    return this.ans;
  }
}
