package com.armageddon.android.flickrdroid.ui.fragments.list

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.common.AppPreferences.getOauthToken
import com.armageddon.android.flickrdroid.common.AppPreferences.getOauthTokenSecret
import com.armageddon.android.flickrdroid.common.AppPreferences.getUserId
import com.armageddon.android.flickrdroid.databinding.CommentListItemBinding
import com.armageddon.android.flickrdroid.databinding.FragmentCommentsBinding
import com.armageddon.android.flickrdroid.model.*
import com.armageddon.android.flickrdroid.network.execptions.ConnectionException
import com.armageddon.android.flickrdroid.network.execptions.NoCommentsException
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_OK
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_NO_COMMENTS
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.viewmodels.BlockListViewModel
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.launch


private const val PHOTO_ID_BUNDLE = "photoId_bundle"
private const val REPORT_URL = "https://www.flickrhelp.com/hc/en-us/requests/new"

class PhotoCommentListFragment : MenuFragment() {
    private var mCallBacks: CallBacks? = null
    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!
    private var mAdapter = CommentsAdapter()
    private val viewModel: GalleryItemViewModel by activityViewModels()
    private val blockListViewModel: BlockListViewModel by activityViewModels()
    lateinit var mPhotoId: String
    lateinit var mCommentsList: MutableList<PhotoComment>
    lateinit var mBlockedList: List<Person>
    private val mItemSwipeTrashColor = Paint()

    companion object {
        fun newInstance(photoId: String): PhotoCommentListFragment  {
            val args = Bundle()
            args.putString(PHOTO_ID_BUNDLE, photoId)
            val fragment = PhotoCommentListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPhotoId = arguments?.getString(PHOTO_ID_BUNDLE) ?: ""
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comments_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.app_bar_menu_lock -> mCallBacks?.reportAbuse()
            R.id.app_bar_menu_button -> mDrawerLayout.openDrawer(GravityCompat.END, true)
        }
        return true
    }

