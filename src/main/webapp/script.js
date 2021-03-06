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

// Create script to add API key.
let script = document.createElement('script');
script.src = 'https://maps.googleapis.com/maps/api/js?key=' + config.API_KEY + 
  '&libraries=places&callback=initScript';
script.defer = true;
script.async = true;

// Attach callback function to the 'window' object.
window.initScript = function() {
  // Add Google Places location autofill to input fields.
  addInputPoiLocationAutofill();
  addInputDestinationLocationAutofill();

  getAuthObject().then((authObject) => {
    // Display the sign-in page or "start trip" form depending on sign in status.
    if (authObject.hasOwnProperty('email')) {
      // Add the link to the "sign out" a element.
      const signOutLink = document.getElementById('sign-out-link');
      signOutLink.href = authObject.url;

      displayStartTripDesign();
    } else {
      // Add the link to the "sign in" a element.
      const signInLink = document.getElementById('sign-in-link');
      signInLink.href = authObject.url;

      displaySignInPage();
    }
  }).catch((error) => {
    // If an error occurs, print error to console and do not display button.
    console.error(error);
  });
}

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

// Update the necessary design elements and display the sign in page.
function displaySignInPage() {
  // Set the width of the content container.
  setContentWidth('650px');

  // Add background image to site. (This id is defined in CSS.)
  document.body.id = 'body-background-image';

  // Get the homepage "registration" block to and add elements.
  document.getElementById('index-registration-block').style.display = 'block';
}

// User is signed in, show the start trip homepage.
function displayStartTripDesign() {
  // Set the width of the content container.
  setContentWidth('1100px');

  // Remove background image id from body.
  document.body.removeAttribute('id');

  // Add Google Places location autofill to input fields.
  addInputPoiLocationAutofill();
  addInputDestinationLocationAutofill();

  // Set up trigger to add hidden POI form elements upon submission.
  addHiddenPoiFormTrigger();

  // Get the homepage "start trip" block to and add elements.
  document.getElementById('index-start-trip-block').style.display = 'block';

  // Display the site header.
  displayHeader();

  // Display the "start trip" form.
  displayStartTripForm();
}

function displayHeader() {
  // Display header for site.
  document.getElementById('header').style.display = 'block';
}

// Set the width of the content container.
function setContentWidth(width) {
  document.getElementById('content').style.width = width;
}

// Display the start trip input form, with location, dates, and POIs.
function displayStartTripForm() {
  // Get the homepage "start trip" block and add elements.
  document.getElementById('index-start-trip-block').style.display = 'block';

  // Initially hide the POI list, "Add POIs" form, and "Submit" button.
  document.getElementById('poi-list-container').style.display = 'none';
  document.getElementById('add-pois-container').style.display = 'none';
  document.getElementById('submit-calculate-trip').style.display = 'none';
  document.getElementById('suggested-location-block-header').style.display = 'none';
  document.getElementById('suggested-location-block').style.display = 'none';

  // Initially make the "Next" , "Add POI", and "Submit" button disabled.
  document.getElementById('toggle-stage-button').disabled = true;
  document.getElementById('addPoiButton').disabled = true;
  document.getElementById('submit-calculate-trip').disabled = true;

  // Add onchange listeners to inputs in "start trip" form.
  addStartTripOnChangeListeners();

  // Add Start Trip onclick listeners to buttons.
  addStartTripOnClickListeners();
  
}

// Add onchange listeners to inputs that call relevant functions in the
// "start trip" form.
function addStartTripOnChangeListeners() {
  // Input trip name onchange listeners to check input and and next / submit.
  document.getElementById('inputTripName').oninput = () => {
    checkValidInput('inputTripName');
    checkNextButton();
  };

  // Input day of travel onchange listeners to check input and and next / submit.
  document.getElementById('inputDayOfTravel').onchange = () => {
    checkValidInput('inputDayOfTravel');
    checkNextButton();
  };
}

