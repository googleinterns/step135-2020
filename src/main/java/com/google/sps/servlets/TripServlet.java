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
import com.google.maps.errors.ApiException;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.LatLng;
import com.google.maps.model.Photo;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.sps.data.Config;
import com.google.sps.Trip;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/calculate-trip")
public class TripServlet extends HttpServlet {

  // Constants to get form inputs.
  private static final String INPUT_TRIP_NAME = "inputTripName";
  private static final String INPUT_DESTINATION = "inputDestination";
  private static final String INPUT_DAY_OF_TRAVEL = "inputDayOfTravel";


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    // Retrieve form inputs to define the Trip object.
    String tripName = request.getParameter(INPUT_TRIP_NAME);
    String tripDestination = request.getParameter(INPUT_DESTINATION);
    String tripDayOfTravel = request.getParameter(INPUT_DAY_OF_TRAVEL);

    GeoApiContext context = new GeoApiContext.Builder()
      .apiKey(Config.API_KEY)
      .build();

    // Get place ID from search of trip destination. Get photo and destination 
    // if not null; otherwise, use a placeholder photo and destination.
    String destinationPlaceId = getPlaceIdFromTextSearch(context, tripDestination);
    String destinationName;
    String photoSrc;
    if (destinationPlaceId == null) {
      destinationName = tripDestination;
      photoSrc = "images/placeholder_image.png";
    } else {
      PlaceDetails placeDetailsResult = getPlaceDetailsFromPlaceId(context, destinationPlaceId);

      // Get the name of the location from the place details result.
      destinationName = placeDetailsResult.name;

      // Get a photo of the location from the place details result.
      if (placeDetailsResult.photos == null) {
        photoSrc = "images/placeholder_image.png";
      } else {
        Photo photoObject = placeDetailsResult.photos[0];
        photoSrc = getUrlFromPhotoReference(400, photoObject.photoReference);
      }
    }

    // Get User Entity. If user not logged in, redirect to homepage.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = AuthServlet.getCurrentUserEntity(AuthServlet.getUserService());
    if (userEntity == null) {
      response.sendRedirect("/");
    }

    // Put Trip Entity into datastore.
    Entity tripEntity = Trip.buildEntity(tripName, destinationName, photoSrc,
      tripDayOfTravel, tripDayOfTravel, userEntity.getKey());
    datastore.put(tripEntity);

    // Print out params to site to verify retrieval of "start trip" user input.
    Enumeration<String> params = request.getParameterNames(); 
    while (params.hasMoreElements()) {
      String paramName = params.nextElement();
      response.getWriter().println(paramName + ": " + request.getParameter(paramName));
    }
  }

  /**
   * Get the place ID of the text search. Return null if no place ID matches
   * the search.
   */ 
  public String getPlaceIdFromTextSearch(GeoApiContext context, String textSearch) 
    throws IOException {

    FindPlaceFromTextRequest findPlaceRequest = PlacesApi.findPlaceFromText(context, 
      textSearch, FindPlaceFromTextRequest.InputType.TEXT_QUERY);

    try {
      FindPlaceFromText findPlaceResult = findPlaceRequest.await();

      // Return place ID of the first candidate result.
      if (findPlaceResult.candidates != null) {
        return findPlaceResult.candidates[0].placeId;
      }

      // No candidate is given, so return null.
      return null;
    } catch(ApiException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  /**
   * Get the PlaceDetails object from the place ID.
   */
  public PlaceDetails getPlaceDetailsFromPlaceId(GeoApiContext context, String placeId)
    throws IOException {

    PlaceDetailsRequest placeDetailsRequest = PlacesApi.placeDetails(context, 
      placeId);
    try {
      return placeDetailsRequest.await();
    } catch(ApiException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  /**
   * Get a URL to show the photo from the photoreference.
   * See https://developers.google.com/places/web-service/photos#place_photo_requests
   * for more info.
   */
  public String getUrlFromPhotoReference(int maxWidth, String photoReference) {
    final String baseUrl = "https://maps.googleapis.com/maps/api/place/photo?";
    return baseUrl + "maxwidth=" + maxWidth + "&photoreference=" + 
      photoReference + "&key=" + Config.API_KEY;
  }


}
