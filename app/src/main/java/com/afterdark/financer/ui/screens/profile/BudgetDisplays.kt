package com.afterdark.financer.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun BudgetDisplay(
    budget:Double,
    budgetChange:(payload:Any)-> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()){
    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        var alert by rememberSaveable { mutableStateOf(false) }

        Text(text = buildAnnotatedString {
            append("Current profile budget ")
            withStyle(style = SpanStyle(color = Color.Green)){
                append(budget.toString())
            }
            append("$")
        })

        Button( onClick = {alert=true}) {
            Text(text = "Change")
        }

        if(alert){
            ChangeBudgetDialogComponent(
                onDismissRequest = {alert=false},
                onConfirmation = budgetChange,
                dialogTitle = "Set a new budget",
                startBudget = budget.toString()
            )
        }
    }
}

@Composable
fun ChangeBudgetDialogComponent(
    onDismissRequest: () -> Unit,
    onConfirmation: (payload: Double) -> Unit,
    dialogTitle: String,
    startBudget: String,
) {
    var userEdit by rememberSaveable { mutableStateOf(startBudget) }

    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            TextField(
                value = userEdit,
                onValueChange = {userEdit=it},
                singleLine = true,
                modifier = Modifier.padding(12.dp)
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation(userEdit.toDoubleOrNull()?:startBudget.toDouble())
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