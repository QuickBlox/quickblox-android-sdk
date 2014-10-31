## Snippets

Dive into code snippets to investigate main principles of QuickBlox.

### How to run Snippets project

* Clone this project

```bash
git clone git@github.com:QuickBlox/quickblox-android-sdk.git
```

* Create new project from existing source (`quickblox-android-sdk/snippets`).

* Add `quickblox-android-sdk/jar` as libs folder for the project or add `quickblox-android-sdk/jar/quickblox-android-x.x.x.jar` as single library.

If you want you can replace hardcoded QuickBlox app credentials with your own in  [InitializeSnippets.java](https://github.com/QuickBlox/quickblox-android-sdk/blob/master/snippets/src/com/quickblox/snippets/InitializeSnippets.java)

* Open DDMS to watch log stream

* Run project. At the top you can select QuickBlox module and perform actions by pressing on list items with corresponding names

<img src="https://img.skitch.com/20121012-di531b1cq2r5fjwtqdhxy56d65.png" height=400/>

* At the same time look at DDMS to see detailed logs ([example](https://gist.github.com/3876684))

![ddms](https://img.skitch.com/20121012-p8tix2r1fqckr4a44agp7hrrq4.png)

* Go to [com.quickblox.snippets.modules](https://github.com/QuickBlox/quickblox-android-sdk/tree/master/snippets/src/com/quickblox/snippets/modules) package and investigate simple code samples for each module.