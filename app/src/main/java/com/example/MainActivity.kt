package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.FreegramDatabase
import com.example.data.local.UserPreferences
import com.example.data.repository.FreegramRepository
import com.example.ui.FreegramApp
import com.example.ui.state.FreegramViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = FreegramDatabase.getInstance(applicationContext)
        val prefs = UserPreferences(applicationContext)
        val repository = FreegramRepository(database.freegramDao(), prefs)

        setContent {
            val viewModel: FreegramViewModel = viewModel(
                factory = FreegramViewModel.Factory(repository)
            )
            FreegramApp(viewModel = viewModel)
        }
    }
}
