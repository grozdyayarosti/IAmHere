package com.example.iamhere
import DatabaseHelper
import android.content.Intent
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.properties.Delegates
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

class MainActivity : AppCompatActivity() {

    //    private var latitude: Double = Nullable
    private lateinit var dbHelper: DatabaseHelper
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var emailContainer: LinearLayout
    private val addEmailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshEmailList()
        }
    }
    val sender = EmailSender(
        username = "semenzabolotko.ib@gmail.com",
        password = ""  // Используйте App Password для Gmail
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.button)
        val emailContainer = findViewById<LinearLayout>(R.id.emailContainer)
        locationTextView = findViewById(R.id.locationTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Проверяем разрешения на доступ к местоположению
        checkLocationPermission()

        // 2. Создаем список email-адресов
        dbHelper = DatabaseHelper(this)
        dbHelper.refreshTable()

        // Получаем email-адреса из БД
        val emails = dbHelper.getAllEmails()
        // 3. Динамически добавляем TextView для каждого email
        emails.forEach { email ->
            val textView = TextView(this).apply {
                text = email
                textSize = 16f
                setPadding(
                    0, 8.dpToPx(this@MainActivity),
                    0, 8.dpToPx(this@MainActivity)
                ) // Добавляем отступы
            }
            emailContainer.addView(textView)
        }

        findViewById<Button>(R.id.addButton).setOnClickListener {
            val intent = Intent(this, AddEmailActivity::class.java)
            addEmailLauncher.launch(intent)
        }

        button.setOnClickListener {
            sender.sendEmail(
                to = "semenzabolotko.ib@gmail.com",
                subject = "SOS! Моё местоположение",
                body = """
                    Широта: $latitude
                    Долгота: $longitude
                    
                    Ссылка на карты: 
                    https://www.google.com/maps?q=$latitude,$longitude
                """.trimIndent()
            )
        }
    }

    class EmailSender(private val username: String, private val password: String) {
        fun sendEmail(to: String, subject: String, body: String) {
            Thread {
                try {
                    // Настройки SMTP для Gmail
                    val props = Properties().apply {
                        put("mail.smtp.host", "smtp.gmail.com")
                        put("mail.smtp.port", "587")
                        put("mail.smtp.auth", "true")
                        put("mail.smtp.starttls.enable", "true")
                    }

                    // Создаем сессию с аутентификацией
                    val session = Session.getInstance(props, object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(username, password)
                        }
                    })

                    // Создаем и отправляем письмо
                    val message = MimeMessage(session).apply {
                        setFrom(InternetAddress(username))
                        addRecipient(Message.RecipientType.TO, InternetAddress(to))
                        setSubject(subject)
                        setText(body)
                    }

                    Transport.send(message)
                    Log.d("EmailSender", "Письмо отправлено!")
                } catch (e: Exception) {
                    Log.e("EmailSender", "Ошибка: ${e.message}")
                }
            }.start()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Разрешение уже предоставлено, можно начинать работу с GPS
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Разрешение предоставлено
                startLocationUpdates()
            } else {
                // Разрешение не предоставлено
                Toast.makeText(
                    this,
                    "Разрешение на доступ к местоположению необходимо",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    locationTextView.text = "Широта: $latitude\nДолгота: $longitude"
                } else {
                    locationTextView.text = "Местоположение недоступно"
                }
            }
        }
    }


    private fun refreshEmailList() {
        emailContainer.removeAllViews()
        val emails = dbHelper.getAllEmails()

        emails.forEach { email ->
            val textView = TextView(this).apply {
                text = email
                gravity = android.view.Gravity.CENTER
                setTextAppearance(android.R.style.TextAppearance_Medium)
                setPadding(0, 16.dpToPx(this@MainActivity), 0, 16.dpToPx(this@MainActivity))
//                background = resources.getDrawable(R.drawable.email_item_border)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8.dpToPx(this@MainActivity))
                }
            }
            emailContainer.addView(textView)
        }
    }

}