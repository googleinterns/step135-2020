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

  addInputPoiLocationAutofill();
  autoscaleTextInputWidth();
}

function populateEditPage() {
  let editObjectContent = {
    "trip":{
        "tripName":"My Favorite Trip",
        "destinationName":"Hotel Seattle",
        "tripKey":"aglub19hcHBfaWRyIgsSBHVzZXIYgICAgICAkAoMCxIEdHJpcBiAgICAgIDwCQw",
        "imageSrc":"https://maps.googleapis.com/maps/api/place/photo?maxwidth\u003d400\u0026photoreference\u003dCmRaAAAA_xIcyHJDhBA73aeywi0a2fK5R9BgDPLkH-DVNED6lMjEDsfehkyME3tlU5EgPc5QGQ8DZOTKebRtAIPDiHLi4NqkkHV-JKJ3TYibe4e_UduH_nlI2TfK5jvWeO34op7fEhAxDp_H3_noI6rIgEBcUhvJGhTzVxhoeC264Eh8YZWN6whQp7jRTw\u0026key\u003dAIzaSyCmQyeeWI_cV0yvh1SuXYGoLej3g_D9NbY",
        "startDate":{
          "year":2020,
          "month":8,
          "day":6
        },
        "endDate":{
          "year":2020,
          "month":8,
          "day":6
        },
        "numDays":1
    },
    "dateEventMap":{
        "Thursday, 8/6/2020":[
          {
              "name":"Space Needle",
              "address":"Space Needle",
              "startTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":10,
                    "minute":7,
                    "second":0,
                    "nano":0
                }
              },
              "endTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":11,
                    "minute":7,
                    "second":0,
                    "nano":0
                }
              },
              "strStartTime":"2020-08-06T10:07:00",
              "strEndTime":"2020-08-06T11:07:00",
              "travelTime":10
          },
          {
              "name":"Gas Works Park",
              "address":"Gas Works Park",
              "startTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":11,
                    "minute":17,
                    "second":0,
                    "nano":0
                }
              },
              "endTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":12,
                    "minute":17,
                    "second":0,
                    "nano":0
                }
              },
              "strStartTime":"2020-08-06T11:17:00",
              "strEndTime":"2020-08-06T12:17:00",
              "travelTime":9
          },
          {
              "name":"Woodland Park Zoo",
              "address":"Woodland Park Zoo",
              "startTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":12,
                    "minute":26,
                    "second":0,
                    "nano":0
                }
              },
              "endTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":13,
                    "minute":26,
                    "second":0,
                    "nano":0
                }
              },
              "strStartTime":"2020-08-06T12:26:00",
              "strEndTime":"2020-08-06T13:26:00",
              "travelTime":3
          },
          {
              "name":"Aurora Bridge",
              "address":"Aurora Bridge",
              "startTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":13,
                    "minute":29,
                    "second":0,
                    "nano":0
                }
              },
              "endTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":14,
                    "minute":29,
                    "second":0,
                    "nano":0
                }
              },
              "strStartTime":"2020-08-06T13:29:00",
              "strEndTime":"2020-08-06T14:29:00",
              "travelTime":38
          },
          {
              "name":"Snoqualmie Falls",
              "address":"Snoqualmie Falls",
              "startTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":15,
                    "minute":7,
                    "second":0,
                    "nano":0
                }
              },
              "endTime":{
                "date":{
                    "year":2020,
                    "month":8,
                    "day":6
                },
                "time":{
                    "hour":16,
                    "minute":7,
                    "second":0,
                    "nano":0
                }
              },
              "strStartTime":"2020-08-06T15:07:00",
              "strEndTime":"2020-08-06T16:07:00",
              "travelTime":33
          }
        ]
    }
  };

  console.log(editObjectContent);
  console.log(editObjectContent.trip);
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
