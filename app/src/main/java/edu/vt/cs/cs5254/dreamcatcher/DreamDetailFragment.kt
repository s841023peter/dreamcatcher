package edu.vt.cs.cs5254.dreamcatcher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import edu.vt.cs.cs5254.dreamcatcher.databinding.ListItemDreamBinding
import edu.vt.cs.cs5254.dreamcatcher.databinding.ListItemDreamEntryBinding
import java.io.File
import java.util.*

private const val TAG = "DetailFragment"
private const val ARG_DREAM_ID = "dream_id"
const val REQUEST_KEY = "request_key"
const val ARG_NEW_DATE = "new_date"
const val REQUEST_KEY_ADD_REFLECTION ="request_key_add_reflection"
const val BUNDLE_KEY_REFLECTION_TEXT ="bundle_key_reflection_text"

private val REFLECTION_BUTTON_COLOR = "#30B5F1"
private val CONCEIVED_BUTTON_COLOR = "#008525"
private val DEFERRED_BUTTON_COLOR = "#D9D9D9"
private val FULFILLED_BUTTON_COLOR = "#C16694"


private val REFLECTION_BLUE = "#3498DB"
private val CONCEIVED_YELLOW = "#F4D03F"
private val DEFERRED_RED = "#E74C3C"
private val FULFILLED_GREEN = "#27AE60"




class DreamDetailFragment : Fragment() {
    private var adapter: DreamEntryAdapter? = null

    private lateinit var dreamWithEntries: DreamWithEntries

    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var photoLauncher: ActivityResultLauncher<Uri>

    private var _binding: FragmentDreamDetailBinding? = null
    private val binding: FragmentDreamDetailBinding
        get() = _binding!!  //never null

//    lateinit var buttonList: List<Button>
//    private val vm: DreamDetailViewModel by lazy {
//        ViewModelProvider(this).get(DreamDetailViewModel::class.java)
//    }
    private val vm: DreamDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dreamWithEntries = DreamWithEntries(Dream(), emptyList())  //????


        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        Log.d(TAG, "Dream Fragment created with ID: $dreamId")  //debug

        vm.loadDream(dreamId)

        setHasOptionsMenu(true)
        photoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                updatePhotoView()
            }
            requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

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

//        buttonList = binding.root //view
//            .children
//            .toList()
//            .filterIsInstance<Button>()
        //check box
//        if (dreamWithEntries.dream.isFulfilled) {
//            binding.dreamFulfilledCheckbox.isChecked = dreamWithEntries.dream.isFulfilled
//            binding.dreamDeferredCheckbox.isEnabled = false
//        }
//        if (dreamWithEntries.dream.isDeferred) {
//            binding.dreamDeferredCheckbox.isChecked = dreamWithEntries.dream.isDeferred
//            binding.dreamFulfilledCheckbox.isEnabled = false
//        }
        binding.dreamEntryRecyclerView.layoutManager = LinearLayoutManager(context)
        refreshView()

        return view
    }


    override fun onViewCreated( view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.dreamLiveData.observe(
            viewLifecycleOwner,
            Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamWithEntries = dreamWithEntries

                    photoFile = vm.getPhotoFile(dreamWithEntries.dream)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "edu.vt.cs.cs5254.dreamcatcher.fileprovider",
                        photoFile
                    )
                    refreshView()
                    updatePhotoView()
                }
            })
    }



    override fun onStart() {
        super.onStart()


        val itemTouchHelper = ItemTouchHelper(
            SwipeToDeleteCallback(
                binding.dreamEntryRecyclerView.adapter!! as DreamEntryAdapter,
                0,
                ItemTouchHelper.LEFT
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.dreamEntryRecyclerView)

        binding.dreamTitleText.doOnTextChanged { text, start, before, count ->
            dreamWithEntries.dream.title = text.toString()
        }


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
                        .filterNot { entry -> entry.kind == DreamEntryKind.FULFILLED }

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
                        .filterNot { entry -> entry.kind == DreamEntryKind.DEFERRED }
                    dreamWithEntries.dreamEntries = newEntry
                }
                refreshView()
            }
        }

