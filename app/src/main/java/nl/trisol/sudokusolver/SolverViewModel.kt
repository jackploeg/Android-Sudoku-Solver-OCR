package nl.trisol.sudokusolver

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
                _uiState.postValue(UiState.Success("Task Completed"))
            } catch (e: Exception) {
                _uiState.postValue(UiState.Error("Something Went Wrong"))
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
                SudokuUtils.set0(it)
                Solver.getInstance().checkStartGridValidity(it)
                Solver.getInstance().solveSudoku(it)
                SudokuUtils.set0(it)
            }
            // Added delay to simulate
            //delay(5000)
        }
    }
}