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

import com.google.appengine.api.datastore.Entity;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

public class User {

  // Identification information for the user.
  private String userId;
  private String email;

  // List of trip IDs.
  private List<String> tripIdList;

  // Constants to create user entity for datastore.
  public static final String USER = "user";
  public static final String USER_EMAIL = "email";
  public static final String USER_ID = "user_id";
  public static final String TRIP_ID_LIST = "trip_id_list";

  /**
   * Constructor to create a User object; the userId is generated automatically,
   * and the trip IDs are added to the User after constructing the object.
   */
  public User(String email) {
    this.userId = createUserId();
    this.email = email;
    this.tripIdList = new ArrayList<String>();
  }

  // Constructor to create a User object with predefined values.
  public User(String userId, String email, List<String> tripIdList) {
    this.userId = userId;
    this.email = email;
    this.tripIdList = tripIdList;
  }

  // Generates a random 16-digit alphanumeric.
  private String createUserId() {
    RandomStringGenerator generator = new RandomStringGenerator.Builder()
      .withinRange('0', 'z')
      .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
      .build();

    return generator.generate(16);
  }

  // Add trip ID.
  public void addTripId(String tripId) {
    tripIdList.add(tripId);
  }

  // Builds an Entity object for datastore based on current User attributes.
  public Entity buildEntity() {
    Entity userEntity = new Entity(USER);
    userEntity.setProperty(USER_EMAIL, this.email);
    userEntity.setProperty(USER_ID, this.userId);
    userEntity.setProperty(TRIP_ID_LIST, this.tripIdList);
    return userEntity;
  }

  // Builds a User object from a User Entity object (typically from datastore).
  public static User buildUserFromEntity(Entity userEntity) {
    String userId = (String) userEntity.getProperty(USER_ID);
    String userEmail = (String) userEntity.getProperty(USER_EMAIL);
    List<String> tripIdList = (List<String>) userEntity.getProperty(TRIP_ID_LIST);
    User user = new User(userId, userEmail, tripIdList);
    return user;
  }

  /**
   * Getter functions for private User variables.
   */
  
  public String getUserId() {
    return this.userId;
  }

  public String getEmail() {
    return this.email;
  }

  public List<String> getTripIdList() {
    return this.tripIdList;
  }

}
