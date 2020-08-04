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

// Create the script tag, set the appropriate attributes
let script = document.createElement('script');
script.src = 'https://maps.googleapis.com/maps/api/js?key=' + config.API_KEY + 
  '&libraries=places&callback=initMap';
script.defer = true;
script.async = true;

var map;
var directionsService;
var directionsRenderer;

// Attach callback function to the `window` object
window.initMap = function() {
  // initialize coords
  const coords = {lat: 0, lng: 0};

  // Create map centered on the (0, 0) coordinates
  map = new google.maps.Map(
      document.getElementById('routeMap'),
      {center: coords, 
      zoom: 13,
      streetViewControl: false,
      });

  // Instantiate a directions service.
  directionsService = new google.maps.DirectionsService;

  // Create a renderer for directions (shows directions on map and panel).
  directionsRenderer = new google.maps.DirectionsRenderer;
  directionsRenderer.setMap(map);
  directionsRenderer.setPanel(document.getElementById('rightPanel'));

  // Get locations from MapServlet and display directions on map.
  displayRouteOnMap();
}

/*
 * Gets locations from MapServlet with the tripKey parameter.
 * Calls showDirections to show the directions with those locations.
 */
function displayRouteOnMap() {
  fetch('/get-map' + getTripKeyQuery()).then(response => response.json()).then((locations) => {
    showDirections(locations);
  });
}

/* 
 * Parses locations, generations DirectionsRequest with those locations,
 * and displays on map with DirectionsRenderer.
 * locations is a list of String addresses where the first element is the origin/destination
 */
function showDirections(locations) {
  let origin = locations[0];
  let waypts = [];

  for (let i = 1; i < locations.length; i++) {
    waypts.push({ location : locations[i]});
  }

  // Create a DirectionsRequest with hotel as start/end and 
  // POIs as waypoints (stops on the route)
  directionsService.route({
    origin: origin,
    destination: origin,
    waypoints: waypts,
    travelMode: 'DRIVING'
  }, function(response, status) {
    // Show directions when found
    if (status === 'OK') {
      directionsRenderer.setDirections(response);
    } else {
      window.alert('Directions request failed due to ' + status);
    }
  });
}

// Append the 'script' element to 'head'
document.head.appendChild(script);
