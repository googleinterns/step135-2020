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
      displaySignOutButton();
    } else {
      displaySignInButton();
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

// Show the sign in button.
function displaySignInButton() {
  document.getElementById('sign-in-button-form').style.display = 'block';
}

// Show the sign out button.
function displaySignOutButton() {
  document.getElementById('sign-out-button-form').style.display = 'block';
}
