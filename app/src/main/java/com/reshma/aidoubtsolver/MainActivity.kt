package com.reshma.aidoubtsolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.reshma.aidoubtsolver.ui.chat.ChatScreen
import com.reshma.aidoubtsolver.ui.theme.DoubtSolverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoubtSolverTheme { ChatScreen() }
        }
    }
}
