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
 * This script handles the authorization of all non-index pages.
 * If the user is not signed in, they are redirected to the index / sign-in
 * page. If the user is signed in, the basic skeleton of the site is constructed
 * (this is currently just the header).
 */

// Triggered upon DOM load.
$(document).ready(() => {
  // Redirect to homepage if user is not signed in.
  getAuthObject().then((authObject) => {
    if (!authObject.loggedIn) {
      window.location.replace('/');
    } else {
      // Add the link to the "sign out" a element.
      const signOutLink = document.getElementById('sign-out-link');
      signOutLink.href = authObject.logoutUrl;

      // Display the site header.
      displayHeader();
    }
  });
});

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

function displayHeader() {
  // Display header for site.
  const header = document.getElementById('header');
  header.style.display = 'block';
}
