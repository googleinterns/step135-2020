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
  '&libraries=places&callback=initEditScript';
script.defer = true;
script.async = true;

// Attach callback function to the 'window' object.
window.initEditScript = () => {
  // Populate the edit page with content.
  populateEditPage();
}

function populateEditPage() {
  // Hard-coded edit content, as no servlet is set up yet.
  fetch('/get-edit-content' + getTripKeyQuery()).then(response => {
    if (response.redirected) {
      window.location.href = response.url;
    } else {
      return response.json();
    }
  }).then(editContent => {
    console.log(editContent);
    // Set the trip title element text and width.
    const tripTitleInput = document.getElementById('edit-trip-title-input');
    const tripTitleSpan = document.getElementById('edit-trip-title-span');
    tripTitleInput.value = editContent.trip.tripName;
    tripTitleSpan.value = editContent.trip.tripName;

    // Set the trip day of travel, and add '0' to the month and day if needed.
    const tripDayOfTravelInput = document.getElementById('inputDayOfTravel');
    editContent.trip.startDate.month = (editContent.trip.startDate.month < 10) ?
      '0' + editContent.trip.startDate.month : editContent.trip.startDate.month;
    editContent.trip.startDate.day = (editContent.trip.startDate.day < 10) ?
      '0' + editContent.trip.startDate.day : editContent.trip.startDate.day;
    tripDayOfTravelInput.value = editContent.trip.startDate.year + '-' + 
      editContent.trip.startDate.month + '-' + editContent.trip.startDate.day;

    // Add the trip content to the body of the edit page.
    const editPageBody = document.getElementById('edit-page-left-column');
    for (date in editContent.dateEventMap) {
      const dateHeaderElement = document.createElement('h1');
      dateHeaderElement.innerText = date;
      editPageBody.appendChild(dateHeaderElement);

      // Add the edit cards to the body of the edit page.
      editContent.dateEventMap[date].forEach(event => {
        editPageBody.appendChild(buildEditCard(event));
      });
      editPageBody.appendChild(document.createElement('br'));
    }

    addInputPoiLocationAutofill();
    autoscaleTextInputWidth();
  }).catch (error => {
    console.error(error);
  });
}

// Add text input POI Google Places autofill.
function addInputPoiLocationAutofill() {
  const inputPoi = document.getElementById('inputPoi');
  let locationAutocomplete = new google.maps.places.Autocomplete(inputPoi);

  // Any time the input changes through user typing, remove the 'is-valid' class
  // Note: this will not be called if the user clicks on the Google Place autofill.
  inputPoi.addEventListener('input', () => {
    inputPoi.classList.remove('is-valid');
  });

  // If the user changes the place (click on Google Place autofill), add
  // 'is-valid' class.
  locationAutocomplete.addListener('place_changed', () => {
    inputPoi.classList.add('is-valid');
  });
}

/**
 * Autoscale the text input width for the edit text inputs.
 */
function autoscaleTextInputWidth() {
  // Get the autoscale text input elements.
  const autoscaleHideSpans = $('.autoscale-hide-span');
  const autoscaleTextEls = $('.autoscale-text');

  for (let i = 0; i < autoscaleHideSpans.length; i++) {
    // Set constants for adding to (for some reason, it's slightly short)
    // and scaling the width.
    const autoscaleHideSpan = $(autoscaleHideSpans[i]);
    const autoscaleText = $(autoscaleTextEls[i]);

    // Set the initial width of the text input.
    setTextInputWidth(autoscaleHideSpan, autoscaleText);

    // Readjust text input width upon edit.
    autoscaleText.on('input', () => {
      setTextInputWidth(autoscaleHideSpan, autoscaleText);
    });
  }
}

// Set the text input width based on the content.
function setTextInputWidth(autoscaleHideSpan, autoscaleText) {
  autoscaleHideSpan.text(autoscaleText.val());
  autoscaleText.width(autoscaleHideSpan.width());
}

