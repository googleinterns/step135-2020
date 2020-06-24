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

  // Get the homepage "registration" block to and add elements.
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
}

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