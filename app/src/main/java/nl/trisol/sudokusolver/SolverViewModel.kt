package nl.trisol.sudokusolver

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolverViewModel : ViewModel() {

    private val _uiState = MutableLiveData<UiState<String>>()
    val uiState: LiveData<UiState<String>> = _uiState

    private val _sudokuBoard = MutableLiveData<Array<Array<SudokuUtils.SudokuCell>>>()
    val sudokuBoard: LiveData<Array<Array<SudokuUtils.SudokuCell>>> = _sudokuBoard

    companion object {
        val TAG = "SolverViewModel"
    }

    init {
        _sudokuBoard.value = SudokuUtils.emptySudoku2DArray()
    }

    fun setSudokuBoard(board: Array<Array<SudokuUtils.SudokuCell>>) {
        _sudokuBoard.value = board
    }

    fun setSudokuCell(row: Int, col: Int, cell: SudokuUtils.SudokuCell) {
        _sudokuBoard.value?.let {
            it[row][col] = cell
        }
    }

    fun startSolver() {
        viewModelScope.launch {
            _uiState.postValue(UiState.Loading)
            try {
                // do a long running task
                doSolve()
                _uiState.postValue(UiState.Success("OK"))
            } catch (e: Exception) {
                _uiState.postValue(e.message?.let { UiState.Error(it) })
            }
        }
    }

//    fun getUiState(): LiveData<UiState<String>> {
//        return _uiState
//    }
//
//    fun getSudokuBoard(): LiveData<Array<Array<SudokuUtils.SudokuCell>>> {
//        return _sudokuBoard
//    }

    private suspend fun doSolve() {
        withContext(Dispatchers.Default) {
            // your code for doing a long running task
            _sudokuBoard.value?.let {
                Log.d(TAG, "doSolve: ")
                SudokuUtils.set0(it)
                if (!Solver.getInstance().checkStartGridValidity(it).isSuccess){
                //if (!Solver.getInstance().solveSudoku(it)) {
                    SudokuUtils.set0(it)
                }
            }
        }
    }
}