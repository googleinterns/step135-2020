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


document.addEventListener('DOMContentLoaded', function() {
  var calendarEl = document.getElementById('calendar');
  var calendar = new FullCalendar.Calendar(calendarEl, {
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth, timeGridWeek, timeGridDay, listWeek'
    },
    initialView: 'timeGridWeek',
    initialDate: '2020-06-29',
    navLinks: true,
    dayMaxEvents: true, //alow "more" link when too many events on one day
  });
  /** 
  fetch('/calculate-trip').then(respone => respone.json()).then((events) => {
    events.forEach((event) => {
      calendar.addEvent({
        
        title: event.name,
        start: event.strStartTime,
        end: event.strEndTime,
        allDay: false
      });
    });
  });*/
  getEvents(calendar);
  calendar.render();
});

/**
 * retrives the events from /calculate-trip url and dynamically adds the events
 */
function getEvents(calendar) {
  fetch('/calculate-trip').then(respone => respone.json()).then((events) => {
    events.forEach((event) => {
      calendar.addEvent({
        title: event.name,
        start: event.strStartTime,
        end: event.strEndTime,
        allDay: false
      });
    });
  });
}
