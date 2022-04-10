package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class DreamDetailViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    private val dreamIdLiveData = MutableLiveData<UUID>()

    var dreamLiveData: LiveData<DreamWithEntries> =
        Transformations.switchMap(dreamIdLiveData) { dreamId ->
            dreamRepository.getDreamWithEntries(dreamId) } //

    fun loadDream(dreamId: UUID) {
        dreamIdLiveData.value = dreamId     }
    fun saveDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        dreamRepository.updateDreamWithEntries(dreamWithEntries)
    }
}


//    private val dreamRepository = DreamRepository.get()
//    lateinit var dream: Dream
//
//    fun loadDream(dreamId: UUID) {
//        dream = Dream()
//    }



//class DreamDetailViewModel : ViewModel() {
//
//    private val dreamRepository = DreamRepository.get()
//    lateinit var dreamWithEntry: DreamWithEntries
//
//    fun loadDream(dreamId: UUID) {  //getDream @209
//
//        dreamWithEntry = dreamRepository.getDreamWithEntries(dreamId) //if null, then
//            ?: throw IllegalArgumentException("Dream with ID $dreamId not found")
//    }
//
//    //get ?
//
//}
