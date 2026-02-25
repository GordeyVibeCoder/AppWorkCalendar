package com.example.appworkcalendar

import android.Manifest
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appworkcalendar.data.AppDatabase
import com.example.appworkcalendar.data.AppRepository
import com.example.appworkcalendar.domain.ContactData
import com.example.appworkcalendar.ui.MainViewModel
import com.example.appworkcalendar.ui.navigation.Screen
import com.example.appworkcalendar.ui.screens.EarningsScreen
import com.example.appworkcalendar.ui.screens.HomeScreen
import com.example.appworkcalendar.ui.screens.ProfileScreen
import com.example.appworkcalendar.ui.theme.AppWorkCalendarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = AppRepository(AppDatabase.get(this).appointmentDao())

        setContent {
            AppWorkCalendarTheme {
                AppRoot(repository = repository)
            }
        }
    }
}

@Composable
private fun AppRoot(repository: AppRepository) {
    val viewModel: MainViewModel = viewModel(factory = MainViewModel.factory(repository))
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val selectedDate by viewModel.currentDate.collectAsStateWithLifecycle()
    val range by viewModel.earningsRange.collectAsStateWithLifecycle()

    var pendingContactCallback by remember { mutableStateOf<((ContactData) -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val contactPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val cursor = navController.context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
            null,
            null,
            null
        )
        var name = ""
        var phone = ""
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getString(0)
                name = it.getString(1) ?: ""
                val phoneCursor = navController.context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(id),
                    null
                )
                phoneCursor?.use { c ->
                    if (c.moveToFirst()) {
                        phone = c.getString(0) ?: ""
                    }
                }
            }
        }
        pendingContactCallback?.invoke(ContactData(name, phone))
        pendingContactCallback = null
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.export(navController.context.contentResolver, it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.import(navController.context.contentResolver, it) } }

    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(Screen.Home, Screen.Earnings, Screen.Profile).forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigate(screen.route) },
                        icon = {
                            when (screen) {
                                Screen.Home -> Icon(Icons.Default.Home, null)
                                Screen.Earnings -> Icon(Icons.Default.ShowChart, null)
                                Screen.Profile -> Icon(Icons.Default.AccountCircle, null)
                            }
                        },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    selectedDate = selectedDate,
                    appointments = appointments,
                    onDateChange = viewModel::setDate,
                    onSave = viewModel::saveAppointment,
                    onPickContact = { callback ->
                        pendingContactCallback = callback
                        contactPicker.launch(null)
                    }
                )
            }
            composable(Screen.Earnings.route) {
                EarningsScreen(
                    appointments = allAppointments,
                    range = range,
                    onRangeSelected = viewModel::setRange
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onExport = { exportLauncher.launch("app_work_calendar_backup.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json")) }
                )
            }
        }
    }
}
