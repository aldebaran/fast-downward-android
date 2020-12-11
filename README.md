Get the library with Gradle:
```groovy
implementation 'com.softbankrobotics:fast-downward-android:1.2.0'
```
(where 1.2.0 can be replaced by latest release)

To use it as a stand-alone Android service application:
- build the `app` module, and install it on the device.
- copy the [AIDL](app/src/main/aidl/com/softbankrobotics/fastdownward/IPlannerService.aidl)
interface to your dependent project
- connect the `PlannerService` and call the method `String searchPlan(String pddl)` with your PDDL
(domain and problem concatenated into a single `String`)
- parse the result plan, formatted as follows:
```
task1 arg1 arg2\n
task2\n
task3 arg\n
```