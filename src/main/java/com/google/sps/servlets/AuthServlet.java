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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.User;
import java.io.IOException;
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
      addUserToDatabase(userEmail);
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
   * Public method to allow test use.
   */
  public static String convertToJson(UserAuth userAuth) {
    Gson gson = new Gson();
    String json = gson.toJson(userAuth);
    return json;
  }
  
  /**
   * If the user is not in database already, add them.
   */
  public void addUserToDatabase(String email) {
    // Query database to see if User has already been added.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);

    // If user is already in database, return.
    for (Entity entity : results.asIterable()) {
      if (entity.getProperty(User.USER_EMAIL).equals(email)) {
        return;
      }
    }

    // User is not in database, create and add user object.
    User user = new User(email);
    Entity userEntity = user.buildEntity();
    datastore.put(userEntity);
  }

  /**
   * Inner class that holds relevant login/logout and user information.
   * Class and constructors are public to enable testing.
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
