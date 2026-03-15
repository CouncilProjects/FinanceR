package com.afterdark.financer.ui.screens.history

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.room.util.performInTransactionSuspending
import com.afterdark.financer.FinanceRApplication
import com.afterdark.financer.data.repositories.TransactionRepository
import com.afterdark.financer.ui.screens.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


fun<T> Flow<T>.asUiState() : Flow<UiState<T>> {
    return map<T, UiState<T>> { UiState.Success(it) as UiState<T> }
        .onStart { emit(UiState.Loading) }
        .catch { err -> emit(UiState.Error(errorMessage = err.message?:"Not know")) }
}


class HistoryViewModel(val savedState: SavedStateHandle,val transactionRepo: TransactionRepository) : ViewModel() {

    val args = savedState.toRoute<History>()

    private val _exportStatus = MutableStateFlow<UiStateTriggered<String>>(UiStateTriggered.NoStart)

    val uiState = combine(
        transactionRepo.getAllProfileTransactions(args.userId).asUiState(),
        _exportStatus
    ) {transactions,exportState ->
        HistoryUiState(transactions=transactions, exportStatus = exportState)
    }.flowOn(Dispatchers.Default)
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun clearHistory(){
        viewModelScope.launch(Dispatchers.IO){
            transactionRepo.clearProfileTransactions(args.userId)
        }
    }


    val pattern = "yyyy-MM-dd hh:mm"
    val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern)

    fun createExcelFile(context: Context, uri: Uri){
        _exportStatus.update { UiStateTriggered.Loading }
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
           _exportStatus.update { UiStateTriggered.Success(data = "File downloaded") }
        }
    }

    fun exportSuccessNotified(){
        _exportStatus.update { UiStateTriggered.NoStart }
    }

    private fun escapeString(input: String): String{
        if(input.contains(",") || input.contains("\n") || input.contains("\"")){
            return  "\"${input.replace("\"","\"\"")}\""
        } else {
            return input
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