//        binding.dreamDate.setOnClickListener {
//            DatePickerFragment.newInstance(dreamWithEntries.dream.date, REQUEST_KEY)  // crime.date
//                .show(parentFragmentManager, REQUEST_KEY)
//        }

        binding.addReflectionButton.setOnClickListener {
            AddReflectionDialog().show(parentFragmentManager, REQUEST_KEY_ADD_REFLECTION)
        }

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_ADD_REFLECTION,
            viewLifecycleOwner
        )
        { _, bundle ->
            val reflectionText = bundle.getString(BUNDLE_KEY_REFLECTION_TEXT, "")

            val newDreamEntry = DreamEntry(
                dreamId = dreamWithEntries.dream.id,
                text = reflectionText,
                kind = DreamEntryKind.REFLECTION
            )

            dreamWithEntries.dreamEntries += newDreamEntry

            refreshView()
        }
    }

    override fun onStop() {
        super.onStop()
        vm.saveDreamWithEntries(dreamWithEntries)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // option menu

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream_detail, menu)
        val cameraAvailable = PictureUtils.isCameraAvailable(requireActivity())
        val menuItem = menu.findItem(R.id.take_dream_photo)
        menuItem.apply {
            Log.d(TAG, "Camera available: $cameraAvailable")
            isEnabled = cameraAvailable
            isVisible = cameraAvailable
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.take_dream_photo -> {
                val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                }
                requireActivity().packageManager
                    .queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    .forEach { cameraActivity ->
                        requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                photoLauncher.launch(photoUri)
                true
            }
            R.id.share_dream -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getSharedDream())
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.dream_share_subject)
                    )
                }.also { intent ->
                    val chooserIntent =
                        Intent.createChooser(intent, getString(R.string.send_share))
                    startActivity(chooserIntent)
                }
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    //TODO
    private fun getSharedDream(): String {
                    val stateString = when {
                        dreamWithEntries.dream.isFulfilled -> {
                            getString(R.string.share_dream_fulfilled)
            }
            dreamWithEntries.dream.isDeferred -> {
                getString(R.string.share_dream_deferred)
            }
            else -> {
                ""
            }
        }
        val dreamTitle = dreamWithEntries.dream.title
        val conceiveDate = dreamWithEntries.dreamEntries.toMutableList().elementAt(0).date
        val date = DateFormat.format("MMM dd, yyyy", conceiveDate)
        var entriesTextList = mutableListOf<String>()
        dreamWithEntries.dreamEntries
            .filter { it -> it.kind == DreamEntryKind.REFLECTION }
            .forEach { it -> entriesTextList += ("- " + it.text) }
        val reflectionText = entriesTextList.joinToString(separator = "\n")
        return getString(
            R.string.dream_share,
            dreamTitle,
            date,
            reflectionText,
            stateString
        )
    }

    private fun refreshView() {

        binding.dreamTitleText.setText(dreamWithEntries.dream.title) //title
        adapter = DreamEntryAdapter(dreamWithEntries.dreamEntries)
        binding.dreamEntryRecyclerView.adapter = adapter


        when {
            dreamWithEntries.dream.isFulfilled -> {
                binding.dreamFulfilledCheckbox.isChecked = true
                binding.dreamDeferredCheckbox.isEnabled = false
                binding.addReflectionButton.isEnabled = false
            }
            dreamWithEntries.dream.isDeferred -> {
                binding.dreamFulfilledCheckbox.isEnabled = false
                binding.dreamDeferredCheckbox.isChecked = true
                binding.addReflectionButton.isEnabled = true
            }
            else -> {
                binding.addReflectionButton.isEnabled = true
                binding.dreamFulfilledCheckbox.isEnabled = true
                binding.dreamDeferredCheckbox.isEnabled = true
                binding.dreamFulfilledCheckbox.isChecked = false
                binding.dreamDeferredCheckbox.isChecked = false
            }
        }
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile.path, 120, 120)
            binding.dreamPhoto.setImageBitmap(bitmap)
        } else {
            binding.dreamPhoto.setImageDrawable(null)
        }
    }


//        buttonList.forEach { it.visibility = View.INVISIBLE }
//
//        buttonList.zip(dreamWithEntries.dreamEntries) {
//                button, entry -> button.visibility = View.VISIBLE
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


    private fun setButtonColor(button: Button, colorString: String) {
        button.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(colorString))

    }

    private fun refreshEntryButton(entryButton: Button, dreamEntry: DreamEntry) {
        when (dreamEntry.kind) {
            DreamEntryKind.CONCEIVED -> {
                entryButton.text = "CONCEIVED"
                setButtonColor(entryButton, CONCEIVED_BUTTON_COLOR)
            }
            DreamEntryKind.REFLECTION -> {
                val time = DateFormat.format("MMM dd, yyyy", dreamEntry.date)
                entryButton.text = time.toString() + ": " + dreamEntry.text
                setButtonColor(entryButton, REFLECTION_BUTTON_COLOR)
            }
            DreamEntryKind.FULFILLED -> {
                entryButton.text = "FULFILLED"
                setButtonColor(entryButton, FULFILLED_BUTTON_COLOR)
            }
            DreamEntryKind.DEFERRED -> {
                entryButton.text = "DEFERRED"
                setButtonColor(entryButton, DEFERRED_BUTTON_COLOR)
            }
        }
    }
    // DreamEntry Holder && Adapter
    inner class DreamEntryHolder(val itemBinding: ListItemDreamEntryBinding) :
        RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        private lateinit var dreamEntry: DreamEntry

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(dreamEntry: DreamEntry) {
            this.dreamEntry = dreamEntry
            refreshEntryButton(itemBinding.dreamEntryButton, dreamEntry)
        }
        override fun onClick(v: View) {
//            callbacks?.onDreamSelected(dream.id)
        }
        }


    private inner class DreamEntryAdapter(var dreamEntries: List<DreamEntry>) :
        RecyclerView.Adapter<DreamEntryHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
        ): DreamEntryHolder {
            val itemBinding = ListItemDreamEntryBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return DreamEntryHolder(itemBinding)
        }
        //

        override fun getItemCount() = dreamEntries.size
        override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
            val dream = dreamEntries[position]
            //onBindViewHolder every time when scroll, so it's more frequently
            holder.bind(dream)
        }


        fun deleteItem(position: Int) {
            val recentlyDeletedItem = dreamEntries[position]
            if (recentlyDeletedItem.kind == DreamEntryKind.REFLECTION) {
                dreamWithEntries.dreamEntries =
                    dreamWithEntries.dreamEntries.filter { it.id != recentlyDeletedItem.id }
            }
            refreshView()
        }
    }


    private inner class SwipeToDeleteCallback(
        adapter: RecyclerView.Adapter<DreamEntryHolder>,
        dragDirs: Int,
        swipeDirs: Int
    ) :
        ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position: Int = viewHolder.adapterPosition
            adapter?.deleteItem(position)
        }
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }



    }

//        override fun onClick(v: View) {
//            callbacks?.onDreamSelected(dream.id) //view model
//        } //callbacks"?" -> safe call operator, if not return anything, return null?

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






