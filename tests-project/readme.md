#QuickBlox Android sample tests
Android Junit tests on "Custom Objects" module.

###To run tests project:
* Clone this project:
      ``git clone git@github.com:QuickBlox/quickblox-android-sdk.git``
* Create new project from existing source (quickblox-android-sdk/tests-project).

* Add `quickblox-android-sdk/jar` as libs folder for the project or add `quickblox-android-sdk/jar/quickblox-android-x.x.x.jar` as single library.
      <img src="http://files.quickblox.com/project-dependency.png" height=400/>
* Project contains tests project `sdk-tests`. Add it as test module and add to it dependencies main project and junit.jar library with version above 3.8

<img src="http://files.quickblox.com/sdk-test-dependency.png" height=400/>

* Onec project has been compiled you can run test individually, by package or module

<img src="http://files.quickblox.com/run-tests.png" height=400/>
