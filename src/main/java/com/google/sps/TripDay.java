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

import com.google.appengine.api.datastore.Entity;
import com.google.sps.data.Event;
import java.util.ArrayList;
import java.util.List;

/**
 * TripDay objects store the trip/route information for a single day.
 */
public class TripDay {
  private String origin;
  private String destination;
  private List<String> locations;
  private String date;

  // Entity params
  private static final String ORIGIN = "origin";
  private static final String DESTINATION = "destination";

  /**
   * Creates a new TripDay.
   *
   * @param origin The departure location (Google Maps Place ID string) for this day. Must be non-null.
   * @param destination The final destination (Google Maps Place ID string) for this day. Must be non-null.
   * @param locations The list of POIs (list of Google Maps Place ID strings) 
                      that are stopovers for this day. Must be non-null.
   */
  public TripDay(String origin, String destination, List<String> locations, String date) {
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

    // instantiate events
    this.events = new ArrayList<>();
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
  public String getDate() {
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
   * Builds entity corresponds to current TripDay
   */
  public Entity buildEntity() {
    Entity tripDayEntity = new Entity("trip-day");
    tripDayEntity.setProperty(ORIGIN, this.origin);
    tripDayEntity.setProperty(DESTINATION, this.destination);
    return tripDayEntity;
  }

  /**
   * Builds entity corresponds to current TripDay with parent ID
   */
  public Entity buildEntity(String parentKeyID) {
    Entity tripDayEntity = new Entity("trip-day", parentKeyID);
    tripDayEntity.setProperty(ORIGIN, this.origin);
    tripDayEntity.setProperty(DESTINATION, this.destination);
    return tripDayEntity;
  }
}
