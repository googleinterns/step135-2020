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
var service;

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
  calendar.render();
}

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

function createMap(modalBody, eventObj) {
  const infoDisplay = document.createElement('div');
  let address = document.createElement('p');
  var urlFormatAddress = eventObj.extendedProps.address.replace(/\s/g, '+')
  var url = 'https://www.google.com/maps/search/?api=1&query=' + urlFormatAddress + '&query_place_id=' + eventObj.extendedProps.placeId;
  infoDisplay.innerHTML = '<b>Address: </b>' + '<a href=' + url + '>' + eventObj.extendedProps.address + '</a>' + '<br>';
  infoDisplay.appendChild(address);
  modalBody.appendChild(infoDisplay)

  // create new div to hold map
  const mapDis = document.createElement('div');
  mapDis.id = 'map';
  modalBody.appendChild(mapDis);

  // instantiate map
  map = new google.maps.Map(document.getElementById('map'), {
      zoom: 13,
      center: new google.maps.LatLng(0, 0)
    });

  service = new google.maps.places.PlacesService(map);
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
    try {
      var stringFullDate = eventObj.extendedProps.stringDate;
      var dateStr = stringFullDate.split('T')[0]; 
      var intOfWeek = getintOfWeek(dateStr);

      var dateObj = new Date(stringFullDate);
      var openTime = formatAMPM(result.opening_hours.periods[intOfWeek].open.time);
      var closeTime = formatAMPM(result.opening_hours.periods[intOfWeek].close.time);
      openHours.innerHTML = '<b>Open: </b>' +  openTime + '</br>' +
          '<b>Close: </b>' + closeTime;
    } catch(e) {
      openHours.innerHTML = '<b> Open All Day </b>';
    }

    infoDisplay.appendChild(openHours);

    var marker = new google.maps.Marker({
      map: map,
      position: result.geometry.location
    });
  });
}

// takes a string in date format and return the int of the week
function getintOfWeek(date) {
  const intOfWeek = new Date(date).getDay();    
  return isNaN(intOfWeek) ? null : 
    intOfWeek;
}

// converts from 24hr (hh:mm) to 12hr format (hh:mm AM/PM)
function formatAMPM(time) {
  var hours = time.substring(0, 2);
  var minutes = time.substring(2, 4);
  var ampm = hours >= 12 ? 'pm' : 'am';
  hours = hours % 12;
  hours = hours ? hours : 12; // the hour '0' should be '12'
  minutes = (minutes < 10 && minutes > 0) ? '0' + minutes : minutes;
  var strTime = hours + ':' + minutes + ' ' + ampm;
  return strTime;
}

// Append the 'script' element to the document head.
document.head.appendChild(script);
