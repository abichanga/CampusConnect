package com.example.mobile_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.mobile_app.auth.ui.AuthRoute
import com.example.mobile_app.ui.theme.MobileappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileappTheme {
                CampusConnectApp()
            }
        }
    }
}

@Composable
fun CampusConnectApp() {
    AuthRoute()
}