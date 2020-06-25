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
    header: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,dayGridWeek, dayGridDay, listWeek'
    },
    initialView: 'dayGridMonth',
    initialDate: '2018-11-18',
    eventLimit: true,
    events: [{
            title: 'Front-End Conference',
            start: '2018-11-16',
            end: '2018-11-18',
            allDay: false
        },
        {
            title: 'Hair stylist with Mike',
            start: '2018-11-20',
            allDay: true
        },
        {
            title: 'Car mechanic',
            start: '2018-11-14T09:00:00',
            end: '2018-11-14T11:00:00'
        },
        {
            title: 'Dinner with Mike',
            start: '2018-11-21T19:00:00',
            end: '2018-11-21T22:00:00'
        },
        {
            title: 'Chillout',
            start: '2018-11-15',
            allDay: true
        },
        {
            title: 'Vacation',
            start: '2018-11-23',
            end: '2018-11-29'
        },
    ]
  });
  calendar.render();
});
