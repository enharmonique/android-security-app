import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.androidsecurityapp.data.local.models.User
import java.security.MessageDigest

// Database Constants
private const val DATABASE_NAME = "Login.db"
private const val DATABASE_VERSION = 1

// Table Name
private const val TABLE_USERS = "users"

// Column Names
private const val COLUMN_ID = "id"
private const val COLUMN_USERNAME = "username"
private const val COLUMN_PASSWORD = "password"

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        /**
         * Returns the singleton instance of DatabaseHelper.
         *
         * @param context The application context.
         * @return The singleton instance of DatabaseHelper.
         */
        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Called when the database is created for the first time
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_USERS_TABLE = ("CREATE TABLE $TABLE_USERS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_USERNAME TEXT UNIQUE NOT NULL, "
                + "$COLUMN_PASSWORD TEXT NOT NULL)")
        db.execSQL(CREATE_USERS_TABLE)
    }

    // Called when the database needs to be upgraded
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add password column to existing users table
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_PASSWORD TEXT NOT NULL DEFAULT ''")
        }
    }

    // Hashing function using SHA-256
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Insert a new user into the database
    fun addUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val hashedPassword = hashPassword(password)
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, hashedPassword)
        }
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    // Retrieve a user by ID
    fun getUser(id: Long): User? {
        val db = this.readableDatabase
        var user: User? = null

        val cursor: Cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD),
            "$COLUMN_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val userId = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
                val username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME))
                val password = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                user = User(userId, username, password)
            }
        }
        db.close()
        return user
    }

    // Retrieve all users from the database
    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val selectQuery = "SELECT * FROM $TABLE_USERS"

        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val userId = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
                    val username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME))
                    val password = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                    users.add(User(userId, username, password))
                } while (it.moveToNext())
            }
        }
        db.close()
        return users
    }

    // Update an existing user
    fun updateUser(user: User): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            // Only update password if it's not empty
            if (user.password.isNotEmpty()) {
                put(COLUMN_PASSWORD, hashPassword(user.password))
            }
        }

        // Updating row
        val rowsAffected = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(user.id.toString())
        )
        db.close()
        return rowsAffected
    }

    // Delete a user by ID
    fun deleteUser(id: Long): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_USERS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsDeleted
    }

    // Check login credentials
    fun checkLogin(username: String, password: String): Boolean {
        val db = this.readableDatabase
        var isValid = false

        val hashedPassword = hashPassword(password)

        val cursor: Cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, hashedPassword),
            null,
            null,
            null
        )

        cursor.use {
            isValid = it.moveToFirst()
        }
        db.close()
        return isValid
    }
}
