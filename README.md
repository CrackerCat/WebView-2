# WebView
### Advanced, Lifecycle and theme aware WebView component to support basic Browser features. 

**Some of the features:**
- Handles file downloads and uploads.
- Handles permission requests from websites.
- Support for dark theme on websites.
- Lifecycle aware.
- Content blocker support (Block hosts).
- Progress bar can be attached to display a state of a website.
- Handle malformed urls, auto prefixes hosts if they don't contain protocol.
- Search engines for issuing search requests.
- Can play fullscreen videos.
- Many other useful features.

## Download

Add it in your root build.gradle at the end of repositories:

```Gradle
allprojects {
   repositories {
    ...
    maven { url 'https://jitpack.io' }
   }
}
```
Step 2. Add the dependency
```Gradle
dependencies {
  implementation 'com.github.elmurzaev:WebView:+'
}
```
