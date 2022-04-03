package edu.vt.cs.cs5254.dreamcatcher

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.children
import androidx.lifecycle.Observer
import edu.vt.cs.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import java.util.*

private const val TAG = "DetailFragment"
private const val ARG_DREAM_ID = "dream_id"

private val REFLECTION_BLUE = "#3498DB"
private val CONCEIVED_YELLOW = "#F4D03F"
private val DEFERRED_RED = "#E74C3C"
private val FULFILLED_GREEN = "#27AE60"


class DreamDetailFragment : Fragment() {

    private lateinit var dreamWithEntries: DreamWithEntries

    private var _binding: FragmentDreamDetailBinding? = null
    private val binding: FragmentDreamDetailBinding
        get() = _binding!!  //never null

    lateinit var buttonList: List<Button>
    private val vm: DreamDetailViewModel by lazy {
        ViewModelProvider(this).get(DreamDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dreamWithEntries = DreamWithEntries(Dream(), emptyList())  //????


        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        Log.d(TAG, "Dream Fragment created with ID: $dreamId")  //debug

        vm.loadDream(dreamId)

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
//        vm.loadDream(dreamId)
//
//        Log.d(TAG, "Detail fragment with ID ${dreamWithEntries.dream.id}")
//    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        crime = Crime()
//        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
//        Log.d(TAG, "args bundle crime ID: $crimeId")
//        viewModel.loadCrime(crimeId) This will trigger database
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDreamDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        buttonList = binding.root //view
            .children
            .toList()
            .filterIsInstance<Button>()
        //check box
//        if (dreamWithEntries.dream.isFulfilled) {
//            binding.dreamFulfilledCheckbox.isChecked = dreamWithEntries.dream.isFulfilled
//            binding.dreamDeferredCheckbox.isEnabled = false
//        }
//        if (dreamWithEntries.dream.isDeferred) {
//            binding.dreamDeferredCheckbox.isChecked = dreamWithEntries.dream.isDeferred
//            binding.dreamFulfilledCheckbox.isEnabled = false
//        }
//
//        updateUI()

            //???
//        binding.dreamDate.apply {
//            text = vm.dreamWithEntry.dream.date.toString()
//            isEnabled = false
//        }

        return view
    }


    override fun onViewCreated( view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.dreamLiveData.observe(
            viewLifecycleOwner,
            Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamWithEntries = dreamWithEntries
                    refreshView()
                }
            })
    }


    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(sequence: CharSequence?,
                                       start: Int, before: Int, count: Int) {
                dreamWithEntries.dream.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) { }
        }

        //

        binding.dreamTitleText.addTextChangedListener(titleWatcher)  //text or label ?
        binding.dreamFulfilledCheckbox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                dreamWithEntries.dream.isFulfilled = isChecked

                if(isChecked){
                    val dreamId = dreamWithEntries.dream.id
                    if (!dreamWithEntries.dreamEntries.any {it.kind == DreamEntryKind.FULFILLED}){
                        dreamWithEntries.dreamEntries += DreamEntry(
                            kind = DreamEntryKind.FULFILLED,
                            dreamId = dreamId)
                    }

                }else{
                    val newEntry = dreamWithEntries.dreamEntries.toMutableList()
                    newEntry.removeLast()
                    dreamWithEntries.dreamEntries = newEntry
                }

                refreshView()
            }  //viewModel.crime.isSolved = isChecked }
        }

        binding.dreamDeferredCheckbox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                dreamWithEntries.dream.isDeferred = isChecked
                if(isChecked){
                    val dreamId = dreamWithEntries.dream.id

                    if (!dreamWithEntries.dreamEntries.any {it.kind == DreamEntryKind.FULFILLED}){
                        dreamWithEntries.dreamEntries += DreamEntry(
                            kind = DreamEntryKind.DEFERRED,
                            dreamId = dreamId)
                    }

                }else{
                    val newEntry = dreamWithEntries.dreamEntries.toMutableList()
                    newEntry.removeLast()
                    dreamWithEntries.dreamEntries = newEntry
                }
                refreshView()
            }
        }

    }

    override fun onStop() {
        super.onStop()
        vm.saveDream(dreamWithEntries)
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



//    private fun updateUI() {
//
//
//        binding.dreamTitleText.setText(dreamWithEntries.dream.title) //title
//
//        // zip entries and button
//        buttonList = binding.root //view
//            .children
//            .toList()
//            .filterIsInstance<Button>()
//
//        buttonList.zip(dreamWithEntries.dreamEntries) { button, entry ->
//            button.visibility = View.VISIBLE
//            when (entry.kind) {
//                DreamEntryKind.CONCEIVED -> {
//                    button.text = "CONCEIVED"
//                    setButtonColor(button, CONCEIVED_YELLOW)
//                }
//                DreamEntryKind.REFLECTION -> {
//                    val time = DateFormat.format("MMM dd, yyyy", entry.date)
//                    button.text = time.toString() + ": " + entry.text
//                    setButtonColor(button, REFLECTION_BLUE)
//                }
//                DreamEntryKind.FULFILLED -> {
//                    button.text = "FULFILLED"
//                    setButtonColor(button, FULFILLED_GREEN)
//                }
//                DreamEntryKind.DEFERRED -> {
//                    button.text = "DEFERRED"
//                    setButtonColor(button, DEFERRED_RED)
//                }
//            }
//        }
//        binding.dreamFulfilledCheckbox.jumpDrawablesToCurrentState() //??
//        binding.dreamDeferredCheckbox.jumpDrawablesToCurrentState()
//    }

    private fun refreshView() {

        binding.dreamTitleText.setText(dreamWithEntries.dream.title) //title

        when {
            dreamWithEntries.dream.isFulfilled -> {
                binding.dreamFulfilledCheckbox.isChecked = true
                binding.dreamDeferredCheckbox.isEnabled = false
            }
            dreamWithEntries.dream.isDeferred -> {
                binding.dreamFulfilledCheckbox.isEnabled = false
                binding.dreamDeferredCheckbox.isChecked = true
            }
            else -> {
                binding.dreamFulfilledCheckbox.isEnabled = true
                binding.dreamDeferredCheckbox.isEnabled = true
                binding.dreamFulfilledCheckbox.isChecked = false
                binding.dreamDeferredCheckbox.isChecked = false
            }
        }

        buttonList.forEach { it.visibility = View.INVISIBLE }

        buttonList.zip(dreamWithEntries.dreamEntries) {
                button, entry -> button.visibility = View.VISIBLE
            when (entry.kind) {
                DreamEntryKind.CONCEIVED -> {
                    button.text = "CONCEIVED"
                    setButtonColor(button, CONCEIVED_YELLOW)
                }
                DreamEntryKind.REFLECTION -> {
                    val time = DateFormat.format("MMM dd, yyyy", entry.date)
                    button.text = time.toString() + ": " + entry.text
                    setButtonColor(button, REFLECTION_BLUE)
                }
                DreamEntryKind.FULFILLED -> {
                    button.text = "FULFILLED"
                    setButtonColor(button, FULFILLED_GREEN)
                }
                DreamEntryKind.DEFERRED -> {
                    button.text = "DEFERRED"
                    setButtonColor(button, DEFERRED_RED)
                }
            }
        }
    }

    private fun setButtonColor(button: Button, colorString: String) {
        button.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(colorString))

    }

    companion object {
        fun newInstance(dreamId: UUID): DreamDetailFragment { //MainActivity call
            val args = Bundle().apply {
                putSerializable(ARG_DREAM_ID, dreamId)
            }
            return DreamDetailFragment().apply {
                arguments = args }
        }
    }
}

