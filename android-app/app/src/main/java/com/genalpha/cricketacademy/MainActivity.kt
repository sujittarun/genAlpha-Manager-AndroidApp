package com.genalpha.cricketacademy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.genalpha.cricketacademy.data.SessionPrefs
import com.genalpha.cricketacademy.data.SupabaseRepository
import com.genalpha.cricketacademy.ui.AcademyApp
import com.genalpha.cricketacademy.ui.AcademyViewModel
import com.genalpha.cricketacademy.ui.AcademyViewModelFactory

class MainActivity : ComponentActivity() {
    private val repository by lazy { SupabaseRepository() }
    private val sessionPrefs by lazy { SessionPrefs(this) }
    private val viewModel: AcademyViewModel by viewModels {
        AcademyViewModelFactory(repository, sessionPrefs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AcademyApp(viewModel = viewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    override fun onStop() {
        viewModel.onAppBackgrounded()
        super.onStop()
    }
}
