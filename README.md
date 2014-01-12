# WiFiLocalizer

This project is an Android app for localizing oneself by use of WiFi APs.  
It was developed as a semester project in the subject "Mobile applications".

## Features

* Localizing yourself in two adjustable modes
 * Fingerprint method
 * Triangulation method
* Show a list of current measured signal strengths of nearby WiFi APs
* Set up your own mapbase where you can store positions of fetched signals strengths

Just take a picture of a location plan or load an existing image to start measuring and localizing.  
For more information open the help dialog in the application.

## Howto build project in eclipse

* Clone the repository
* Import the project as an Android application
* Due to backwards compatibility to API level 7 you need to include the support library with resources ("appcompat" is needed)
* Just follow this guide, section "Adding libraries with resources": http://developer.android.com/tools/support-library/setup.html#add-library
* You're ready to compile and test the application

## Permissions explained
* ACCESS_WIFI_STATE: Check if measuring can be started
* CHANGE_WIFI_STATE: Turn WiFi on if it's not already done
* READ_EXTERNAL_STORAGE: Grab images from the device
* VIBRATE: Provide some user feedback for the long press action in localization mode

## Authors
Jens Kappel, Tobias Knispel

Students of  
_Department of Information Technology_  
_University of Applied Sciences Mannheim_