// Add onclick listeners to buttons that call relevant functions in the 
// "start trip" form.
function addStartTripOnClickListeners() {
  // Toggle stage button toggles the current "stage" of the "start trip" form.
  document.getElementById('toggle-stage-button').onclick = () => {
    toggleStartTripInputStage();
  };

  // Add POI Button adds the current POI in text input, and checks submit button.
  const addPoiButton = document.getElementById('addPoiButton');
  addPoiButton.onclick = () => {
    // Get the text from the text input POI, add that POI, then clear the text input.
    const inputPoi = document.getElementById('inputPoi');
    addPoi(inputPoi.value);

    // Reset "Add POI" button to disabled, reset text of POI text input, and
    // remove 'is-valid' class.
    addPoiButton.disabled = true;
    inputPoi.value = '';
    inputPoi.classList.remove('is-valid');

    // Check the submit button.
    checkSubmitButton();
  };
}

// Set up trigger to add hidden POI elements.
function addHiddenPoiFormTrigger() {
  $('#startTripForm').submit(() => {
    // Fetch the current POI inputs, and add them to the form.
    const poiInputs = document.getElementsByClassName('poi-input');
    Array.prototype.forEach.call(poiInputs, (poiInput) => {
      // Add hidden input to the "start trip" form.
      $('<input>').attr('type', 'hidden')
        .attr('name', 'poiList')
        .attr('value', poiInput.innerText)
        .appendTo('#startTripForm');
    });
    return true;
  });
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

// Returns true if all name, location, and date inputs are valid; otherwise, false;
function isStartingInputValid() {
  // Get three input fields.
  const inputTripName = document.getElementById('inputTripName');
  const inputDestination = document.getElementById('inputDestination');
  const inputDayOfTravel = document.getElementById('inputDayOfTravel');

  return inputTripName.classList.contains('is-valid') &&
    inputDestination.classList.contains('is-valid') && 
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
  if (isStartingInputValid()) {    
    toggleStartTripStageButton.disabled = false;
  } else {
    toggleStartTripStageButton.disabled = true;
  }
}

// If location and date forms have valid inputs, and there is at least one POI,
// enable submit button; otherwise, disable button.
function checkSubmitButton() {
  const startTripSubmitButton = document.getElementById('submit-calculate-trip');

  // Enable submit button if all starting forms (name/location/date) have valid input.
  if (isStartingInputValid() && isPoiSubmitted()) {    
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
    // Display the POI list, "Add POIs" form, "Submit" button, and suggested POIs.
    document.getElementById('poi-list-container').style.display = 'block';
    document.getElementById('add-pois-container').style.display = 'flex';
    document.getElementById('submit-calculate-trip').style.display = 'inline-block';
    document.getElementById('suggested-location-block-header').style.display = 'block';
    document.getElementById('suggested-location-block').style.display = 'inline-block';

    // Change name, location, and date inputs to be readonly.
    document.getElementById('inputTripName').readOnly = true;
    document.getElementById('inputDestination').readOnly = true;
    document.getElementById('inputDayOfTravel').readOnly = true;

    // Change the text of the toggle button to 'Back'.
    toggleStartTripStageButton.value = 'Back';
  } else {
    // Hide the POI list, "Add POIs" form, "Submit" button, and suggested POIs.
    document.getElementById('poi-list-container').style.display = 'none';
    document.getElementById('add-pois-container').style.display = 'none';
    document.getElementById('submit-calculate-trip').style.display = 'none';
    document.getElementById('suggested-location-block-header').style.display = 'none';
    document.getElementById('suggested-location-block').style.display = 'none';

    // Change name, location, and date inputs to be editable.
    document.getElementById('inputTripName').readOnly = false;
    document.getElementById('inputDestination').readOnly = false;
    document.getElementById('inputDayOfTravel').readOnly = false;

    // Change the text of the toggle button to 'Next'.
    toggleStartTripStageButton.value = 'Next';
  }
}

// Add an HTML button element as a POI to the form.
function addPoi(poi) {
  // Add POI input button to the page.
  const poiListContainer = document.getElementById('poi-list-container');
  poiListContainer.appendChild(buildPoiObject(poi));
}

// Build and return a user-added POI HTML object.
function buildPoiObject(poi) {
  // Create POI element, and remove the hover attributes.
  const poiElement = document.createElement('button');
  poiElement.className = 'btn-nohover btn-light-nohover';
  poiElement.id = 'poi-element';
  poiElement.type = 'button';

  // Add span element with POI text.
  const spanPoiText = document.createElement('span');
  spanPoiText.className = 'poi-input';
  spanPoiText.id = 'poi-element-text';
  spanPoiText.innerText = poi;

  // Add span element with spacing between POI text and trash can.
  const spanPoiSpace = document.createElement('span');
  spanPoiSpace.id = 'poi-element-space';
  spanPoiSpace.innerHTML += '&nbsp;&nbsp;&nbsp;&nbsp;';

  // Add trash icon to end of button, and add onclick function to icon.
  const trashIconButton = document.createElement('i');
  trashIconButton.id = 'poi-trash-icon';
  trashIconButton.className = 'fa fa-trash';
  trashIconButton.onclick = () => {
    poiElement.remove();
    checkSubmitButton();
  };

  // Add hover styling through mouseenter and mouseleave listeners.
  trashIconButton.addEventListener('mouseenter', function(event) {
    spanPoiText.style.textDecoration = 'line-through';
    poiElement.style.backgroundColor = '#e2e6ea';
    poiElement.style.borderColor = '#dae0e5';
  });
  trashIconButton.addEventListener('mouseleave', function(event) {
    spanPoiText.style.textDecoration = 'none';
    poiElement.style.backgroundColor = '#f8f9fa';
    poiElement.style.borderColor = '#f8f9fa';
  });

  // Add span elements to POI element, and return POI element.
  poiElement.appendChild(spanPoiText);
  poiElement.appendChild(spanPoiSpace);
  poiElement.appendChild(trashIconButton);
  return poiElement;
}

// Add text input POI Google Places autofill.
function addInputPoiLocationAutofill() {
  const inputPoi = document.getElementById('inputPoi');
  let locationAutocomplete = new google.maps.places.Autocomplete(inputPoi);

  // Any time the input changes through user typing, remove the 'is-valid' class
  // and check "Add POI" button. This will not be called if the user clicks on 
  // the Google Place autofill.
  inputPoi.addEventListener('input', () => {
    inputPoi.classList.remove('is-valid');
    checkAddPoiButton();
  });

  // If the user changes the place (click on Google Place autofill), add
  // 'is-valid' class and check "Add POI" button.
  locationAutocomplete.addListener('place_changed', () => {
    inputPoi.classList.add('is-valid');
    checkAddPoiButton();
  });
}

// Add text input POI Google Places autofill.
function addInputDestinationLocationAutofill() {
  const inputDestination = document.getElementById('inputDestination');
  let locationAutocomplete = new google.maps.places.Autocomplete(inputDestination);

  // Any time the input changes through user typing, remove the 'is-valid' class
  // and check "Next" button. This will not be called if the user clicks on 
  // the Google Place autofill.
  inputDestination.addEventListener('input', () => {
    inputDestination.classList.remove('is-valid');
    checkNextButton();
  });

  // If the user changes the place (click on Google Place autofill), add
  // 'is-valid' class and check "Next" button.
  locationAutocomplete.addListener('place_changed', () => {
    inputDestination.classList.add('is-valid');
    checkNextButton();

    // Remove any current elements, then get and add the suggested locations.
    removeSuggestedLocations();
    const radius = 50000;
    let location = new google.maps.LatLng(locationAutocomplete.getPlace().geometry.location.lat(), 
      locationAutocomplete.getPlace().geometry.location.lng());
    getAndAddSuggestedLocations(location, radius);
  });
}

// Get suggested locations based on a central location (latitude, longitude) and
// radius (meters). All suggested locations are of type "tourist attraction".
// Then, call addSuggestedLocations function.
function getAndAddSuggestedLocations(centralLocation, radius) {
  let googlePlacesObject = new google.maps.places.PlacesService(document.createElement('div'));

  let placesRequest = {
    location: centralLocation,
    radius: radius,
    type: 'tourist_attraction'
  };

  googlePlacesObject.nearbySearch(placesRequest, (placesList, placesServiceStatus, _) => {
    if (placesServiceStatus === google.maps.places.PlacesServiceStatus.OK) {
      addSuggestedLocations(placesList);
    }
  });
}

// Removes all of the current suggested locations by setting innerHTML to empty.
function removeSuggestedLocations() {
  const suggestedLocationBlock = document.getElementById('suggested-location-block');
  suggestedLocationBlock.innerHTML = '';
}

// Add suggested locations for POIs after user has submitted initial "Start 
// Trip" form details (name of trip, location, and date).
function addSuggestedLocations(suggestedLocations) {
  const suggestedLocationBlock = document.getElementById('suggested-location-block');

  // Add all of the suggested locations to the page.
  suggestedLocations.forEach((location) => {
    // If photo is present, get the photo source; if not, use placeholder.
    let photoSrc;
    if (location.photos !== undefined) {
      photoSrc = location.photos[0].getUrl();
    } else {
      photoSrc = 'images/placeholder_image.png';
    }
    
    const suggestedLocationWidget = buildSuggestedLocationWidget(location.name,
      location.vicinity, photoSrc);
    suggestedLocationBlock.appendChild(suggestedLocationWidget);
  });
}

// Builds and returns an HTML widget of a suggested location.
function buildSuggestedLocationWidget(name, vicinity, photoSrc) {
  // The container that holds the full card.
  const cardContainer = document.createElement('div');
  cardContainer.className = 'card';

  // Add the photo of this location.
  const photoElement = document.createElement('img');
  photoElement.className = 'card-img-top';
  photoElement.src = photoSrc;
  photoElement.alt = 'Image of ' + name;

  // Create the body container for the card.
  const cardBodyContainer = document.createElement('div');
  cardBodyContainer.className = 'card-body';

  // Create the card title, which is the name of the location.
  const titleElement = document.createElement('h5');
  titleElement.className = 'card-title';
  titleElement.innerText = name;

  // Create the general address / vicinity of the locagtion.
  const addressText = document.createElement('p');
  addressText.className = 'card-title';
  addressText.innerText = vicinity;

  // Create the card footer, where action items ("Add this POI") will be placed.
  const cardFooter = document.createElement('div');
  cardFooter.className = 'card-footer';

  // Add a button that allows you to add this suggested location as a POI.
  const addSuggestedPoiButton = document.createElement('button');
  addSuggestedPoiButton.className = 'btn btn-primary';
  addSuggestedPoiButton.innerText = 'Add this POI';
  addSuggestedPoiButton.onclick = () => {
    // Add this POI.
    addPoi(name + ', ' + vicinity);

    // Check the submit button.
    checkSubmitButton();

    // Remove the card from the page.
    cardContainer.remove();
  }

  // Add the "Add this POI" button to the footer.
  cardFooter.appendChild(addSuggestedPoiButton);

  // Add the title and address to the card body.
  cardBodyContainer.appendChild(titleElement);
  cardBodyContainer.appendChild(addressText);

  // Add the photo element and card body to the card container.
  cardContainer.appendChild(photoElement);
  cardContainer.appendChild(cardBodyContainer);
  cardContainer.appendChild(cardFooter);
  
  return cardContainer;
}

// Append the 'script' element to the document head.
document.head.appendChild(script);

