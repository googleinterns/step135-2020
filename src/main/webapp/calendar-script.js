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

    eventMouseEnter: function (MouseEnterInfo) {
        MouseEnterInfo.el.popover({
            title: 'titleTest',
            content: 'content holder',
            trigger: 'hover',
            placement: 'top',
            container: 'body'
        });
    },

    eventClick: function(info) {
      var eventObj = info.event;

      alert(
          'title: ' + eventObj.title + '.\n' +
          'Address: ' + eventObj.extendedProps.address + '.\n' +
          'Opening hours: ' + eventObj.extendedProps.openTime + '.\n' +
          'Closing hours: ' + eventObj.extendedProps.closeTime + '.\n'
        );
    }
  });
  getEvents(calendar);
  calendar.render();
  createPopOver();
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
 * Create popover functions
 */
function createPopOver() {
  const cal = document.getElementById('practiceContent');
  const prac = document.createElement('a');
  prac.id = 'temp123';
  prac.modal = 'popover';
  prac.title = 'Title ex';
  prac.content = 'to display';
  $(prac.id).popover('toggle');
}
