package com.afterdark.financer.ui.screens.history

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afterdark.financer.ui.TriggeredUi
import java.text.SimpleDateFormat
import java.util.Date
import com.afterdark.financer.ui.UiState


@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.FACTORY)) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAlert by rememberSaveable { mutableStateOf(false) }

    val fileChoseLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) {resultUri->
        resultUri?.let { viewModel.createExcelFile(context,it) }
    }

    if (uiState.exportStatus is UiState.Ok) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, (uiState.exportStatus as UiState.Ok).data, Toast.LENGTH_LONG).show()
            viewModel.exportSuccessNotified()
        }
    } else if (uiState.deletionStatus is UiState.Ok) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, (uiState.deletionStatus as UiState.Ok).data, Toast.LENGTH_LONG).show()
            viewModel.clearHistory(acknowledgement = true)
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text="Your finances Recalled", color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))
        if (uiState.transactions is UiState.Ok && (uiState.transactions as UiState.Ok).data.isNotEmpty()) {
            Column(
                Modifier.width(IntrinsicSize.Max)
            ){
                Button(
                    onClick = {
                        val date = SimpleDateFormat("yyyy-MM-dd").format(Date())
                        fileChoseLauncher.launch("financeR-${date}.csv")
                    },
                    enabled = (uiState.exportStatus is TriggeredUi.NotStarted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Export to file")
                        if (uiState.exportStatus is UiState.Loading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null
                            )
                        }

                    }
                }

                Button(
                    onClick = {showAlert=true},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.deletionStatus !is UiState.Loading,
                    colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.onErrorContainer, containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(text="Clear history")
                }
            }
        }

        if(showAlert){
            YouSureAlert(
                onDismissRequest = {showAlert=false},
                onConfirmation = {viewModel.clearHistory()}
            )
        }

        when(uiState.transactions){
            is UiState.Loading -> {
                CircularProgressIndicator()
                Text(text = "Loading history...")
            }

            is UiState.Error ->{
                Text(text = (uiState.transactions as UiState.Error).errorMessage, color = MaterialTheme.colorScheme.error)
            }

            is UiState.Ok -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(modifier = Modifier.weight(2f),text = "Date")

                            Text(modifier = Modifier.weight(2f), text = "Value")

                            Text(modifier = Modifier.weight(2f), text = "From")

                            Text(modifier = Modifier.weight(1f), text = "Note")
                        }
                    }

                    items(
                        (uiState.transactions as UiState.Ok).data,
                        key = {tra -> tra.transaction.id}
                    ) {
                        transaction ->
                        HistoryEntry(transact = transaction, modifier = Modifier.fillMaxHeight())
                    }
                }
            }

            else -> {}
        }

    }
}

@Composable
fun YouSureAlert(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Warning, contentDescription = "Example Icon")
        },
        title = {
            Text(text = "Clear history ?")
        },
        text = {
            Text(text = "This will delete the individual entries from history but will retain category expense info")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                    onDismissRequest()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}
