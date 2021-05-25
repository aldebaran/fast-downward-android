# Fast Downward for Android

This is a port of the [Fast Downward PDDL Planner](http://www.fast-downward.org/) for Android.

This project implements the interface provided by the library
[`com.softbankrobotics.pddl:pddl-planning`](https://github.com/aldebaran/pddl-planning-android).

## Usage

The library and its dependencies are publicly available on Maven Central, but **not** in JCenter.
The `build.gradle` at the root of your project should mention:

```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```

### Usage as a library

Make your module depend on it:
```groovy
implementation 'com.softbankrobotics.pddl:fast-downward-android:2.2.0'
```
Use `setupFastDownwardPlanner` to get a `PlanSearchFunction`,
that can be used along with the utilities provided by `com.softbankrobotics:pddl-planning`.

### Usage as a stand-alone service

To use it as a stand-alone Android service application:
- [build](#building) the `app` module, and install it on the device.
  It will show up under the settings, in the applications section,
  but not in the main launcher app, because it has no main activity.
- let your module depend on the PDDL Planning interface for Android:
  ```groovy
  implementation 'com.softbankrobotics.pddl:pddl-planning:1.4.0'
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