    interface CallBacks {
        fun onCommentClick(person: Person)
        fun reportAbuse()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        binding.apply {
            toolbar.title = getString(R.string.comments)
            swipeRefreshLayout.setColors()
            swipeRefreshLayout.setOnRefreshListener {
                swipeRefreshLayout.isRefreshing = false
                updateUI(true)
            }
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

        }
        return binding.root
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    private fun updateUI(isRefreshing: Boolean) {
        mAdapter = CommentsAdapter()
        viewLifecycleOwner.lifecycleScope.launch {
            binding.apply {
                if (isRefreshing) {
                    recyclerView.visibility = View.INVISIBLE
                    spinKit.visibility = View.VISIBLE
                }
                val query = Query(
                    id = mPhotoId,
                    oauthToken = getOauthToken(requireContext()) ?: "",
                    oauthTokenSecret = getOauthTokenSecret(requireContext()) ?: ""
                )
                val response = viewModel.getPhotoComments(query)
                if (!getUserId(requireActivity()).isNullOrBlank()) {
                    messageFrame.visibility = View.VISIBLE
                    messageSendButton.setOnClickListener {
                        lifecycleScope.launch {
                            viewModel.isCommentsQuantityChanged = true
                           swipeRefreshLayout.isRefreshing = true
                            messageField.closeSoftKeyboard()
                            val commentQuery = Query(
                                id = mPhotoId,
                                text = messageField.text.toString(),
                                oauthToken = getOauthToken(requireContext())!!,
                                oauthTokenSecret = getOauthTokenSecret(requireContext())!!
                            )
                            val commentResponse = viewModel.addPhotoComment(commentQuery)
                            if (commentResponse.stat != RESPONSE_DATA_OK) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.internet_connection_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                                swipeRefreshLayout.isRefreshing = false
                                messageField.setText(commentQuery.text)
                            } else {

                                messageField.text.clear()
                                updateUI(false)
                            }
                        }
                    }
                }
                when (response.stat) {
                    RESPONSE_DATA_OK -> {
                        mCommentsList = response.dataArray.reversed().toMutableList()
                        val blockedPersonsId = mBlockedList.map { it.id }
                        mCommentsList.removeAll { it.author in blockedPersonsId }
                        mAdapter.submitList(mCommentsList)
                        recyclerView.adapter = mAdapter
                        val itemTouchHelper = ItemTouchHelper(touchHelper)
                        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
                        recyclerView.visibility = View.VISIBLE
                        spinKit.visibility = View.INVISIBLE
                    }
                    RESPONSE_NO_COMMENTS -> {
                        recyclerView.visibility = View.VISIBLE
                        spinKit.visibility = View.INVISIBLE
                        recyclerView.adapter = ErrorAdapter(NoCommentsException()) {}
                    }
                    else -> {
                        recyclerView.visibility = View.VISIBLE
                        spinKit.visibility = View.INVISIBLE
                        recyclerView.adapter = ErrorAdapter(ConnectionException()) { updateUI(true) }
                    }
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        blockListViewModel.blockListLiveData.observe(viewLifecycleOwner) {
            mBlockedList = it
        }
        updateUI(true)
    }

    private inner class CommentHolder (view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        val bindingHolder = CommentListItemBinding.bind(view)
        lateinit var mComment: PhotoComment

        init {
            itemView.setOnClickListener (this)
        }

        fun bind(comment: PhotoComment) {
            mComment = comment

            Glide.with(itemView)
                .load(comment.getAuthorLogo())
                .error(R.drawable.no_logo_account)
                .into(bindingHolder.authorIcon)
            bindingHolder.apply {
                authorName.text = comment.getUserName()
                commentText.text = comment.getComment()
                commentTimeValue.text = comment.getCommentTime(requireContext(), TIME_VALUE)
                commentTimeLabel.text = comment.getCommentTime(requireContext(), TIME_NAME)
                blockButton.setOnClickListener {
                    if (mComment.author == getUserId(requireActivity())) {
                        Snackbar.make(binding.root, R.string.self_block_warning, Snackbar.LENGTH_SHORT)
                            .setTextColor(resources.getColor(R.color.colorBlack, null))
                            .setBackgroundTint(resources.getColor(R.color.colorLightGrey, null))
                            .show()
                        return@setOnClickListener
                    }
//                    Toast.makeText(itemView.context, "Block", Toast.LENGTH_SHORT).show()
                    val builder = AlertDialog.Builder(requireContext(), R.style.DialogAllCapsFalseStyle).apply {
                        setMessage(getString(R.string.lock_person))
                        setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
                        setPositiveButton(R.string.ok) { _, _ ->
                            val person = Person(
                                id = mComment.author,
                                realName = mComment.authorRealName,
                                userName = mComment.authorName,
                                iconFarm = mComment.iconfarm,
                                iconServer = mComment.iconserver
                            )
                            blockListViewModel.blockPerson(person)
                            mCommentsList.removeAt(bindingAdapterPosition)
                            mAdapter.notifyItemRemoved(bindingAdapterPosition)
                            if (mAdapter.itemCount == 0) {
                                binding.recyclerView.adapter = ErrorAdapter(NoCommentsException()) {}
                            }
                        }
                    }
                    val alert = builder.create()
                    alert.show()
                }
                reportButton.setOnClickListener {
//                    Toast.makeText(itemView.context, "Report", Toast.LENGTH_SHORT).show()
                    Intent().apply {
                        action = Intent.ACTION_VIEW
                        addCategory(Intent.CATEGORY_BROWSABLE)
                        data = Uri.parse(REPORT_URL)
                        startActivity(this)
                    }
                }
            }
        }

        override fun onClick(v: View?) {
            val person = Person(
                id = mComment.author,
                userName = mComment.authorName,
                realName = mComment.authorRealName,
                iconServer = mComment.iconserver,
                iconFarm = mComment.iconfarm
            )
            mCallBacks?.onCommentClick(person)
        }
    }

    private inner class CommentsAdapter
        : ListAdapter<PhotoComment, CommentHolder>(object
        : DiffUtil.ItemCallback<PhotoComment>() {
        override fun areItemsTheSame(oldItem: PhotoComment, newItem: PhotoComment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PhotoComment, newItem: PhotoComment): Boolean {
            return oldItem == newItem
        }
    }) {

        override fun onBindViewHolder(holder: CommentHolder, position: Int) {
            getItem(position)?.let { holder.bind(it) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
            val view = layoutInflater.inflate(R.layout.comment_list_item, parent, false)
            return CommentHolder(view)
        }
    }

    private fun EditText.closeSoftKeyboard() {
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.windowToken, 0)
            this.text.clear()
            this.clearFocus()
        }
    }

    private val touchHelper: ItemTouchHelper.Callback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val commentAuthorId = mCommentsList[viewHolder.bindingAdapterPosition].author
                return if (commentAuthorId != getUserId(requireContext())) 0
                else super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                try {
                 viewModel.isCommentsQuantityChanged = true
                   val comment = mCommentsList[viewHolder.bindingAdapterPosition]
                    if (comment.author == getUserId(requireContext())) {
                        val commentQuery = Query(
                            id = comment.id,
                            oauthToken = getOauthToken(requireContext())!!,
                            oauthTokenSecret = getOauthTokenSecret(requireContext())!!
                        )
                        mCommentsList.removeAt(viewHolder.bindingAdapterPosition)
                        mAdapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)
                        lifecycleScope.launch {
                            val response = viewModel.delPhotoComment(commentQuery)
                            if (response.stat != RESPONSE_DATA_OK) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.internet_connection_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                                updateUI(false)
                            } else {

                            }
                        }
                    }
//                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(ContextCompat.getColor(
                            requireContext(),
                            R.color.colorDelRed
                        )
                    )
                    .addActionIcon(R.drawable.ic_baseline_delete_forever_24)
                    .create()
                    .decorate()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

}