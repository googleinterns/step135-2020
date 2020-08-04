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

// Map marker label constant
var labels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

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
  directionsRenderer = new google.maps.DirectionsRenderer({suppressMarkers: true});
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

  // Add markers to map.
  addPoiMarker(origin, true, 0);  
  // Markers must be added after map is recentered by directionsRenderer (above).
  for (let i = 0; i < waypts.length; i++) {
    let index = i + 1;
    addPoiMarker(waypts[i].location, false, index);
  }
}

/* 
 * Adds marker to map with appropriate letter label and an info window.
 * location: String name or address of the POI.
 * isOrigin: boolean indicating if the location is the origin (hotel) or not.
             Used to determine the icon of the map marker.
 * index: index of waypoint. Used to determine marker label.
 */
function addPoiMarker(location, isOrigin, index) {
  // Query the location String to get Place details.
  var service = new google.maps.places.PlacesService(map);
  service.textSearch({
    location: map.getCenter(),
    radius: '50000',
    query: location
    }, function(results, status) {
      if (status == google.maps.places.PlacesServiceStatus.OK) {
        // Add marker to map.
        let marker = new google.maps.Marker({
          map: map,
          place: {
            placeId: results[0].place_id,
            location: results[0].geometry.location
          },
        });
        // Change icon to green if the location is the origin.
        if (isOrigin) {
          marker.setIcon("http://maps.google.com/mapfiles/ms/icons/green-dot.png");
        } else {
          marker.setLabel({
            color: "white",
            // Set text to correct letter label.
            text: labels[index % labels.length]
          });
        } 
        // Add info window to marker.     
        addInfoWindow(results, marker);
      } else {
        alert(status);
        return;
      }
  });
}

/* 
 * Adds info window to a marker.
 * Info window includes place name, address, and link to Google Maps.
 */
function addInfoWindow(results, marker) {
  // Format and add place name, address, and link to Google Maps.
  const info = document.createElement('div');
  info.id = 'infoWindow';

  // Add place name in bold.
  const boldName = document.createElement('b');
  boldName.innerText = results[0].name;
  info.appendChild(boldName);

  // Add formatted address.
  const address = document.createElement('p');
  address.innerText = results[0].formatted_address;
  info.appendChild(address);

  // Add link to Google Maps.
  const urlFormatAddress = results[0].name.replace(/\s/g, '+');
  const url = 'https://www.google.com/maps/search/?api=1&query=' + urlFormatAddress 
              + '&query_place_id=' + results[0].placeId;
  const link = document.createElement('a');
  link.href = url;
  link.target = '_blank';
  link.innerText = 'View on Google Maps';
  info.appendChild(link);

  // Construct infoWindow.
  const infoWindow = new google.maps.InfoWindow({
    content: info,
    maxWidth: 200
  });
  // Open/close infoWindow on marker click.
  let infoWindowOpen = false;
  marker.addListener("click", () => {
    if (infoWindowOpen) {
      infoWindow.close();
      infoWindowOpen = false;
    } else {
      infoWindow.open(map, marker);
      infoWindowOpen = true;            
    }
  });
  // Close infoWindow on map click.
  map.addListener("click", () => {
    infoWindow.close();
    infoWindowOpen = false;
  });
}

// Append the 'script' element to 'head'
document.head.appendChild(script);
