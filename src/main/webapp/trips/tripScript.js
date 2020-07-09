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

/**
 * This script sets the content width of the site. (The header and auth redirect
 * are handled in the authScript.js file.)
 */

// Triggered upon DOM load.
$(document).ready(() => {
  // Set the content width of the site.
  setContentWidth('800px');

  // Add the trip cards to the site.
  getAndAddTripCards();
});

// Set the width of the content container.
function setContentWidth(width) {
  document.getElementById('content').style.width = width;
}

// Fetch the trip details for the user from the database, add trip cards.
function getAndAddTripCards() {
  fetch('/user-trips').then(response => response.json()).then((trips) => {
    // If trips are present, add them to the page.
    if (trips !== null) {
      addTripCards(trips);
    }
  }).catch((error) => {
    reject(error);
  });
}

// Add the trip cards to the site.
function addTripCards(trips) {
  trips.forEach((trip) => {
    const tripCardContainer = document.getElementById('trips-cards-container');

    // Build and add the trip card.
    const tripCard = buildTripCard(trip.tripName, trip.imageSrc, trip.startDate, 
      trip.endDate, trip.tripId);
    tripcardContainer.appendChild(tripCard);
  });
}

// Build HTML trip card to display.
function buildTripCard(tripTitle, imageSrc, startDate, endDate, tripId) {
  // Create the card container.
  const cardContainer = document.createElement('div');
  cardContainer.className = 'card';
  cardContainer.style.width = '18rem';

  // Create a trip image object (image of the destination).
  const tripImage = document.createElement('img');
  tripImage.className = 'card-img-top';
  tripImage.src = imageSrc;
  tripImage.alt = 'Image of ' + tripTitle;

  // Create a container for the card body.
  const cardBodyContainer = document.createElement('div');
  cardBodyContainer.className = 'card-body';

  // Create a title, dates, and action buttons (calendar and maps) for the card.
  const titleElement = document.createElement('h5');
  titleElement.className = 'card-title';
  titleElement.innerText = tripTitle;

  const datesElement = document.createElement('p');
  datesElement.className = 'card-text';
  datesElement.innerText = startDate + ' - ' + endDate;

  const calendarButton = document.createElement('a');
  calendarButton.className = 'btn btn-primary';
  calendarButton.href = '../calendar.html?tripId=' + tripId;
  calendarButton.innerText = 'Visit Calendar';

  const mapsButton = document.createElement('a');
  mapsButton.className = 'btn btn-primary';
  mapsButton.href = '../maps.html?tripId=' + tripId;
  mapsButton.innerText = 'Visit Maps';

  // Add the body elements to the card body container.
  cardBodyContainer.appendChild(titleElement);
  cardBodyContainer.appendChild(datesElement);
  cardBodyContainer.appendChild(calendarButton);
  cardBodyContainer.appendChild(mapsButton);

  // Add the trip image and card body to the card container.
  cardContainer.appendChild(tripImage);
  cardContainer.appendChild(cardBodyContainer);

  return cardContainer;
}
