package nl.trisol.sudokusolver

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import nl.trisol.sudokusolver.R
import nl.trisol.sudokusolver.imageproc.OpenCV
import org.opencv.core.Mat

//class SolveImageFragment(_fileName: String, _sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>) : Fragment() {
class SolveImageFragment(val fileName: String, val viewModel: SolverViewModel) : Fragment() {

    //private val fileName = _fileName
    //private val sudokuBoard = _sudokuBoard
    private var EDIT = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_solve_on_image, container, false)

        // Get the bitmap from file saved in saved in storage
        val file = context?.getFileStreamPath(fileName)
        var bitmap = BitmapFactory.decodeFile(file?.path)

        // TODO alleen bitmap laden als er iets zinvols is gescand, anders default sudoku image
        var mat: Mat
        if (bitmap == null) {
            mat = SudokuUtils.bitmapToMat(ResourcesCompat.getDrawable(resources, R.drawable.blank, null)?.toBitmap()!!)
        } else {
            mat = SudokuUtils.bitmapToMat(bitmap)
        }
        viewModel.sudokuBoard.let { sudokuboarddata ->
            sudokuboarddata.value?.let { sudokuBoard ->
                OpenCV.overlaySolutionOnImage(mat, sudokuBoard)

                bitmap = SudokuUtils.matToBitmap(mat)

                view.findViewById<ImageView>(R.id.image_view).setImageBitmap(bitmap)

                (activity as SolverActivity).findViewById<Button>(R.id.editBtn)
                    .setBackgroundResource(R.drawable.baseline_edit_24)
                (activity as SolverActivity).bindListenerToView<Button>(R.id.editBtn) {
                    //EDIT = !EDIT
                    //if(EDIT) {
                    (activity as SolverActivity).changeActiveFragment(SolveEditFragment())
                    //}
                }

            }
        }
        return view
    }
}