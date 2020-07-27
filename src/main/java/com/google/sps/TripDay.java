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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TripDay objects store the trip/route information for a single day.
 */
public class TripDay {
  private String origin;
  private String destination;
  private List<String> locations;
  private LocalDate date;

  // Entity params
  private static final String ORIGIN = "origin";
  private static final String DESTINATION = "destination";
  private static final String DATE = "date";
  
  // query string
  public static final String QUERY_STRING = "trip-day";

  // constants for entity construction
  private static final String LOCATION_ENTITY_TYPE = "location";
  private static final String NAME = "name";
  private static final String ORDER = "order";

  /**
   * Creates a new TripDay.
   *
   * @param origin The departure location (Google Maps Place ID string) for this day. Must be non-null.
   * @param destination The final destination (Google Maps Place ID string) for this day. Must be non-null.
   * @param locations The list of POIs (list of Google Maps Place ID strings) 
                      that are stopovers for this day. Must be non-null.
   * @param date The date of when this TripDay occurs.
   */
  public TripDay(String origin, String destination, List<String> locations, LocalDate date) {
    if (origin == null) {
      throw new IllegalArgumentException("origin cannot be null");
    }

    if (destination == null) {
      throw new IllegalArgumentException("destination cannot be null");
    }

    if (locations == null) {
      throw new IllegalArgumentException("locations cannot be null. Use empty array instead.");
    }

    this.origin = origin;
    this.destination = destination;
    this.date = date;

    // Duplicate locations to not modify original parameter
    this.locations = new ArrayList<>();
    this.locations.addAll(locations);
  }

  /**
   * Returns the starting point for this TripDay.
   */
  public String getOrigin() {
    return this.origin;
  }

  /**
   * Returns the final destination for this TripDay.
   */
  public String getDestination() {
    return this.destination;
  }

  /**
   * Returns the date for this TripDay.
   */
  public LocalDate getDate() {
    return this.date;
  }

  /**
   * Returns a List<String> copy of locations for this TripDay.
   */
  public List<String> getLocations() {
    List<String> locationsCopy = new ArrayList<>();
    locationsCopy.addAll(this.locations);
    return locationsCopy;
  }

  /**
   * Build location entities to be put in datastore. 
   * @param locations ArrayList of locations (in optimized order) as strings
   * @param parentKeyId Key of TripDay entity that these locations are a part of  
   */
  public static List<Entity> locationsToEntities(List<String> locations, Key parentKeyId) {
    List<Entity> locationEntities = new ArrayList<>();
    for (int i = 0; i < locations.size(); i++) {
      Entity locationEntity = new Entity(LOCATION_ENTITY_TYPE, parentKeyId);
      locationEntity.setProperty(NAME, locations.get(i));
      locationEntity.setProperty(ORDER, i);
      locationEntities.add(locationEntity);
    }
    return locationEntities;
  } 

  /**
   * Store list location entities to be put in datastore.
   * @param locationEntities ArrayList of Entities to be put in Datastore
   * @param datastore Datastore for storing 
   */
  public static void storeLocationsInDatastore(List<Entity> locationEntities, DatastoreService datastore) {
    for (Entity locationEntity : locationEntities) {
      datastore.put(locationEntity);
    }

  /**
   * Add/set locations
   */
  public void setLocations(ArrayList<String> locations) {
    List<String> temp = new ArrayList<>();
    temp.addAll(locations);
    this.locations = temp;
  }

  /**
   * Builds entity corresponds to current TripDay
   */
  public Entity buildEntity() {
    Entity tripDayEntity = new Entity(QUERY_STRING);
    tripDayEntity.setProperty(ORIGIN, this.origin);
    tripDayEntity.setProperty(DESTINATION, this.destination);
    tripDayEntity.setProperty(DATE, this.date.toString());
    return tripDayEntity;
  }

  /**
   * Builds entity corresponds to current TripDay with parent ID (from
   * Trip Entity)
   */
  public Entity buildEntity(Key parentKeyID) {
    Entity tripDayEntity = new Entity(QUERY_STRING, parentKeyID);
    tripDayEntity.setProperty(ORIGIN, this.origin);
    tripDayEntity.setProperty(DESTINATION, this.destination);
    tripDayEntity.setProperty(DATE, this.date.toString());
    return tripDayEntity;
  }

  /**
   * Build tripDay from tripDay entity. Set origin, destination, and date
   * NOTE: Since Lists cannot be stored in datastore, must call setLocations()
   * after!
   */
  public static TripDay tripDayFromEntity(Entity tripDayEntity) {
    String origin = (String) tripDayEntity.getProperty(ORIGIN);
    String destination = (String) tripDayEntity.getProperty(DESTINATION);
    List<String> locations = new ArrayList<>();
    String date = (String) tripDayEntity.getProperty(DATE);

    TripDay tripDay = new TripDay(origin, destination, locations, LocalDate.parse(date));
    return tripDay;
  }
}
