package com.armageddon.android.flickrdroid.ui.fragments

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.databinding.FragmentIntroBinding

private const val INTRO_PAGE = "intro_page"

class LogInFragment : Fragment() {
    private var _binding: FragmentIntroBinding? = null
    private val binding get() = _binding!!
    private var introPageNumber = 0
    private lateinit var textShader : LinearGradient

    companion object {
        const val SLIDE_COUNT = 3

        fun newInstance(introPage: Int) = LogInFragment().apply {
            val args = Bundle().apply {
                putInt(INTRO_PAGE, introPage)
            }
            arguments = args
        }
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        introPageNumber = arguments?.getInt(INTRO_PAGE) ?: 0

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textShader = LinearGradient(
            0f, 0f, 0f,
            binding.logoText.paint.textSize,
            intArrayOf(
                resources.getColor(R.color.colorPinkFlickr, null),
                resources.getColor(R.color.colorBlueFlickr, null)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )

        val lp = binding.descriptionText.layoutParams as ConstraintLayout.LayoutParams

        binding.descriptionText.layoutParams.apply {
            height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        }

        when (introPageNumber) {
            0 -> {
                binding.apply {
                    headerText.text = getText(R.string.log_in_header_1)
                    descriptionText.text = getText(R.string.log_in_description_1)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.menu_icon_info))
                    logoImage.scaleType = ImageView.ScaleType.FIT_CENTER
                }
            }
            1 -> {
                binding.apply {
                    headerText.text = getText(R.string.log_in_header_2)
                    descriptionText.text = getText(R.string.log_in_description_2)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.login1)
                    )
                }
            }
            2 -> {
                binding.apply {
                    headerText.text = getText(R.string.log_in_header_3)
                    descriptionText.text = getText(R.string.log_in_description_3)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.login2))
                }
            }
        }
    }
}