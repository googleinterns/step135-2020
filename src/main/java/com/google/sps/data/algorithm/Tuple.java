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


public class Tuple {

  // represents number of mins
  private int currAns; 
  private List<Integer> currPath; 

  public static final int ZERO = 0;
  
  public Tuple(int currAns, List<Integer> currPath) { 
    if (currAns < ZERO) {
      throw new IllegalArgumentException("current answer cannot be less than " + ZERO);
    } else if (currPath == null) {
      throw new IllegalArgumentException("pois list is null");
    }

    this.currAns = currAns; 
    this.currPath = currPath;
  }

  /**
   * increment current cost or answer
   */
  public void incCurrAns(int inc) {
    if (inc < ZERO) {
        throw new IllegalArgumentException("cannot increment by less than " + ZERO);
    }
    this.currAns += inc;
  }

  public void setCurrAns(int ans) {
    if (ans < ZERO) {
        throw new IllegalArgumentException("cannot increment by less than " + ZERO);
    }
    this.currAns = ans;  
  }

  /**
   * add int (represents string) to the current path
   */
  public void addIntPoiToPath(int addPoi, int numberOfPois) {
    if (addPoi < ZERO) {
      throw new IllegalArgumentException("cannot increment by less than " + ZERO);
    } else if (addPoi >= numberOfPois) {
      throw new IllegalArgumentException("index of Poi is greater than number of Pois: " + numberOfPois);  
    }
    this.currPath.add(addPoi);
  }

  /**
   * Checks structural equality of the two objects
   */
  public boolean areEqual(Tuple other) {
    if (other == this) {
        return true;
    }

    // check that currAns and currPath are the same
    return (other.getCurrPath().equals(this.currPath)) && 
        (other.getCurrAns() == this.currAns);
  }

  //getter methods
  public int getCurrAns() {
    return this.currAns;
  }

  public List getCurrPath() {
    return this.currPath;
  }
}