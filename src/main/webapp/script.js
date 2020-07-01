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
  // Add Google Places location autofill to input fields.
  addLocationAutofill();

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

  // Initially hide the POI list, "Add POIs" form, and "Submit" button.
  const poiListContainer = document.getElementById('poi-list-container');
  poiListContainer.style.display = 'none';
  const addPoiContainer = document.getElementById('add-pois-container');
  addPoiContainer.style.display = 'none';
  const startTripSubmitButton = document.getElementById('submit-calculate-trip');
  startTripSubmitButton.style.display = 'none';

  // Set "Submit" button to disabled; enable once all forms are filled out.
  startTripSubmitButton.disabled = true;

  // Set "Add POI" button to disabled; enable once text input is valid.
  const addPoiButton = document.getElementById('addPoiButton');
  addPoiButton.disabled = true;

  // Add onchange listeners to inputs in "start trip" form.
  addStartTripOnChangeListeners();
}

// Add onchange listeners to inputs that call relevant functions in the
// "start trip" form.
function addStartTripOnChangeListeners() {
  // Input day of travel onchange listeners to check input and and next / submit.
  const inputDayOfTravel = document.getElementById('inputDayOfTravel');
  inputDayOfTravel.onchange = () => {
    checkValidInput('inputDayOfTravel');
    checkNextButton();
  };

  // Input POI onchange listeners to check input and enable add POI button.
  const inputPoi = document.getElementById('inputPoi');
  inputPoi.onchange = () => {
    checkAddPoiButton();
  };
}

// Set up trigger to add hidden POI elements.
function addHiddenPoiFormTrigger() {
  $('#startTripForm').submit(() => {
    // Fetch the current POI inputs, and add them to the form.
    const poiInputs = document.getElementsByClassName('poi-input');
    let count = 1;
    Array.prototype.forEach.call(poiInputs, (poiInput) => {
      let poiNumber = 'poi-' + count;
      addHiddenInput(poiNumber, poiNumber, poiInput.name);
      count++;
    });
    return true;
  });
}

// Add hidden input to the "start trip" form.
function addHiddenInput(name, id, value) {
  $('<input>').attr('type', 'hidden')
    .attr('name', name)
    .attr('id', id)
    .attr('value', value)
    .appendTo('#startTripForm');
}

// Checks whether the input is valid (non-empty), and adds the 'is-valid'
// Bootstrap class; otherwise, removes the 'is-valid' class if it exists.
function checkValidInput(elementId) {
  const input = document.getElementById(elementId);

  // If an input exists, add 'is-valid' class.
  if (input.value !== '') {
    input.classList.add('is-valid');
  } else if (input.classList.contains('is-valid')) {
    input.classList.remove('is-valid');
  }
}

// Returns true if all location and date inputs are valid; otherwise, false;
function isLocationDateInputValid() {
  // Get three input fields.
  const inputDestination = document.getElementById('inputDestination');
  const inputDayOfTravel = document.getElementById('inputDayOfTravel');

  return inputDestination.classList.contains('is-valid') && 
    inputDayOfTravel.classList.contains('is-valid');
}

// Return true if user has submitted a POI; otherwise, false.
function isPoiSubmitted() {
  const poiInputs = document.getElementsByClassName('poi-input');
  return poiInputs.length !== 0;
}

// If location and date forms have valid inputs, enable next button; otherwise,
// disable button.
function checkNextButton() {
  const toggleStartTripStageButton = document.getElementById('toggle-stage-button');

  // Enable next button if all forms have valid input.
  if (isLocationDateInputValid()) {    
    toggleStartTripStageButton.disabled = false;
  } else {
    toggleStartTripStageButton.disabled = true;
  }
}

// If location and date forms have valid inputs, and there is at least one POI,
// enable submit button; otherwise, disable button.
function checkSubmitButton() {
  const startTripSubmitButton = document.getElementById('submit-calculate-trip');

  // Enable submit button if all forms have valid input.
  if (isLocationDateInputValid() && isPoiSubmitted()) {    
    startTripSubmitButton.disabled = false;
  } else {
    startTripSubmitButton.disabled = true;
  }
}

