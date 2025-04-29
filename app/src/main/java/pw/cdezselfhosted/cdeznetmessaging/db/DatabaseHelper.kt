package pw.cdezselfhosted.cdeznetmessaging.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import pw.cdezselfhosted.cdeznetmessaging.Message

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "messages.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_MESSAGES = "messages"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_CHATROOM = "chatRoom"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_MESSAGES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_CHATROOM TEXT NOT NULL,
                $COLUMN_MESSAGE TEXT NOT NULL,
                $COLUMN_TIMESTAMP TEXT NOT NULL,
                UNIQUE($COLUMN_USERNAME, $COLUMN_CHATROOM, $COLUMN_MESSAGE, $COLUMN_TIMESTAMP) ON CONFLICT IGNORE
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        onCreate(db)
    }

    fun insertMessage(username: String, chatRoom: String, message: String, timestamp: String) {
        val db = writableDatabase

        // Check if the message already exists
        val cursor = db.query(
            TABLE_MESSAGES,
            null,
            "$COLUMN_USERNAME = ? AND $COLUMN_CHATROOM = ? AND $COLUMN_MESSAGE = ? AND $COLUMN_TIMESTAMP = ?",
            arrayOf(username, chatRoom, message, timestamp),
            null,
            null,
            null
        )

        if (cursor.count == 0) { // Only insert if the message does not exist
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, username)
                put(COLUMN_CHATROOM, chatRoom)
                put(COLUMN_MESSAGE, message)
                put(COLUMN_TIMESTAMP, timestamp)
            }
            db.insert(TABLE_MESSAGES, null, values)
        }

        cursor.close()
    }

    fun getMessagesForChatRoom(chatRoom: String): List<Message> {
        val db = readableDatabase
        val cursor = db.query(
            true, // Set distinct to true
            TABLE_MESSAGES,
            null,
            "$COLUMN_CHATROOM = ?",
            arrayOf(chatRoom),
            null,
            null,
            "$COLUMN_TIMESTAMP ASC",
            null
        )

        val messages = mutableListOf<Message>()
        with(cursor) {
            while (moveToNext()) {
                val username = getString(getColumnIndexOrThrow(COLUMN_USERNAME))
                val message = getString(getColumnIndexOrThrow(COLUMN_MESSAGE))
                val timestamp = getString(getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                messages.add(Message(username, chatRoom, message, timestamp))
            }
        }
        cursor.close()
        Log.d("DatabaseHelper", "Fetched messages: $messages")
        return messages
    }
}