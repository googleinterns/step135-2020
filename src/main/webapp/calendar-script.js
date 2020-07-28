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

let script = document.createElement('script');
script.src = 'https://maps.googleapis.com/maps/api/js?key=' + config.API_KEY + 
  '&libraries=places&callback=initMod';
script.defer = true;
script.async = true;

var map;

// Triggered upon DOM load.
window.initMod = function() {
  var calendarEl = document.getElementById('calendar');
  var calendar = new FullCalendar.Calendar(calendarEl, {
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth, timeGridWeek, timeGridDay, listWeek'
    },
    initialView: 'timeGridWeek',
    navLinks: true,
    dayMaxEvents: true, //alow "more" link when too many events on one day
    eventClick: function(info) {
      var eventObj = info.event;
      console.log(eventObj);

      // title of pop-up
      const modalLabel = document.getElementById('exampleModalLabel');
      modalLabel.innerHTML = eventObj.title;

      // body of pop-up
      $('#exampleModalBody').empty();
      const modalBody = document.getElementById('exampleModalBody');

      createMap2(modalBody, eventObj);
      $('#exampleModal').modal('show');
      console.log("8");
    }
  });
  getEvents(calendar);
  calendar.render();
};

/**
 * retrives the events from /calculate-trip url and dynamically adds the events
 */
function getEvents(calendar) {
  const urlParams = new URLSearchParams(window.location.search);
  const tripKey = urlParams.get('tripKey');
  const tripKeyQuery = (tripKey != null && tripKey != '') ? '?tripKey=' + tripKey : '';

  fetch('/get-calendar' + tripKeyQuery).then(response => response.json()).then((events) => {
    events.forEach((event) => {
      calendar.addEvent({     
        title: event.name,
        start: event.strStartTime,
        end: event.strEndTime,
        allDay: false,
        extendedProps: {
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
 * function that initializes map for popup, w specific address as marker
 */
function createMap(modalBody, eventObj) {
  var geocoder = new google.maps.Geocoder();
  var coords;

  // text to dispaly in popover
  // let infoDisplay = document.createElement('p');
  // infoDisplay.innerHTML = '<b>Address: </b>' + eventObj.extendedProps.address + '<br>' +
  // '<b>Opening hours: </b>' + eventObj.extendedProps.openTime + '<br>' +
  // '<b>Closing hours: </b>' + eventObj.extendedProps.closeTime + '<br>';
  // modalBody.appendChild(infoDisplay)

  let infoDisplay = document.createElement('p');
  infoDisplay.innerHTML = '<b>Address: </b>' + eventObj.extendedProps.address + '<br>' +
  '<b>Opening hours: </b>' + eventObj.extendedProps.openTime + '<br>' +
  '<b>Closing hours: </b>' + eventObj.extendedProps.closeTime + '<br>';
  modalBody.appendChild(infoDisplay)

  // create new div to hold map
  const mapDis = document.createElement('div');
  mapDis.id = 'map'
  modalBody.appendChild(mapDis);

  geocoder.geocode({ placeId: eventObj.extendedProps.address}, function(results, status) {
    if (status == google.maps.GeocoderStatus.OK) {
      var latitude = results[0].geometry.location.lat();
      var longitude = results[0].geometry.location.lng();
      coords = {lat: latitude, lng: longitude};
    }

    var map = new google.maps.Map(document.getElementById('map'), {
      zoom: 4,
      center: coords
    });
    var marker = new google.maps.Marker({
      position: coords,
      map: map,
      title: eventObj.title
    });
  });
}

function createMap2(modalBody, eventObj) {
  let infoDisplay = document.createElement('p');
  infoDisplay.innerHTML = '<b>Address: </b>' + eventObj.extendedProps.address + '<br>' +
  '<b>Opening hours: </b>' + eventObj.extendedProps.openTime + '<br>' +
  '<b>Closing hours: </b>' + eventObj.extendedProps.closeTime + '<br>';
  modalBody.appendChild(infoDisplay)

  // create new div to hold map
  const mapDis = document.createElement('div');
  mapDis.id = 'map';
  modalBody.appendChild(mapDis);

  // instantiate map
  map = new google.maps.Map(document.getElementById('map'), {
      zoom: 14,
      center: new google.maps.LatLng(0, 0)
    });

  console.log("1");
  var service = new google.maps.places.PlacesService(map);
  console.log("2");
  service.getDetails({
    placeId: eventObj.extendedProps.placeId
  }, function(result, status) {
    console.log("3");
    if (status != google.maps.places.PlacesServiceStatus.OK) {
      console.log("4");
      alert(status);
      return;
    }
    console.log("5");
    map.setCenter(result.geometry.location);

    console.log("6");
    var marker = new google.maps.Marker({
      map: map,
      position: result.geometry.location
    });
    console.log("7");
  });
}

// Append the 'script' element to the document head.
document.head.appendChild(script);
