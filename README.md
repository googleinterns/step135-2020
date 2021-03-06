# TravIS: Travel Information System
### Google STEP Capstone Project

This is the Google STEP capstone project by Chris, Adam, and Eshika. 
TravIS, the **T**ravel **I**nformation **S**ystem, is meant to facilitate travel planning, allowing you to plan the optimal trip.

Visit the website at http://offroad-pod-step-2020.appspot.com. (Note: This link may become defunct at some point.)

## Main Features
1. Create a new trip by adding a destination (hotel), start and end dates, and points of interest (POIs) the user would like to visit. TravIS will calculate the optimal route and itinerary for the user's trip.
    - TravIS will also provide suggested points of interest based on the user's destination.
2. View the itinerary and route directions for each day through an embedded Google Maps widget on the Maps page.
    - TravIS reorders the POIs entered by the user and calculates a daily route that is optimized for travel time using the Maps API. This route starts and ends at the user's hotel and includes the user's desired POIs as stops along the way.
3. See the overall trip schedule and different calendar views on the Calendar page.
    - The event address, opening / closing hours, and an embedded map are available in the event pop-up.
    - TravIS creates an itinerary that corresponds to the calculated route. This itinerary incorporates the travel times calculated by the Maps API.
    - The current algorithm does not take into account opening hours of the locations. The new algorithm has been designed and implemented but not fully integrated into the code base as there was not enough time.
4. Edit the trip details, including the trip name, event names, and adding or deleting POIs on the Edit page. *
5. Access all of the trips under the current user, and the Maps, Calendar, and Edit pages, on the Trips page.

Note: All of the features above require the user to sign in first.

\* This feature was still a work in progress by the end of the internship, and is not implemented in the final version.

## TravIS Screenshots
### Sign In Page
![Sign In Page](src/main/webapp/images/site_screenshots/sign_in_page.png)

### Create a New Trip
![Calendar a New Trip with Suggested and Added POIs](src/main/webapp/images/site_screenshots/start_new_trip_suggested_added_pois.png)

### Maps Page
![Maps Page](src/main/webapp/images/site_screenshots/maps_page.png)

### Calendar Page
![Calendar Page](src/main/webapp/images/site_screenshots/calendar_page.png)

### Calendar Page (Event Popup)
![Calendar Page](src/main/webapp/images/site_screenshots/calendar_page_event_popup.png)

### Edit Page *
![Edit Page](src/main/webapp/images/site_screenshots/edit_page.png)

### Trips Page
![Trips Page](src/main/webapp/images/site_screenshots/trips_page.png)

\* This screenshot of the Edit page is not accessible through the master branch (those changes were not merged).

## Tools and Languages Used
TravIS was built with Java, JavaScript, and HTML/CSS with Bootstrap using the Google App Engine Platform.
