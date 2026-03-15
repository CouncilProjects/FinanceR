package com.afterdark.financer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afterdark.financer.data.models.ProfileEntity
import com.afterdark.financer.data.models.TransactionWithCategory
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun TopStats(
    profile: ProfileEntity,
    transaction: UiState<TransactionWithCategory?>,
    totalSpend: Double,
    modifier: Modifier = Modifier
){
    val percentSpend = ("%.2f".format (totalSpend / profile.budget * 100)).toDouble()
    val pattern = "yyyy-MM-dd"
    val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern)

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Profile : ${profile.name}")
        Text(text = "Funds spend ${"%.2f/%.2f".format(totalSpend, profile.budget)} (${percentSpend} %)")
        Text(text = "Funds remaining ${"%.2f".format(profile.budget - totalSpend)} $ (${"%.2f".format(100.0 - percentSpend)} %)")
        if(transaction is UiState.Success && transaction.data!=null){
            Text(text = "Last entry: ${transaction.data.categoryName} ${"%.2f".format(transaction.data.transaction.valueMoved)}$ ${simpleDateFormat.format(
                Date(transaction.data.transaction.createdAt))}")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier= Modifier
                .height(20.dp)
                .padding(4.dp)
        ) {
            if(percentSpend>0){
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(percentSpend.toFloat())
                        .fillMaxSize()
                        .background(color = Color.Red)
                ) {
                    if(percentSpend>30.0){
                        Text(text = "Expense", fontSize = 10.sp, lineHeight = 10.sp)
                    }
                }


            }
            if (percentSpend<100.0) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight((100.0 - percentSpend).toFloat())
                        .fillMaxSize()
                        .background(color = Color.Green)

                ) {
                    if(100.0-percentSpend>30.0){
                        Text(text = "Remains", fontSize = 10.sp, lineHeight = 10.sp)
                    }
                }
            }
        }
    }
}