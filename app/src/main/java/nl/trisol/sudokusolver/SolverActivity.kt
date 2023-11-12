package nl.trisol.sudokusolver

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nl.trisol.sudokusolver.R

class SolverActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SudokuSolver"

        private const val STATE_IMAGE = 0
        private const val STATE_EDIT = 1
    }

    private lateinit var sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>

    private var STATE = STATE_EDIT
    private var solvable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solve)

        if (intent.hasExtra("sudokuBoard")) {
            sudokuBoard =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getSerializableExtra("sudokuBoard", Array<Array<SudokuUtils.SudokuCell>>::class.java)!!
                else
                    intent.getSerializableExtra("sudokuBoard") as Array<Array<SudokuUtils.SudokuCell>>

            solvable = Solver.getInstance().solveSudoku(sudokuBoard)
            changeActiveFragment(SolveEditFragment(sudokuBoard))
            findViewById<Button>(R.id.editBtn).setBackgroundResource(R.drawable.baseline_done_24)

        } else {
            sudokuBoard = SudokuUtils.emptySudoku2DArray()

            changeActiveFragment(SolveEditFragment(sudokuBoard))
            findViewById<Button>(R.id.editBtn).setBackgroundResource(R.drawable.baseline_done_24)
        }

        // SudokuUtils.printSudokuBoard(sudokuBoard)

        findViewById<Button>(R.id.backBtn).setOnClickListener {
            finish()
        }

//        findViewById<Button>(R.id.editBtn).setOnClickListener {
//            if(STATE == STATE_IMAGE) {
//                STATE = STATE_EDIT
//
//                SudokuUtils.set0(sudokuBoard)
//                changeActiveFragment(SolveEditFragment(sudokuBoard))
//
//                findViewById<Button>(R.id.editBtn).setBackgroundResource(R.drawable.baseline_done_24)
//            } else {
//                STATE = STATE_IMAGE
//                changeActiveFragment(SolveImageFragment("tmpBitmap", sudokuBoard))
//                findViewById<Button>(R.id.editBtn).setBackgroundResource(R.drawable.baseline_edit_24)
//
//            }
//        }
    }

    /**
     *  Provide a way for the fragments to bind a listener to a view that is not inside
     *  the fragment, but is accessible within the activity
     */
    fun <T : View> bindListenerToView(id: Int, listener: OnClickListener) {
        findViewById<T>(id).setOnClickListener(listener)
    }

    fun changeActiveFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commit()
    }
}