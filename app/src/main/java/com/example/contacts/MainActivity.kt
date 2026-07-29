package com.example.contacts

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var contactsTextViews: List<TextView>
    private val receiver = Receiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        contactsTextViews = listOf(
            findViewById(R.id.tvContact1),
            findViewById(R.id.tvContact2),
            findViewById(R.id.tvContact3),
            findViewById(R.id.tvContact4),
            findViewById(R.id.tvContact5)
        )
        checkContactsPermission()
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_LOW)
        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadAndDisplayContacts()
        }
    }

    private fun checkContactsPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            loadAndDisplayContacts()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun loadAndDisplayContacts() {
        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val columnIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            var contactIndex = 0
            while (cursor.moveToNext() && contactIndex < MAX_CONTACTS_NUMBER) {
                val contactName = cursor.getString(columnIndex)
                contactsTextViews[contactIndex].text = contactName
                contactIndex++
            }
        }
    }

    companion object {

        const val MAX_CONTACTS_NUMBER = 5
    }
}