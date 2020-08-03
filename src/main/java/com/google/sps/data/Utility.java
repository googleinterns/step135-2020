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

import com.google.gson.Gson;
import com.google.maps.errors.ApiException;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.LatLng;
import com.google.maps.model.Photo;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.sps.data.Config;
import com.google.sps.data.User;
import com.google.sps.Trip;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that Utility functions; that is, functions that are used to initialize
 * certain variables or states common throughout the codebase.
 */
public class Utility {

  /**
   * Get the place ID of the text search. Return null if no place ID matches
   * the search.
   * 
   * @param context The entry point for making requests against the Google Geo 
   * APIs (googlemaps.github.io/google-maps-services-java/v0.1.2/javadoc/com/google/maps/GeoApiContext.html).
   * @param textSearch The text query to be entered in the findPlaceFromText(...)
   * API call. Must be non-null.
   */ 
  public static String getPlaceIdFromTextSearch(GeoApiContext context, String textSearch) 
    throws IOException {

    FindPlaceFromTextRequest findPlaceRequest = PlacesApi.findPlaceFromText(context, 
      textSearch, FindPlaceFromTextRequest.InputType.TEXT_QUERY);

    try {
      FindPlaceFromText findPlaceResult = findPlaceRequest.await();

      // Return place ID of the first candidate result.
      if (findPlaceResult.candidates != null) {
        return findPlaceResult.candidates[0].placeId;
      }
      
      // No candidate is given, so return null.
      return null;
    } catch(ApiException | InterruptedException e) {
      throw new IOException(e);
    }
  }

    /**
   * Get the PlaceDetails object from the place ID.
   * 
   * @param context The entry point for making requests against the Google Geo 
   * APIs (googlemaps.github.io/google-maps-services-java/v0.1.2/javadoc/com/google/maps/GeoApiContext.html).
   * @param placeId The place ID of the location. Must be non-null.
   */
  public static PlaceDetails getPlaceDetailsFromPlaceId(GeoApiContext context, String placeId)
    throws IOException {

    PlaceDetailsRequest placeDetailsRequest = PlacesApi.placeDetails(context, 
      placeId);
    try {
      return placeDetailsRequest.await();
    } catch(ApiException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  /**
   * Get a URL to show the photo from the photoreference.
   * See https://developers.google.com/places/web-service/photos#place_photo_requests
   * for more info.
   * 
   * @param maxWidth This is the maximum width of the image.
   * @param photoReference This is the photo reference String stored in the 
   * Google Maps Photo object; this is used to retrieve the actual photo URL.
   */
  public static String getUrlFromPhotoReference(int maxWidth, String photoReference) {
    final String baseUrl = "https://maps.googleapis.com/maps/api/place/photo?";
    return baseUrl + "maxwidth=" + maxWidth + "&photoreference=" + 
      photoReference + "&key=" + Config.API_KEY;
  }

  /**
   * Converts an Object into a JSON string using the Gson library.
   *
   * @param object Object to be converted into a JSON string.
   */
  public static String convertToJson(Object object) {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    return json;
  }

}
