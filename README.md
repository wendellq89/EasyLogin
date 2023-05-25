# third-party logins
## Notice
Google login use firebase, you should open google login in firebase console,then download the updated Firebase config file (google-services.json)


## About this project
Wrap authorized logins for Google, Facebook, Twitter, LinkedIn and get their id, email, name.

## 1. Preparation
* Register the developers of each platform, obtain the configuration fields of each platform, and fill them in **strings.xml**
* add your **google-services.json** file in the app directory
* Modify the **namespace** and **applicationId** in **build.gradle** to ensure that it is consistent with the **package_name** in **google-services.json**
* add **signingConfigs** in **build.gradle** which the SHA-1 needs to be the same as in firebase


## 2. How to use
* Use implementation project(':thirdpartylogin') in app module
* Call ThirdPartyLogin.initOnCreate() in the activity or fragment onCreate life cycle
* Call ThirdPartyLogin.login or ThirdPartyLogin.loginFacebook etc. at the buttons used


# Thanks

- [LinkedInSignInAndroidExample](https://github.com/johncodeos-blog/LinkedInSignInAndroidExample)
