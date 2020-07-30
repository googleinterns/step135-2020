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

  public final Integer currAns; 
  public final List<Integer> currPath; 

  public static final Integer ZERO = 0;
  
  public Tuple(Integer currAns, ArrayList<Integer> currPath) { 
    if (currAns < 0) {
      throw new IllegalArgumentException("current Answer cannot be less than " + ZERO);
    } else if (pois.isEmpty()) {
      throw new IllegalArgumentException("Pois input list is empty");
    }

    this.currAns = currAns; 
    this.currPath = currPath;
  }

  public void incrementAnswer(int inc) {
    this.currAns += inc;
  }
}