package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel

class DreamListViewModel : ViewModel() {


    private val dreamRepository = DreamRepository.get()
    val dreamListLiveData = dreamRepository.getDreams()

    fun addDream(dreamWithEntries: DreamWithEntries) {
        val newEntries = mutableListOf<DreamEntry>()
        newEntries += DreamEntry(kind = DreamEntryKind.CONCEIVED, dreamId = dreamWithEntries.dream.id)
        dreamWithEntries.dreamEntries =newEntries
        dreamRepository.addDreamWithEntries(dreamWithEntries)

    }

    fun deleteAllDreams() {
        dreamRepository.deleteAllDreamEntriesInDatabase()
        dreamRepository.deleteAllDreamsInDatabase()
    }
}