package com.armageddon.android.flickrdroid.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.HistoryListItemBinding
import com.armageddon.android.flickrdroid.databinding.RecycleViewBinding
import com.armageddon.android.flickrdroid.model.HistoryElement
import com.armageddon.android.flickrdroid.ui.viewmodels.HistoryViewModel

private const val HISTORY_TYPE = "history_type"
const val HISTORY_KEY = "history_key"
const val HISTORY_BUNDLE = "history_bundle"

class HistoryListFragment : Fragment() {
    private var mCallBacks: CallBacks? = null
    private var _binding: RecycleViewBinding? = null
    private val binding get() = _binding!!
    private val mAdapter = HistoryListAdapter()
    private val viewModel: HistoryViewModel by activityViewModels()
    lateinit var historyType: HistoryType

    companion object {
        fun newInstance(historyTypeIndex: Int): HistoryListFragment {
            val args = Bundle()
            val fragment = HistoryListFragment()
            args.putInt(HISTORY_TYPE, historyTypeIndex)
            fragment.arguments = args
            return fragment
        }
    }

    interface CallBacks {
        fun onHistoryItemClick(historyElement: HistoryElement)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBacks = context as CallBacks
    }

    override fun onDetach() {
        super.onDetach()
        mCallBacks = null
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val historyTypeIndex = arguments?.getInt(HISTORY_TYPE) ?: 0
        historyType = HistoryType.values()[historyTypeIndex]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = RecycleViewBinding.inflate(inflater, container, false)
        binding.spinKit.visibility = View.GONE
        binding.swipeRefreshLayout.isEnabled = false
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
//            adapter = mAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val historyLiveData = when (historyType) {
            HistoryType.PHOTO -> viewModel.photoHistoryLiveData
            HistoryType.GROUP -> viewModel.groupHistoryLiveData
            HistoryType.PERSON -> viewModel.personHistoryLiveData
        }

        historyLiveData.observe(viewLifecycleOwner) {
            when {
                !AppPreferences.getHistorySearch(requireContext()) ->
                    binding.recyclerView.adapter = HintAdapter(R.string.history_off)
                it.isNullOrEmpty() ->
                    binding.recyclerView.adapter = HintAdapter(R.string.history_hint)
                else -> {
                    binding.recyclerView.adapter = mAdapter
                    mAdapter.submitList(it)
                }
            }
        }
    }


    private inner class HistoryHolder (view: View) : RecyclerView.ViewHolder (view) {
        val bindingHolder = HistoryListItemBinding.bind(view)

        fun bind (historyElement: HistoryElement?) {
            if (historyElement == null) {
                bindingHolder.apply {
                    historyLayout.visibility = View.GONE
                    clearButton.apply {
                        visibility = View.VISIBLE
                        setOnClickListener { viewModel.clearHistoryType(historyType) }
                    }
                }
            } else {
                bindingHolder.apply {
                    textView.apply {
                        text = historyElement.text
                        setOnClickListener {
                            val queryTypes = QueryTypes.HISTORY.apply {
                                query1 = historyElement.text.lowercase()
                                query2 = historyType.ordinal.toString()
                            }
                            mCallBacks?.onHistoryItemClick(historyElement)
                        }
                    }
                    iconClose.setOnClickListener {
                        viewModel.removeHistoryElement(historyElement)
                    }
                }
            }


        }
    }

    private inner class HintHolder(view: View) : RecyclerView.ViewHolder (view) {
        val textView: TextView = itemView.findViewById(R.id.text_view)
        val bind : (CharSequence) -> Unit = textView::setText
    }

    private inner class HintAdapter(val message: Int) : RecyclerView.Adapter<HintHolder> () {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HintHolder {
            val view = layoutInflater.inflate(R.layout.history_hint_list_item, parent, false)
            return HintHolder(view)
        }

        override fun onBindViewHolder(holder: HintHolder, position: Int) {
            holder.bind(getString(message))
        }

        override fun getItemCount() = 1

    }


    private inner class HistoryListAdapter
        : ListAdapter<HistoryElement, HistoryHolder>(object : DiffUtil.ItemCallback<HistoryElement>() {
        override fun areItemsTheSame(oldItem: HistoryElement, newItem: HistoryElement): Boolean {
            return oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: HistoryElement, newItem: HistoryElement): Boolean {
            return oldItem == newItem
        }}
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
            val view = layoutInflater.inflate(R.layout.history_list_item, parent, false)
            return HistoryHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
            if (position < itemCount - 1) {
                getItem(position)?.let { holder.bind(it) }
            } else {
                holder.bind(null)
            }
        }

        override fun getItemCount(): Int {
            return super.getItemCount() + 1
        }

    }
}