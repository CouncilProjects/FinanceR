package com.afterdark.financer.ui.screens.home


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afterdark.financer.data.models.CategoryEntity
import kotlinx.coroutines.launch
import com.afterdark.financer.ui.UiState

data class ExpenseActions(
    val addExpense: (CategoryEntity, Double, String?) -> Unit,
    val deleteCategory: (CategoryEntity) -> Unit,
    val renameCategory: (CategoryEntity, String,()->Unit) -> Unit,
    val removeExpense: (CategoryEntity, Double, String?) -> Unit,
    val setItemizedBudget: (CategoryEntity,Double) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel : HomeScreenViewModel = viewModel(factory = HomeScreenViewModel.Factory)) {
    val uiState = viewModel.uiState.collectAsState()

    val errorUi = viewModel.errorUi.collectAsState()

    var clearConfirm by rememberSaveable { mutableStateOf(false)}


    var totalSpend = 0.0
    val scope = rememberCoroutineScope()
    var createCategoryDiag by rememberSaveable { mutableStateOf(false) }

    if(uiState.value.categoryUi is UiState.Ok){
       totalSpend =
           (uiState.value.categoryUi as UiState.Ok).data.sumOf { cat -> cat.currentExpense }
    }

    val actions = ExpenseActions(
        addExpense = { cat, am, com ->
            viewModel.addExpense(cat, am, com)
        },

        deleteCategory = { cat ->
            viewModel.deleteCategory(cat)
        },

        renameCategory = { cat, newName,laterDismisal ->
            scope.launch {
                val ok =viewModel.renameCategory(cat, newName)
                if(ok){
                    laterDismisal()
                }
            }
        },

        removeExpense = { cat, am, com ->
            viewModel.removeExpense(cat, am, com)
        },

        setItemizedBudget = {cat,newBudget ->
            viewModel.setItemizedBudget(cat,newBudget)
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(uiState.value.isLoading){
            CircularProgressIndicator()
            return@Column
        }

        if(uiState.value.selectedUi is UiState.Ok){
            TopStats((uiState.value.selectedUi as UiState.Ok).data ,totalSpend=totalSpend, transaction = uiState.value.latestTransaction)
        }

        if(uiState.value.categoryUi is UiState.Ok && uiState.value.selectedUi is UiState.Ok){
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            ) {
                items(
                    (uiState.value.categoryUi as UiState.Ok).data,
                    key = {cat -> cat.id}
                ){
                    cat -> CategoryComponent(
                        category = cat,
                        budget = (uiState.value.selectedUi as UiState.Ok).data.budget,
                        personalView = (uiState.value.itemizedBudget as UiState.Ok).data ?: false,
                        actions=actions,
                        errors = errorUi.value.categoryRename
                    )
                }
            }
        }

        Column(
            Modifier.width(IntrinsicSize.Max)
        ) {
            Button(onClick = {createCategoryDiag=true}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Create category")
            }

            when(uiState.value.itemizedBudget){
                is UiState.Loading -> Unit
                is UiState.Error -> Text(text="Error")
                is UiState.Ok -> {
                    Button(onClick = {viewModel.setItemizedView()},modifier = Modifier.fillMaxWidth()) {
                        if(!(uiState.value.itemizedBudget as UiState.Ok).data){
                            Text(text = "Go to itemized view")
                        } else {
                            Text(text = "Go to global view")
                        }
                    }
                }

                else -> {}
            }

            Button(
                onClick = {clearConfirm=true},
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Clear all expenses")
            }
        }

        if(clearConfirm){
            ChangeValueDialogComponent(
                onDismissRequest = {clearConfirm=false},
                onConfirmation = { inp ->
                    viewModel.clearAllExpenses()
                    clearConfirm=false
                },
                dialogTitle = "Clear expenses",
                contentShow = false
            )
        }



        if(createCategoryDiag){
            ChangeValueDialogComponent(
                onDismissRequest = {createCategoryDiag=false},
                validation = { it.isNotEmpty() },
                onConfirmation = { input: Pair<String?, String?>->
                    scope.launch {
                        if(input.first==null) return@launch
                        val ok = viewModel.addCategory(input.first as String)
                        if(ok){
                            createCategoryDiag=false
                        }
                    }
                },
                dialogTitle = "Give category name",
                startInitValue = "",
                errors = errorUi.value.categoryCreation
            )
        }
    }
}

