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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

  // Set redirect URL after login / logout as index page.
  public static final String redirectUrl = "/";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    // Set up user auth objects.
    UserService userService = UserServiceFactory.getUserService();
    getUserAuthJson(response, userService);
  }

  /**
   * Write the UserAuth object in JSON form through response.
   */
  public void getUserAuthJson(HttpServletResponse response, UserService userService) throws IOException {
    // Create UserAuth object with relevant login / logout information.
    UserAuth userAuth;
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String logoutUrl = userService.createLogoutURL(redirectUrl);

      // Create UserAuth object to represent logged-in user.
      userAuth = new UserAuth(logoutUrl, userEmail);

      // Add user to database (if not already present).
      getOrCreateUserInDatabase(userEmail);
    } else {
      String loginUrl = userService.createLoginURL(redirectUrl);

      // Create UserAuth object to represent logged-out user.
      userAuth = new UserAuth(loginUrl);
    }

    // Create and send the JSON.
    String json = convertToJson(userAuth);
    response.getWriter().println(json);
  }

  /**
   * Converts a UserAuth object into a JSON string using the Gson library.
   */
  private static String convertToJson(UserAuth userAuth) {
    Gson gson = new Gson();
    String json = gson.toJson(userAuth);
    return json;
  }
  
  /**
   * If the user is not in database already, add them.
   * This method returns the Entity object in the database (or the newly-created
   * user Entity, if none previously existed).
   */
  public static Entity getOrCreateUserInDatabase(String email) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Only add User to database if they are not already present there.
    Entity userEntity = getUserEntityFromEmail(email);
    if (userEntity == null) {
      // User is not in database, create and add user object.
      User newUser = new User(email);
      Entity newUserEntity = newUser.buildEntity();
      datastore.put(newUserEntity);

      // Return the new user object.
      return newUserEntity;
    }

    // Return the user already in the database.
    return userEntity;
  }

  /**
   * Get the Entity object of the current user that is signed in. If not already
   * in database, this method adds them and returns the newly-added Entity object.
   * If no user is signed in, return null.
   */
  public static Entity getCurrentUserEntity() {
    UserService userService = UserServiceFactory.getUserService();

    // Return null if no user is logged in.
    if (!userService.isUserLoggedIn()) {
      return null;
    }

    // Get user email from UserService.
    String email = userService.getCurrentUser().getEmail();

    // Return user from datastore; if not present, add the user and return that.
    Entity userEntity = getOrCreateUserInDatabase(email);
    return userEntity;
  }

  /**
   * Return Entity object of user from database using email, or null if user 
   * email is not in db.
   */
  public static Entity getUserEntityFromEmail(String email) {
    // Query database to see if User has already been added.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter emailFilter =
      new FilterPredicate(User.USER_EMAIL, FilterOperator.EQUAL, email);
    Query query = new Query(User.USER).setFilter(emailFilter);
    PreparedQuery results = datastore.prepare(query);

    // Get the list of results.
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());
    if (listResults.isEmpty()) {
      return null;
    }

    // Return the Entity object of the User.
    return listResults.get(0);
  }

  /**
   * Inner class that holds relevant login/logout and user information.
   */
  class UserAuth {
    // Fields that hold relevant login data.
    private String url;
    private String email;

    // Constructor to create UserAuth object with no user logged in.
    // Null represents no value.
    private UserAuth(String url) {
      this(url, null);
    }

    // Full constructor to assign values to all fields.
    private UserAuth(String url, String email) {
      this.url = url;
      this.email = email;
    }
  }
}
