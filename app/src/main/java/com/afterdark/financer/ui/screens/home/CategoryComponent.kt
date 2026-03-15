package com.afterdark.financer.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.afterdark.financer.data.models.CategoryEntity
import com.jaikeerthick.composable_graphs.composables.bar.BarGraph
import com.jaikeerthick.composable_graphs.composables.bar.model.BarData
import com.jaikeerthick.composable_graphs.composables.bar.style.BarGraphColors
import com.jaikeerthick.composable_graphs.composables.bar.style.BarGraphFillType
import com.jaikeerthick.composable_graphs.composables.bar.style.BarGraphStyle

enum class CurrentExpenseAction{
    ADD,
    REMOVE,
    DELETE,
    RENAME,

    ITEMIZE,
    NONE
}
@Composable
fun CategoryComponent(
    category: CategoryEntity,
    budget: Double,
    personalView: Boolean,
    actions: ExpenseActions,
    errors:String?,
    modifier: Modifier = Modifier
){
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val oneThirdWidth = screenWidth / 2.5f



    var openAction by rememberSaveable {  mutableStateOf(CurrentExpenseAction.NONE) }

    val spendedPercent = if(personalView){
        (category.currentExpense / (category.personalizedBudget?: budget) ) * 100
    } else {
        (category.currentExpense /  budget ) * 100
    }

    val spendedPercentRounded = "%.1f".format(spendedPercent).toDouble()

    var graphColors = mutableListOf<Color>()

     if(spendedPercent>=100.0){
         graphColors.add(Color.DarkGray)
     } else if (spendedPercent>=60.0){
         graphColors.add(Color.Red)
     } else if (spendedPercent>=30.0){
         graphColors.add(Color.Yellow)
     } else {
         graphColors.add(Color.Green)
     }

    if(spendedPercent>=100.0){
        graphColors.add(Color.Black)
    } else if(personalView && category.personalizedBudget!=null && category.personalizedBudget>0.0){
        graphColors.add(Color.Blue)
    } else {
        graphColors.add(Color.Green)
    }

    val actionTitle = when(openAction){
        CurrentExpenseAction.ADD -> "Add expense to ${category.name}"
        CurrentExpenseAction.DELETE -> "Delete category ${category.name}"
        CurrentExpenseAction.REMOVE -> "Remove expense from ${category.name}"
        CurrentExpenseAction.RENAME -> "Rename category ${category.name}"
        CurrentExpenseAction.ITEMIZE -> "Add a specific budget fro ${category.name} all metrics will then be based on that"
        CurrentExpenseAction.NONE -> ""
    }

    val initValue = when(openAction){
        CurrentExpenseAction.ADD,
        CurrentExpenseAction.REMOVE,
        CurrentExpenseAction.DELETE,
        CurrentExpenseAction.NONE -> ""

        CurrentExpenseAction.RENAME -> category.name

        CurrentExpenseAction.ITEMIZE -> (category.personalizedBudget ?: 0).toString()
    }


    val actionValidator: (String) -> Boolean = when (openAction) {
        CurrentExpenseAction.ADD,
        CurrentExpenseAction.ITEMIZE,
        CurrentExpenseAction.REMOVE -> { it -> (it.toDoubleOrNull() ?: -1.0) > 0.0 }

        CurrentExpenseAction.RENAME -> { it -> it.isNotEmpty() }

        CurrentExpenseAction.DELETE,
        CurrentExpenseAction.NONE -> { _ -> true }
    }

    val actionNeedsComment: Boolean = when (openAction) {
        CurrentExpenseAction.ADD,
        CurrentExpenseAction.REMOVE -> true

        else -> false
    }

    data class ExpenseActionInput(
        val textPrim: String? = null,
        val text: String? = null
    )

    val actionConfirmation : (Pair<String?, String?>)-> Unit = when (openAction) {
        CurrentExpenseAction.ADD ->{actionInp ->
            val amount = actionInp.first?.toDoubleOrNull()
            if(amount!=null) actions.addExpense(category,amount,actionInp.second)
            openAction= CurrentExpenseAction.NONE
        }
        CurrentExpenseAction.REMOVE -> {actionInp ->
            val amount = actionInp.first?.toDoubleOrNull()
            if(amount!=null) actions.removeExpense(category,amount,actionInp.second)
            openAction= CurrentExpenseAction.NONE
        }

        CurrentExpenseAction.RENAME -> {actionInp ->
            val name = actionInp.first
            if(name?.isNotEmpty() == true) actions.renameCategory(category,name,{openAction= CurrentExpenseAction.NONE})
        }

        CurrentExpenseAction.DELETE -> {actionInp -> actions.deleteCategory(category)
            openAction= CurrentExpenseAction.NONE}

        CurrentExpenseAction.ITEMIZE -> {actionInp ->
            val primaryValue = actionInp.first?.toDoubleOrNull()
            if(primaryValue!=null) actions.setItemizedBudget(category,primaryValue)
            openAction = CurrentExpenseAction.NONE
        }
        CurrentExpenseAction.NONE -> { _ -> true }
    }

    val menuittems = listOf(
        MenuItem(
            name = "Remove expense",
            icon = Icons.Default.Remove,
            clicked = {openAction= CurrentExpenseAction.REMOVE},
        ),
        MenuItem(
            name = "Set specific budget",
            icon = Icons.Default.Edit,
            clicked = {openAction= CurrentExpenseAction.ITEMIZE},
        ),
        MenuItem(
            name = "Rename",
            icon = Icons.Default.Edit,
            clicked = {openAction= CurrentExpenseAction.RENAME},
        ),
        MenuItem(
            name = "Delete",
            icon = Icons.Default.Delete,
            clicked = {openAction= CurrentExpenseAction.DELETE},
        ),
    )

    Card {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
                .width(oneThirdWidth)
        ) {
            // Top info column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = category.name)
                Text(text = "Spent : ${"%.2f".format(category.currentExpense)} $")
                if (personalView && category.personalizedBudget!=null) {
                    Text(
                        text = "Limit: ${category.personalizedBudget}$",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(text = "Total : $spendedPercentRounded %")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bar chart
            Box(
                modifier = Modifier.weight(5f)
            ) {
                BarGraph(
                    data = listOf(
                        BarData(x = "Budget", y = 100.0),
                        BarData(x = "Expense", y = spendedPercent.coerceAtMost(100.0))
                    ),
                    style = BarGraphStyle(
                        colors = BarGraphColors(
                            fillType = BarGraphFillType.Gradient(
                                brush = Brush.verticalGradient(graphColors)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                    // fixed height or can use weight in Box later
                )
            }

            // Bottom row with buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
            ) {
                LongBasicDropdownMenu(
                    menuData = menuittems
                )

                IconButton(onClick = { openAction= CurrentExpenseAction.ADD }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                    )
                }
            }
        }

        if(openAction!= CurrentExpenseAction.NONE){
            ChangeValueDialogComponent(
                dialogTitle = actionTitle,
                onDismissRequest = {openAction= CurrentExpenseAction.NONE},
                validation = actionValidator,
                onConfirmation = actionConfirmation as (payload: Any) -> Unit,
                startInitValue = initValue,
                addedComment = actionNeedsComment,
                contentShow = openAction != CurrentExpenseAction.DELETE,
                numeric = openAction== CurrentExpenseAction.ADD || openAction== CurrentExpenseAction.REMOVE,
                errors = if(openAction == CurrentExpenseAction.RENAME) errors else null
            )
        }
    }
}