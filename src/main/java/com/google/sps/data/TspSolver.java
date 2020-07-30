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

package com.google.sps.data;

import java.time.LocalTime;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList; 

public class TspSolver {

  Double[][] distanceMatrix;
  boolean[] visited;
  int currPos;
  int numNodes;
  HashMap<int, String> placeIdToInt;
  LocalTime currentTime;
  HashMap<int, List<LocalTime>> openHours;
  int count;
  int cost;
  Pair<int, List<int>> ans;

  /**
   * Create Distance Matrix using DistanceMatrixAPI
   * 
   * @param pois list of pois, 0 is in start and stop point
   */
  public void createDistanceMatrix(ArrayList<String> pois) {

  }

}
