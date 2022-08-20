package com.armageddon.android.flickrdroid.ui.fragments.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.AppPreferences
import com.armageddon.android.flickrdroid.common.QueryTypes

const val STYLE_KEY = "style_key"
const val PARENT_COORDINATES_KEY = "parent_location_key"
const val FILTER_REQUEST_KEY = "filter_request_key"


class PhotoFilterDialog : DialogFragment() {
    var style = 0
    var parentCoordinates = IntArray(0)

    companion object {
        fun newInstance(style: Int, parentCoordinates: IntArray): PhotoFilterDialog {
            val args = Bundle()
            val fragment = PhotoFilterDialog()
            args.putInt(STYLE_KEY, style)
            args.putIntArray(PARENT_COORDINATES_KEY, parentCoordinates)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        style = arguments?.getInt(STYLE_KEY) ?: 0
        parentCoordinates = arguments?.getIntArray(PARENT_COORDINATES_KEY) ?: IntArray(0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window = dialog?.window
        val density = requireActivity().resources.displayMetrics.density


        val wmlp = dialog!!.window!!.attributes
        val height = (355 * density + 0.5f).toInt()
        wmlp.gravity = Gravity.TOP or Gravity.END
        wmlp.y = parentCoordinates[1]
        window?.attributes = wmlp
        window?.setLayout(window.attributes.width, height)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = (layoutInflater.inflate(R.layout.list, null) as ListView).apply {
            adapter = ArrayAdapter(
                requireContext(),
                R.layout.checkbox_list_item,
                QueryTypes.Sort.values().map {getString(it.title)})
            choiceMode = ListView.CHOICE_MODE_SINGLE
            setItemChecked(AppPreferences.getPhotoFilter(requireContext()), true)
            setOnItemClickListener { parent, view, position, id ->
                AppPreferences.setPhotoFilter(requireContext(), position)
                setFragmentResult(FILTER_REQUEST_KEY, Bundle())
                dialog?.dismiss()
            }
        }

        return AlertDialog.Builder(activity,style)
            .setView(dialog)
            .show()
    }
}