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

import java.util.ArrayList; 
import java.util.List;

public class Tuple<Integer, List<Integer>> {

  private Integer currAns; 
  private List<Integer> currPath; 

  public static final Integer ZERO = 0;
  
  public Tuple(Integer currAns, ArrayList<Integer> currPath) { 
    if (currAns < ZERO) {
      throw new IllegalArgumentException("current answer cannot be less than " + ZERO);
    } else if (pois.isEmpty()) {
      throw new IllegalArgumentException("Pois input list is empty");
    }

    this.currAns = currAns; 
    this.currPath = currPath;
  }

  /**
   * increment current cost or answer
   */
  public void incrementCurrAnswer(int inc) {
    if (inc < ZERO) {
        throw new IllegalArgumentException("cannot increment by less than " + ZERO);
    }
    this.currAns += inc;
  }

  /**
   * add int (represents string) to the current path
   */
  public void addIntPoiToPath(int inc) {
    if (inc < ZERO) {
        throw new IllegalArgumentException("cannot increment by less than " + ZERO);
    }
    this.currAns += inc;
  }

  /**
   * Checks structural equality of the two objects
   */
  @Override
  public boolean equals(Object other) {
    if (other == this) {
        return true;
    }

    if (!(other instanceof Tuple)){
        return false;
    }

    // cast to Tuple class
    Tuple<Integer, List<Integer>> otherTuple = (Tuple<Integer, List<Integer>> ) other;

    // check that currAns and currPath are the same
    return (otherTuple.getCurrPath().equals(this.currPath)) && 
        (otherTuple.getCurrAns == this.currAns);

  }

  //getter methods
  public int getCurrAns() {
    return this.currAns;
  }

  public List<Integer> getCurrPath() {
    return this.currPath;
  }
}