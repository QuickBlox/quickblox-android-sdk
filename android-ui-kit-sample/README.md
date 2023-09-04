# Overview

The QuickBlox UIKit for Android is a comprehensive user interface kit specifically designed for building chat
applications. It provides a collection of pre-built components, modules, and utilities that simplify the process of
creating chat applications.

The main goal of the QuickBlox UIKit for Android is to offer developers a streamlined and efficient way to implement
chat functionality within their Android applications.

The QuickBlox UIKit for Android offers modules that encapsulate complex chat functionalities, such as dialogs and chat
management and real-time updates. These modules provide a simplified interface for integrating chat features into
applications without the need for extensive knowledge of the underlying protocols or server-side infrastructure.

# Features

QuickBlox UIKit for Android provides next functionality:

- List of dialogs
- Create dialog(Private or Group)
- Dialog screen
- Send text, image, video, audio, file messages
- Dialog info screen
- List, invite, remove members

# Send your first message

The QuickBlox UIKit for Android comprises a collection of pre-assembled UI components that enable effortless creation of
an in-app chat equipped with all the necessary messaging functionalities. Our development kit encompasses light and dark
themes, colors, and various other features. These components can be personalized to fashion an engaging messaging
interface that reflects your brand's distinct identity.

The QuickBlox UIKit fully supports both private and group dialogs. To initiate the process of sending a message from the
ground up using Java or Kotlin, please refer to the instructions provided in the guide below.

## Requirements

The minimum requirements for QuickBlox UIKit for Android are:

- Android 5.0 (API level 21) or higher
- Java 8 or higher
- Android Gradle plugin 4.0.1 or higher

## Before you begin

