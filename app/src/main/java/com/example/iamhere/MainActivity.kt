package com.example.iamhere
import android.content.Intent
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

//    private var latitude: Double = Nullable
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.button)
        locationTextView = findViewById(R.id.locationTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Проверяем разрешения на доступ к местоположению
        checkLocationPermission()

//        val label = findViewById<TextView>(R.id.textView)
//        val userData: EditText = findViewById(R.id.editTextText)
//        val button: Button = findViewById(R.id.button)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        button.setOnClickListener {
//            val latitude = 59.9343  // Пример: координаты Санкт-Петербурга
//            val longitude = 30.3351

            // Формируем текст с координатами и ссылкой на карты
            val locationText = """
                Моё текущее местоположение:
                Широта: $latitude
                Долгота: $longitude
                
                Ссылка на карты: 
                https://www.google.com/maps?q=$latitude,$longitude
            """.trimIndent()

//            sendLocationByEmail(
//                email = "semenzabolotko.ib@gmail.com", // Замените на нужный email
//                subject = "Моё местоположение",
//                message = locationText
//            )
            openGmailDirectly(
                context = this,
                emailTo = "semenzabolotko.ib@gmail.com",
                subject = "Моё местоположение",
                body = "Широта: $latitude, Долгота: $longitude\nhttps://maps.google.com?q=59.9343,30.3351"
            )

//            val text = userData.text.toString().trim()
//            if (text == "toast")
//                Toast.makeText(this, "User enter toast", Toast.LENGTH_SHORT).show()
//            else
//                label.text = text

        }
    }

    private fun openGmailDirectly(
        context: Context,
        emailTo: String,
        subject: String,
        body: String
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailTo))  // Получатель
            putExtra(Intent.EXTRA_SUBJECT, subject)         // Тема
            putExtra(Intent.EXTRA_TEXT, body)              // Текст
            setPackage("com.google.android.gm")             // Указываем пакет Gmail
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Если Gmail не установлен, предлагаем альтернативу
            Toast.makeText(context, "Gmail не найден", Toast.LENGTH_SHORT).show()

            // Открываем стандартный почтовый Intent
            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailTo")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(fallbackIntent)
            } else {
                Toast.makeText(context, "Нет почтовых приложений1", Toast.LENGTH_SHORT).show()
            }
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
                Toast.makeText(this, "Разрешение на доступ к местоположению необходимо", Toast.LENGTH_SHORT).show()
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

}

