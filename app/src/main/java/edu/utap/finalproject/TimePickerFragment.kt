package edu.utap.finalproject

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.text.DateFormat
import java.util.Calendar

class TimePickerFragment(
    private val onSetCallback: (hourOfDay: Int, minute: Int) -> Unit
) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hour = 12
        val minute = 0
        return TimePickerDialog(activity, this, hour, minute, false)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        Log.d(javaClass.simpleName, "User picked $hourOfDay:$minute")
        onSetCallback(hourOfDay, minute)
    }
}