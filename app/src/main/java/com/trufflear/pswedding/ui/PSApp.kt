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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.trufflear.pswedding.service.PlaybackService
import com.trufflear.pswedding.ui.theme.PSTheme

@OptIn(ExperimentalPermissionsApi::class)
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
            val (isServiceRunning, setServiceRunning) = remember { mutableStateOf(false) }
            val audioRecorderPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
            val context = LocalContext.current

            MediaButton(isServiceRunning) {
                startAudioExperience(isServiceRunning, setServiceRunning, context, audioRecorderPermissionState)
            }
        }
    }
}

@Composable
fun MediaButton(
    isServiceRunning: Boolean,
    onButtonClick: () -> Unit
) {
    Button(
        onClick = onButtonClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isServiceRunning) {
                Color.Red
            } else {
                Color.Green
            }
        )
    ) {
        Text(text = "Start the experience")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun startAudioExperience(
    isServiceRunning: Boolean,
    setServiceRunning: (Boolean) -> Unit,
    context: Context,
    permissionState: PermissionState,
) {
    if (permissionState.status.isGranted) {
        setServiceRunning(!isServiceRunning)
        val intent = Intent(context, PlaybackService::class.java)
        if (isServiceRunning) {
            Log.d("Compose", "start service")
            context.startService(intent)
        } else {
            context.stopService(intent)
        }
    } else {
        permissionState.launchPermissionRequest()
    }
}