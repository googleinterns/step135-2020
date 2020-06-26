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
var script = document.createElement('script');
script.src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyCmQyeeWI_cV0yvh1SuXYGoLej3g_D9NbY&libraries=places&callback=initMap';
script.defer = true;
script.async = true;

var map;
var directionsService;
var directionsRenderer;

// Attach your callback function to the `window` object
window.initMap = function() {
  // Manhattan coords
  const coords = {lat: 40.771, lng: -73.974};

  // Create map centered on Manhattan
  map = new google.maps.Map(
      document.getElementById('routeMap'),
      {center: coords, 
      zoom: 13,
      streetViewControl: false,
  });

  // // Instantiate a directions service.
  directionsService = new google.maps.DirectionsService;

  // Create a renderer for directions and bind it to the map.
  directionsRenderer = new google.maps.DirectionsRenderer;
  directionsRenderer.setMap(map);
  directionsRenderer.setPanel(document.getElementById('rightPanel'));


  // Search for Google's office in Australia.
  var request = {
    location: map.getCenter(),
    radius: '500',
    query: 'Four Seasons Hotel New York'
  };

  var service = new google.maps.places.PlacesService(map);
  service.textSearch(request, callback);
}

// Checks that the PlacesServiceStatus is OK, and adds a marker
// using the place ID and location from the PlacesService.
function callback(results, status) {
  if (status == google.maps.places.PlacesServiceStatus.OK) {
    let timesSquareID = 'ChIJmQJIxlVYwokRLgeuocVOGVU';
    
    let centralParkID = 'ChIJ4zGFAZpYwokRGUGph3Mf37k';
    
    let worldTradeID = 'ChIJy7cGfBlawokR5l2e93hsoEA';
    let empireStateID = 'ChIJtcaxrqlZwokRfwmmibzPsTU';
    let hotelID = 'ChIJ68J3tfpYwokR2HaRoBcB4xg';


    let waypts = [{location : {'placeId': worldTradeID}},
                  {location : {'placeId': empireStateID}},
                  {location : {'placeId': timesSquareID}},
                  {location : {'placeId': centralParkID}},
                  ];

    // Retrieve the start and end locations and create a DirectionsRequest using
    directionsService.route({
      // Times square
      origin: {'placeId': hotelID},
      destination: {'placeId': hotelID},
      waypoints: waypts,
      optimizeWaypoints: true,
      travelMode: 'DRIVING'
    }, function(response, status) {
      // Route the directions and pass the response to a function to create
      // markers for each step.
      if (status === 'OK') {
        directionsRenderer.setDirections(response);
      } else {
        window.alert('Directions request failed due to ' + status);
      }
    });
  }

};

// Append the 'script' element to 'head'
document.head.appendChild(script);

// function initMap() {
//   // Manhattan coords
//   const coords = {lat: 40.771, lng: -73.974};

//   // Create map centered on Manhattan
//   let map = new google.maps.Map(
//       document.getElementById('routeMap'),
//       {center: coords, 
//       zoom: 13,
//       streetViewControl: false,
//   });

  // // Instantiate a directions service.
  // let directionsService = new google.maps.DirectionsService;

  // // Create a renderer for directions and bind it to the map.
  // let directionsRenderer = new google.maps.DirectionsRenderer({map: map});
  // directionsRenderer.setPanel(document.getElementById('rightPanel'));

  // // Instantiate an info window to hold step text.
  // let stepDisplay = new google.maps.InfoWindow;

  // let timesSquareID = 'ChIJmQJIxlVYwokRLgeuocVOGVU';
  // let centralParkID = 'ChIJ4zGFAZpYwokRGUGph3Mf37k';
  // let worldTradeID = 'ChIJy7cGfBlawokR5l2e93hsoEA';
  // let empireStateID = 'ChIJtcaxrqlZwokRfwmmibzPsTU';

  // let waypts = [{location : timesSquareID}, {location : centralParkID},
  //               {location : worldTradeID}, {location : empireStateID}];

  // let hotelID = 'ChIJ68J3tfpYwokR2HaRoBcB4xg';

  // // Retrieve the start and end locations and create a DirectionsRequest using
  // // WALKING directions.
  // directionsService.route({
  //   // Times square
  //   origin: {placeID:hotelID},
  //   destination: {placeID:hotelID},
  //   waypoints: waypts,
  //   optimizeWaypoints: true,
  //   travelMode: 'DRIVING'
  // }, function(response, status) {
  //   // Route the directions and pass the response to a function to create
  //   // markers for each step.
  //   if (status === 'OK') {
  //     directionsRenderer.setDirections(response);
  //   } else {
  //     window.alert('Directions request failed due to ' + status);
  //   }
  // });
// }

