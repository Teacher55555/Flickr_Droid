package com.armageddon.android.flickrdroid.ui.fragments.controllers

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.databinding.FragmentPrivatePersonControllerBinding
import com.armageddon.android.flickrdroid.model.Person

private const val PERSON = "person"
private const val INDEX = "index"

class PrivatePersonFragment : Fragment() {
    private var _binding : FragmentPrivatePersonControllerBinding? = null
    private val binding get() = _binding!!
    private lateinit var mPerson : Person
    private var mHasLogo : Boolean = true
    lateinit var mLogo : Drawable
    private var mCallBack: CallBacks? = null
    private var mIndex = R.id.bar_home

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBack = context as CallBacks
    }


    interface CallBacks {
        fun onHomeClick()
        fun onSearchClick()
        fun onFavoritesClick()
        fun onContactsClick()
        fun onPersonClick()
    }

    companion object {
        fun newInstance(person: Person, index: Int = 0): PrivatePersonFragment{
            val args = Bundle()
            val fragment = PrivatePersonFragment()
            args.putSerializable(PERSON, person)
            args.putInt(INDEX, index)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.person_private_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPerson = arguments?.getSerializable(PERSON) as Person
        mIndex = arguments?.getInt(INDEX) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrivatePersonControllerBinding.inflate(layoutInflater, container, false)
//        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = mPerson.realName
//            setIcon(R.drawable.logo)

        }

        when (mIndex) {
            R.id.bar_home -> {
                mCallBack?.onHomeClick()
            }
            R.id.bar_faves -> {
                mCallBack?.onFavoritesClick()
            }
            R.id.bar_search -> mCallBack?.onSearchClick()
            R.id.bar_contacts -> mCallBack?.onContactsClick()
            R.id.bar_person -> mCallBack?.onPersonClick()
            else -> {
                mCallBack?.onHomeClick()
            }
        }

        binding.apply {
            bottomNav.setOnItemSelectedListener {
               mIndex = when (it.itemId) {
                   R.id.bar_home -> {
                       mCallBack?.onHomeClick()
                       R.id.bar_home
                   }
                   R.id.bar_faves -> {
                       mCallBack?.onFavoritesClick()
                       R.id.bar_faves
                   }
                   R.id.bar_search -> {
                       mCallBack?.onSearchClick()
                       R.id.bar_search
                   }
                   R.id.bar_contacts -> {
                       mCallBack?.onContactsClick()
                       R.id.bar_contacts
                   }
                   R.id.bar_person -> {
                       mCallBack?.onPersonClick()
                       R.id.bar_person
                   }
                   else -> R.id.bar_home
                }
                true
            }
        }
    }


    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    private fun ImageView.setDrawable(drawableId: Int) {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
        this.setImageDrawable(drawable)
    }
}