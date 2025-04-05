package pw.cdezselfhosted.cdeznetmessaging

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private var serverAddress: String = "http://localhost:5000"
    private var username: String = "anonymous"
    private var chatroom: String = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        // Set up the Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up the ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle menu item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_server_address -> {
                    showInputDialog("Server Address", serverAddress) { newValue ->
                        serverAddress = newValue
                        Toast.makeText(this, "Server Address updated to $serverAddress", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_username -> {
                    showInputDialog("Username", username) { newValue ->
                        username = newValue
                        Toast.makeText(this, "Username updated to $username", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_chatroom -> {
                    showInputDialog("Chatroom", chatroom) { newValue ->
                        chatroom = newValue
                        Toast.makeText(this, "Chatroom updated to $chatroom", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_theme -> {
                    Toast.makeText(this, "Theme changed!", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_connect -> {
                    Toast.makeText(this, "Connect/Disconnect clicked!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Show an input dialog to update a value.
     * @param title The title of the dialog.
     * @param currentValue The current value to display in the input field.
     * @param onValueChanged Callback to handle the updated value.
     */
    private fun showInputDialog(title: String, currentValue: String, onValueChanged: (String) -> Unit) {
        val input = EditText(this)
        input.setText(currentValue)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val newValue = input.text.toString()
                if (newValue.isNotEmpty()) {
                    onValueChanged(newValue)
                } else {
                    Toast.makeText(this, "$title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}