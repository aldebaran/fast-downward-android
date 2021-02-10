package com.softbankrobotics.fastdownward

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.softbankrobotics.pddlplanning.IPDDLPlannerService.ACTION_SEARCH_PLANS_FROM_PDDL
import com.softbankrobotics.pddlplanning.createPlanSearchFunctionFromService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val domain = stringFromRawResource(this, R.raw.fast_downward_1_domain)
        val problem = stringFromRawResource(this, R.raw.fast_downward_1_problem)
        val intent = Intent(ACTION_SEARCH_PLANS_FROM_PDDL)
        intent.`package` = packageName
        GlobalScope.launch {
            val searchPlan = createPlanSearchFunctionFromService(this@MainActivity, intent)
            searchPlan(domain, problem) { Log.i("FastDownward", it) }
        }
    }
}

fun stringFromRawResource(context: Context, resourceId: Int): String {
    val inputStream = context.resources.openRawResource(resourceId)
    return Scanner(inputStream, "UTF-8").useDelimiter("\\A").next()
}