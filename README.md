# WebView [![](https://jitpack.io/v/elmurzaev/WebView.svg)](https://jitpack.io/#elmurzaev/WebView)
### Advanced, Lifecycle and theme aware WebView component to support basic Browser features. 

**Some of the features:**
- Handles file downloads and uploads.
- Handles permission requests from websites.
- Supports dark theme on websites.
- Supports desktop mode.
- Design mode out of the box.
- Lifecycle aware.
- Content blocker support (Block hosts).
- ProgressBar can be attached to display a state of a website. This library will control 
it by itself, updating it's progress and hiding it after a page is loaded.
- Handles malformed urls, auto prefixes hosts if they don't contain protocol and issues search 
request if the url is not actually a url.
- Built-in search engines for search requests.
- Can play fullscreen videos.
- Can scroll your AppBar.
- Many other useful features that I might have missed to mention here.

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

## How to use

Include in your layout.
```xml
<com.elmurzaev.webview.WebView 
    android:id="@+id/webView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```
In your Activity.
```java
WebView webView = findViewById(R.id.webView);
// no configuration needed like enabling javascript etc.
// everything is configuried for you, just load your url.
webView.loadUrl("https://example.com");
```
Also you need to add this in your Activity:
```java
// in onRequestPermissionsResult method
webView.onRequestPermissionsResult(requestCode, permissions, grantResults);
// in onActivityResult method
webView.onActivityResult(requestCode, resultCode, data);
```

## License
MIT License

Copyright (c) 2022 Ramzan Elmurzaev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
