# Assignment 3

Open this folder in Android Studio and run `app` on an Android 13 or newer emulator/device. The starter uses SDK 37 and Java 25 for Gradle.

Sign in with your UCSC email and student number. Select a workspace, channel, and message. Use the compose button to add a message with 16-280 characters. Swipe right to delete your own messages or messages in a workspace you own. Dates display in UTC.

The app uses Compose, a shared ViewModel, LiveData lists, and HttpsURLConnection. The extracted lecture projects and PDFs are in `references/`.

Build and run the local unit tests:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

Run the UI tests with local test data on a running emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=edu.ucsc.cse118.assignment3.AppUiTest"
```

`BasicTest` and `AdvancedStretchTest` accept credentials as instrumentation arguments. `AdvancedStretchTest` contains the API-backed creation and deletion tests and skips when credentials are absent. Pass your credentials when running it:

```powershell
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=edu.ucsc.cse118.assignment3.AdvancedStretchTest" "-Pandroid.testInstrumentationRunnerArguments.email=YOUR_EMAIL" "-Pandroid.testInstrumentationRunnerArguments.password=YOUR_STUDENT_NUMBER"
```

Those tests use Molly Workspace / Molly Channel, create unique test messages, and remove their own test messages afterward. The workspace-owner test also uses an owned workspace containing another member's message and cancels deletion. They do not reset the account. They use visible text and the required `Compose message` description, following the assignment's rules for tests against the model solution.

The supplied `BasicTest.kt` has only its three credential/name lines changed to read instrumentation arguments, with placeholders as defaults. To run it, use `BasicTest` as the class argument and also pass `-Pandroid.testInstrumentationRunnerArguments.name=YOUR_NAME`. Credentials do not need to be saved in the source files.

Verified on the Android API 37 emulator: all 50 tests passed (19 UI tests with local data, 17 Basic tests, and 14 authenticated AdvancedStretch tests). Debug builds and 8 unit tests passed. Android lint reported no errors. Test-created API messages were removed afterward. See `output/test-results/README.md` in the working folder for the results. Testing against the model solution still needs that app.
