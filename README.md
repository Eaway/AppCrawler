# AppCrawler

Android App UI Crawler using UiAutomator

AppCrawler is an automatic UI testing tool based on android UIAutomator.
It tries to traverse through the app, analyzes each screen, take screenshot and perform test on testable UI components.
This is handy in case of smoke test and compatibility testing across different devices.
It is also a convenient screenshot tool for GUI review, marketing reference, etc.

## Features

* Easy to use as monkey, No need of writing test script and source code of target app.
* Able to detect ANR/Crash with human reproducible steps log (compare with monkey).
* Screen-based CPU/Memory performance log.
* Can handle(dismiss) common system/3rd-party popups during the test.
* Configuration options, such as setting the number of events to attempt.

## Limitations

* Requires Android 4.3 (API level 18) or higher device due to UIAutomator.
* Can not test App with no launchable Activity, for example IME.
* Depends on target app characteristic, UI coverage might be low.

## How to use AppCrawler

### Installation
    $ adb install -r AppCrawlerUtil.apk
    $ adb install -r AppCrawlerTest.apk

### Execution
    $ adb shell am instrument -e target <package> -w com.eaway.appcrawler.test/android.support.test.runner.AndroidJUnitRunner

### Output
The generated screenshots and logs are saved on device.

    /sdcard/AppCrawler/<package>/

### Command Options Reference

| Option                | Description           |
| --------------------- | --------------------- |
| -e target [package]  | Target package to be tested |
| -e max-steps [number] | Maximum test steps (e.g. click, scroll), default 999 |
| -e max-depth [number] | Maximum depth from the root activity (default launchable activity), default 30 |
| -e max-screenshot [number] | Maximum screenshot file, default 999 |
| -e max-screenloop [number] | Maximum screens loop to avoid being infinite loop, default 20 |
| -e max-runtime [second] | Maximum run time in second, default 3600 |
| -e capture-steps [true\|false] | Take screenshot for every steps, this will generate more screenshots (may be duplicate), default false. |
| -e random-text [true\|false] | Input some random text to EditText if any, default true. |
| -e launch-timeout [millisecond] | timeout millisecond for launching app package, default 5000 |
| -e waitidle-timeout [millisecond] | timeout millisecond for wait app idle, default 100 |


## FAQ

#### How AppCrawler works?

* Basically, it use Depth-First-Search (DFS) algorithm to inspect screen's view hierarchy, find testable Views on screen (Clickable, Scrollable, EditText, ...), performance test in turn, and check screen changes repeatedly.

#### How it compare with android Monkey?

* Monkey test is based on random strategy, may not able to go every screen in a short time, can spend too much time on trivial screens.
* Monkey test generates too many events, it is hard to replay test steps by human for bug reproduction.


## Next Actions

* Support more user input events: long-click, scroll, gesture, etc.
* Support more system events: Screen orientation, Language, Wireless, Power/Volume keys, etc.
* Heuristic algorithm to enhance UI coverage.
* Extensibility: Logon support.

## Reference

A GUI Crawling-based technique for Android Mobile Application Testing
http://www.cs.umd.edu/~atif/testbeds/TESTBEDS2011-papers/Amalfitano.pdf

Guided GUI Testing of Android Apps with Minimal Restart and Approximate Learning
http://www.cs.berkeley.edu/~necula/Papers/swifthand-oopsla13.pdf

SuperMonkey
https://github.com/testobject/supermonkey

## Release

* [v1.0.0.0](https://github.com/Eaway/AppCrawler/releases/tag/v1.0.0.0)

## Author

Eaway Lu <<eawaylu@gmail.com>>
