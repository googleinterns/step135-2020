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
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.servlets.CalendarServlet;
// import java.io.PrintWriter;
// import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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

@RunWith(JUnit4.class)
public final class CalendarServletTest {

  @Test
  public void testGetEventsCorrectly() {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);    

    when(request.getParameter("username")).thenReturn("me");
    when(request.getParameter("password")).thenReturn("secret");

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new CalendarServlet().doGet(request, response);

    verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...
    writer.flush(); // it may not have been flushed yet...
    assertTrue(stringWriter.toString().contains("My expected string"));

    HttpServletResponse response = mock(HttpServletResponse.class);

    // // Mock UserService methods as logged-in user.
    // UserService userServiceMock = mock(UserService.class);
    // when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    // // This is the User object from Google Appengine (full path given to avoid
    // // conflict with local User.java file).
    // when(userServiceMock.getCurrentUser()).thenReturn(
    //     new com.google.appengine.api.users.User(EMAIL, AUTH_DOMAIN));
    // when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGOUT_URL);

    // // Create writers to check against actual output.
    // StringWriter stringWriter = new StringWriter();
    // PrintWriter writer = new PrintWriter(stringWriter);
    // when(responseMock.getWriter()).thenReturn(writer);

    // // Create the expected JSON logged-in string.
    // String expectedJson = "{\"url\":\"" + LOGOUT_URL_UNICODE + "\",\"email\":\"" 
    //   + EMAIL + "\"}";

    // // Run getUserAuthJson(...), and test whether output matches expected.
    // authServlet.getUserAuthJson(responseMock, userServiceMock);
    // writer.flush();
    // Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

}