package nl.trisol.sudokusolver

import android.content.Context
import android.util.Log
import android.widget.Toast

class Solver(_context: Context) {

    private lateinit var sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>

    private val context = _context.applicationContext

    companion object {

        private lateinit var instance: Solver
        private const val TAG = "Solver"

        fun init(_context: Context) {
            instance = Solver(_context)
        }

        fun getInstance() = instance
    }

    fun solveSudoku(_sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>): Boolean {
        sudokuBoard = _sudokuBoard


        // If solve() returns false then there is no solution for the puzzle
        if(!solveFromLowToHigh()) {
            Toast.makeText(
                context,
                context.getString(R.string.unsolvable),
                Toast.LENGTH_SHORT
            ).show()
            SudokuUtils.set0(sudokuBoard)
            return false
        }
        return true
    }

    /**
     * Check if we can place a number at the given row and column.
     */
    private fun numberIsValidForPosition(row: Int, col: Int, n: Int): Boolean {
        // Check on row and column
        for (i in 0 until 9) {
            if (sudokuBoard[row][i].number == n || sudokuBoard[i][col].number == n)
                return false
        }

        // Check in the 3x3 subgrid
        // Get the top left row and column indexes of the subgrid and iterate the subgrid
        val subgridTLi = row / 3
        val subgridTlj = col / 3
        for (i in subgridTLi * 3 until subgridTLi * 3 + 3) {
            for (j in subgridTlj * 3 until subgridTlj * 3 + 3) {
                if (sudokuBoard[i][j].number == n && i != row && j != col)
                    return false
            }
        }

        return true
    }

    fun checkStartGridValidity(_sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>): Result<Boolean> {
        sudokuBoard = _sudokuBoard
        for (row in 0 .. 8) {
            for (col in 0..8) {
                val entry = sudokuBoard[row][col].number
                if (entry > 0) {
                    // Check on row and column
                    for (i in 0 until 9) {
                        if (i != col && sudokuBoard[row][i].number == entry)
                            return error(context.getString(R.string.duplicate_in_row))
                        if (i != row && sudokuBoard[i][col].number == entry)
                            return error(context.getString(R.string.duplicate_in_column))
                    }

                    // Check in the 3x3 subgrid
                    // Get the top left row and column indexes of the subgrid and iterate the subgrid
                    val subgridTLi = row / 3
                    val subgridTlj = col / 3
                    for (i in subgridTLi * 3 until subgridTLi * 3 + 3) {
                        for (j in subgridTlj * 3 until subgridTlj * 3 + 3) {
                            if (sudokuBoard[i][j].number == entry && i != row && j != col)
                                return error(context.getString(R.string.duplicate_in_square))
                        }
                    }
                }
            }
        }

        return singleSolution()
    }

    private fun singleSolution(): Result<Boolean> {
        SudokuUtils.set0(sudokuBoard)
        if (solveFromLowToHigh()) {
            val sbUp = StringBuilder()
            sudokuBoard.forEach {row -> row.forEach { cell -> sbUp.append(cell.number)}}
            val up = sbUp.toString()
            Log.d(TAG, "singleSolution: $up")

            SudokuUtils.set0(sudokuBoard)
            solveFromHighToLow()
            val sbDown = StringBuilder()

            sudokuBoard.forEach {row -> row.forEach { cell -> sbDown.append(cell.number)}}
            val down = sbDown.toString()
            Log.d(TAG, "singleSolution: $down")
            if (up == down) {
                return Result.success(true)
            } else {
                return error(context.getString(R.string.multiple_solutions))
            }
        }
        return error(context.getString(R.string.multiple_solutions))
    }

    /**
     * Function to solve the sudoku
     *
     * Find all numbers that must be completed and for each one try every number from 1 to 9.
     * If a solution is valid place the number and try solving further, otherwise try another solution
     */
    private fun solveFromLowToHigh(index: Int = 0): Boolean {
        for (i in index until 81) {
            val r = i / 9
            val c = i % 9
            if (sudokuBoard[r][c].number == 0) {
                for (n in 1..9) {
                    if (numberIsValidForPosition(r, c, n)) {
                        sudokuBoard[r][c].number = n
                        if (solveFromLowToHigh(i + 1)) {
                            return true
                        }
                        sudokuBoard[r][c].number = 0
                    }
                }
                return false
            }
        }
        return true
    }

    private fun solveFromHighToLow(index: Int = 0): Boolean {
        for (i in index until 81) {
            val r = i / 9
            val c = i % 9
            if (sudokuBoard[r][c].number == 0) {
                for (n in 9 downTo 1) {
                    if (numberIsValidForPosition(r, c, n)) {
                        sudokuBoard[r][c].number = n
                        if (solveFromLowToHigh(i + 1)) {
                            return true
                        }
                        sudokuBoard[r][c].number = 0
                    }
                }
                return false
            }
        }
        return true
    }
}