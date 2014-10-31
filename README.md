# QuickBlox Android SDK

This project contains QuickBlox Android SDK, that includes

* [framework library jars](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/jars)
* [snippets](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/snippets) (shows main use cases of using this one)
* samples (separated samples for each QuickBlox module)
  * [Chat Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-chat)
  * [Users Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-users)
  * [Push Notifications Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-messages)
  * [Location Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-location)
  * [Custom Objects Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-custom-objects)
  * [Content Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-content)
  * [Video Chat Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-videochat)
  * [Video Chat WebRTC Sample](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/sample-videochat-webrtc)

## How to start

To start work you should just put library jars into your project and call desired methods.

Latest jar-packed framework file you can clone from [jars  folder](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/jars).

## Documentation

* [Project page on QuickBlox developers section](http://quickblox.com/developers/Android)
* [Framework reference in JavaDoc format](http://sdk.quickblox.com/android/)

## Oh, please, please show me the code

Android SDK is really simple to use. Just in few minutes you can power your mobile app with huge amount of awesome functions to store, pass and represent your data. 

### 1. Get app credentials

* [How to get app credentials](http://quickblox.com/developers/Getting_application_credentials)

### 2. Create new Android project
### 3. Add [jar library](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/jar) to project libs folder.
Eclipse users: If you got 'Unable to execute dex: Java heap size' - try to upgrade eclipse.ini to https://groups.google.com/forum/?fromgroups=#!topic/phonegap/yWePvssyiLE

### 4. Declare internet permission for Android application

* Go to AndroidManifest.xml and add 

```xml
<uses-permission android:name="android.permission.INTERNET" />
```
inside `<manifest>` tag.

### 5. Make QuickBlox API calls

The common way to interact with QuickBlox can be presented with following sequence of actions:

1. [Initialize framework with application credentials](#51-initialize-framework-with-application-credentials)
2. [Create session](#52-create-session)
3. [Login with existing user or register new one](#53-registerlogin)
4. [Perform actions with any QuickBlox data entities (users, locations, files, custom objects, pushes etc.)](#54-perform-actions)

#### 5.1 Initialize framework with application credentials

```java
QBSettings.getInstance().fastConfigInit("961", "PBZxXW3WgGZtFZv", "vvHjRbVFF6mmeyJ");
```

or step by step


```java
QBSettings.getInstance().setApplicationId("961");
QBSettings.getInstance().setAuthorizationKey("PBZxXW3WgGZtFZv");
QBSettings.getInstance().setAuthorizationSecret("vvHjRbVFF6mmeyJ");
```

#### 5.2. Create session


```java
QBAuth.createSession(new QBCallbackImpl() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // result comes here if authorization is success
        }
    }
});
```

#### 5.3. Register/login

First create (register) new user

```java
// Register new user
QBUsers.signUp("indianajones", "indianapassword", new QBCallbackImpl() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // result comes here if request has been completed successfully
        }
    }
});
```

then authorize user

```java
// Login
QBUsers.signIn("indianajones", "indianapassword", new QBCallbackImpl() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // result comes here if request has been completed successfully
        }
    }
});
```

#### 5.4. Perform actions

Create new location for Indiana Jones

```java
double lat = 25.224820; // Somewhere in Africa
double lng = 9.272461;
String statusText = "trying to find adventures";
QBLocation location = new QBLocation(lat, lng, statusText);
QBLocations.createLocation(location, new QBCallbackImpl() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // result comes here if authorizations is success
        }
    }
});
```

or put Holy Grail into storage

```java
File file = new File("holy_grail.txt");
Boolean fileIsPublic = true;
QBContent.uploadFileTask(file, fileIsPublic, new QBCallbackImpl() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // file has been successfully uploaded
        }
    }
});
```

Java Framework provides following services to interact with QuickBlox functions (each service is represented by model with suite of static methods):

* QBAuth
* QBUsers
* QBCustomObjects
* QBLocations
* QBContent
* QBRatings
* QBMessages
* QBChat

## How to run snippets project

* See <https://github.com/QuickBlox/quickblox-android-sdk/tree/master/snippets#snippets>

## See also

* [QuickBlox REST API](http://quickblox.com/developers/Overview)
