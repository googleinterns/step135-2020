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
  isSignedIn().then((signInStatus) => {
    // Display "sign in" or "sign out" button depending on sign in status.
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

// Create the necessary elements and display the sign in page.
function displaySignInPage() {
  // Set the width of the content container.
  setContentWidth('650px');

  // Add background image to site. (This id is defined in CSS.)
  document.body.id = 'body-background-image';

  // Get the homepage "registration" block and add elements.
  const indexRegistrationBlock = document.getElementById('index-registration-block');
  indexRegistrationBlock.style.display = 'block';

  // Create h1 element as title for TravIS.
  const titleElement = document.createElement('h1');
  titleElement.innerText = 'TravIS: Travel Information System';
  titleElement.className = 'index-registration-block-child';

  // Create p element to describe TravIS.
  const textElement = document.createElement('p');
  textElement.innerText = 
    'TravIS, your personal Travel Information System, allows you to plan ' +
    'the optimal trip to wherever you want to visit.';
  textElement.className = 'index-registration-block-child';
  textElement.id = 'index-registration-block-p';

  // Create form element to allow sign in.
  const formElement = document.createElement('form');
  formElement.className = 'index-registration-block-child';
  formElement.id = 'sign-in-button-form';
  formElement.action = '/auth';
  formElement.method = 'POST';

  // Create input element to allow sign in within the form element.
  const inputElement = document.createElement('input');
  inputElement.className = 'btn btn-primary';
  inputElement.type = 'submit';
  inputElement.value = 'Sign in';

  // Add input element to the form element.
  formElement.appendChild(inputElement);

  // Add above elements to the homepage "registration" block.
  indexRegistrationBlock.appendChild(titleElement);
  indexRegistrationBlock.appendChild(textElement);
  indexRegistrationBlock.appendChild(formElement);
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

  // Add the site header.
  addHeader();

  // Add the "start trip" form.
  addStartTripForm();
}

// Add the site header.
function addHeader() {
  // Get the content container (add the header to this component).
  const contentContainer = document.getElementById('content');

  // Create header for site.
  const header = document.createElement('div');
  header.id = 'header';

  // Create h1 element as title for TravIS.
  const titleElement = document.createElement('h3');
  titleElement.innerText = 'TravIS';
  titleElement.className = 'header-child float-left';
  titleElement.id = 'header-title';

  // Create container element to hold the link below.
  const aContainerElement = document.createElement('div');
  aContainerElement.className = 'header-child float-right';

  // Create a element to redirect to the "/trips" page.
  const aElement = document.createElement('a');
  aElement.innerText = "Trips";
  aElement.className = 'btn btn-primary';
  aElement.href = "/trips/";

  // Add a element to container element.
  aContainerElement.appendChild(aElement);

  // Create form element to allow sign out.
  const formElement = document.createElement('form');
  formElement.className = 'header-child float-right';
  formElement.id = 'sign-out-button-form';
  formElement.action = '/auth';
  formElement.method = 'POST';

  // Create input element to allow sign out within the form element.
  const inputElement = document.createElement('input');
  inputElement.className = 'btn btn-primary';
  inputElement.type = 'submit';
  inputElement.value = 'Sign out';

  // Add input element to the form element.
  formElement.append(inputElement);

  // Add above elements to the homepage "start trip" block.
  header.appendChild(titleElement);
  header.appendChild(formElement);
  header.appendChild(aContainerElement);

  // Add the header as the first child of the content container.
  contentContainer.prepend(header);
}

// Set the width of the content container.
function setContentWidth(width) {
  const contentContainer = document.getElementById('content');
  contentContainer.style.width = width;
}

// Add start trip input form, with location, dates, and POIs.
function addStartTripForm() {
  // Get the homepage "start trip" block and add elements.
  const indexStartTripBlock = document.getElementById('index-start-trip-block');
  indexStartTripBlock.style.display = 'block';

  // Create a form container which will encompass the entire frontpage form.
  const startTripForm = document.createElement('form');

  // Create location-dates and POI containers.
  const locationDatesContainer = buildLocationDatesContainer();
  const poiContainer = buildPoiContainer();
  const submitStartTripButton = buildSubmitStartTripButton();

  // Add the form row containers, and the final submit button, to the form.
  startTripForm.appendChild(locationDatesContainer);
  startTripForm.appendChild(poiContainer);
  startTripForm.appendChild(submitStartTripButton);

  // Add start trip form to the homepage "start trip" block.
  indexStartTripBlock.appendChild(startTripForm);

}

// Build and return the "start trip" button.
function buildSubmitStartTripButton() {
  // Create final "submit" button for the full form.
  const submitStartTripButton = buildButton('Submit', 'submit', 'btn btn-success');
  return submitStartTripButton;
}

// Build and return the location-dates container, which allows location and
// start / end date input to start trips.
function buildLocationDatesContainer() {
  // Create a container for the email and dates.
  const locationDatesContainer = document.createElement('div');
  locationDatesContainer.className = 'form-row';

  // Create the location input container and form.
  const locationInputContainer = document.createElement('div');
  locationInputContainer.className = 'form-group col-md-3';
  const locationInput = 
    buildInput('text', 'Destination', 'form-control', 'inputDestination', false);
  locationInputContainer.appendChild(locationInput);

  // Create the start date input container and form.
  const startDateInputContainer = document.createElement('div');
  startDateInputContainer.className = 'form-group col-md-3';
  const startDateInput = 
    buildInput('text', 'Start Date', 'form-control', 'inputStartDate', false);
  startDateInputContainer.appendChild(startDateInput);

  // Create the end date input container and form.
  const endDateInputContainer = document.createElement('div');
  endDateInputContainer.className = 'form-group col-md-3';
  const endDateInput = 
    buildInput('text', 'End Date', 'form-control is-valid', 'inputEndDate', true);
  endDateInputContainer.appendChild(endDateInput);

  // Create the end date input container and form.
  const nextButtonContainer = document.createElement('div');
  nextButtonContainer.className = 'form-group';
  const nextButton = buildButton('Next', 'button', 'btn btn-secondary');
  nextButtonContainer.appendChild(nextButton);

  // Add input containers for location and start / end dates to first form row container.
  locationDatesContainer.appendChild(locationInputContainer);
  locationDatesContainer.appendChild(startDateInputContainer);
  locationDatesContainer.appendChild(endDateInputContainer);
  locationDatesContainer.appendChild(nextButtonContainer);

  return locationDatesContainer;
}

// Build and return the POI container, which holds POI input boxes and samples.
function buildPoiContainer() {
  // Create a container for adding POIs.
  const poiContainer = document.createElement('div');
  poiContainer.className = 'form-row';

  // Create the POI input container and form.
  const poiInputContainer = document.createElement('div');
  poiInputContainer.className = 'form-group col-md-3';
  const poiInput = 
    buildInput('text', 'Point of Interest', 'form-control', 'inputEmail', false);
  poiInputContainer.appendChild(poiInput);

  // Create the POI add button container and form.
  const poiAddButtonContainer = document.createElement('div');
  poiAddButtonContainer.className = 'form-group';
  const poiAddButton = buildButton('Add POIs', 'button', 'btn btn-primary');
  poiAddButtonContainer.appendChild(poiAddButton);

  // Create a sample (California) added POI container and button.
  const poiSampleCaliforniaContainer = document.createElement('div');
  poiSampleCaliforniaContainer.className = 'form-group';
  const poiSampleCalifornia = 
    buildButton('California (click to remove)', 'button', 'btn btn-secondary');
  poiSampleCaliforniaContainer.appendChild(poiSampleCalifornia);

  // Create a sample (Oregon) added POI container and button.
  const poiSampleOregonContainer = document.createElement('div');
  poiSampleOregonContainer.className = 'form-group';
  const poiSampleOregon = 
    buildButton('Oregon (click to remove)', 'button', 'btn btn-secondary');
  poiSampleOregonContainer.appendChild(poiSampleOregon);

  // Add input containers for POIs to second form row container.
  poiContainer.appendChild(poiInputContainer);
  poiContainer.appendChild(poiAddButtonContainer);
  poiContainer.appendChild(poiSampleCaliforniaContainer);
  poiContainer.appendChild(poiSampleOregonContainer);

  return poiContainer;
}

// Build button attribute; has innerText, type, className params.
function buildButton(innerText, type, className) {
  const button = document.createElement('button');
  button.innerText = innerText;
  button.type = type;
  button.className = className;
  return button;
}

// Build input attribute; has type, placeholder, className, id, readOnly params.
function buildInput(type, placeholder, className, id, readOnly) {
  const form = document.createElement('input');
  form.type = type;
  form.placeholder = placeholder;
  form.className = className;
  form.id = id;
  form.readOnly = readOnly;
  return form;
}