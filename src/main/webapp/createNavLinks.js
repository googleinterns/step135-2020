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
  createGroupNavLinks();
});

/**
 * Create the group nav links for the calendar and maps pages.
 */
function createGroupNavLinks() {
  // Get the calendar and maps button links.
  const calendarButton = document.getElementById('button-group-nav-calendar');
  const mapsButton = document.getElementById('button-group-nav-maps');

  // Get the tripKey from the URL to add to the button links.
  const tripKeyQuery = getTripKeyQuery();

  // Add the href links to the buttons.
  calendarButton.href = '/calendar.html' + tripKeyQuery;
  mapsButton.href = '/maps.html' + tripKeyQuery;
}
