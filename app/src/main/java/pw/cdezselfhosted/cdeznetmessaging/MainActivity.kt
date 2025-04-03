package pw.cdezselfhosted.cdeznetmessaging

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

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

        // Set default values
        val serverAddress = navigationView.menu.findItem(R.id.nav_server_address).actionView as EditText?
        val username = navigationView.menu.findItem(R.id.nav_username).actionView as EditText?
        val chatroom = navigationView.menu.findItem(R.id.nav_chatroom).actionView as EditText?

        serverAddress?.setText("http://localhost:5000")
        username?.setText("anonymous")
        chatroom?.setText("default")

        // Handle menu item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
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
}