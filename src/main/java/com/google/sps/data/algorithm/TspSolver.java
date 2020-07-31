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

public class TspSolver {

  // variables to create before alg
  Double[][] distanceMatrix;
  boolean[] visited;
  HashMap<Integer, List<LocalTime>> openHours;

  // variables used for tracking
  int currPos;
  int numNodes;
  HashMap<Integer, String> placeIdToInt;
  LocalTime currentTime;
  int count;
  int cost;
  Tuple ans;

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

}
