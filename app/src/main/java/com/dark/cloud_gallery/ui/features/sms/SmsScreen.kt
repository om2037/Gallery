package com.dark.cloud_gallery.ui.features.sms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dark.cloud_gallery.domain.model.SmsBackup
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmsScreen(
    navController: NavController,
    viewModel: SmsViewModel = hiltViewModel()
) {
    val smsBackups by viewModel.smsBackups.collectAsState()

    LazyColumn(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(smsBackups) { backup ->
            SmsBackupItem(backup = backup) {
                val encodedUrl = URLEncoder.encode(backup.filePath, StandardCharsets.UTF_8.toString())
                navController.navigate("webview/$encodedUrl")
            }
        }
    }
}

@Composable
fun SmsBackupItem(backup: SmsBackup, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Device: ${backup.deviceModel}")
            Text(text = "Date: ${formatTimestamp(backup.timestamp)}")
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
