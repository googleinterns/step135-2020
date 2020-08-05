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

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.OpeningHours;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.TravelMode;
import com.google.sps.data.algorithm.TspSolver;
import com.google.sps.data.algorithm.Tuple;
import java.io.IOException;
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
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.*;

/** */
@RunWith(JUnit4.class)
public final class TspSolverTest {

  // variables for timeMatrixHostel
  private static final int MONDAY_INT = 1;
  private static final String HOSTEL_ID = "ChIJb-VpERaHhYARLSiOPmNcZzE";
  private static final String UNI_ID = "ChIJgeLABbB9j4AR00VqlJ98eqU";
  private static final String CAFE_ID = "ChIJgSqBnZaHhYARhZdyjXrqU-E";
  private static final String LOUNGE_ID = "ChIJy6k6HhGHhYAR5moxQxcAv-w";

  private static final String DUTCH_WINDMILL_ID =  "ChIJyzM7ebmHhYARKXUIhJF_VOI";
  private static final String MANDARIN_REST_ID = "ChIJI8kEiXp9j4ARByCiwa0PF-Q";
  private static final String CITY_COLLEGE_ID = "ChIJ32IQ69R9j4ARubnrNf2KXk8";
  private static final String BILLY_HILL_ID = "ChIJKfERrm9-j4AROcxSCHx2gE0";
  private static final String FABLE_REST_ID = "ChIJ369T0Rp-j4ARftWt7DRMdo4";
  
  private static List<String> pois;

  @Test
  public void testPopulateIntMap() throws IOException {
    GeoApiContext mockGeoApiContext = mock(GeoApiContext.class);
    TspSolver tsp = new TspSolver(mockGeoApiContext, MONDAY_INT);

    pois = constructPoisList();

    //tsp.solver(GEARY_HOTEL_ID, pois);
  }

  /**
   * Construct the pois list used for all testing
   */
  private List<String> constructPoisList() {
    pois = new ArrayList<>();
    pois.add(CAFE_ID);
    pois.add(LOUNGE_ID);
    pois.add(UNI_ID);
    return pois;
  }

  // creates a time matrix where hostel is central location
  private int[][] createTimeMatrixHostel() {
    int[][] timeMatrix = new int[4][4];

    for (int i = 0; i < timeMatrix.length; i++) {
      timeMatrix[i][i] = 0;
    }

    // hostel to lounge
    int hostelToLounge = 16;
    timeMatrix = setIndices(1, 0, hostelToLounge, timeMatrix);

    // hostel to cafe
    int hostelToCafe = 23;
    timeMatrix = setIndices(2, 0, hostelToCafe, timeMatrix);

    // hostel to university
    int hostelToUni = 17;
    timeMatrix = setIndices(2, 0, hostelToUni, timeMatrix);

    // cafe to lounge
    int cafeToLounge = 10;
    timeMatrix = setIndices(1, 2, cafeToLounge, timeMatrix);

    // cafe to university
    int cafeToUni = 11;
    timeMatrix = setIndices(1, 3, cafeToUni, timeMatrix);

    // lounge to university
    int loungeToUni = 15;
    timeMatrix = setIndices(2, 3, loungeToUni, timeMatrix);

    return timeMatrix;
  }

  // sets a value at (i, j) and (j, i) of the timeMatrix
  private int[][] setIndices(int i, int j, int value, int[][] timeMatrix) {
    timeMatrix[i][j] = value;
    timeMatrix[j][i] = value;
    return timeMatrix;
  }

  private Map<Integer, String> createIntToPlaceIdHostel() {
    Map<Integer, String> intToPlaceId = new HashMap<>();
    intToPlaceId.put(0, HOSTEL_ID);
    intToPlaceId.put(1, CAFE_ID);
    intToPlaceId.put(2, LOUNGE_ID);
    intToPlaceId.put(3, UNI_ID);
    return intToPlaceId;
  }

  private HashMap<Integer, OpeningHours.Period> createOpenHours() {
    HashMap<Integer, OpeningHours.Period> openHours = new HashMap<>();

    // set hostel hours
    OpeningHours.Period hostelHours = null;
    openHours.put(0, hostelHours);

    // set cafe hours
    OpeningHours.Period cafeHours = new new OpeningHours.Period();
    cafeHours.open.time = LocalTime.of(7, 0);
    cafeHours.close.time = LocalTime.of(17, 0);
    openHours.put(1, cafeHours);

    // set lounge hours
    OpeningHours.Period loungeHours = new new OpeningHours.Period();
    loungeHours.open.time = LocalTime.of(11, 0);
    loungeHours.close.time = LocalTime.of(19, 30);
    openHours.put(2, loungeHours);

    // set university hours
    OpeningHours.Period uniHours = null;
    openHours.put(3, uniHours);

    return openHours;
  }
}