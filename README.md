<div align="center">

  <h1>
    ✂️
    Android-CutOut
  </h1>
  Android image background cutting library
</div>

[ ![Version](https://api.bintray.com/packages/gabrielbb/Android-CutOut/Android-CutOut/images/download.svg) ](https://bintray.com/gabrielbb/Android-CutOut/Android-CutOut/_latestVersion)
[ ![Build](https://api.travis-ci.org/GabrielBB/Android-CutOut.svg?branch=master) ](https://api.travis-ci.org/GabrielBB/Android-CutOut.svg?branch=master)

## Usage

Add Gradle dependency:
```groovy
implementation 'com.github.gabrielbb:cutout:0.1.2'
```

Start the CutOut screen with this single line:

```java
CutOut.activity().start(this);
```

<img src="/images/Capture.JPG" width="200"> &nbsp; <img src="/images/Capture_2.JPG" width="200">

### Getting the result

```java
@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE) {

            switch (resultCode) {
                case Activity.RESULT_OK:
                    Uri imageUri = CutOut.getUri(data);
                    // Save the image using the returned Uri here
                    break;
                case CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE:
                    Exception ex = CutOut.getError(data);
                    break;
                default:
                    System.out.print("User cancelled the CutOut screen");
            }
        }
    }
```

## Features

<img src="/images/Magic_Wand.JPG" width="250"> &nbsp; <img src="/images/Pencil.JPG" width="250"> &nbsp; <img src="/images/Zoom.JPG" width="250">


## Options

You can use one or more options from these:

```java
        CutOut.activity()
                    .src(uri)
                    .bordered()
                    .noCrop()
                    .intro()
                    .start(this);
```

 - #### src

By default the user can select images from camera or gallery but you can also pass an `android.net.Uri` of an image that is already saved:

  ```java
Uri uri = Uri.parse("/images/cat.jpg");

CutOut.activity().src(uri).start(this);
```


 - #### bordered

  ```java
CutOut.activity().bordered().start(this);
```

This option makes the final PNG have a border around it. The default border color is White. You can also pass the `android.graphics.Color` of your choice.


 - #### noCrop

  ```java
CutOut.activity().noCrop().start(this);
```

By default and thanks to this library: [Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper), the user can crop or rotate the image. This option disables that cropping screen.



 - #### intro

  ```java
CutOut.activity().intro().start(this);
```

Display an intro explaining every button usage. The user can skip the intro and it is only shown once. The images displayed in the intro are the same you saw in the "Features" section of this document.

## Change log
*0.1.2*
- Removed Admob Ads automatic integration. I will probably add it later. For now, it was causing problems.
- Images are now saved as temporary files in the cache directory. This guarantees that these images will be deleted when users uninstall your app or when the disk memory is low. If you want the images to live forever on the Gallery, you should take the returned Uri and save the image there by yourself.

## License
Copyright (c) 2018 Gabriel Basilio Brito

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to use, 
copy, modify, merge, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

1 - You cannot use this software to make an Android app that its main goal is to remove background from images and publish it to the Google Play Store, but you can use it to integrate the functionality in an existing app or a new app that does more than just this.

2 - The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
