package edu.vt.cs.cs5254.dreamcatcher

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.util.*


private const val ARG_ORIGINAL_DATE = "original_date"
private const val ARG_REQUEST_KEY = "request_key"


class DatePickerFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dateListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                val resultDate: Date = GregorianCalendar(year, month, day).time

                parentFragmentManager.setFragmentResult(
                    arguments?.getString(ARG_REQUEST_KEY).toString(),
                    bundleOf(ARG_NEW_DATE to resultDate)
                )
            }
        val date = arguments?.getSerializable(ARG_ORIGINAL_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    companion object {
        fun newInstance(date: Date, requestKey: String): DatePickerFragment
        {
            val args = Bundle().apply {
                putSerializable(ARG_ORIGINAL_DATE, date)
                putString(ARG_REQUEST_KEY, requestKey)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}

