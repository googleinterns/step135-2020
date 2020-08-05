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

package com.google.sps;

import com.google.sps.data.algorithm.Tuple;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.ArrayList; 
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** 
 * Tuple class to store the answer in the TspSolver
*/
@RunWith(JUnit4.class)
public final class TupleTest {

  private static final int CURR_ANS = 1;
  private static final int TOTAL_POIS = 5;

  // test that constructor assigns vars properly
  @Test
  public void testConstructorNorm() {
    List<Integer> path = new ArrayList<>();
    path.add(4);
    path.add(3);
    path.add(7);

    Tuple tuple = new Tuple(CURR_ANS, path);
    Assert.assertEquals(tuple.getCurrAns(), CURR_ANS);
    Assert.assertEquals(tuple.getCurrPath(), path);
  }

  // curr Ans cannot be less than 0
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorAnsBelowZero() {
    Tuple tuple = new Tuple(-1, new ArrayList<>());
  }

  // curr Ans cannot be less than 0
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNull() {
    Tuple tuple = new Tuple(CURR_ANS, null);
  }

  // test increment answer
  @Test
  public void testIncrementCurrAns() {
    int numToAdd = 2;
    Tuple tuple = new Tuple(CURR_ANS, new ArrayList<>());
    tuple.incCurrAns(numToAdd);

    Assert.assertEquals(tuple.getCurrAns(), CURR_ANS + numToAdd);
  }

  // error when trying to decrease curr Ans
  @Test(expected = IllegalArgumentException.class)
  public void testCannotDecrAns() {
    Tuple tuple = new Tuple(CURR_ANS, new ArrayList<>());
    tuple.incCurrAns(-1);
  }

  // test poi added not below 0
  @Test(expected = IllegalArgumentException.class)
  public void testAddPoiBelowZero() {
    Tuple tuple = new Tuple(CURR_ANS, new ArrayList<>());
    tuple.addIntPoiToPath(-1, TOTAL_POIS);
  }

  // test poi added not greater than total pois
  @Test(expected = IllegalArgumentException.class)
  public void testAddPoiGreaterThanTotal() {
    Tuple tuple = new Tuple(CURR_ANS, new ArrayList<>());
    tuple.addIntPoiToPath(6, TOTAL_POIS);
  }

  // test add pois to path
  @Test
  public void testAddPoisToPath() {
    Tuple tuple = new Tuple(CURR_ANS, new ArrayList<>());
    List<Integer> expected = new ArrayList<>();
    expected.add(1);
    expected.add(2);

    tuple.addIntPoiToPath(1, TOTAL_POIS);
    tuple.addIntPoiToPath(2, TOTAL_POIS);

    Assert.assertEquals(tuple.getCurrPath(), expected);
  }

  @Test
  public void testEqualsSame() {
    Tuple tuple = new Tuple(CURR_ANS, new ArrayList<>());

    Assert.assertTrue(tuple.areEqual(tuple));
  }

  @Test
  public void testEqualsDifEquals() {
    Tuple tuple1 = new Tuple(CURR_ANS, new ArrayList<>());
    tuple1.addIntPoiToPath(1, TOTAL_POIS);
    Tuple tuple2 = new Tuple(CURR_ANS, new ArrayList<>());
    tuple2.addIntPoiToPath(1, TOTAL_POIS);

    Assert.assertTrue(tuple1.areEqual(tuple2));
  }

  @Test
  public void testEqualsFalse() {
    Tuple tuple1 = new Tuple(CURR_ANS, new ArrayList<>());
    tuple1.addIntPoiToPath(1, TOTAL_POIS);
    Tuple tuple2 = new Tuple(CURR_ANS, new ArrayList<>());
    tuple2.addIntPoiToPath(2, TOTAL_POIS);

    Assert.assertFalse(tuple1.areEqual(tuple2));
  }
}