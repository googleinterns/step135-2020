//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.Entity;
import com.google.maps.errors.ApiException;
import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.LatLng;
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

    /**
    inputDestination: California, USA
    poi-1: Table Mountain Casino
    poi-2: Fresno Chaffee Zoo
    inputTripName: My Favorite Trip
    inputDayOfTravel: 2020-07-17
    poi-3: Sierra Bicentennial Park
    */

    // Retrieve form inputs to define the Trip object.
    String tripName = request.getParameter(INPUT_TRIP_NAME);
    String tripDestination = request.getParameter(INPUT_DESTINATION);
    String tripDayOfTravel = request.getParameter(INPUT_DAY_OF_TRAVEL);

    // Create Trip object.
    Trip trip = new Trip(tripName, tripDayOfTravel);
    Entity tripEntity = trip.buildEntity();

    GeoApiContext context = new GeoApiContext.Builder()
      .apiKey(Config.API_KEY)
      .build();

    FindPlaceFromTextRequest findPlaceRequest = findPlaceFromText(context, 
      tripDestination, FindPlaceFromTextRequest.InputType.TEXT_QUERY);

    try {
      FindPlaceFromText findPlaceResult = findPlaceRequest.await();
      response.getWriter().println(findPlaceResult.candidates[0]);

      PlaceDetailsRequest placeDetailsRequest = placeDetails(context, 
        findPlaceResult.candidates[0].placeId);
      PlaceDetails placeDetailsResult = placeDetailsRequest.await();
      response.getWriter().println(placeDetailsResult);
    } catch (ApiException | InterruptedException e) {
      throw new IOException(e);
    }



    // Print out params to site to verify retrieval of "start trip" user input.
    Enumeration<String> params = request.getParameterNames();
    while (params.hasMoreElements()) {
      String paramName = params.nextElement();
      response.getWriter().println(paramName + ": " + request.getParameter(paramName));
    }
  }

  /**
   * Find places using either search text, or a phone number.
   *
   * @param context The context on which to make Geo API requests.
   * @param input The input to search on.
   * @param inputType Whether the input is search text, or a phone number.
   * @return Returns a FindPlaceFromTextRequest that you can configure and execute.
   */
  public static FindPlaceFromTextRequest findPlaceFromText(
      GeoApiContext context, String input, FindPlaceFromTextRequest.InputType inputType) {
    FindPlaceFromTextRequest request = new FindPlaceFromTextRequest(context);
    request.input(input).inputType(inputType);
    return request;
  }

}
