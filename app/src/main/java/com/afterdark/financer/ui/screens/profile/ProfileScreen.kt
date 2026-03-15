package com.afterdark.financer.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afterdark.financer.ui.UiState
import com.afterdark.financer.ui.theme.AppTheme
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel : ProfileScreenViewModel = viewModel(factory = ProfileScreenViewModel.Factory)) {
    val uiState by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope() // Compose coroutine scope
    val context = LocalContext.current
    val selected = uiState.selectedProfile

    var createDiag by rememberSaveable { mutableStateOf(false) }
    var deleteDiag by rememberSaveable { mutableStateOf(false) }



    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        if(uiState.deviceProfiles is UiState.Loading){
            CircularProgressIndicator()
            return
        }

        if(selected!=null){
            UserDisplay(
                profileName = selected.name,
                created = selected.createdAt,
                deleteUser = {deleteDiag=true}
            )

            Spacer(modifier= Modifier.height(4.dp))
            HorizontalDivider(thickness = 2.dp)

            BudgetDisplay(
                budget = selected.budget,
                budgetChange = { payload ->
                    viewModel.changeProfileBudget(payload as Double)
                }
            )
        } else {
            Text(text = "Select or create a profile")
        }

        Spacer(modifier= Modifier.height(4.dp))
        HorizontalDivider(thickness = 2.dp)

        LongBasicDropdownMenu(
            selected = selected?.name,
            menuData = (uiState.deviceProfiles as UiState.Ok)
                .data
                .filter { profile -> profile.id!=selected?.id }
                .map { profile ->  Pair(profile.id,profile.name) },
            selectNewProfile = {newId -> viewModel.changeActiveProfile(newId) }
        )

        Spacer(modifier= Modifier.height(4.dp))
        HorizontalDivider(thickness = 2.dp)

        Button(
            onClick = {createDiag=true}
        ) {
            Text(text = "Add a profile")
        }

        if(uiState.creationErrors is UiState.Ok){
            Toast.makeText(context,(uiState.creationErrors as UiState.Ok).data, Toast.LENGTH_LONG).show()
            viewModel.ackSuccessfulCreation()
        }

        if(createDiag){
            CreateProfileDialogComponent(
                dialogTitle = "Create a new profile",
                onDismissRequest = {createDiag=false},
                onConfirmation = {newProfile ->
                    scope.launch {
                        val ok = viewModel.createProfile(newProfile.first,newProfile.second)
                        if(ok){
                            createDiag=false
                        }
                    }

                                 },
                createErrorUi = uiState.creationErrors
            )
        } else if (deleteDiag){
            YouSureAlert(
                onDismissRequest = {deleteDiag=false},
                onConfirmation = {viewModel.deleteUser()}
            )
        }
    }
}

@Composable
fun LongBasicDropdownMenu(
    selected: String?,
    menuData:List<Pair<Long,String>>,
    selectNewProfile:(id:Long)-> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            Text(text = "Select another profile")
            Button(onClick = { expanded = !expanded }) {
                if(selected!=null){
                    Text(text = selected)
                } else {
                    Text(text = "Select a profile")
                }

                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuData.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(option.second)
                            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                           },
                    onClick = {
                        selectNewProfile(option.first)
                        expanded=false
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                )
            }
        }
    }
}


@Composable
fun CreateProfileDialogComponent(
    onDismissRequest: () -> Unit,
    onConfirmation: (payload: Pair<String, Double>) -> Unit,
    dialogTitle: String,
    createErrorUi: UiState<String>
) {
    var userEdit by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("0") }

    val okToProcedd= userEdit.isNotEmpty() && ((budget.toDoubleOrNull()?:-1.0)>0)
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Column {
                TextField(
                    value = userEdit,
                    onValueChange = {userEdit=it},
                    singleLine = true,
                    label = {Text(text = "Name")},
                    isError = createErrorUi is UiState.Error,

                    modifier = Modifier.padding(12.dp)
                )
                if (createErrorUi is UiState.Error) {
                    Text(
                        text = createErrorUi.errorMessage, // your error message
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextField(
                    value = budget,
                    onValueChange = {budget=it},
                    singleLine = true,
                    label = {Text(text = "Budget")},
                    modifier = Modifier.padding(12.dp)

                )
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                enabled = okToProcedd,
                onClick = {
                    val numBudget = budget.toDoubleOrNull() ?: 0.0
                    onConfirmation(Pair(userEdit,numBudget))
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
            Text(text = "Delete Profile")
        },
        text = {
            Text(text = "This is a permanent action that will delete the profile all its associated categories and history")
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

@Preview(showBackground = true,uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfilePreview(){
    AppTheme {
        LongBasicDropdownMenu(
            selected = "Jhon jhon",
            menuData = List(100) { Pair(1222,"Option ${it + 1}") },
            selectNewProfile = {},
        )
    }
}