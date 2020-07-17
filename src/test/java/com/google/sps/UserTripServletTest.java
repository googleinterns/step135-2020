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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.Config;
import com.google.sps.data.User;
import com.google.sps.servlets.AuthServlet;
import com.google.sps.servlets.UserTripServlet;
import com.google.sps.servlets.TripServlet;
import java.io.PrintWriter;
import java.io.StringWriter;
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
@PrepareForTest(UserServiceFactory.class)
public final class UserTripServletTest {

  // Add constants necessary to testing retrieval of the current User.
  public static final String EMAIL = "testemail@gmail.com";
  public static final String AUTH_DOMAIN = "gmail.com";
  public static final String LOGOUT_URL = "/_ah/logout?continue=%2F";
  public static final String LOGIN_URL = "/_ah/login?continue=%2F";

  // Create UserTripServlet and TripServlet objects.
  UserTripServlet userTripServlet;
  TripServlet tripServlet;

  // Add helper to allow datastore testing in local JUnit tests.
  // See https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting.
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void initServlets() {
    userTripServlet = new UserTripServlet();
    tripServlet = new TripServlet();
  }

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testDoGetResponse() throws Exception {
    // Mock request and response.  
    HttpServletRequest requestMock = mock(HttpServletRequest.class);    
    HttpServletResponse responseMock = mock(HttpServletResponse.class);
    
    // Create Trip Entity properties.
    final String tripName = "Family Vacation";
    final String destinationName = "Island of Hawai'i";
    final String tripDayOfTravel = "2020-07-17";
    final String photoSrc = "../images/placeholder_image.png";

    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    // This is the User object from Google Appengine (full path given to avoid
    // confusion with local User.java file).
    when(userServiceMock.getCurrentUser()).thenReturn(
        new com.google.appengine.api.users.User(EMAIL, AUTH_DOMAIN));
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGOUT_URL);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);

    // Run storeTripEntity(...), with the User logged in (so trip is stored).
    tripServlet.storeTripEntity(responseMock,
      tripName, destinationName, tripDayOfTravel, photoSrc);

    // Create writers to pass into 
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(responseMock.getWriter()).thenReturn(writer);

    // Run the UserServlet doGet(...) method with request and response mocks. 
    userTripServlet.doGet(requestMock, responseMock); 

    // Create the expected JSON String outputted from the doGet(...) function.
    // The tripKey variable was copied from the output.
    String expectedJson = "[{\"tripName\":\"Family Vacation\",\"destinationName\""
      + ":\"Island of Hawai\\u0027i\",\"tripKey\":\"agR0ZXN0chQLEgR1c2VyGAEMCxIEdHJpcBgCDA"
      + "\",\"imageSrc\":\"../images/placeholder_image.png"
      + "\",\"startDate\":{\"year\":2020,\"month\":7,\"day\":17},\"endDate\""
      + ":{\"year\":2020,\"month\":7,\"day\":17},\"numDays\":1}]";

    writer.flush(); // Flush the writer.
    Assert.assertTrue(stringWriter.toString().contains(expectedJson));
  }

}
