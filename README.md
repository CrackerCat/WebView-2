# WebView [![](https://jitpack.io/v/elmurzaev/WebView.svg)](https://jitpack.io/#elmurzaev/WebView)
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
