![Logo](https://i.imgur.com/R9h4PbC.png)
# Khonsu: Indoor Navigation Application
Khonsu is a android application helps tracking indoor of buildings, where GPS and other location services are not working. Using sensor technology, this app can navigate users through the building and update their current location on map.

## Requirement and Dependencies
- Minimum Version: Android 5.0 API 21.0.0
- Target SDK Version: Android 8.0 26.0.1
- Android phones with Step Detector Sensor and Rotation Vector sensor, or Accelerometer and Magnetometer.
- CraftAR library

## Installation
### 1. Clone or download this repo.
`git clone https://github.com/tringuyen1121/Khonsu.git`
### 2. Import project into Android Studio
Import project into Android Studio and install all requirements
### 3. Deploy the application
Connect android phone that meet the requirements, make sure USB debugging is enabled. Hit "Run app" and choose your phone. It is recommended to test the app on a real phone in order to properly operate sensors.

The app currently use the map and location of Helsinki Metropolia UAS, therfore, to experience the application to the fullest, you need to go to this place.
### 4. Further development
For further development, this application use Image Recognition Technology from CraftAR library, simply go to [CraftAR](https://catchoom.com/product/craftar/augmented-reality-and-image-recognition/) and follow instruction to create set of your own image. Continue following the development guide to use Image Recognition on Android. 

Building map is stored in inside the project in assets folder, in SVG format. You can use either SVG, PNG, or JPG formats, store suitable data in server or external database and retrieve them to draw the map on screen.

Instead of using sensors, you can use geographical coordinates and GPS service to display locations. In this project scope, sensors are prefered.

## Screenshots
|                  LAUNCHER                   |                  HOME                     |                  MAP                  |       
| ------------------------------------------- |-------------------------------------------|---------------------------------------|
|![Launcher](https://i.imgur.com/9DDB8q7.png) |  ![Home](https://i.imgur.com/p3gGMDI.png) | ![Map](https://i.imgur.com/oZnDlFV.png)|


## Test
This app was tested on LG Nexus 5X, Android 8.0 and Samsung galaxy S4 Android 5.0. The tests were performed at Metropolia UAS Campus. 

## Liscense
This project is licensed under the MIT License - see [License](https://github.com/tringuyen1121/Khonsu/blob/master/LICENSE) 

## Bugs and Feedback
For bugs, questions and discussions please use the [GitHub Issues](https://github.com/tringuyen1121/Khonsu/issues).

