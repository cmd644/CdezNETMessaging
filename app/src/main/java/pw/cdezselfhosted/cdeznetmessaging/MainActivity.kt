package pw.cdezselfhosted.cdeznetmessaging

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Button
import pw.cdezselfhosted.cdeznetmessaging.api.ChatApi
import pw.cdezselfhosted.cdeznetmessaging.api.MessageResponse
import pw.cdezselfhosted.cdeznetmessaging.db.DatabaseHelper
import android.util.Log
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import android.os.Build
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var chatApi: ChatApi
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageAdapter: MessageAdapter

    private var serverAddress: String = "http://cdez.net:5000"
    private var username: String = "anonymous"
    private var chatroom: String = "default"
    private var lastMessageCount = 0 // Track the number of messages

    // Handler for periodic message fetching
    private val handler = Handler(Looper.getMainLooper())
    private val fetchMessagesRunnable = object : Runnable {
        override fun run() {
            fetchMessageHistory() // Fetch messages from the server
            handler.postDelayed(this, 1000) // Schedule the next fetch after 1 second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#2D7DD2")
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

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(serverAddress)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        chatApi = retrofit.create(ChatApi::class.java)

        // Initialize DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("CdezNETMessagingPrefs", MODE_PRIVATE)

        // Initialize RecyclerView and Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.message_list)
        messageAdapter = MessageAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        // Load messages from the database
        loadMessagesFromDatabase()

        // Load preferences
        loadFromPreferences()

        // Handle menu item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_server_address -> {
                    showInputDialog("Server Address", serverAddress) { newValue ->
                        serverAddress = newValue
                        saveToPreferences("serverAddress", serverAddress)
                        Toast.makeText(this, "Server Address updated to $serverAddress", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_username -> {
                    showInputDialog("Username", username) { newValue ->
                        username = newValue
                        saveToPreferences("username", username)
                        Toast.makeText(this, "Username updated to $username", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_chatroom -> {
                    showInputDialog("Chatroom", chatroom) { newValue ->
                        chatroom = newValue
                        saveToPreferences("chatroom", chatroom)
                        Toast.makeText(this, "Chatroom updated to $chatroom", Toast.LENGTH_SHORT).show()

                        // Clear the RecyclerView
                        messageAdapter.submitList(emptyList())

                        // Reload messages for the new chatroom
                        loadMessagesFromDatabase()

                        // Restart periodic fetching for the new chatroom
                        handler.removeCallbacks(fetchMessagesRunnable)
                        handler.post(fetchMessagesRunnable)
                    }
                    true
                }
                R.id.nav_clear_messages -> {
                    // Clear the RecyclerView
                    messageAdapter.submitList(emptyList())
                    Toast.makeText(this, "Messages cleared!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        val sendButton = findViewById<Button>(R.id.send_button)
        val messageInput = findViewById<EditText>(R.id.message_input)

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageInput.text.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Start periodic message fetching
        handler.post(fetchMessagesRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(fetchMessagesRunnable) // Stop periodic fetching to prevent memory leaks
    }

    private fun loadMessagesFromDatabase() {
        val messages = databaseHelper.getMessagesForChatRoom(chatroom)
            .sortedBy { it.timestamp } // Sort messages by timestamp in ascending order
        Log.d("MainActivity", "Loaded messages for chatroom '$chatroom': $messages") // Debugging log

        // Update the adapter's data
        messageAdapter.submitList(messages)

        // Scroll to the newest message only if new messages have been added
        if (messages.size > lastMessageCount) {
            val recyclerView = findViewById<RecyclerView>(R.id.message_list)
            recyclerView.scrollToPosition(messages.size - 1)
        }

        // Update the last message count
        lastMessageCount = messages.size
    }

    private fun fetchMessageHistory() {
        chatApi.getMessageHistory(chatroom).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    val rawMessages = response.body()?.messages ?: emptyList()
                    Log.d("fetchMessageHistory", "Fetched raw messages from API: $rawMessages")

                    val messages = rawMessages.map { rawMessage ->
                        try {
                            val regex = Regex("""\[(.*?)\] (.*?): (.*)""") // Matches "[timestamp] username: message"
                            val matchResult = regex.matchEntire(rawMessage)

                            if (matchResult != null) {
                                val (rawTimestamp, username, message) = matchResult.destructured

                                // Interpret the server's timestamp as UTC
                                val utcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                                    timeZone = TimeZone.getTimeZone("UTC")
                                }
                                val utcTimestamp = utcFormat.format(utcFormat.parse(rawTimestamp)!!)

                                Message(
                                    username = username.ifEmpty { "Unknown" },
                                    chatRoom = chatroom,
                                    message = message,
                                    timestamp = utcTimestamp
                                )
                            } else {
                                Log.e("fetchMessageHistory", "Failed to parse message: $rawMessage")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e("fetchMessageHistory", "Error parsing message: $rawMessage", e)
                            null
                        }
                    }.filterNotNull()

                    // Insert messages into the database
                    for (msg in messages) {
                        databaseHelper.insertMessage(msg.username, msg.chatRoom, msg.message, msg.timestamp)
                    }

                    // Reload messages from the database
                    loadMessagesFromDatabase()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch history: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage(message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(System.currentTimeMillis())

        val messageObj = pw.cdezselfhosted.cdeznetmessaging.api.Message(
            username = username,
            chat_room = chatroom,
            message = message
        )

        chatApi.sendMessage(messageObj).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    databaseHelper.insertMessage(username, chatroom, message, timestamp)

                    // Reload messages from the database
                    loadMessagesFromDatabase()

                    // Scroll to the bottom of the RecyclerView
                    val recyclerView = findViewById<RecyclerView>(R.id.message_list)
                    recyclerView.scrollToPosition(messageAdapter.itemCount - 1)

                    Toast.makeText(this@MainActivity, "Message sent!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to send message: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

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

    private fun saveToPreferences(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun loadFromPreferences() {
        serverAddress = sharedPreferences.getString("serverAddress", "http://cdez.net:5000") ?: "http://cdez.net:5000"
        username = sharedPreferences.getString("username", "anonymous") ?: "anonymous"
        chatroom = sharedPreferences.getString("chatroom", "default") ?: "default"
    }
}