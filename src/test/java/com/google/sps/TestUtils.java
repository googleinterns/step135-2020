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
 
package com.google.sps.servlets;

import static org.mockito.Mockito.*;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.google.sps.data.Utility;
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

/**
 * This class tests the methods in the Utility.java file.
 * This is different from UtilityTest.java, as that provides useful functions
 * for the testing methods.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FindPlaceFromTextRequest.class,PlacesApi.class})
public class TestUtils {

  // Create Utility object.
  private Utility utility;

  @Before
  public void initUtility() {
    utility = new Utility();
  }

  // Constants to pass into PlacesApi methods.
  private static final String TEXT_LOCATION_SEARCH = "Big Island, Hawaii, USA";
  private static final String PLACE_ID = "ChIJWTr3xcHnU3kRNIHX-ZKkVRQ";

  @Test
  public void testGetPlaceIdFromTextSearchCandidatesPresent() throws Exception {
    // Mock the GeoApiContext object to be passed into PlacesApi methods,
    // and the FindPlaceFromTextRequest.
    GeoApiContext mockGeoApiContext = mock(GeoApiContext.class);
    FindPlaceFromTextRequest findPlaceRequest = 
      PowerMockito.mock(FindPlaceFromTextRequest.class);

    // Create the FindPlaceFromText object with a valid place ID.
    FindPlaceFromText findPlaceResult = new FindPlaceFromText();
    findPlaceResult.candidates = new PlacesSearchResult[1];
    findPlaceResult.candidates[0] = new PlacesSearchResult();
    findPlaceResult.candidates[0].placeId = PLACE_ID;

    // Have the findPlaceRequest.await() method return the FindPlaceFromText
    // object using PowerMockito, as await() is a final method.
    PowerMockito.when(findPlaceRequest.await()).thenReturn(findPlaceResult);

    // Mock the PlacesApi object.
    PowerMockito.mockStatic(PlacesApi.class);
    when(PlacesApi.findPlaceFromText(any(), anyString(), any()))
      .thenReturn(findPlaceRequest);

    // Run the getPlaceIdFromTextSearch(...) method to test the result.
    String placeIdResult = utility.getPlaceIdFromTextSearch(mockGeoApiContext, 
      TEXT_LOCATION_SEARCH);

    // Confirm that the method returns the correct place ID.
    Assert.assertEquals(PLACE_ID, placeIdResult);
  }

  @Test
  public void testGetPlaceIdFromTextSearchCandidatesNull() throws Exception {
    // Mock the GeoApiContext object to be passed into PlacesApi methods,
    // and the FindPlaceFromTextRequest.
    GeoApiContext mockGeoApiContext = mock(GeoApiContext.class);
    FindPlaceFromTextRequest findPlaceRequest = 
      PowerMockito.mock(FindPlaceFromTextRequest.class);

    // Create the FindPlaceFromText object with no place ID.
    FindPlaceFromText findPlaceResult = new FindPlaceFromText();

    // Have the findPlaceRequest.await() method return the FindPlaceFromText
    // object using PowerMockito, as await() is a final method.
    PowerMockito.when(findPlaceRequest.await()).thenReturn(findPlaceResult);

    // Mock the PlacesApi object.
    PowerMockito.mockStatic(PlacesApi.class);
    when(PlacesApi.findPlaceFromText(any(), anyString(), any()))
      .thenReturn(findPlaceRequest);

    // Run the getPlaceIdFromTextSearch(...) method to test the result.
    String placeIdResult = utility.getPlaceIdFromTextSearch(mockGeoApiContext, 
      TEXT_LOCATION_SEARCH);

    // Confirm that the method returns null.
    Assert.assertNull(placeIdResult);
  }

}
