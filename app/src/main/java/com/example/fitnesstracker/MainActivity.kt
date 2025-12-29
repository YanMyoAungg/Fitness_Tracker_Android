package com.example.fitnesstracker

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var fabMenu: FloatingActionButton
    private lateinit var fabAddRecord: FloatingActionButton
    private lateinit var fabHistory: FloatingActionButton
    private lateinit var fabProfile: FloatingActionButton

    private var isFabMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Define top-level destinations. These won't have a back arrow.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.homeFragment)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Initialize FABs
        fabMenu = findViewById(R.id.fab_menu)
        fabAddRecord = findViewById(R.id.fab_add_record)
        fabHistory = findViewById(R.id.fab_history)
        fabProfile = findViewById(R.id.fab_profile)

        // Main FAB Toggle
        fabMenu.setOnClickListener {
            toggleFabMenu()
        }

        // Sub FAB Actions
        fabAddRecord.setOnClickListener {
            navController.navigate(R.id.addRecordFragment)
            closeFabMenu()
        }

        fabHistory.setOnClickListener {
            navController.navigate(R.id.historyFragment)
            closeFabMenu()
        }

        fabProfile.setOnClickListener {
            navController.navigate(R.id.profileFragment)
            closeFabMenu()
        }

        // Destination Listener to show/hide FAB and close menu
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Always close menu on navigation change
            closeFabMenu()

            when (destination.id) {
                R.id.homeFragment, R.id.historyFragment -> fabMenu.show()
                else -> fabMenu.hide()
            }
        }
    }

    private fun toggleFabMenu() {
        if (isFabMenuOpen) {
            closeFabMenu()
        } else {
            showFabMenu()
        }
    }

    private fun showFabMenu() {
        isFabMenuOpen = true
        fabMenu.animate().rotation(45f)

        // Show items
        showFabItem(fabAddRecord)
        showFabItem(fabHistory)
        showFabItem(fabProfile)
    }

    private fun closeFabMenu() {
        if (!isFabMenuOpen) return
        isFabMenuOpen = false
        fabMenu.animate().rotation(0f)

        // Hide items
        hideFabItem(fabAddRecord)
        hideFabItem(fabHistory)
        hideFabItem(fabProfile)
    }

    private fun showFabItem(fab: FloatingActionButton) {
        fab.visibility = View.VISIBLE
        fab.alpha = 0f
        fab.animate().alpha(1f)
    }

    private fun hideFabItem(fab: FloatingActionButton) {
        fab.animate().alpha(0f).withEndAction {
            fab.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
