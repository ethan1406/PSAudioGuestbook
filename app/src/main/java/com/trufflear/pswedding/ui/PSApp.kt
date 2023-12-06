package com.trufflear.pswedding.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.trufflear.pswedding.service.PlaybackService
import com.trufflear.pswedding.ui.theme.PSTheme

@Composable
@Preview("home screen")
fun PSApp() {
    PSTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.White
                ),
            contentAlignment = Alignment.Center
        ) {
            MediaButton(LocalContext.current)


        }
    }
}

@Composable
fun MediaButton(
    context: Context,
) {
    var isServiceRunning by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isServiceRunning = !isServiceRunning
            handleButtonClick(isServiceRunning, context)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isServiceRunning) {
                Color.Green
            } else {
                Color.Red
            }
        )
    ) {
        Text(text = "Start the experience")
    }
}

private fun handleButtonClick(
    isServiceRunning: Boolean,
    context: Context
) {
    val intent = Intent(context, PlaybackService::class.java)
    if (isServiceRunning) {
        Log.d("Compose", "start service")
        context.startService(intent)
    } else {
        context.stopService(intent)
    }
}