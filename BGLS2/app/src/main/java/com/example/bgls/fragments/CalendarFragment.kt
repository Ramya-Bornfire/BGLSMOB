package com.example.bgls.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.CalendarAdapter
import com.example.bgls.Adapter.HolidayAdapter
import com.example.bgls.DataModels.CalendarModel
import com.example.bgls.DataModels.HolidayModel
import com.example.bgls.R

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerCalendar)
        val tabCalendar = view.findViewById<TextView>(R.id.tabCalendar)
        val tabHoliday = view.findViewById<TextView>(R.id.tabHoliday)
        val holidayLayout = view.findViewById<LinearLayout>(R.id.layoutHoliday)
        val header = view.findViewById<LinearLayout>(R.id.layoutTableHeader)
        val holidayHeader = view.findViewById<LinearLayout>(R.id.layoutHolidayHeader)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val months = listOf(
            "JAN","FEB","MAR","APR","MAY","JUN",
            "JUL","AUG","SEP","OCT","NOV","DEC"
        )

        val list = months.map {
            CalendarModel("2026", it)
        }
        val holidaylist = listOf(
            HolidayModel("2026", "JAN", "01-01-2026", "NEW YEAR", "Celebration Govt Leave"),
            HolidayModel("2026", "JAN", "14-01-2026", "PONGAL", "Public Holiday"),
            HolidayModel("2026", "JAN", "15-01-2026", "THIRUVALLUVAR DAY", "Public Holiday"),

            HolidayModel("2026", "FEB", "10-02-2026", "Festival", "Holiday"),
            HolidayModel("2026", "MAR", "01-03-2026", "RAMZAN", "Public Holiday"),
            HolidayModel("2026", "APR", "03-04-2026", "GOOD FRIDAY", "Public Holiday"),
            HolidayModel("2026", "APR", "14-04-2026", "TAMIL NEW YEAR", "Public Holiday"),
            HolidayModel("2026", "MAY", "01-05-2026", "INTERNATIONAL WORKERS DAY", "Public Holiday"),
            HolidayModel("2026", "AUG", "06-08-2026", "MAARIYAMAN FESTIVAL", "Public Holiday"),
            HolidayModel("2026", "AUG", "15-08-2026", "INDEPENDENCE DAY", "Public Holiday"),
            HolidayModel("2026", "SEP", "07-09-2026", "VINAYAGAR CHATURTHI", "Public Holiday"),
            HolidayModel("2026", "OCT", "02-10-2026", "GANDHI JAYANTHI", "Public Holiday"),
            HolidayModel("2026", "OCT", "20-10-2026", "SARASWATHI POOJA", "Public Holiday"),
            HolidayModel("2026", "NOV", "04-11-2026", "DIWALI", "Public Holiday"),
            HolidayModel("2026", "DEC", "25-12-2026", "CHRISTMAS", "Public Holiday")
        )

        // 🔥 Calendar TAB CLICK
        tabCalendar.setOnClickListener {

            // UI selection
            tabCalendar.setBackgroundResource(R.drawable.tab_selected)
            tabHoliday.setBackgroundResource(R.drawable.tab_unselected)

            // Show recycler
            recycler.visibility = View.VISIBLE
            holidayLayout.visibility = View.GONE
            header.visibility = View.VISIBLE
            holidayHeader.visibility = View.GONE

            recycler.adapter = CalendarAdapter(list) { selectedMonth ->

                val filteredList = holidaylist.filter {
                    it.month == selectedMonth
                }
                recycler.visibility = View.VISIBLE
                header.visibility = View.GONE
                holidayHeader.visibility = View.VISIBLE

                recycler.adapter = HolidayAdapter(filteredList)
            }
        }

        // 🔥 Holiday TAB CLICK
        tabHoliday.setOnClickListener {

            tabHoliday.setBackgroundResource(R.drawable.tab_selected)
            tabCalendar.setBackgroundResource(R.drawable.tab_unselected)

            recycler.visibility = View.GONE   // 🔥 list hide
            holidayLayout.visibility = View.VISIBLE   // 🔥 form show

            header.visibility = View.GONE
            holidayHeader.visibility = View.GONE
            // 🔥 Show ALL holidays or default month

        }

        // ✅ Default
        tabCalendar.performClick()
    }

}