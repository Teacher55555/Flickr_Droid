package com.armageddon.android.flickrdroid.ui.fragments

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.databinding.FragmentIntroBinding

private const val INTRO_PAGE = "intro_page"

class IntroFragment : Fragment() {
    private var _binding: FragmentIntroBinding? = null
    private val binding get() = _binding!!
    private var introPageNumber = 0
    private lateinit var textShader : LinearGradient

    companion object {
        const val SLIDE_COUNT = 12

        fun newInstance(introPage: Int) = IntroFragment().apply {
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

        when (introPageNumber) {
            0 -> {
                binding.apply {
                    logoText.apply {
                        paint.shader = textShader
                        text = getText(R.string.app_name)
                    }
                    headerText.text = getString(R.string.intro_header1)
                    descriptionText.text = getString(R.string.intro_description1)
                }
            }

            1 -> {
                binding.apply {
                    headerText.text = getText(R.string.intro_header2)
                    descriptionText.text = getText(R.string.intro_description2)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_2))
                }
            }
            2 -> {
                binding.apply {
                    headerText.text = getText(R.string.intro_header3)
                    descriptionText.text = getText(R.string.intro_description3)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_3)
                    )
                }
            }
            3 -> {
                binding.apply {
                    descriptionText.apply {
                        text = getText(R.string.intro_description4)
//                        textAlignment = View.TEXT_ALIGNMENT_INHERIT
                    }
                    headerText.text = getText(R.string.intro_header4)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_4))
                }
            }
            4 -> {
                binding.apply {
                    descriptionText.text = getText(R.string.intro_description12)
                    headerText.text = getText(R.string.intro_header12)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_11))
                }
            }
            5 -> {
                binding.apply {
                    descriptionText.text = getText(R.string.intro_description5)
                    headerText.text = getText(R.string.intro_header5)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_5))
                }
            }
            6 -> {
                binding.apply {
                    descriptionText.text = getText(R.string.intro_description6)
                    headerText.text = getText(R.string.intro_header6)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_6))
                }
            }
            7 -> {
                binding.apply {
                    descriptionText.text = getText(R.string.intro_description7)
                    headerText.text = getText(R.string.intro_header7)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_7))
                }
            }
            8 -> {
                binding.apply {
                    descriptionText.text = getText(R.string.intro_description8)
                    headerText.text = getText(R.string.intro_header8)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_8))
                }
            }
            9 -> {
                binding.apply {
                    descriptionText.text = getText(R.string.intro_description9)
                    headerText.text = getText(R.string.intro_header9)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_9))
                }
            }
            10 -> {
                binding.apply {
                    descriptionText.text = getText(R.string.intro_description10)
                    headerText.text = getText(R.string.intro_header10)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_10))
                }
            }
            11 -> {
                binding.apply {
                    descriptionText.text = getText(R.string.intro_description11)
                    headerText.text = getText(R.string.intro_header11)
                    logoImage.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.intro_photo_12))
                }
            }
        }
    }
}