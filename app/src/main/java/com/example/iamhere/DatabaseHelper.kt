import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "EmailsDB", null, 1) {

    companion object {
        private const val TABLE_NAME = "emails"
        private const val COL_ID = "id"
        private const val COL_EMAIL = "email"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("""
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EMAIL TEXT NOT NULL UNIQUE
            )
        """.trimIndent())

//         Добавляем тестовые данные
        db.execSQL("INSERT INTO $TABLE_NAME ($COL_EMAIL) VALUES ('email1@example.com')")
//        db.execSQL("INSERT INTO $TABLE_NAME ($COL_EMAIL) VALUES ('email2@example.com')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun getAllEmails(): List<String> {
        val emails = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_EMAIL FROM $TABLE_NAME", null)

        while (cursor.moveToNext()) {
            emails.add(cursor.getString(0))
        }

        cursor.close()
        return emails
    }

    fun addEmail(email: String): Boolean {
        val db = writableDatabase
        return try {
            db.execSQL("INSERT OR IGNORE INTO $TABLE_NAME ($COL_EMAIL) VALUES (?)", arrayOf(email))
            true
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun refreshTable() {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
//        return try {
//            db.execSQL("INSERT OR IGNORE INTO $TABLE_NAME ($COL_EMAIL) VALUES (?)", arrayOf(email))
//            true
//        } catch (e: Exception) {
//            false
//        } finally {
//            db.close()
//        }
    }

}