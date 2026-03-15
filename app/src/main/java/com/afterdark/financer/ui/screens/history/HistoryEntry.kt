package com.afterdark.financer.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.afterdark.financer.data.models.TransactionEntity
import com.afterdark.financer.data.models.TransactionWithCategory
import java.text.SimpleDateFormat


@Composable
fun HistoryEntry(transact: TransactionWithCategory,modifier: Modifier = Modifier){
    var openComment by rememberSaveable { mutableStateOf(false) }
    val pattern = "yyyy-MM-dd"
    val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern)

    val actionColor = if(transact.transaction.valueMoved<0){
        Color.Green
    } else {
        Color.Red
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(modifier = Modifier.weight(2f),text = simpleDateFormat.format(transact.transaction.createdAt))

        Text(modifier = Modifier.weight(2f),text = "%.2f$".format(transact.transaction.valueMoved), color = actionColor)

        Text(modifier = Modifier.weight(2f),text = transact.categoryName)
        if (transact.transaction.comment!=null) {
            IconButton(onClick = {openComment=true},modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Press for note")
            }
        } else {
            IconButton(onClick = {},modifier = Modifier.weight(1f), enabled = false) {
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)

    if(openComment){
        AlertDialogComment(
            onDismissRequest = {openComment=false},
            dialogText = transact.transaction.comment?:""
        )
    }
}



@Composable
fun AlertDialogComment(
    onDismissRequest: () -> Unit,
    dialogText: String,
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Info, contentDescription = "Example Icon")
        },
        title = {
            Text(text = "Note left")
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {},
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

val testTran = TransactionEntity(
    id = 111,
    valueMoved = 4092.0,
    categoryId = 11,
    createdAt = 1773412737064,
    comment = null
)

val trans = TransactionWithCategory(
    transaction = testTran,
    categoryName = "Food"
)

@Preview
@Composable
fun PreviewEntry(){
    MaterialTheme {
        HistoryEntry(transact = trans)
    }
}