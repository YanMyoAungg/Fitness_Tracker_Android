package com.example.fitnesstracker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val goalViewModel: GoalViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var isFabMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        setupFabMenu()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val fabMenu = findViewById<FloatingActionButton>(R.id.fab_menu)
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    bottomNav.visibility = View.GONE
                    fabMenu.visibility = View.GONE
                    supportActionBar?.hide()
                }

                else -> {
                    bottomNav.visibility = View.VISIBLE
                    fabMenu.visibility = View.VISIBLE
                    supportActionBar?.show()
                }
            }
        }
    }

    private fun setupFabMenu() {
        val fabMenu = findViewById<FloatingActionButton>(R.id.fab_menu)
        val fabAddRecord = findViewById<ImageView>(R.id.fab_add_record)
        val fabHistory = findViewById<ImageView>(R.id.fab_history)
        val fabProfile = findViewById<ImageView>(R.id.fab_profile)

        val fabItems = listOf(fabAddRecord, fabHistory, fabProfile)

        fabMenu.setOnClickListener {
            isFabMenuOpen = !isFabMenuOpen
            if (isFabMenuOpen) {
                openFabMenu(fabItems)
            } else {
                closeFabMenu(fabItems)
            }
        }

        fabAddRecord.setOnClickListener { closeMenuAndNavigate(R.id.addRecordFragment, fabItems) }
        fabHistory.setOnClickListener { closeMenuAndNavigate(R.id.historyFragment, fabItems) }
        fabProfile.setOnClickListener { closeMenuAndNavigate(R.id.profileFragment, fabItems) }
    }

    private fun openFabMenu(fabItems: List<ImageView>) {
        findViewById<FloatingActionButton>(R.id.fab_menu).setImageResource(R.drawable.ic_close)
        var offset = 20f
        fabItems.forEach { fab ->
            fab.translationY = 0f // Reset position before animating
            fab.visibility = View.VISIBLE
            fab.animate().translationY(-offset).alpha(1f).setDuration(300).start()
            offset += 20f
        }
    }

    private fun closeFabMenu(fabItems: List<ImageView>) {
        findViewById<FloatingActionButton>(R.id.fab_menu).setImageResource(R.drawable.ic_add)
        fabItems.forEach { fab ->
            fab.animate().translationY(0f).alpha(0f).setDuration(300).withEndAction {
                fab.visibility = View.GONE
            }.start()
        }
    }

    private fun closeMenuAndNavigate(destination: Int, fabItems: List<ImageView>) {
        isFabMenuOpen = false
        closeFabMenu(fabItems)
        navController.navigate(destination)
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
