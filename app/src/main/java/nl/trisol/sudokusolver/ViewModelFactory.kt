package nl.trisol.sudokusolver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//class ViewModelFactory(private val apiHelper: ApiHelper, private val dbHelper: DatabaseHelper) :
class ViewModelFactory() :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(SingleNetworkCallViewModel::class.java)) {
//            return SingleNetworkCallViewModel(apiHelper, dbHelper) as T
//        }
//        if (modelClass.isAssignableFrom(SeriesNetworkCallsViewModel::class.java)) {
//            return SeriesNetworkCallsViewModel(apiHelper, dbHelper) as T
//        }
//        if (modelClass.isAssignableFrom(ParallelNetworkCallsViewModel::class.java)) {
//            return ParallelNetworkCallsViewModel(apiHelper, dbHelper) as T
//        }
//        if (modelClass.isAssignableFrom(RoomDBViewModel::class.java)) {
//            return RoomDBViewModel(apiHelper, dbHelper) as T
//        }
//        if (modelClass.isAssignableFrom(TimeoutViewModel::class.java)) {
//            return TimeoutViewModel(apiHelper, dbHelper) as T
//        }
//        if (modelClass.isAssignableFrom(TryCatchViewModel::class.java)) {
//            return TryCatchViewModel(apiHelper, dbHelper) as T
//        }
//        if (modelClass.isAssignableFrom(ExceptionHandlerViewModel::class.java)) {
//            return ExceptionHandlerViewModel(apiHelper, dbHelper) as T
//        }
//        if (modelClass.isAssignableFrom(LongRunningTaskViewModel::class.java)) {
//            return LongRunningTaskViewModel(apiHelper, dbHelper) as T
//        }
//        if (modelClass.isAssignableFrom(TwoLongRunningTasksViewModel::class.java)) {
//            return TwoLongRunningTasksViewModel(apiHelper, dbHelper) as T
//        }
        if (modelClass.isAssignableFrom(SolverViewModel::class.java)) {
            return SolverViewModel() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}