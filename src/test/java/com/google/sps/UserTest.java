
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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.User;
import java.util.List;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class UserTest {

  // Add constants for testing User class.
  public static final String EMAIL = "testemail@gmail.com";
  public static final String TRIP_ID = "dJE93mn20dMs01nc";
  public static final int USER_ID_LENGTH = 16;
  public static final List<String> EMPTY_TRIP_ID_LIST = new ArrayList<String>();

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testUserSetup() {
    User user = new User(EMAIL);

    // Confirm email and trips variable.
    Assert.assertEquals(EMAIL, user.getEmail());
    Assert.assertEquals(EMPTY_TRIP_ID_LIST, user.getTripIdList());

    // User ID is random 16-digit alphanumeric string, verify through length.
    Assert.assertEquals(USER_ID_LENGTH, user.getUserId().length());
  }

  @Test
  public void testUserAddTripId() {
    User user = new User(EMAIL);
    user.addTripId(TRIP_ID);

    // Confirm that this single trip ID was added.
    Assert.assertEquals(1, user.getTripIdList().size());
    Assert.assertEquals(TRIP_ID, user.getTripIdList().get(0));
  }

  @Test
  public void testUserBuildEmptyTripEntity() {
    User user = new User(EMAIL);
    Entity userEntity = user.buildEntity();

    // Confirm email and trips variable.
    Assert.assertEquals(EMAIL, userEntity.getProperty(User.USER_EMAIL));
    Assert.assertEquals(EMPTY_TRIP_ID_LIST, userEntity.getProperty(User.TRIP_ID_LIST));

    // User ID is random 16-digit alphanumeric string, verify through length.
    Assert.assertEquals(USER_ID_LENGTH, 
      ((String) userEntity.getProperty(User.USER_ID)).length());
  }

  @Test
  public void testUserBuildNonEmptyTripEntity() {
    User user = new User(EMAIL);
    user.addTripId(TRIP_ID);
    Entity userEntity = user.buildEntity();

    // Confirm email variable.
    Assert.assertEquals(EMAIL, userEntity.getProperty(User.USER_EMAIL));

    // Confirm that this single trip ID was added.
    Assert.assertEquals(1, ((List<String>) userEntity.getProperty(User.TRIP_ID_LIST)).size());
    Assert.assertEquals(TRIP_ID, ((List<String>) userEntity.getProperty(User.TRIP_ID_LIST)).get(0));

    // User ID is random 16-digit alphanumeric string, verify through length.
    Assert.assertEquals(USER_ID_LENGTH, 
      ((String) userEntity.getProperty(User.USER_ID)).length());
  }

}
