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
  // Manhattan coords
  const coords = {lat: 0, lng: 0};

  // Create map centered on Manhattan
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

  // Hard coded place IDs - will replace with user input
  let timesSquareID = 'ChIJmQJIxlVYwokRLgeuocVOGVU';
  let centralParkID = 'ChIJ4zGFAZpYwokRGUGph3Mf37k';
  let worldTradeID = 'ChIJy7cGfBlawokR5l2e93hsoEA';
  let empireStateID = 'ChIJtcaxrqlZwokRfwmmibzPsTU';
  let hotelID = 'ChIJ68J3tfpYwokR2HaRoBcB4xg';

  // let locations = getLocations();

  // let origin = locations[0];
  // console.log(origin);

  let waypts = [{location : {'placeId': worldTradeID}},
                {location : {'placeId': empireStateID}},
                {location : {'placeId': timesSquareID}},
                {location : {'placeId': centralParkID}},
              ];

  // Create a DirectionsRequest with hotel as start/end and 
  // POIs as waypoints (stops on the route)
  directionsService.route({
    origin: {'placeId': hotelID},
    destination: {'placeId': hotelID},
    waypoints: waypts,
    optimizeWaypoints: true,
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

function getLocations() {
  fetch('/get-map').then(response => response.json()).then(locations);
}