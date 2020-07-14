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
  '&libraries=places&callback=initScript';
script.defer = true;
script.async = true;

// Triggered upon DOM load.
$(document).ready(() => {
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

      // title of pop-up
      const modalLabel = document.getElementById('exampleModalLabel');
      modalLabel.innerHTML = eventObj.title;

      //body of pop-up
      const modalBody = document.getElementById('exampleModalBody');
      modalBody.innerHTML = '<b>Address: </b>' + eventObj.extendedProps.address + '<br>' +
          '<b>Opening hours: </b>' + eventObj.extendedProps.openTime + '<br>' +
          '<b>Closing hours: </b>' + eventObj.extendedProps.closeTime
      $('#exampleModal').modal('show');
    }
  });
  getEvents(calendar);
  calendar.render();  
});

/**
 * retrives the events from /calculate-trip url and dynamically adds the events
 */
function getEvents(calendar) {
  fetch('/get-calendar').then(response => response.json()).then((events) => {
    events.forEach((event) => {
      console.log(event.address);
      calendar.addEvent({     
        title: event.name,
        start: event.strStartTime,
        end: event.strEndTime,
        allDay: false,
        extendedProps: {
          address: event.address,
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
function createMap(address) {
  var geocoder = new google.maps.Geocoder();

  geocoder.geocode( { 'address': address}, function(results, status) {

  if (status == google.maps.GeocoderStatus.OK) {
    var latitude = results[0].geometry.location.lat();
    var longitude = results[0].geometry.location.lng();
  }

  console.log(latitude);
  console.log(longitude);

  var coords = {lat: latitude, lng: longitude};

  var map = new google.maps.Map(document.getElementById('map'), {
    zoom: 4,
    center: coords
  });

  var marker = new google.maps.Marker({
    position: coords,
    map: map,
    title: 'Hello World!'
    });
  });
}

// Append the 'script' element to the document head.
document.head.appendChild(script);
