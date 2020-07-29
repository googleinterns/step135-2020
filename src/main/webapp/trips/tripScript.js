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
  setContentWidth('1100px');

  // Add the trip cards to the site.
  getAndAddTripCards();
});

// Set the width of the content container
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
    console.error(error);
  });
}

// Add the trip cards to the site.
function addTripCards(trips) {
  const tripCardContainer = document.getElementById('trips-cards-container');

  // If no trips are present, then add a "Create a New Trip" button card.
  if (trips.length === 0) {
    // Create the card container.
    const cardContainer = document.createElement('div');
    cardContainer.id = 'card-create-new-trip';
    cardContainer.className = 'card text-white bg-primary mb-3';
    cardContainer.style = 'width: 18rem';

    // Create a container for the card body.
    const cardBodyContainer = document.createElement('div');
    cardBodyContainer.className = 'card-body';

    // Create the title for the card body.
    const cardBodyTitle = document.createElement('h5');
    cardBodyTitle.className = 'card-title';
    cardBodyTitle.innerText = 'Create a New Trip';

    // Create a stretched link so that the card links to the homepage.
    const stretchedLinkSubheader = document.createElement('a');
    stretchedLinkSubheader.href = '/';
    stretchedLinkSubheader.className = 'stretched-link card-text text-white';
    stretchedLinkSubheader.innerText = 'Add POIs and go!';

    // Append all the different components, and add the card to the UI.
    cardBodyContainer.appendChild(cardBodyTitle);
    cardBodyContainer.appendChild(stretchedLinkSubheader);
    cardContainer.appendChild(cardBodyContainer);
    tripCardContainer.appendChild(cardContainer);
  }

  trips.forEach((trip) => {
    // Build and add the trip card.
    const tripCard = buildTripCard(trip.tripName, trip.destinationName, 
      trip.imageSrc, trip.startDate, trip.endDate, trip.tripKey);
    tripCardContainer.appendChild(tripCard);
  });
}

// Build HTML trip card to display.
function buildTripCard(tripTitle, destinationName, imageSrc, startDate, endDate, tripKey) {
  // Create the card container.
  const cardContainer = document.createElement('div');
  cardContainer.className = 'card';

  // Create a trip image object (image of the destination).
  const tripImage = document.createElement('img');
  tripImage.className = 'card-img-top';
  tripImage.src = imageSrc;
  tripImage.alt = 'Image of ' + tripTitle;

  // Create a container for the card body.
  const cardBodyContainer = document.createElement('div');
  cardBodyContainer.className = 'card-body';

  // Create a title, destination and dates, and action buttons (calendar and 
  // maps) for the card.
  const titleElement = document.createElement('h5');
  titleElement.className = 'card-title';
  titleElement.innerText = tripTitle;

  const datesDestinationElement = document.createElement('p');
  datesDestinationElement.className = 'card-text';
  datesDestinationElement.innerText = destinationName + ', ' + startDate.year + 
    '-' + startDate.month + '-' + startDate.day;

  const calendarButton = document.createElement('a');
  calendarButton.className = 'btn btn-primary trip-button';
  calendarButton.href = '../calendar.html?tripKey=' + tripKey;
  calendarButton.innerText = 'Calendar';

  const mapsButton = document.createElement('a');
  mapsButton.className = 'btn btn-primary trip-button';
  mapsButton.href = '../maps.html?tripKey=' + tripKey;
  mapsButton.innerText = 'Maps';

  const editButton = document.createElement('a');
  editButton.className = 'btn btn-primary trip-button';
  editButton.href = '../edit.html?tripKey=' + tripKey;
  editButton.innerText = 'Edit';

  // Create the card footer, where action items (calendar, maps, edit) will be placed.
  const cardFooter = document.createElement('div');
  cardFooter.className = 'card-footer';

  // Add the calendar, maps, and edit buttons to the footer.
  cardFooter.appendChild(calendarButton);
  cardFooter.appendChild(mapsButton);
  cardFooter.appendChild(editButton);

  // Add the body elements to the card body container.
  cardBodyContainer.appendChild(titleElement);
  cardBodyContainer.appendChild(datesDestinationElement);

  // Add the trip image and card body to the card container.
  cardContainer.appendChild(tripImage);
  cardContainer.appendChild(cardBodyContainer);
  cardContainer.appendChild(cardFooter);

  return cardContainer;
}
