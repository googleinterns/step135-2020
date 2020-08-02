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
  addInputPoiLocationAutofill();
  autoscaleTextInputWidth();
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
    autoscaleHideSpan.text(autoscaleText.val());
    autoscaleText.width(autoscaleHideSpan.width());

    // Readjust text input width upon edit.
    autoscaleText.on('input', () => {
      autoscaleHideSpan.text(autoscaleText.val());
      autoscaleText.width(autoscaleHideSpan.width());
    });
  }
}


// Append the 'script' element to the document head.
document.head.appendChild(script);
