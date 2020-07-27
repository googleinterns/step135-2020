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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.maps.errors.ApiException;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.FindPlaceFromText;
import com.google.sps.data.Config;
import com.google.sps.data.User;
import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Class that Utility functions; that is, functions that are used to initialize
 * certain variables or states common throughout the codebase.
 */
public class Utility {

  // Datastore field variable, used to place and retrieve items to / from Datastore.
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  // Add constants necessary to testing retrieval of the current User.
  private static final String EMAIL = "testemail@gmail.com";
  private static final String AUTH_DOMAIN = "gmail.com";
  private static final String LOGOUT_URL = "/_ah/logout?continue=%2F";
  private static final String LOGIN_URL = "/_ah/login?continue=%2F";

  // Constants to represent different Trip attributes.
  private static final String TRIP_NAME = "Trip to California";
  private static final String DESTINATION_NAME = "California";
  private static final String IMAGE_SRC =
    "https://lh3.googleusercontent.com/p/AF1QipM7tbCZOj_5SOft9cYgI7un3bmieieqvdYkCPT5=s1600-w400";
  private static final String TRIP_DAY_OF_TRAVEL = "2020-02-29";

  // Constants to represent different Trip attributes for a second Trip.
  private static final String TRIP_NAME_2 = "Family Vacation";
  private static final String DESTINATION_NAME_2 = "Island of Hawai'i";
  private static final String IMAGE_SRC_2 = "../images/placeholder_image.png";
  private static final String TRIP_DAY_OF_TRAVEL_2 = "2020-07-17";

  // Add helper to allow datastore testing in local JUnit tests.
  // See https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting.
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initDatastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Add the logged-in user to Datastore. Get property names from the User class
   * and test property values from field variables.
   *
   * @return The Key of the User Entity in Datastore.
   */ 
  public static Key addLoggedInUserToDatastore() {
    // Add the logged-in User to Datastore, and get the User Entity Key.
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);
    Key userEntityKey = userEntity.getKey();
    return userEntityKey;
  }

  /**
   * Add a Trip Entity to Datastore under the passed-in Key of the User Entity.
   *
   * @param userEntityKey The Key from the User Entity in Datastore. Must be 
   * non-null.
   * @param tripName The human-readable name for the trip. Must be non-null.
   * @param destinationName The name of the destination the user is heading to.
   * @param tripKey The key of the trip as created by the Trip Entity.
   * @param imageSrc The image source / URL to represent the trip.
   * @param startDate The start date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   * @param endDate The end date for the trip. Must be non-null. Must be in yyyy-MM-dd date format.
   */
  public static Entity addTripEntityToDatastoreUnderUser(Key userEntityKey,
    String tripName, String destinationName, String imageSrc, String startDate, 
    String endDate) {

    // Add a single Trip to Datastore with the User Entity Key.
    Entity tripEntity = new Entity(Trip.TRIP, userEntityKey);
    tripEntity.setProperty(Trip.TRIP_NAME, tripName);
    tripEntity.setProperty(Trip.DESTINATION_NAME, destinationName);
    tripEntity.setProperty(Trip.IMAGE_SRC, imageSrc);
    tripEntity.setProperty(Trip.START_DATE, startDate);
    tripEntity.setProperty(Trip.END_DATE, endDate);
    datastore.put(tripEntity);
    return tripEntity;
  }

    /**
   * Get the place ID of the text search. Return null if no place ID matches
   * the search.
   * 
   * @param context The entry point for making requests against the Google Geo 
   * APIs (googlemaps.github.io/google-maps-services-java/v0.1.2/javadoc/com/google/maps/GeoApiContext.html).
   * @param textSearch The text query to be entered in the findPlaceFromText(...)
   * API call. Must be non-null.
   */ 
  public String getPlaceIdFromTextSearch(GeoApiContext context, String textSearch) 
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
  public PlaceDetails getPlaceDetailsFromPlaceId(GeoApiContext context, String placeId)
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
  public String getUrlFromPhotoReference(int maxWidth, String photoReference) {
    final String baseUrl = "https://maps.googleapis.com/maps/api/place/photo?";
    return baseUrl + "maxwidth=" + maxWidth + "&photoreference=" + 
      photoReference + "&key=" + Config.API_KEY;
  }

  /**
   * Converts a UserAuth object into a JSON string using the Gson library.
   *
   * @param userAuth Object...
   */
  public static String convertToJson(UserAuth userAuth) {
    Gson gson = new Gson();
    String json = gson.toJson(userAuth);
    return json;
  }



}
