//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.NotFoundException;
import com.google.maps.model.AddressType;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.GeocodedWaypointStatus;
import com.google.maps.model.LatLng;
import com.google.maps.model.TrafficModel;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TransitRoutingPreference;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import com.google.sps.Trip;
import com.google.sps.TripDay;
import com.google.sps.data.Config;

@WebServlet("/calculate-trip")
public class TripServlet extends HttpServlet {

  // Saves user input and calculates optimal route using Maps Java Client
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    Enumeration<String> params = request.getParameterNames();
    String tripName = request.getParameter("inputTripName");
    String origin = request.getParameter("inputDestination");
    String startDate = request.getParameter("inputDayOfTravel");

    // Print user input for now
    response.getWriter().println("startDate: " + startDate);
    response.getWriter().println("tripName: " + tripName);
    response.getWriter().println("origin: " + origin);

    // Save POIs to an array
    List<String> pois = new ArrayList<>(); 
    while (params.hasMoreElements()) {
      String paramName = params.nextElement();
      if (paramName.contains("poi")) {
        String newPOI = request.getParameter(paramName);
        pois.add(newPOI);
      }
    }
    String[] poiStrings = new String[pois.size()]; 
    poiStrings = pois.toArray(poiStrings); 

   	GeoApiContext distCalcer = new GeoApiContext.Builder()
		    .apiKey(Config.API_KEY)
		    .build();

    // Generate directions request
    DirectionsApiRequest directionsRequest = DirectionsApi.newRequest(distCalcer)
        .origin(origin)
        .destination(origin)
        .waypoints(poiStrings)
        .optimizeWaypoints(true)
        .mode(TravelMode.DRIVING);

    // Calculate route and save travelTimes and waypointOrder to two ArrayLists
    try {
      DirectionsResult dirResult = directionsRequest.await();
      int[] waypointOrder = dirResult.routes[0].waypointOrder;
      List<Integer> travelTimes = new ArrayList<>();
      for (DirectionsLeg leg : dirResult.routes[0].legs) {
        int travelTime = (int) leg.duration.inSeconds / 60;
        travelTimes.add(travelTime);
      }

      // Generate an ordered list of location Strings from waypointOrder
      List<String> orderedLocationStrings = new ArrayList<>();
      for (int i = 0; i < waypointOrder.length; i++) {
        orderedLocationStrings.add(pois.get(waypointOrder[i]));
      }

      // Print out results on page for now
      response.getWriter().println(travelTimes.toString());
      response.getWriter().println(orderedLocationStrings.toString());
    } catch (ApiException | InterruptedException e) {
      throw new IOException(e);
    } 
  }
}
