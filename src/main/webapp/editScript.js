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
  autoscaleTextInputWidth();
});

/**
 * Autoscale the text input width for the edit text inputs.
 */
function autoscaleTextInputWidth() {
  // Constant text width difference and multiple (for changed font size);
  const textWidthAdd = 8;
  const textWidthMultiple = 2.5;

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
    autoscaleText.width((autoscaleHideSpan.width() + textWidthAdd) * textWidthMultiple);

    // Readjust text input width upon edit.
    autoscaleText.on('input', () => {
      autoscaleHideSpan.text(autoscaleText.val());
      autoscaleText.width(autoscaleHideSpan.width() * textWidthMultiple);
    });
  }
}
