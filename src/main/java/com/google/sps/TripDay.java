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
import java.util.ArrayList;
import java.util.List;

/**
 * TripDay objects store the trip/route information for a single day.
 */
public class TripDay {
  private String origin;
  private String destination;
  private List<String> locations;

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
   */
  public TripDay(String origin, String destination, List<String> locations) {
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
   * Returns a List<String> copy of locations for this TripDay.
   */
  public List<String> getLocations() {
    List<String> locationsCopy = new ArrayList<>();
    locationsCopy.addAll(this.locations);
    return locationsCopy;
  }

  /**
   * Build location entity to be put in datastore 
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

  public static void storeLocationsInDatastore(List<String> locations, Key parentKeyId, DatastoreService datastore) {
    List<Entity> locationEntities = locationsToEntities(locations, parentKeyId);
    for (Entity locationEntity : locationEntities) {
      datastore.put(locationEntity);
    }
  }

  /**
   * Get location name from a location entity
   */
  public static String eventFromEntity(Entity locationEntity) {
    return (String) locationEntity.getProperty(NAME);
  }
}
