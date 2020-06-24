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

function initMap() {
  // Manhattan coords
  const coords = {lat: 40.771, lng: -73.974};

  // Create map centered on Manhattan
  let map = new google.maps.Map(
      document.getElementById('routeMap'),
      {center: coords, 
      zoom: 13,
      streetViewControl: false,
  });

  // Instantiate a directions service.
  let directionsService = new google.maps.DirectionsService;

  // Create a renderer for directions and bind it to the map.
  let directionsRenderer = new google.maps.DirectionsRenderer({map: map});

  // Instantiate an info window to hold step text.
  let stepDisplay = new google.maps.InfoWindow;
}
