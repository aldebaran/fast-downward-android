# Fast Downward for Android

![](https://img.shields.io/maven-central/v/com.softbankrobotics.pddl/fast-downward-android "latest version on Maven Central")
![](https://img.shields.io/github/workflow/status/aldebaran/fast-downward-android/Android%20CI "master build state")

This is a port of the [Fast Downward PDDL Planner](http://www.fast-downward.org/) for Android.

This project implements the interface provided by the library
[`com.softbankrobotics.pddl:pddl-planning`](https://github.com/aldebaran/pddl-planning-android).

## Usage

This planner can be used as a library, or as an Android service, in a stand-alone app.
In both cases you will need to use the interface library
[`com.softbankrobotics.pddl:pddl-planning`](https://github.com/aldebaran/pddl-planning-android).

It is available on Maven Central, but **not** in JCenter.
The `build.gradle` at the root of your project should mention:

```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```

And your module's dependency should include:
```groovy
implementation 'com.softbankrobotics.pddl:pddl-planning:1.4.1'
```

### Usage as a library

Add it to your module's dependencies:
```groovy
implementation 'com.softbankrobotics.pddl:fast-downward-android:2.2.0'
```
Use `setupFastDownwardPlanner` to get a `PlanSearchFunction`,
that can be used along with the utilities provided by `com.softbankrobotics:pddl-planning`.

Example:
```kotlin
val searchPlan: PlanSearchFunction = setupFastDownwardPlanner(context)
// [...]
searchPlan(domain, problem)
```

### Usage as a stand-alone service

To use it as a stand-alone Android service application:
- [build](#building) the `app` module, and install it on the device.
  It will show up under the settings, in the applications section,
  but not in the main launcher app, because it has no main activity.
- use `createPlanSearchFunctionFromService` with the right `Intent` to target this app's service
  to get a `PlanSearchFunction`:
  ```kotlin
  val intent = Intent(ACTION_SEARCH_PLANS_FROM_PDDL)
  intent.`package` = "com.softbankrobotics.fastdownward"
  val searchPlan: PlanSearchFunction = createPlanSearchFunctionFromService(context, intent)
  // [...]
  searchPlan(domain, problem)
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
