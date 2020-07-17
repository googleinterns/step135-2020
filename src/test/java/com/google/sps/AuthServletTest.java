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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.User;
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
@PrepareForTest(UserServiceFactory.class)
public final class AuthServletTest {

  // Create AuthServlet object.
  AuthServlet authServlet;

  // Add constants for testing User class.
  public static final String EMAIL = "testemail@gmail.com";
  public static final String SECOND_EMAIL = "testemail2@gmail.com";
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
  public void initAuthServlet() {
    authServlet = new AuthServlet();
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
  public void testGetUserAuthJsonLoggedInReturn() throws Exception {
    // Mock response.      
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    // This is the User object from Google Appengine (full path given to avoid
    // conflict with local User.java file).
    when(userServiceMock.getCurrentUser()).thenReturn(
        new com.google.appengine.api.users.User(EMAIL, AUTH_DOMAIN));
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGOUT_URL);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(responseMock.getWriter()).thenReturn(writer);

    // Create the expected JSON logged-in string.
    String expectedJson = "{\"url\":\"" + LOGOUT_URL_UNICODE + "\",\"email\":\"" 
      + EMAIL + "\"}\n";

    // Run getUserAuthJson(...), and test whether output matches expected.
    authServlet.getUserAuthJson(responseMock, userServiceMock);
    writer.flush();
    Assert.assertEquals(expectedJson, stringWriter.toString());
  }

  @Test
  public void testGetUserAuthJsonNotLoggedInReturn() throws Exception {
    // Mock response.      
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Mock UserService methods as logged-out user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(false);
    when(userServiceMock.createLoginURL(AuthServlet.redirectUrl)).thenReturn(LOGIN_URL);

    // Create writers to check against actual output.
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(responseMock.getWriter()).thenReturn(writer);

    // Create the expected JSON logged-out string.
    String expectedJson = "{\"url\":\"" + LOGIN_URL_UNICODE + "\"}\n";

    // Run getUserAuthJson()(...), and test whether output matches expected.
    authServlet.getUserAuthJson(responseMock, userServiceMock);
    writer.flush();
    Assert.assertEquals(expectedJson, stringWriter.toString());
  }

  @Test
  public void testGetOrCreateUserInDatabaseNotPresent() throws Exception {
    // Run getOrCreateUserInDatabase(...), with the User not present in datastore.
    Entity userEntityReturn = AuthServlet.getOrCreateUserInDatabase(EMAIL);

    // Retrieve the datastore results.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Check whether the proper Entity and count were returned.
    Assert.assertEquals(1, listResults.size());
    Assert.assertEquals(EMAIL, listResults.get(0).getProperty(User.USER_EMAIL));

    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(listResults.get(0), userEntityReturn);
  }

  @Test
  public void testGetOrCreateUserInDatabaseNotPresentRunTwiceSameEmails() throws Exception {
    // Run getOrCreateUserInDatabase(...), with the User not present in datastore.
    Entity userEntityReturn = AuthServlet.getOrCreateUserInDatabase(EMAIL);
    Entity userEntitySecondReturn = AuthServlet.getOrCreateUserInDatabase(EMAIL);

    // Retrieve the datastore results.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Check whether the proper Entity and count were returned.
    Assert.assertEquals(1, listResults.size());
    Assert.assertEquals(EMAIL, listResults.get(0).getProperty(User.USER_EMAIL));

    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(listResults.get(0), userEntityReturn);
  }

  @Test
  public void testGetOrCreateUserInDatabaseNotPresentRunTwiceDifferentEmails() throws Exception {
    // Run getOrCreateUserInDatabase(...), with the User not present in datastore.
    Entity userEntityReturn = AuthServlet.getOrCreateUserInDatabase(EMAIL);
    Entity userEntitySecondReturn = AuthServlet.getOrCreateUserInDatabase(SECOND_EMAIL);

    // Retrieve the datastore results.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Check whether the proper Entity and count were returned.
    Assert.assertEquals(2, listResults.size());
    Assert.assertEquals(EMAIL, listResults.get(0).getProperty(User.USER_EMAIL));
    Assert.assertEquals(SECOND_EMAIL, listResults.get(1).getProperty(User.USER_EMAIL));

    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(listResults.get(0), userEntityReturn);
    Assert.assertEquals(listResults.get(1), userEntitySecondReturn);
  }

