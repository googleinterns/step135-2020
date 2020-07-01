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
  // Redirect to homepage if user is not signed in.
  getAuthObject().then((authObject) => {
    if (!authObject.loggedIn) {
      window.location.replace('/');
    } else {
      // Add the link to the "sign out" a element.
      const signOutLink = document.getElementById('sign-out-link');
      signOutLink.href = authObject.logoutUrl;

      displayHeader();
    }
  });

  var calendarEl = document.getElementById('calendar');
  var calendar = new FullCalendar.Calendar(calendarEl, {
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth, timeGridWeek, timeGridDay, listWeek'
    },
    initialView: 'timeGridWeek',
    initialDate: '2020-06-29',
    eventLimit: true,
    events: [{
            title: 'Golden Gate Bridge',
            start: '2020-06-29T09:00:00',
            end: '2020-06-29T10:00:00',
            allDay: false
        },
        {
            title: 'Fishermans Wharf',
            start: '2020-06-29T10:30:00',
            end: '2020-06-29T11:30:00',
            allDay: false
        },
        {
            title: 'Mission District',
            start: '2020-06-29T12:00:00',
            end: '2020-06-29T13:00:00',
            allDay: false
        },
        {
            title: 'Lunch Time',
            start: '2020-06-29T13:00:00',
            end: '2020-06-29T14:00:00',
            allDay: false
        },
        {
            title: 'Sutro Tower',
            start: '2020-06-29T14:30:00',
            end: '2020-06-29T15:30:00',
            allDay: false
        },
        {
            title: 'West Portal Neighborhood',
            start: '2020-06-29T16:00:00',
            end: '2020-06-29T17:00:00',
            allDay: false
        },
    ]
  });
  calendar.render();
});

// Returns Promise with the auth object, containing login status and information.
function getAuthObject() {
  return new Promise((resolve, reject) => {
    fetch('/auth').then(response => response.json()).then((authObject) => {
      resolve(authObject);
    }).catch((error) => {
      reject(error);
    });
  });
}

function displayHeader() {
  // Display header for site.
  const header = document.getElementById('header');
  header.style.display = 'block';
}