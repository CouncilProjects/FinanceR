package com.afterdark.financer.ui.screens.history

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.afterdark.financer.FinanceRApplication
import com.afterdark.financer.data.repositories.TransactionRepository
import com.afterdark.financer.ui.TriggeredUi
import com.afterdark.financer.ui.UiState
import com.afterdark.financer.ui.asUiState
import com.afterdark.financer.ui.screens.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


class HistoryViewModel(val savedState: SavedStateHandle,val transactionRepo: TransactionRepository) : ViewModel() {

    val args = savedState.toRoute<History>()

    private val _exportStatus = MutableStateFlow<UiState<String>>(TriggeredUi.NotStarted)

    private val _deletionStatus = MutableStateFlow<UiState<String>>(TriggeredUi.NotStarted)

    val uiState = combine(
        transactionRepo.getAllProfileTransactions(args.userId).asUiState(),
        _exportStatus,
        _deletionStatus
    ) {transactions,exportState,deleteState ->
        HistoryUiState(transactions=transactions, exportStatus = exportState, deletionStatus = deleteState)
    }.flowOn(Dispatchers.Default)
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun clearHistory(acknowledgement: Boolean = false){
        if(acknowledgement){
            _deletionStatus.update { TriggeredUi.NotStarted }
            return
        }

        _deletionStatus.update { UiState.Loading }
        viewModelScope.launch(Dispatchers.IO){
            transactionRepo.clearProfileTransactions(args.userId)
            _deletionStatus.update { UiState.Ok(data = "Successfully deleted history") }
        }
    }


    val pattern = "yyyy-MM-dd hh:mm"
    val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern)

    fun createExcelFile(context: Context, uri: Uri){
        _exportStatus.update { UiState.Loading }
        viewModelScope.launch(Dispatchers.IO) {
            val transactions = transactionRepo.getAllProfileTransactions(args.userId).first() //get a snapshot of it
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.bufferedWriter(Charsets.UTF_8).use{writer ->
                    writer.write('\uFEFF'.code) //BOM
                    writer.write("Date,Amount transferred,Category,Comment")
                    writer.newLine()
                    transactions.forEach {transaction->
                        var csvLine = simpleDateFormat.format(transaction.transaction.createdAt)
                        csvLine+=",${transaction.transaction.valueMoved}"
                        csvLine+=",${escapeString(transaction.categoryName)}"
                        csvLine+=",${escapeString(transaction.transaction.comment?:"")}"

                        writer.write(csvLine)
                        writer.newLine()
                    }

                    writer.close()
                }
                stream.close()
            }
           _exportStatus.update { UiState.Ok(data = "File downloaded") }
        }
    }

    fun exportSuccessNotified(){
        _exportStatus.update { TriggeredUi.NotStarted }
    }

    private fun escapeString(input: String): String{
        return if(input.contains(",") || input.contains("\n") || input.contains("\"")){
            "\"${input.replace("\"","\"\"")}\""
        } else {
            input
        }
    }


    companion object{
        val FACTORY : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val rememberstate = createSavedStateHandle()
                val app = (this[APPLICATION_KEY] as FinanceRApplication)
                val transactRepo = app.container.transactionRepository
                HistoryViewModel(savedState = rememberstate, transactionRepo = transactRepo)
            }
        }
    }
}