  @Test
  public void testGetOrCreateUserInDatabasePresent() throws Exception {
    // Add a User to the database.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);

    // Run getOrCreateUserInDatabase(...), with the User already present in datastore.
    Entity userEntityReturn = AuthServlet.getOrCreateUserInDatabase(EMAIL);

    // Retrieve the datastore results.
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Check whether the proper Entity and count were returned.
    Assert.assertEquals(1, listResults.size());
    Assert.assertEquals(EMAIL, listResults.get(0).getProperty(User.USER_EMAIL));

    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(listResults.get(0), userEntityReturn);
  }

  @Test
  public void testGetUserEntityFromEmailNotPresent() throws Exception {
    // Run getUserEntityFromEmail(...), with the User not present in datastore.
    Entity userEntityReturn = AuthServlet.getUserEntityFromEmail(EMAIL);

    // Retrieve the datastore results.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Verify that the Entity is not in the database, and the null method return.
    Assert.assertNull(userEntityReturn);
    Assert.assertTrue(listResults.isEmpty());
  }

  @Test
  public void testGetUserEntityFromEmailPresent() throws Exception {
    // Add a User to the database.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);

    // Run getUserEntityFromEmail(...), with the User present in datastore.
    Entity userEntityReturn = AuthServlet.getUserEntityFromEmail(EMAIL);

    // Retrieve the datastore results.
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Verify that the Entity is in the database, and the Entity method return.
    Assert.assertEquals(EMAIL, userEntityReturn.getProperty(User.USER_EMAIL));
    Assert.assertEquals(1, listResults.size());

    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(listResults.get(0), userEntityReturn);
  }

  @Test
  public void testGetCurrentUserEntityNotLoggedIn() throws Exception {
    // Mock UserService methods as logged-out user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(false);
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGIN_URL);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);
    
    // Run getCurrentUserEntity(...).
    Entity userEntityReturn = AuthServlet.getCurrentUserEntity();
    
    // Confirm that the method returns null, as there is no "current" user.
    Assert.assertNull(userEntityReturn);
  }

  @Test
  public void testGetCurrentUserEntityLoggedInNotPresent() throws Exception {
    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    // This is the User object from Google Appengine (full path given to avoid
    // conflict with local User.java file).
    when(userServiceMock.getCurrentUser()).thenReturn(
        new com.google.appengine.api.users.User(EMAIL, AUTH_DOMAIN));
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGOUT_URL);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);

    // Run getCurrentUserEntity(...), with the User present in datastore.
    Entity userEntityReturn = AuthServlet.getCurrentUserEntity();

    // Retrieve the datastore results.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Verify that the Entity is in the database, and the Entity method return.
    Assert.assertEquals(EMAIL, userEntityReturn.getProperty(User.USER_EMAIL));
    Assert.assertEquals(1, listResults.size());

    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(listResults.get(0), userEntityReturn);
  }

  @Test
  public void testGetCurrentUserEntityLoggedInPresent() throws Exception {
    // Mock UserService methods as logged-in user.
    UserService userServiceMock = mock(UserService.class);
    when(userServiceMock.isUserLoggedIn()).thenReturn(true);
    // This is the User object from Google Appengine (full path given to avoid
    // conflict with local User.java file).
    when(userServiceMock.getCurrentUser()).thenReturn(
        new com.google.appengine.api.users.User(EMAIL, AUTH_DOMAIN));
    when(userServiceMock.createLogoutURL(AuthServlet.redirectUrl)).thenReturn(LOGOUT_URL);

    // PowerMock static getUserService() method, which is used to get the user.
    PowerMockito.mockStatic(UserServiceFactory.class);
    when(UserServiceFactory.getUserService()).thenReturn(userServiceMock);

    // Add a User to the database.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = new Entity(User.USER);
    userEntity.setProperty(User.USER_EMAIL, EMAIL);
    datastore.put(userEntity);

    // Run getCurrentUserEntity(...), with the User present in datastore.
    Entity userEntityReturn = AuthServlet.getCurrentUserEntity();

    // Retrieve the datastore results.
    Query query = new Query(User.USER);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

    // Verify that the Entity is in the database, and the Entity method return.
    Assert.assertEquals(EMAIL, userEntityReturn.getProperty(User.USER_EMAIL));
    Assert.assertEquals(1, listResults.size());

    // Confirm that the Entity in the database matches the method return.
    Assert.assertEquals(listResults.get(0), userEntityReturn);
  }

}
