# Final test results

Run on October 12, 2026 using Medium_Phone_API_37.0 (Android API 37).

| Check | Result |
| --- | --- |
| UI tests with local data | 19 passed, 0 failures |
| Full supplied Basic tests against the real API | 17 passed, 0 failures |
| Authenticated AdvancedStretch tests against the real API | 14 passed, 0 failures, 0 skipped |
| Total emulator tests | 50 passed, 0 failures |
| Debug app and test APK build | Passed |
| Unit tests | 8 passing results; Gradle reused the unchanged test results |
| Android lint | 0 errors, 12 warnings |

The emulator tests exercised login validation, authenticated workspace/channel/message requests, message details, creation, the 16-character minimum, cancellation, author/workspace-owner deletion permissions, swipe direction, confirmation dialogs, snackbars, channel counts, request failures, and logout. Cleanup was verified: the test-created API messages were removed and the three original Molly Channel messages remained.

Raw emulator output is saved in `local-ui.txt`, `BasicTest.txt`, and `AdvancedStretchTest.txt` beside this file. Credentials are redacted in these logs and are not saved in the source files. Only the three credential/name lines in BasicTest were changed to read instrumentation arguments. No application code changes were needed. These tests ran against this app; the model solution was not supplied.
