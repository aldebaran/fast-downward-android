package com.softbankrobotics.fastdownward

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun stringFromRawResource(context: Context, resourceId: Int): String {
    val inputStream = context.resources.openRawResource(resourceId)
    return Scanner(inputStream, "UTF-8").useDelimiter("\\A").next()
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val domain = stringFromRawResource(this, R.raw.fast_downward_1_domain)
        val problem = stringFromRawResource(this, R.raw.fast_downward_1_problem)

        val plannerServiceConnection = object : ServiceConnection {

            // Called when the connection with the service is established
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                println("Service connected, performing plan")
                // Following the example above for an AIDL interface,
                // this gets an instance of the IRemoteInterface, which we can use to call on the service
                val plannerService = IPlannerService.Stub.asInterface(service)
                val plan = plannerService.searchPlan(domain + problem)
                println(plan)
            }

            // Called when the connection with the service disconnects unexpectedly
            override fun onServiceDisconnected(className: ComponentName) {
                println("Service has unexpectedly disconnected")
            }
        }

        val intent = Intent(ACTION_SEARCH_PLANS_FROM_PDDL)
        intent.`package` = "com.softbankrobotics.fastdownward"
        bindService(intent, plannerServiceConnection, Context.BIND_AUTO_CREATE)
    }
}
