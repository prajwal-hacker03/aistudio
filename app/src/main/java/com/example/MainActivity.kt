package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GradeTrackApp
import com.example.ui.theme.GradeTrackTheme
import com.example.ui.viewmodel.GradeTrackViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GradeTrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

            GradeTrackTheme(darkTheme = profile.isDarkMode) {
                GradeTrackApp(viewModel = viewModel)
            }
        }
    }
}
