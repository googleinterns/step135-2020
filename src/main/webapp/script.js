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

  // Initially make the "Next" button disabled.
  const toggleStartTripStageButton = document.getElementById('toggle-stage-button');
  toggleStartTripStageButton.disabled = true;

  // Initially hide the "Add POIs" form and "Submit" button.
  const addPoiContainer = document.getElementById('add-pois-container');
  addPoiContainer.style.display = 'none';
  const startTripSubmitButton = document.getElementById('submit-calculate-trip');
  startTripSubmitButton.style.display = 'none';
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

// Checks whether the input is a valid location, and adds the 'is-valid' 
// Bootstrap class; otherwise, removes the 'is-valid' class if it exists.
function checkValidLocation(elementId) {
  const locationInput = document.getElementById(elementId);

  // If an input exists, add 'is-valid' class.
  if (locationInput.value !== '') {
    locationInput.classList.add('is-valid');
  } else if (locationInput.classList.contains('is-valid')) {
    locationInput.classList.remove('is-valid');
  }
}

// Checks whether the input is a valid date, and adds the 'is-valid' 
// Bootstrap class; otherwise, removes the 'is-valid' class if it exists.
function checkValidDate(elementId) {
  const dateInput = document.getElementById(elementId);

  // If an input exists, add 'is-valid' class.
  if (dateInput.value !== '') {
    dateInput.classList.add('is-valid');
  } else if (dateInput.classList.contains('is-valid')) {
    dateInput.classList.remove('is-valid');
  }
}

// Assign the current start date value to the end date value, as the MVP only 
// allows one-day trips.
function setEndDateValue() {
  const startDateInput = document.getElementById('inputStartDate');
  const endDateInput = document.getElementById('inputEndDate');
  endDateInput.value = startDateInput.value;
  checkValidDate('inputEndDate');
}

// If location and date forms have valid inputs, enable next button; otherwise,
// disable button.
function checkNextButton() {
  const toggleStartTripStageButton = document.getElementById('toggle-stage-button');

  // Get three input fields.
  const destinationInput = document.getElementById('inputDestination');
  const startDateInput = document.getElementById('inputStartDate');
  const endDateInput = document.getElementById('inputEndDate');

  // Enable next button if all forms have valid input.
  if (destinationInput.classList.contains('is-valid') && 
    startDateInput.classList.contains('is-valid') &&
    endDateInput.classList.contains('is-valid')) {
      
    toggleStartTripStageButton.disabled = false;
  } else {
    toggleStartTripStageButton.disabled = true;
  }
}

/**
 * Toggle the start trip input stage, initiated from click of "Next/Back" button.
 * If toggle "Next/Back" button equals "Next", set location and date inputs to
 * readonly and show POI input and submit button.
 * If toggle "Next/Back" button equals "Back", set location and date inputs to 
 * editable and hide POI input and submit button.
 */
function toggleStartTripInputStage() {
  const toggleStartTripStageButton = document.getElementById('toggle-stage-button');

  if (toggleStartTripStageButton.value === 'Next') {
    // Display the "Add POIs" form and "Submit" button.
    const addPoiContainer = document.getElementById('add-pois-container');
    addPoiContainer.style.display = 'flex';
    const startTripSubmitButton = document.getElementById('submit-calculate-trip');
    startTripSubmitButton.style.display = 'inline-block';

    // Change location and date inputs to be readonly.
    const destinationInput = document.getElementById('inputDestination');
    destinationInput.readOnly = true;
    const startDateInput = document.getElementById('inputStartDate');
    startDateInput.readOnly = true;

    // Change the text of the toggle button to 'Back'.
    toggleStartTripStageButton.value = 'Back';
  } else {
    // Hide the "Add POIs" form and "Submit" button.
    const addPoiContainer = document.getElementById('add-pois-container');
    addPoiContainer.style.display = 'none';
    const startTripSubmitButton = document.getElementById('submit-calculate-trip');
    startTripSubmitButton.style.display = 'none';

    // Change location and date inputs to be editable.
    const destinationInput = document.getElementById('inputDestination');
    destinationInput.readOnly = false;
    const startDateInput = document.getElementById('inputStartDate');
    startDateInput.readOnly = false;

    // Change the text of the toggle button to 'Next'.
    toggleStartTripStageButton.value = 'Next';
  }
}