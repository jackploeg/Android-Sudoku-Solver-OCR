package nl.trisol.sudokusolver

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import nl.trisol.sudokusolver.R

class SolverActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SudokuSolver"
    }

    private lateinit var viewModel: SolverViewModel

    private lateinit var sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solve)
        setupViewModel()
        setupSolverWatcher()

        if (intent.hasExtra("sudokuBoard")) {
            sudokuBoard =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getSerializableExtra("sudokuBoard", Array<Array<SudokuUtils.SudokuCell>>::class.java)!!
                else
                    intent.getSerializableExtra("sudokuBoard") as Array<Array<SudokuUtils.SudokuCell>>

            //solvable = Solver.getInstance().solveSudoku(sudokuBoard)

            viewModel.setSudokuBoard(sudokuBoard)
            changeActiveFragment(SolveEditFragment())
            findViewById<Button>(R.id.editBtn).setBackgroundResource(R.drawable.baseline_done_24)

        } else {
            sudokuBoard = SudokuUtils.emptySudoku2DArray()
            viewModel.setSudokuBoard(sudokuBoard)
            changeActiveFragment(SolveEditFragment())
            findViewById<Button>(R.id.editBtn).setBackgroundResource(R.drawable.baseline_done_24)
        }

        // SudokuUtils.printSudokuBoard(sudokuBoard)

        findViewById<Button>(R.id.backBtn).setOnClickListener {
            finish()
        }

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

    fun solveSudoku() {
        viewModel.startSolver()
    }

    private fun setupSolverWatcher() {
        viewModel.uiState.observe(this) {
            when (it) {
                is UiState.Success -> {
//                    progressBar.visibility = View.GONE
//                    textView.text = it.data
//                    textView.visibility = View.VISIBLE
                    Toast.makeText(this, "Succes!",Toast.LENGTH_LONG).show()
                }
                is UiState.Loading -> {
//                    progressBar.visibility = View.VISIBLE
//                    textView.visibility = View.GONE
                }
                is UiState.Error -> {
                    //Handle Error
//                    progressBar.visibility = View.GONE
//                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
            }
        }
//        viewModel.startSolver()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
        )[SolverViewModel::class.java]
    }

}