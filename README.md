# QuickBlox Android SDK

This project contains QuickBlox Android SDK, that includes

  * [Core module](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-core) contains base classes and util components
  * [Chat Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-chat)
  * [Video Chat WebRTC Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-videochat-webrtc)
  * [Users Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-users)
  * [Push Notifications Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-pushnotifications)
  * [Location Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-location)
  * [Custom Objects Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-custom-objects)
  * [Content Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-content)

# Overview 

QuickBlox  is Communication as a Service provider. The platform provides chat using the XMPP protocol, WebRTC signalling for video/voice calling and an API for sending push notifications. It provides a user management system, data storage and more. 

# Sample structure

Each sample depends from core module, which contains mutual dependencies such as CoreApp, BaseActivity, BaseListAdapter and other useful utils like a ImagePicker, KeyboardUtils, NotificationUtils, etc. Also core module keeps common resources  colors, strings, dimens and others. It makes code more clean and clear, and also more object-oriented. In addition the Samples have renewed up-to-date design.

# How to run samples

To run samples on Android Studio go to menu **File - Import Project**. Select path to sample, select **Use default gradle wrapper(recommended)** and click OK.

# Connect SDK to your existing apps 

To get the QuickBlox SDK project running you will need Android Studio and Maven installed.

The repository https://github.com/QuickBlox/quickblox-android-sdk-releases contains binary distributions of QuickBlox Android SDK and an instruction how to connect SDK to your project. Check it out.

# Documentation

* [Project page on QuickBlox developers section](http://quickblox.com/developers/Android)
* [Framework reference in JavaDoc format](http://sdk.quickblox.com/android/)

# Questions and feedback

Please raise questions, requests for help etc. via http://stackoverflow.com/questions/tagged/quickblox

# License
BSD
