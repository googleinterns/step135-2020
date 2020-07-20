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

public class User {

  // Identification information for the user.
  private String email;

  // Constants to create user entity for datastore.
  public static final String USER = "user";
  public static final String USER_EMAIL = "email";

  /**
   * Constructor to create a User object; the userId is generated automatically,
   * and the trip IDs are added to the User after constructing the object.
   *
   * @param email The email of the user.
   */
  public User(String email) {
    this.email = email;
  }

  // Builds an Entity object for datastore based on current User attributes.
  public Entity buildEntity() {
    Entity userEntity = new Entity(USER);
    userEntity.setProperty(USER_EMAIL, this.email);
    return userEntity;
  }

  // Get the private email field.
  public String getEmail() {
    return this.email;
  }

}
