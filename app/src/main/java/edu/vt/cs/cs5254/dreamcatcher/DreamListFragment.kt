package edu.vt.cs.cs5254.dreamcatcher

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.databinding.FragmentDreamListBinding
import edu.vt.cs.cs5254.dreamcatcher.databinding.ListItemDreamBinding

import java.util.*

private const val TAG = "DreamListFragment"

class DreamListFragment : Fragment() {

    interface Callbacks {
        fun onDreamSelected(dreamId: UUID)
    }
    private var callbacks: Callbacks? = null


    private var _binding: FragmentDreamListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DreamListViewModel by viewModels()

    private var adapter: Adapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // @192 _binding = FragmentDreamListBinding.inflate(layoutInflater, container, false)
        _binding = FragmentDreamListBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.dreamRecyclerView.layoutManager = LinearLayoutManager(context)

        //updateUI()  //refresh view     //Remove the call to updateUI in onCreateView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.dreamListLiveData.observe(
            viewLifecycleOwner,
            Observer { dreams ->
                dreams?.let {
                    Log.i(TAG, "Got dreams ${dreams.size}")
                    updateUI(dreams)
                }
            }
        )
    }


    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    // option menu callbacks
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream_list, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_dream -> {
//                val dream = Dream()
//                val newEntries = mutableListOf<DreamEntry>()
//                newEntries += DreamEntry(kind = DreamEntryKind.CONCEIVED, dreamId = dream.id)


                val dreamWithEntries = DreamWithEntries(Dream(), emptyList()) //Dream
                viewModel.addDream(dreamWithEntries)
                callbacks?.onDreamSelected(dreamWithEntries.dream.id)   // dream?????
                true
            }
            R.id.delete_all_dreams -> {
                viewModel.deleteAllDreams()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }




    private fun updateUI(dreams: List<Dream>) {

        adapter = Adapter(dreams)
        binding.dreamRecyclerView.adapter = adapter
    }


    // Holder && Adapter
    public inner class DreamHolder(val itemBinding: ListItemDreamBinding) :
        RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        private lateinit var dream: Dream

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(dream: Dream) { //adapter
            this.dream = dream
            itemBinding.dreamItemTitle.text = this.dream.title

            val date = DateFormat.format("MMM dd, yyyy", dream.date)
            itemBinding.dreamItemDate.text = date.toString()
            when {
                dream.isDeferred -> {
                    itemBinding.dreamItemImage.setImageResource(R.drawable.dream_deferred_icon)
                    itemBinding.dreamItemImage.tag = R.drawable.dream_deferred_icon
                }
                dream.isFulfilled -> {
                    itemBinding.dreamItemImage.setImageResource(R.drawable.dream_fulfilled_icon)
                    itemBinding.dreamItemImage.tag = R.drawable.dream_fulfilled_icon
                }
                else -> {
                    itemBinding.dreamItemImage.setImageResource(0)
                    itemBinding.dreamItemImage.tag = 0
                }
            }


        }

        override fun onClick(v: View) {
            callbacks?.onDreamSelected(dream.id) //view model
        } //callbacks"?" -> safe call operator, if not return anything, return null?
    }


    private inner class Adapter(var dreams: List<Dream>) : RecyclerView.Adapter<DreamHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
            val itemBinding = ListItemDreamBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return DreamHolder(itemBinding)
        }
        //

        override fun getItemCount() = dreams.size
        override fun onBindViewHolder(holder: DreamHolder, position: Int) {
            val dream = dreams[position]

            //onBindViewHolder every time when scroll, so it's more frequently

            holder.bind(dream)
        }
    }


    companion object {
        fun newInstance(): DreamListFragment {
            return DreamListFragment()
        }
    }
}