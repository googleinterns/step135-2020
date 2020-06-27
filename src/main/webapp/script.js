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
  // Set up trigger to add hidden POI form elements upon submission.
  addHiddenPoiFormTrigger();

  isSignedIn().then((signInStatus) => {
    // Display the sign-in page or "start trip" form depending on sign in status.
    if (signInStatus) {
      displayStartTripDesign();
    } else {
      displaySignInPage();
    }
  }).catch((error) => {
    // If an error occurs, print error to console and do not display button.
    console.error(error);
  })
});

// Returns Promise with the sign in status in a boolean.
function isSignedIn() {
  return new Promise((resolve, reject) => {
    fetch('/auth').then(response => response.json()).then((signInStatus) => {
      resolve(signInStatus);
    }).catch((error) => {
      reject(error);
    });
  });
}

// Update the necessary design elements and display the sign in page.
function displaySignInPage() {
  // Set the width of the content container.
  setContentWidth('650px');

  // Add background image to site. (This id is defined in CSS.)
  document.body.id = 'body-background-image';

  // Get the homepage "registration" block to and add elements.
  const indexRegistrationBlock = document.getElementById('index-registration-block');
  indexRegistrationBlock.style.display = 'block';
}

// User is signed in, show the start trip homepage.
function displayStartTripDesign() {
  // Set the width of the content container.
  setContentWidth('800px');

  // Remove background image id from body.
  document.body.removeAttribute('id');

  // Get the homepage "start trip" block to and add elements.
  const indexStartTripBlock = document.getElementById('index-start-trip-block');
  indexStartTripBlock.style.display = 'block';

  // Display the site header.
  displayHeader();

  // Display the "start trip" form.
  displayStartTripForm();
}

function displayHeader() {
  // Display header for site.
  const header = document.getElementById('header');
  header.style.display = 'block';
}

// Set the width of the content container.
function setContentWidth(width) {
  const contentContainer = document.getElementById('content');
  contentContainer.style.width = width;
}

// Display the start trip input form, with location, dates, and POIs.
function displayStartTripForm() {
  // Get the homepage "start trip" block and add elements.
  const indexStartTripBlock = document.getElementById('index-start-trip-block');
  indexStartTripBlock.style.display = 'block';
}

// Set up trigger to add hidden POI elements.
function addHiddenPoiFormTrigger() {
  $('#startTripForm').submit(() => {
    // Fetch the current POI inputs, and add them to the form.
    const poiInputs = document.getElementsByClassName('poi-input');
    let count = 1;
    Array.prototype.forEach.call(poiInputs, (poiInput) => {
      $('<input>').attr('type', 'hidden')
      .attr('name', 'poi-' + count)
      .attr('value', poiInput.name)
      .appendTo('#startTripForm');
      count++;
    });
    return true;
  });
}
