# QuickBlox Android SDK

This project is suite of code snippets which shows main use cases of using QuickBlox Android SDK.

## How to start

To start work you should just put library jar into your project and call desired methods.

Latest jar-packed framework file you can download from [downloads page](https://github.com/QuickBlox/quickblox-android-sdk/downloads).

## Oh, please, please show me the code

Android SDK is really simple to use. Just in few minutes you can power your mobile app with huge amount of awesome functions to store, pass and represent your data. 

### 1. Get QuickBlox account

* [http://admin.quickblox.com/register](http://admin.quickblox.com/register)

### 2. Create application in QB Admin Panel

* [http://admin.quickblox.com/apps/new](http://admin.quickblox.com/apps/new)

### 3. Copy app credentials

* Go to edit app page, e.g. http://admin.quickblox.com/apps/{app_id}/edit
* Copy app credentials 

![App credentials](https://img.skitch.com/20121012-ksn2u9xwhatrm2rb5ttwccx16h.png 300)

### 4. Create new Android project
### 5. Add [jar library](https://github.com/QuickBlox/quickblox-android-sdk/downloads) to project

### 6. Declare internet permission for Android application

* Go to AndroidManifest.xml and add 

```xml
<uses-permission android:name="android.permission.INTERNET" />
```
inside `<manifest>` tag.

### 7. Make QuickBlox API calls

The common way to interact with QuickBlox is presented with following sequence of actions:

1. [Initialize framework with application credentials](#init)
2. [Authorize application](#auth)
3. [Login with existing user or register new one](#login)
4. [Perform actions with any QuickBlox data entities (users, locations, files, custom objects, pushes etc.)](#perform)

#### <a id="init">7.1 Initialize framework with application credentials</a>

```java
QBSettings.getInstance().fastConfigInit("961", "PBZxXW3WgGZtFZv", "vvHjRbVFF6mmeyJ");
```

or step by step


```java
QBSettings.getInstance().setApplicationId("961");
QBSettings.getInstance().setAuthorizationKey("PBZxXW3WgGZtFZv");
QBSettings.getInstance().setAuthorizationSecret("vvHjRbVFF6mmeyJ");
```

#### <a id="auth">7.2. Authorize application</a>


```java
QBAuth.authorizeApp(new QBCallback() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // you comes here if authorizations is success
        }
    }
});
```

#### <a id="login">7.3. Register/login</a>

First

```java
// Register new user
QBUsers.signUp("indianajones", "indianapassword", new QBCallback() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // you comes here if authorizations is success
        }
    }
});
```

then

```java
// Login
QBUsers.signUp("indianajones", "indianapassword", new QBCallback() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // you comes here if authorizations is success
        }
    }
});
```

#### <a id="login">7.4. <strike>Conquer the World</strike> Perform actions</a>

Create new location for Indiana Jones

```java
double lat = 25.224820; // Somewhere in Africa
double lng = 9.272461;
String statusText = "trying to find adventures";
QBLocation location = new QBLocation(lat, lng, statusText);
QBLocations.createLocation(location, new QBCallback() {
    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // you comes here if authorizations is success
        }
    }
});
```

or put Holy Grail into storage

```java
File file = new File("holy_grail.txt");
Boolean fileIsPublic = true;
QBContent.uploadFileTask(file, fileIsPublic, new QBCallback() {
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

## Documentation

Javadoc is currently under development, but you can deep into code snippets.

* Clone this project

`git clone git@github.com:QuickBlox/quickblox-android-sdk.git`

If you want you can replace hardcoded QuickBlox app credentials with your own in  [InitializeSnippets.java](https://github.com/QuickBlox/quickblox-android-sdk/blob/master/src/com/quickblox/android/framework/snippets/InitializeSnippets.java)

* Open DDMS to watch log stream

* Run project. At the top you can select QuickBlox module and perform actions by pressing on list items with corresponding names

<center><img src="https://img.skitch.com/20121012-di531b1cq2r5fjwtqdhxy56d65.png" height=400/></center>

* At the same time look at DDMS to see detailed logs

![ddms](https://img.skitch.com/20121012-p8tix2r1fqckr4a44agp7hrrq4.png)

* Go to [com.quickblox.android.framework.snippets.modules](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/src/com/quickblox/android/framework/snippets/modules) package and investigate simple code samples for each module.