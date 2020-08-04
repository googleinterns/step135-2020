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

// constant for zoom level of map
const zoomThirteen = 13;

// Triggered upon DOM load.
window.initMod = function() {
  const calendarEl = document.getElementById('calendar');
  const calendar = new FullCalendar.Calendar(calendarEl, {
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth, timeGridWeek, timeGridDay, listWeek'
    },
    initialView: 'timeGridWeek',
    navLinks: true,
    dayMaxEvents: true, //alow "more" link when too many events on one day
    eventClick: function(info) {
      const eventObj = info.event;

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

  fetch('/get-calendar' + tripKeyQuery).then(response => {
    if (response.redirected) {
      // Redirect to new page; causes an error, but this disappears as the new page refreshes.
      window.location.href = response.url;
    } else {
      return response.json();
    }
  }).then(events => {
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
  }).catch(error => {
    console.error(error);
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

// Append the 'script' element to the document head.
document.head.appendChild(script);
