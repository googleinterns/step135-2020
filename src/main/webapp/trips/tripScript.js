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
  // Redirect to homepage if user is not signed in.
  getAuthObject().then((authObject) => {
    if (authObject.loggedIn) {
      // Set the content width of the site.
      setContentWidth('800px');
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

// Set the width of the content container.
function setContentWidth(width) {
  const contentContainer = document.getElementById('content');
  contentContainer.style.width = width;
}
