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

  private static final int MONDAY_INT = 1;
  private static final String GEARY_HOTEL_ID = "ChIJb-VpERaHhYARLSiOPmNcZzE";
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

    DirectionsApiRequest mockRequest = PowerMockito.mock(DirectionsApiRequest.class);
    
    // Create the FindPlaceFromText object with a valid place ID.
    FindPlaceFromText findPlaceResult = new FindPlaceFromText();
    findPlaceResult.candidates = new PlacesSearchResult[2];
    findPlaceResult.candidates[0] = new PlacesSearchResult();
    findPlaceResult.candidates[0].placeId = GEARY_HOTEL_ID;
    findPlaceResult.candidates[1] = new PlacesSearchResult();
    findPlaceResult.candidates[1].placeId = FABLE_REST_ID;
    findPlaceResult.candidates[2] = new PlacesSearchResult();
    findPlaceResult.candidates[2].placeId = CITY_COLLEGE_ID;
    findPlaceResult.candidates[3] = new PlacesSearchResult();
    findPlaceResult.candidates[3].placeId = BILLY_HILL_ID;
    findPlaceResult.candidates[4] = new PlacesSearchResult();
    findPlaceResult.candidates[4].placeId = DUTCH_WINDMILL_ID;
    findPlaceResult.candidates[5] = new PlacesSearchResult();
    findPlaceResult.candidates[5].placeId = MANDARIN_REST_ID;

    // Have the findPlaceRequest.await() method return the FindPlaceFromText
    // object using PowerMockito, as await() is a final method.
    PowerMockito.when(findPlaceRequest.await()).thenReturn(findPlaceResult);

    // Mock the PlacesApi object.
    PowerMockito.mockStatic(PlacesApi.class);
    when(PlacesApi.findPlaceFromText(any(), anyString(), any()))
      .thenReturn(findPlaceRequest);

    // Construct directionsResult object
    DirectionsResult expectedResult = new DirectionsResult();
    expectedResult.routes = new DirectionsRoute[1];
    expectedResult.routes[0] = new DirectionsRoute();

    tsp.solver(GEARY_HOTEL_ID, pois);
  }

  /**
   * Construct the pois list used for all testing
   */
  private List<String> constructPoisList() {
    pois = new ArrayList<>();
    pois.add(FABLE_REST_ID);
    pois.add(CITY_COLLEGE_ID);
    pois.add(BILLY_HILL_ID);
    pois.add(DUTCH_WINDMILL_ID);
    pois.add(MANDARIN_REST_ID);
    return pois;
  }
}