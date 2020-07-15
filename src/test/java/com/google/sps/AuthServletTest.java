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
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.sps.servlets.AuthServlet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Gson.class,UserService.class,User.class})
public final class AuthServletTest {

  // Add constants for testing User class.
  public static final String EMAIL = "testemail@gmail.com";
  public static final String AUTH_DOMAIN = "gmail.com";
  public static final String LOGOUT_URL = "/_ah/logout?continue=%2F";
  public static final String LOGIN_URL = "/_ah/login?continue=%2F";

  // Create logout and login URLs with unicode.
  public static final String LOGOUT_URL_UNICODE = "/_ah/logout?continue\\u003d%2F";
  public static final String LOGIN_URL_UNICODE = "/_ah/login?continue\\u003d%2F";

  // Add helper to allow datastore testing in local JUnit tests.
  // See https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting.
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
  public void testGetUserAuthJsonLoggedInReturn() throws Exception {
    // Mock request and response.      
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    when(userServiceMock.getCurrentUser()).thenReturn(new User(EMAIL, AUTH_DOMAIN));
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGOUT_URL);

    // Initialize AuthServlet object.
    AuthServlet authServlet = new AuthServlet();

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(responseMock.getWriter()).thenReturn(writer);

    // Create the expected JSON logged-in string.
    String expectedJson = "{\"url\":\"" + LOGOUT_URL_UNICODE + "\",\"email\":\"" 
      + EMAIL + "\"}";

    // Run doGet(...), and test whether output matches expected.
    authServlet.getUserAuthJson(responseMock, userServiceMock);
    writer.flush();
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

  @Test
  public void testGetUserAuthJsonNotLoggedInReturn() throws Exception {
    // Mock request and response.      
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Mock UserService methods as logged-out user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(false);
    when(userServiceMock.createLoginURL(AuthServlet.redirectUrl)).thenReturn(LOGIN_URL);

    // Initialize AuthServlet object.
    AuthServlet authServlet = new AuthServlet();

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(responseMock.getWriter()).thenReturn(writer);

    // Create the expected JSON logged-in string.
    String expectedJson = "{\"url\":\"" + LOGIN_URL_UNICODE + "\"}";

    // Run doGet(...), and test whether output matches expected.
    authServlet.getUserAuthJson(responseMock, userServiceMock);
    writer.flush();
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

}