@Composable
fun ChangeValueDialogComponent(
    onDismissRequest: () -> Unit,
    onConfirmation: (Pair<String?, String?>) -> Unit,
    dialogTitle: String,
    validation:(input: String)-> Boolean={true},
    addedComment: Boolean=false,
    contentShow: Boolean=true,
    numeric: Boolean=false,
    startInitValue: String = "",
    errors: String? = null
) {
    var userEdit by rememberSaveable { mutableStateOf(startInitValue) }
    var comment by rememberSaveable { mutableStateOf<String?>(null) }
    var times by rememberSaveable { mutableStateOf<Int>(1) }

    val valid = validation(userEdit)

    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            if (contentShow) {
                Column {
                    TextField(
                        value = userEdit,
                        onValueChange = { userEdit = it },
                        isError = errors != null,
                        singleLine = true,
                        modifier = Modifier.padding(12.dp)
                    )
                    if(numeric){
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {times= (times-1).coerceAtLeast(1)}) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Decrease the number of times we add the value")
                            }

                            IconButton(onClick = { times += 1 }) {
                                Icon(imageVector = Icons.Default.ArrowDropUp, contentDescription = "Increase the number of times we add the value by 1")
                            }

                            Text(text = "value x ${times}")
                        }
                    }

                    if(errors!=null){
                        Text(text = errors, color = MaterialTheme.colorScheme.error)
                    }

                    if(addedComment){
                        TextField(
                            value = comment?:"",
                            onValueChange = { comment = it },
                            label = {Text(text = "Optional comment")},
                            singleLine = true,
                            modifier = Modifier.padding(12.dp)
                        )
                    }


                }
            }

        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    val primaryValue = if(numeric) (userEdit.toDouble() * times).toString() else userEdit
                    //always send a Pair of the 2 possible inputs even when one is not used
                    onConfirmation(Pair(primaryValue,comment))

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







//
//@Preview(showBackground = true)
//@Composable
//fun Previews(){
//    MaterialTheme() {
////        Column {
////            TopStats(
////                profile = ProfileEntity(
////                    id = 0,
////                    name = "Jhon Jhon",
////                    budget = 299.0,
////                    createdAt = 1233433
////                ),
////                totalSpend = 275.70
////            )
////            LazyRow(
////                horizontalArrangement = Arrangement.spacedBy(4.dp),
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .fillMaxHeight(0.9f)
////            ) {
////                item {
////                    CategoryComponent(
////                        budget = 100.0,
////                        personalView = false,
////                        category = CategoryEntity(
////                            id = 0,
////                            name = "Food",
////                            profileId = 0,
////                            createdAt = 100,
////                            currentExpense = 10.0,
////                            personalizedBudget = 640.0
////                        )
////                    )
////                }
////
////                item {
////                    CategoryComponent(
////                        budget = 100.0,
////                        personalView = true,
////                        category = CategoryEntity(
////                            id = 0,
////                            name = "Food2",
////                            profileId = 0,
////                            createdAt = 100,
////                            currentExpense = 30.0,
////                            personalizedBudget = 650.0
////                        )
////                    )
////                }
////
////                item {
////                    CategoryComponent(
////                        budget = 100.0,
////                        personalView = false,
////                        category = CategoryEntity(
////                            id = 0,
////                            name = "Food",
////                            profileId = 0,
////                            createdAt = 100,
////                            currentExpense = 30.0,
////                            personalizedBudget = 60.0
////                        )
////                    )
////                }
////
////                item {
////                    CategoryComponent(
////                        budget = 100.0,
////                        personalView = false,
////                        category = CategoryEntity(
////                            id = 0,
////                            name = "Food",
////                            profileId = 0,
////                            createdAt = 100,
////                            currentExpense = 30.0,
////                            personalizedBudget = 60.0
////                        )
////                    )
////                }
////            }
//        }
//    }
//}

