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
let mapScript = document.createElement('script');
mapScript.src = 'https://maps.googleapis.com/maps/api/js?key=' + config.API_KEY + 
  '&libraries=places&callback=initMap';
mapScript.defer = true;
mapScript.async = true;

var map;
var directionsService;
var directionsRenderer;
var calendar;
var mapMarkers = [];

// Map marker label constant
var labels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

// constant for zoom level of map
const zoomThirteen = 13;

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

  document.getElementById('inputMapDate').onchange = () => {
    const date = document.getElementById('inputMapDate').value;
    calendar.gotoDate(date);
    clearMarkers();
    if (date !== '') {
      displayRouteOnMap(date);
    }
  };

  // Get locations from MapServlet and display directions on map.
  displayRouteOnMap('');
}

/*
 * Gets locations from MapServlet with the tripKey parameter.
 * Calls showDirections to show the directions with those locations.
 */
function displayRouteOnMap(date) {
  const urlParams = new URLSearchParams(window.location.search);
  const tripKey = urlParams.get('tripKey');
  const tripKeyQuery = (tripKey != null && tripKey != '') ? '?tripKey=' + tripKey : '';
  const dateQuery = (date != null && date != '') ? '&date=' + date : '';
  
  fetch('/get-map' + tripKeyQuery + dateQuery).then(response => response.json()).then((locations) => {
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
        mapMarkers.push(marker);
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

function clearMarkers() {
  for (let i = 0; i < mapMarkers.length; i++) {
    mapMarkers[i].setMap(null);
  }
  mapMarkers.length = 0;
}

// Append the 'script' element to 'head'
document.head.appendChild(mapScript);

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

// let script = document.createElement('script');
// script.src = 'https://maps.googleapis.com/maps/api/js?key=' + config.API_KEY + 
//   '&libraries=places&callback=initMod';
// script.defer = true;
// script.async = true;



// Triggered upon DOM load.
$(document).ready(() => {
  const calendarEl = document.getElementById('mapCalendar');
  calendar = new FullCalendar.Calendar(calendarEl, {
    headerToolbar: {
      start: '',
      center: '',
      end: ''
    },
    initialView: 'timeGridDay',
    navLinks: true,
    dayMaxEvents: true, //alow "more" link when too many events on one day
    eventClick: function(info) {
      const eventObj = info.event;

      // No popup is shown for the first event.
      if (eventObj.title === 'Depart Hotel') {
        return;
      }

      // title of pop-up
      const modalLabel = document.getElementById('exampleModalLabel');
      modalLabel.innerHTML = eventObj.title;

      // body of pop-up
      $('#exampleModalBody').empty();
      const modalBody = document.getElementById('exampleModalBody');
      
      createMap(modalBody, eventObj);
      $('#exampleModal').modal('show');
    }
  });
  getEvents(calendar);
  // document.getElementById('inputMapDate').onchange = () => {
  //   const date = document.getElementById('inputMapDate').value
  //   calendar.gotoDate(date);
  // };

  calendar.render();
});

/**
 * retrives the events from /calculate-trip url and dynamically adds the events
 */
function getEvents(calendar) {
  const tripKeyQuery = getTripKeyQuery();
  var first = true;
  var initialDate;
  var startDate;

  fetch('/get-calendar' + tripKeyQuery).then(response => response.json()).then((events) => {
    events.forEach((event) => {
      if (first) {
        initialDate = event.strStartTime.split('T')[0];
        startDate = initialDate;
        first = false;
        createInitialEvent(calendar, initialDate);
      }
      let newDate = event.strStartTime.split('T')[0];
      if (newDate != initialDate) {
        initialDate = newDate;
        createInitialEvent(calendar, initialDate);
      }
      if (newDate < startDate) {
        startDate = newDate;
      }

      calendar.gotoDate(startDate);
      document.getElementById('inputMapDate').value = startDate;
      calendar.addEvent({     
        title: event.name,
        start: event.strStartTime,
        end: event.strEndTime,
        allDay: false,
        extendedProps: {
          stringDate: event.strStartTime,
          address: event.address,
          placeId: event.placeId, 
          openTime: '9AM',
          closeTime: '5PM',
        }
      });
    });
  });
}

/**
 * Create the initial event of leaving the hotel
 */
function createInitialEvent(calendar, initialDate) {
  calendar.addEvent({
    title: 'Depart Hotel',
    start: initialDate + 'T09:30:00',
    end: initialDate + 'T10:00:00',
    allDay: false
  });
}

/**
 * Create the initial event of leaving the hotel
 */
function createLastEvent(calendar, startTime) {
  var d1 = new Date(startTime);
  var d2 = new Date(d1);
  d2.setMinutes(d1.getMinutes() + 30);
  calendar.addEvent({
    title: 'Return to Hotel',
    start: startTime,
    end: d2.toString(),
    allDay: false
  });
}

/**
 * Dyanmically displays the address (w/ link), open and closing hours
 * and the map and marker of the event
 */
function createMap(modalBody, eventObj) {
  // infoDisplay holds address and openHours
  const infoDisplay = document.createElement('div');
  
  // create the address with link
  createAddressLine(modalBody, eventObj, infoDisplay);

  // create new div to hold map
  instantiateMapDiv(modalBody);

  // instantiate map
  const map = new google.maps.Map(document.getElementById('map'), {
    zoom: zoomThirteen
  });
  const service = new google.maps.places.PlacesService(map);
  service.getDetails({
    placeId: eventObj.extendedProps.placeId
  }, function(result, status) {
    if (status != google.maps.places.PlacesServiceStatus.OK) {
      alert(status);
      return;
    }
    // set center of map
    map.setCenter(result.geometry.location);
  
    // add open hours to mod info Display
    let openHours = document.createElement('p');

    // if there are open hours display them, otherwise open all day
    let openTime;
    let closeTime;
    try {
      const stringFullDate = eventObj.extendedProps.stringDate;
      const dateStr = stringFullDate.split('T')[0]; 
      const intOfWeek = getIntOfWeek(dateStr);

      openTime = formatAMPM(result.opening_hours.periods[intOfWeek].open.time); 
      closeTime = formatAMPM(result.opening_hours.periods[intOfWeek].close.time);

      createOpenCloseHours('Open', openTime, openHours);
      createOpenCloseHours('Close', closeTime, openHours);
    } catch(e) {
      // create bold element open all day if no hours available
      const boldAllDay = document.createElement('b');
      boldAllDay.innerText = 'Open All Day ';
      openHours.appendChild(boldAllDay);
    }

    infoDisplay.appendChild(openHours);

    const marker = new google.maps.Marker({
      map: map,
      position: result.geometry.location
    });
  });
}

// create the address in the popup, add it to modalBody
function createAddressLine(modalBody, eventObj, infoDisplay) {
  const addressLine = document.createElement('p');
  const boldAddress = document.createElement('b');
  boldAddress.innerText = 'Address: ';
  addressLine.appendChild(boldAddress);

  const urlFormatAddress = eventObj.extendedProps.address.replace(/\s/g, '+')
  const url = 'https://www.google.com/maps/search/?api=1&query=' + urlFormatAddress + '&query_place_id=' + eventObj.extendedProps.placeId;
  const link = document.createElement('a');
  link.href = url;
  link.target = '_blank';
  link.innerText = eventObj.extendedProps.address

  addressLine.appendChild(link);
  addressLine.appendChild(document.createElement('br'));
  infoDisplay.appendChild(addressLine);

  // infoDisplay.appendChild(address);
  modalBody.appendChild(infoDisplay);
}

// create div to hold map
function instantiateMapDiv(modalBody, map) {
  const mapDis = document.createElement('div');
  mapDis.id = 'map';
  modalBody.appendChild(mapDis);
}

// takes a string in date format and return the int of the week
function getIntOfWeek(date) {
  const intOfWeek = new Date(date).getDay();    
  return isNaN(intOfWeek) ? null : 
    intOfWeek;
}

// converts from 24hr (hh:mm) to 12hr format (hh:mm AM/PM)
function formatAMPM(time) {
  let hours = time.substring(0, 2);
  let minutes = time.substring(2, 4);
  const ampm = hours >= 12 ? 'pm' : 'am';
  hours = hours % 12;
  hours = hours ? hours : 12; // the hour '0' should be '12'
  const strTime = hours + ':' + minutes + ' ' + ampm;
  return strTime;
}

// create bolded hours section
function createOpenCloseHours(type, time, openHours) {
  if (type === 'open' || type === 'close') {
    throw new IllegalArgumentException("hours must be open or close")
  }
  const holder = document.createElement('p');
  const boldType = document.createElement('b');
  boldType.innerText = type + ': ';
  holder.appendChild(boldType);
  holder.innerHTML += time;
  holder.appendChild(document.createElement('br'));
  openHours.appendChild(holder);
}

// // Append the 'script' element to the document head.
// document.head.appendChild(script);

