package com.armageddon.android.flickrdroid.ui.fragments.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.ui.fragments.controllers.CALENDAR_REQUEST_KEY
import java.util.*


const val CALENDAR_BUNDLE_KEY = "calendar_bundle_key"
const val CALENDAR_HOLLOW_KEY = "calendar_hollow_key"
const val RESTORE_CALENDAR_KEY = "restore_calendar_key"
private const val PARENT_DATE_COORDINATES_KEY = "parent_date_location_key"


class DatePickerFragment : DialogFragment() {
    private val resetCalendar = GregorianCalendar().apply { add(Calendar.DATE, -1) }
    private val calendarMinDate = GregorianCalendar(2004, 7, 1).timeInMillis
    lateinit var calendar: GregorianCalendar
    lateinit var datePicker: DatePicker
    lateinit var userCalender: GregorianCalendar
    var parentCoordinates = IntArray(0)

    companion object {
        fun newInstance(calendar: GregorianCalendar, parentCoordinates: IntArray): DatePickerFragment {
            val args = Bundle()
            args.putSerializable(CALENDAR_BUNDLE_KEY, calendar)
            args.putIntArray(PARENT_DATE_COORDINATES_KEY, parentCoordinates)
            val fragment = DatePickerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(RESTORE_CALENDAR_KEY, getUserCalendar())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentCoordinates = arguments?.getIntArray(PARENT_DATE_COORDINATES_KEY) ?: IntArray(0)

        if (savedInstanceState != null) {
            userCalender = savedInstanceState
                .getSerializable(RESTORE_CALENDAR_KEY) as GregorianCalendar
        }

        calendar = arguments?.let {
            it.getSerializable(CALENDAR_BUNDLE_KEY) as GregorianCalendar
        } ?: GregorianCalendar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window = dialog?.window
        val density = requireActivity().resources.displayMetrics.density

        /* Dialog Margrin Top */
        val wmlp = dialog!!.window!!.attributes
        wmlp.gravity = Gravity.TOP or Gravity.START
        wmlp.y = parentCoordinates[1]
        window?.attributes = wmlp

        /* Dialog width and height */
        val width = (300 * density + 0.5f).toInt()
        val height = (250 * density + 0.5f).toInt()

        window?.setLayout(width, height)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.date_dialog, null)
        datePicker = (view as DatePicker).apply {
            minDate = calendarMinDate
            val initCalendar = when (this@DatePickerFragment::userCalender.isInitialized) {
                true -> userCalender
                false -> calendar
            }
            init(
                initCalendar.get(Calendar.YEAR),
                initCalendar.get(Calendar.MONTH),
                initCalendar.get(Calendar.DATE),
            null
            )
        }

        return AlertDialog.Builder(activity,R.style.DialogAnimationStyle)
            .setView(datePicker)
            .setTitle(getString(R.string.date_dialog_title))
            .setNegativeButton(getString(R.string.reset)) { _, _ ->
                setFragmentResult(
                    getCalenderKey(resetCalendar),
                    bundleOf(CALENDAR_BUNDLE_KEY to resetCalendar))
            }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                setFragmentResult(
                    getCalenderKey(calendar),
                    bundleOf(CALENDAR_BUNDLE_KEY to getUserCalendar()))
            }.show()
    }


    private fun dateCompare(calendar: GregorianCalendar): Boolean {
        val date1 = calendar[Calendar.DATE]
        val date2 = datePicker.dayOfMonth
        val month1 = calendar[Calendar.MONTH]
        val month2 = datePicker.month
        val year1 = calendar[Calendar.YEAR]
        val year2 = datePicker.year
        return date1 == date2 && month1 == month2 && year1 == year2
    }

    private fun getUserCalendar () : GregorianCalendar {
        return GregorianCalendar(
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth
        )
    }

    private fun getCalenderKey (calendar: GregorianCalendar): String {
       return when (dateCompare(calendar)) {
           false -> CALENDAR_REQUEST_KEY
           true -> CALENDAR_HOLLOW_KEY
        }
    }

}