// Build and return an edit event card.
function buildEditCard(event) {
  // Create the container of the edit card.
  const editCardContainer = document.createElement('div');
  editCardContainer.className = 'card edit-card';

  // Create the inner container for the edit card and add it to editCardContainer.
  const editCardInnerContainer = document.createElement('div');
  editCardInnerContainer.className = 'row no-gutters edit-card-height';
  editCardContainer.appendChild(editCardInnerContainer);

  // Create image column container and add it to editCardInnerContainer.
  const imageCardContainer = document.createElement('div');
  imageCardContainer.className = 'col-sm-4 edit-card-height';
  editCardInnerContainer.appendChild(imageCardContainer);

  // Create the img HTML object and add it to the image column container.
  getImageFromPlaceId(event.placeId).then(imageUrl => {
    const imageElement = document.createElement('img');
    imageElement.className = 'card-img edit-card-height';
    imageElement.src = imageUrl;
    imageElement.alt = 'Image of ' + event.name;
    imageCardContainer.appendChild(imageElement);
  });

  // Craete text body column container and add it to editCardInnerContainer.
  const textBodyContainer = document.createElement('div');
  textBodyContainer.className = 'col-sm-8';
  editCardInnerContainer.appendChild(textBodyContainer);

  // Create a card text body inner div and add it to textBodyContainer.
  const textBodyInnerContainer = document.createElement('div');
  textBodyInnerContainer.className = 'card-body edit-card-align';
  textBodyContainer.appendChild(textBodyInnerContainer);

  // Create a span element for the input and add it to textBodyInnerContainer.
  const spanTitleElement = document.createElement('span');
  spanTitleElement.className = 'autoscale-hide-span edit-card-title-size';
  spanTitleElement.innerText = event.name;
  textBodyInnerContainer.appendChild(spanTitleElement);

  // Create an input element and add it to textBodyInnerContainer.
  const inputTitleElement = document.createElement('input');
  inputTitleElement.className = 'autoscale-text edit-card-title-size';
  inputTitleElement.type = 'text';
  inputTitleElement.value = event.name;
  textBodyInnerContainer.appendChild(inputTitleElement);

  // Create an edit icon and add it to textBodyInnerContainer.
  const editTitleIcon = document.createElement('i');
  editTitleIcon.className = 'fa fa-edit edit-card-icon';
  textBodyInnerContainer.appendChild(editTitleIcon);

  // Create an address paragraph element and add it to textBodyInnerContainer.
  const addressElement = document.createElement('p');
  addressElement.className = 'edit-card-text';
  addressElement.innerText = event.address;
  textBodyInnerContainer.appendChild(addressElement);

  // Create an time paragraph element and add it to textBodyInnerContainer.
  const timeElement = document.createElement('p');
  timeElement.className = 'edit-card-text';
  timeElement.innerText = createTimeStringFromEvent(event);
  textBodyInnerContainer.appendChild(timeElement);

  // Create the trash icon and add it to textBodyContainer.
  const trashIcon = document.createElement('i');
  trashIcon.className = 'fa fa-trash edit-card-delete';
  textBodyContainer.appendChild(trashIcon);

  // Return the newly-created edit card.
  return editCardContainer;
}

// Get the image URL from the placeId.
function getImageFromPlaceId(placeId) {  
  return new Promise((resolve, reject) => {
    let service = new google.maps.places.PlacesService(document.createElement('div'));
    let request = {
      placeId: placeId
    };
    service.getDetails(request, (place, status) => {
      if (status == google.maps.places.PlacesServiceStatus.OK && place.photos != null) {
        resolve(place.photos[0].getUrl());
      } else {
        resolve('../images/placeholder_image.png');
      }
    });
  });
}

// Create a time string from an event.
function createTimeStringFromEvent(event) {
  let timeString = event.strStartTime.substring(event.strStartTime.indexOf('T') + 1) +
    ' - ' + event.strEndTime.substring(event.strEndTime.indexOf('T') + 1);
  return timeString;
}

// Append the 'script' element to the document head.
document.head.appendChild(script);
