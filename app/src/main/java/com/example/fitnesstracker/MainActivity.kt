package com.example.fitnesstracker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.ui.viewmodel.GoalViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val goalViewModel: GoalViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_main)
        sessionManager = SessionManager(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.homeFragment, R.id.historyFragment, R.id.profileFragment)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    bottomNav.visibility = View.GONE
                    supportActionBar?.hide()
                }

                else -> {
                    bottomNav.visibility = View.VISIBLE
                    supportActionBar?.show()
                }
            }
        }
    }

    fun showGoalRequiredDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_goal_required, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.button_positive).setOnClickListener {
            showSetGoalDialog()
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.button_negative)
            .setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    fun showSetGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_goal, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val input = dialogView.findViewById<EditText>(R.id.dialog_input)
        dialogView.findViewById<Button>(R.id.button_positive).setOnClickListener {
            val target = input.text.toString().toIntOrNull()
            if (target != null && target > 0) {
                goalViewModel.setGoal(sessionManager.getUserId(), target)
                dialog.dismiss()
            }
        }
        dialogView.findViewById<Button>(R.id.button_negative)
            .setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