Register a new account following [this link](https://admin.quickblox.com/signup). Type in your email and password to
sign in. You can also sign in with your Google or Github accounts.
Create the app clicking New app button.
Configure the app. Type in the information about your organization into corresponding fields and click Add button.
Go to Dashboard => YOUR_APP => Overview section and copy your Application ID, Authorization Key, Authorization Secret,
and Account Key .

## Install QuickBlox UIKit

There are several ways to install to QuickBlox UIKit from:

- Repository
- Local source

### Install QuickBlox UIKit from repository

To install QuickBlox UIKit to your app, import QuickBlox UIKit and QuickBlox SDK dependencies via build.gradle file.
Include reference to SDK repository in your **project-level build.gradle** file at the root directory or to **
settings.gradle** file. Specify the URL of QuickBlox repository where the files are stored. Following this URL, gradle
finds SDK artifacts.

```
repositories {
    maven {
        url "https://github.com/QuickBlox/android-ui-kit-releases/raw/master/"
    }
    maven {
        url "https://github.com/QuickBlox/quickblox-android-sdk-releases/raw/master/"
    }
    maven {
        url "https://github.com/QuickBlox/android-ai-releases/raw/main/"
    }
}
```

Then need to add implementation of QuickBlox UIKit and QuickBlox SDK to dependencies in your module-level(App) **
build.gradle** file.

```
dependencies {
    implementation "com.quickblox:android-ui-kit:0.2.3"

    implementation 'com.quickblox:quickblox-android-sdk-messages:4.1.1'
    implementation 'com.quickblox:quickblox-android-sdk-chat:4.1.1'
    implementation 'com.quickblox:quickblox-android-sdk-content:4.1.1'
}
```

### Install QuickBlox UIKit from local source

To connect QuickBlox SDK to your app, import QuickBlox SDK dependencies via build.gradle file.
Include reference to SDK repository in your **project-level build.gradle** file at the root directory or to **
settings.gradle** file. Specify the URL of QuickBlox repository where the files are stored. Following this URL, gradle
finds SDK artifacts.

```
repositories {
    google()
    mavenCentral()
    maven {
        url "https://github.com/QuickBlox/quickblox-android-sdk-releases/raw/master/"
    }
}
```

Then need to download the QuickBlox UIKit from the GitHub repository
at [this link](https://github.com/QuickBlox/android-ui-kit) to include UIKit locally in your project.

Specify the path of the UIKit project in **settings.gradle** file.

```
include ':ui-kit'
project(':ui-kit').projectDir = new File('YourPath/android-ui-kit/ui-kit')
```

Also, need to add implementation of UIKit project to dependencies in your module-level(App) **build.gradle** file.

```
dependencies {
    implementation project(':ui-kit')
}
```

## Init QuickBlox SDK

To init QuickBlox SDK you need to pass Application ID, Authorization Key, Authorization Secret, and Account Key to the
init() method.
How to get credentials is described in the [Before you begin](#before-you-begin) section.

```
private const val APPLICATION_ID = "67895"
private const val AUTH_KEY = "lkjdueksu7392kj"
private const val AUTH_SECRET = "BTFsj7Rtt27DAmT"
private const val ACCOUNT_KEY = "9yvTe17TmjNPqDoYtfqp"

QBSDK.init(applicationContext, APPLICATION_ID, AUTH_KEY, AUTH_SECRET, ACCOUNT_KEY)
```

## Authentication and start QuickBlox UIKit

Before sending your first message you need to authenticate users in the QuickBlox system. You can read more about
different ways of authentication by [this link](https://docs.quickblox.com/docs/android-authentication).
In our example we show how to authenticate user with login and password.
After successfully sign-in, you need to initialize the QuickBlox UIKit by invoke **init(applicationContext)** method of
the **QuickBloxUiKit** and start Dialogs screen by invoke **show()** method of the **DialogActivity**.

```
val user = QBUser()
user.login = "userlogin"
user.password = "userpassword"

QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
    override fun onSuccess(user: QBUser?, bundle: Bundle?) {
        // init Quickblox UIKit  
        QuickBloxUiKit.init(applicationContext)
        // show Dialogs screen
        DialogsActivity.show(context)  
    }

    override fun onError(exception: QBResponseException?) {
        // handle exception
    }
})
```

# Customization

The QuickBlox UIKit for Android allows you to create your own unique view of the UIKit.

## Default themes

The QuickBlox UIKit for Android has 2 built in themes: DarkUiKitTheme and LightUiKitTheme.
Default theme for UIKit is LightUiKitTheme.
To set theme you need to call QuickBloxUiKit.setTheme() method with chosen theme.
This method must be called before starting screens.

```
QuickBloxUiKit.setTheme(DarkUiKitTheme())
```

## Use your own theme

There are two options how you can create your own theme:

- Customize current theme
- Create your own theme

To customize the current theme you just need to get it and set the color that you need.

```
QuickBloxUiKit.getTheme().setMainBackgroundColor("#FFFFFF")
```

Or you can create your own theme. To do this you need to create a new class that implements the UiKitTheme interface.

```
class CustomUiKitTheme : UiKitTheme {
    private var mainBackgroundColor: String = "#FFFFFF"
    private var statusBarColor: String = "#E4E6E8"
    private var mainElementsColor: String = "#3978FC"
    private var secondaryBackgroundColor: String = "#E7EFFF"
    private var mainTextColor: String = "#0B121B"
    private var disabledElementsColor: String = "#BCC1C5"
    private var secondaryTextColor: String = "#636D78"
    private var secondaryElementsColor: String = "#202F3E"
    private var dividerColor: String = "#E7EFFF"
    private var incomingMessageColor: String = "#E4E6E8"
    private var outgoingMessageColor: String = "#E7EFFF"
    private var inputBackgroundColor: String = "#E4E6E8"
    private var tertiaryElementsColor: String = "#636D78"
    private var errorColor: String = "#FF766E"
​
    override fun getMainBackgroundColor(): Int {
        return parseColorToIntFrom(mainBackgroundColor)
    }
​
    override fun setMainBackgroundColor(colorString: String) {
        mainBackgroundColor = colorString
    }
​
    override fun getStatusBarColor(): Int {
        return parseColorToIntFrom(statusBarColor)
    }
​
    override fun setStatusBarColor(colorString: String) {
        statusBarColor = colorString
    }
​
    override fun getMainElementsColor(): Int {
        return parseColorToIntFrom(mainElementsColor)
    }
​
    override fun setMainElementsColor(colorString: String) {
        mainElementsColor = colorString
    }
​
    override fun getSecondaryBackgroundColor(): Int {
        return parseColorToIntFrom(secondaryBackgroundColor)
    }
​
    override fun setSecondaryBackgroundColor(colorString: String) {
        secondaryBackgroundColor = colorString
    }
​
    override fun setDisabledElementsColor(colorString: String) {
        disabledElementsColor = colorString
    }
​
    override fun getDisabledElementsColor(): Int {
        return parseColorToIntFrom(disabledElementsColor)
    }
​
    override fun getMainTextColor(): Int {
        return parseColorToIntFrom(mainTextColor)
    }
​
    override fun setMainTextColor(colorString: String) {
        mainTextColor = colorString
    }
​
    override fun setSecondaryTextColor(colorString: String) {
        secondaryTextColor = colorString
    }
​
    override fun getSecondaryTextColor(): Int {
        return parseColorToIntFrom(secondaryTextColor)
    }
​
    override fun setSecondaryElementsColor(colorString: String) {
        secondaryElementsColor = colorString
    }
​
    override fun getIncomingMessageColor(): Int {
        return parseColorToIntFrom(incomingMessageColor)
    }
​
    override fun setIncomingMessageColor(colorString: String) {
        incomingMessageColor = colorString
    }
​
    override fun getOutgoingMessageColor(): Int {
        return parseColorToIntFrom(outgoingMessageColor)
    }
​
    override fun setOutgoingMessageColor(colorString: String) {
        outgoingMessageColor = colorString
    }
​
    override fun getDividerColor(): Int {
        return parseColorToIntFrom(dividerColor)
    }
​
    override fun setDividerColor(colorString: String) {
        dividerColor = colorString
    }
​
    override fun getInputBackgroundColor(): Int {
        return parseColorToIntFrom(inputBackgroundColor)
    }
​
    override fun setInputBackgroundColor(colorString: String) {
        inputBackgroundColor = colorString
    }
​
    override fun getTertiaryElementsColor(): Int {
        return parseColorToIntFrom(tertiaryElementsColor)
    }
​
    override fun setTertiaryElementsColor(colorString: String) {
        tertiaryElementsColor = colorString
    }
​
    override fun getSecondaryElementsColor(): Int {
        return parseColorToIntFrom(secondaryElementsColor)
    }
​
    override fun getErrorColor(): Int {
        return parseColorToIntFrom(errorColor)
    }
​
    override fun setErrorColor(colorString: String) {
        errorColor = colorString
    }
​
    override fun parseColorToIntFrom(colorString: String): Int {
        try {
            return Color.parseColor(colorString)
        } catch (exception: IllegalArgumentException) {
            throw Exception(exception.message.toString())
        } catch (exception: NumberFormatException) {
            throw Exception(exception.message.toString())
        }
    }
}

```

To use your own theme you just need to set it. This method must be called before starting screens.

```
QuickBloxUiKit.setTheme(CustomUiKitTheme())
```