// If POI text input is valid, enable "Add POI" button; otherwise, disable button.
function checkAddPoiButton() {
  const addPoiButton = document.getElementById('addPoiButton');
  const inputPoi = document.getElementById('inputPoi');

  // Enable submit button if text input POI is valid.
  if (inputPoi.classList.contains('is-valid')) {
    addPoiButton.disabled = false;
  } else {
    addPoiButton.disabled = true;
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
    // Display the POI list, "Add POIs" form, and "Submit" button.
    const poiListContainer = document.getElementById('poi-list-container');
    poiListContainer.style.display = 'block';
    const addPoiContainer = document.getElementById('add-pois-container');
    addPoiContainer.style.display = 'flex';
    const startTripSubmitButton = document.getElementById('submit-calculate-trip');
    startTripSubmitButton.style.display = 'inline-block';

    // Change location and date inputs to be readonly.
    const inputDestination = document.getElementById('inputDestination');
    inputDestination.readOnly = true;
    const inputDayOfTravel = document.getElementById('inputDayOfTravel');
    inputDayOfTravel.readOnly = true;

    // Change the text of the toggle button to 'Back'.
    toggleStartTripStageButton.value = 'Back';
  } else {
    // Hide the POI list, "Add POIs" form, and "Submit" button.
    const poiListContainer = document.getElementById('poi-list-container');
    poiListContainer.style.display = 'none';
    const addPoiContainer = document.getElementById('add-pois-container');
    addPoiContainer.style.display = 'none';
    const startTripSubmitButton = document.getElementById('submit-calculate-trip');
    startTripSubmitButton.style.display = 'none';

    // Change location and date inputs to be editable.
    const inputDestination = document.getElementById('inputDestination');
    inputDestination.readOnly = false;
    const inputDayOfTravel = document.getElementById('inputDayOfTravel');
    inputDayOfTravel.readOnly = false;

    // Change the text of the toggle button to 'Next'.
    toggleStartTripStageButton.value = 'Next';
  }
}

// Add an HTML button element as a POI to the form. The POI is retrieved from 
// the current POI input text, which is subsequently reset.
function addPoi() {
  const inputPoi = document.getElementById('inputPoi');

  // Add POI input button to the page.
  const poiListContainer = document.getElementById('poi-list-container');
  poiListContainer.appendChild(buildPoiObject(inputPoi.value));

  // Reset "Add POI" button to disabled, reset text of POI text input, and
  // remove 'is-valid' class.
  const addPoiButton = document.getElementById('addPoiButton');
  addPoiButton.disabled = true;
  inputPoi.value = '';
  inputPoi.classList.remove('is-valid');
}

// Build and return a user-added POI HTML object.
function buildPoiObject(poi) {
  const formGroupContainer = document.createElement('div');
  formGroupContainer.className = 'form-group';
  formGroupContainer.style.display = 'block';

  // Create poiInputButton, as well as function to remove the container upon 
  // click. The submit button is also checked.
  const poiInputButton = buildInput('button', poi + ' (click to remove)', 
    'btn btn-secondary poi-input', poi);
  poiInputButton.onclick = 
    () => { formGroupContainer.remove(); checkSubmitButton() };

  // Add the POI input button to the div container, and return the container.
  formGroupContainer.appendChild(poiInputButton);
  return formGroupContainer;
}

// Build input attribute (for button); has type, value, className, name params.
function buildInput(type, value, className, name) {
  const form = document.createElement('input');
  form.type = type;
  form.value = value;
  form.className = className;
  form.name = name;
  return form;
}

// Get all location input fields, and add the Google Places autofill.
function addLocationAutofill() {
  let locationInputs = document.getElementsByClassName('places-autofill');
  Array.prototype.forEach.call(locationInputs, (locationInput) => {
    let locationAutocomplete = new google.maps.places.Autocomplete(locationInput);

    // Any time the input changes through user typing, remove the 'is-valid' class.
    // This will not be called if the user clicks on Google Place autofill.
    locationInput.addEventListener('input', () => {
      locationInput.classList.remove('is-valid');

      // For "input destination" field, check "Next" button; for POI, check 
      // "Add POI" button.
      if (locationInput.id === 'inputDestination') {
        checkNextButton();
      } else if (locationInput.id === 'inputPoi') {
        checkAddPoiButton();
      }
    });

    // If the user changes the place (click on Google Place autofill), add 
    // 'is-valid' class.
    locationAutocomplete.addListener('place_changed', () => {
      locationInput.classList.add('is-valid');
      
      // For "input destination" field, check "Next" button; for POI, check 
      // "Add POI" button.
      if (locationInput.id === 'inputDestination') {
        checkNextButton();
      } else if (locationInput.id === 'inputPoi') {
        checkAddPoiButton();
      }
    });
  });
}

