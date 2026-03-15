package com.afterdark.financer.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun UserDisplay(
    profileName:String,
    created:Long,
    deleteUser:()-> Unit,
    modifier: Modifier= Modifier.fillMaxWidth()
){


    val pattern = "yyyy-MM-dd"
    val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern)
    val date: String = simpleDateFormat.format(Date(created))
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Current profile")
        Text(text = profileName)
        Text(text = "Since: ${date}")
        Button(onClick = {deleteUser()}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
            Row {
                Text(text="Delete")
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete profile")
            }
        }
    }
}