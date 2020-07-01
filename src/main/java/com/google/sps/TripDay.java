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

import java.util.ArrayList;

/**
 * Event is the container class for when a specific group of people are meeting and are therefore
 * busy. Events are considered read-only.
 */
public class TripDay {
  private String origin;
  private String destination;
  private ArrayList<String> locations;

  /**
   * Creates a new TripDay.
   *
   * @param origin The departure location for this day. Must be non-null.
   * @param destination The final destination for this day. Must be non-null.
   * @param locations The list of POIs that are stopovers for this day. Must be non-null.
   */
  public TripDay(String origin, String destination, ArrayList<String> locations) {
    if (origin == null) {
      throw new IllegalArgumentException("origin cannot be null");
    }

    if (destination == null) {
      throw new IllegalArgumentException("destination cannot be null");
    }

    if (locations == null) {
      throw new IllegalArgumentException("locations cannot be null. Use empty array instead.")
    }

    this.origin = origin;
    this.destination = destination;
    this.locations = new ArrayList<>();
    this.locations.addAll(locations);
  }

  /**
   * Returns the starting point for this TripDay.
   */
  public String getOrigin() {
    return origin;
  }

  /**
   * Returns the final destination for this TripDay.
   */
  public String getDestination() {
    return destination;
  }

  /**
   * Returns an ArrayList<String> of locations for this TripDay.
   */
  public ArrayList<String> getLocations() {
    return locations;
  }
}
