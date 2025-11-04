# Project set up

You'll need a JDK supporting Java 21 or above. If you're on linux, you can simply do `java --version` and it should tell you the JDK version you have installed. Anything >=21 is fine.

You'll need an Android Studio new enough to support compile SDK 36 and gradle >=8.14. This shouldn't be a problem if you installed Android Studio recently.

## Firebase

Once you have all these, you'll need to set up a firebase project, refer to the [documentation](https://firebase.google.com/docs/android/setup).

Once you have the firebase console set up and your android app added, enable "Google Sign In" integration under Build > Authentication > Sign-in method > Add new provider > Google.

Redownload the `google-services.json` afterwards and put it inside `./app/`.

Next, you need to authenticate the client app into Firebase. You can do so by generating the SHA1 of the app and adding it to the Firebase project. Refer to the [official instructions](https://developers.google.com/android/guides/client-auth).

Next, you need the google cloud server client ID. Go to [Google cloud console](https://console.cloud.google.com) and choose your firebase project (from the top left). Go to API & Services > Credentials > OAuth 2.0 Client IDs and copy the Client ID with Type = Web Application.

Create a local file `keys.properties` at the root of this project (same level as this README.md). Put in `GOOGLE_CLIENT_ID="<PASTE THE CLIENT ID>"` in its own line. Make sure the client ID you pasted is in quotes.

### Emulator

It is recommended you use firebase emulator to run tests. Run `firebase emulators:start` inside the [firebase](./firebase/) directory to start emulators (do this in a separate terminal).

All instrumented tests should extend `FirebaseEmulatorTest` to hook up all firebase calls with the emulator. Once this is done, running the instrumented tests with an AVD (Android emulator) will use this emulated firebase.

## Google Authentication on Emulator

To enable Google authentication for the Android Studio emulator, open the device manager and create a virtual device. Select a device with "Play" (I used Pixel 7 Pro), and install the recommended system image.

Once installed, boot up the emulator, open the Play store, and then login to your Google account. Now, the emulator is prepared for Google authentication when the Evently is opened.

## Test build

Make sure you can run `gradlew assemble` from the command line (or `./gradlew assemble` on linux).

# Project conventions

Please read [CONVENTIONS.md](./CONVENTIONS.md)

Also see git conventions and tutorial at [GIT.md](./GIT.md)
