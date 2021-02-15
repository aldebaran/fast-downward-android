# Fast Downward for Android

This is a port of the [Fast Downward PDDL Planner](http://www.fast-downward.org/) for Android.

This project implements the interface provided by `com.softbankrobotics:pddl-planning`.

## Usage as a library

Get the library with Gradle:
```groovy
implementation 'com.softbankrobotics:fast-downward-android:2.0.1'
```
Use `setupFastDownwardPlanner` to get a `PlanSearchFunction`,
that can be used along with the utilities provided by `com.softbankrobotics:pddl-planning`.

## Usage as a stand-alone service

To use it as a stand-alone Android service application:
- [build](#Building) the `app` module, and install it on the device.
  It will show up under the settings, in the applications section,
  but not in the main launcher app, because it has no main activity.
- let your module depend on the PDDL Planning interface for Android:
  ```groovy
  implementation 'com.softbankrobotics:pddl-planning:1.1.3'
  ```
- use `createPlanSearchFunctionFromService` with the right `Intent` to target this app's service
  to get a `PlanSearchFunction`:
  ```kotlin
  val intent = Intent(ACTION_SEARCH_PLANS_FROM_PDDL)
  intent.`package` = "com.softbankrobotics.fastdownward"
  val searchPlan: PlanSearchFunction = createPlanSearchFunctionFromService(context, intent)
  ```

## Building

This project has submodules. After cloning it, do not forget to run:

```sh
git submodule update --init
```

This is a standard Android Studio project,
but it includes C++ dependencies and might take long to build.

## License

This project is distributed under the [GPLv3 license](LICENSE).
