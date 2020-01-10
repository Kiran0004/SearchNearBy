Search NearBy

This project allows you to search for places in your current location using Foursquare Places API. 

By Default all nearby restaurants will be displayed,User can change their search criteria and can get result for different category of items.

Search results are represented in List and Map formats. Each item is clickable and navigates you to the detailed page with additional information inside (description, phone, working hours, etc).
Page with details also contains static map representation using Google Static Map API are used with a marker which indicate the selected item location.

Installing

Steps to run the project using the command line:
1.	Get the project locally:
2.	git clone https://github.com/Kiran0004/SearchNearBy.git
3.	Navigate to the /app folder and execute assemblDebug command from Gradle Wrapper:
4.	./gradlew assembleDebug
After the build, app-debug.apk can be found inside your project dir using this path app/build/outputs/apk/debug/
5.	Using adb install project directly to a device or emulator using the command below:
6.	adb install app/build/outputs/apk/debug/app-debug.apk

You can also use Android Studio for that purpose either:
VSC -> Git -> Clone
Insert URL https://github.com/Kiran0004/SearchNearBy.git and press Clone button. Android Studio will clone and build the project after you are good to run the App pressing Run button at the top with the default configuration.
Project tech stack

1. Kotlin

2. Koin (dependency injection)

3. Architecture components (Room, LiveData, ViewModel)

4. Android DataBinding

5. Gson

6. Glide

7. Unit testing

