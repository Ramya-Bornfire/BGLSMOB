package com.example.bgls.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.CalendarAdapter
import com.example.bgls.Adapter.HolidayAdapter
import com.example.bgls.DataModels.CalendarModel
import com.example.bgls.DataModels.HolidayModel
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private lateinit var recycler: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnFilter = view.findViewById<Button>(R.id.btnFilter)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val layoutHoliday = view.findViewById<LinearLayout>(R.id.layoutHoliday)

        recycler = view.findViewById(R.id.recyclerCalendar)
        val tabCalendar = view.findViewById<TextView>(R.id.tabCalendar)
        val tabHoliday = view.findViewById<TextView>(R.id.tabHoliday)
        val header = view.findViewById<LinearLayout>(R.id.layoutTableHeader)
        val holidayHeader = view.findViewById<LinearLayout>(R.id.layoutHolidayHeader)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // 🔥 CALENDAR TAB
        tabCalendar.setOnClickListener {

            tabCalendar.setBackgroundResource(R.drawable.tab_selected)
            tabHoliday.setBackgroundResource(R.drawable.tab_unselected)

            btnFilter.visibility = View.VISIBLE
            btnAdd.visibility = View.GONE

            recycler.visibility = View.VISIBLE
            layoutHoliday.visibility = View.GONE
            header.visibility = View.VISIBLE
            holidayHeader.visibility = View.GONE

            loadCalendar()
        }

        // 🔥 HOLIDAY TAB
        tabHoliday.setOnClickListener {

            tabHoliday.setBackgroundResource(R.drawable.tab_selected)
            tabCalendar.setBackgroundResource(R.drawable.tab_unselected)

            btnFilter.visibility = View.GONE
            btnAdd.visibility = View.VISIBLE

            recycler.visibility = View.VISIBLE
            layoutHoliday.visibility = View.GONE
            header.visibility = View.GONE
            holidayHeader.visibility = View.VISIBLE

//            loadHolidays("")
            val currentMonth = SimpleDateFormat("MMM", Locale.ENGLISH)
                .format(Date())
                .uppercase()

            loadHolidays(null)

        }

        // 🔥 ADD BUTTON
        btnAdd.setOnClickListener {
            layoutHoliday.visibility = View.VISIBLE
            recycler.visibility = View.GONE
            header.visibility = View.GONE
            holidayHeader.visibility = View.GONE
            btnAdd.visibility = View.GONE
        }

        // 🔥 SUBMIT
        btnSubmit.setOnClickListener {
            Toast.makeText(requireContext(), "Holiday Added Successfully", Toast.LENGTH_SHORT).show()

            layoutHoliday.visibility = View.GONE
            recycler.visibility = View.VISIBLE
            holidayHeader.visibility = View.VISIBLE
        }

        tabCalendar.performClick()
    }

    // ✅ LOAD CALENDAR
    private fun loadCalendar() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getCalendar("calender", "2026", null)

                if (response.isSuccessful) {
                    val calList = response.body()?.calender_list ?: emptyList()
                    val mappedList = calList.map {
                        CalendarModel(it.year?.toString() ?: "", it.month ?: "")
                    }

                    // Get UI references (make them accessible inside the lambda)
                    val btnFilter = requireView().findViewById<Button>(R.id.btnFilter)
                    val btnAdd = requireView().findViewById<Button>(R.id.btnAdd)
                    val header = requireView().findViewById<LinearLayout>(R.id.layoutTableHeader)
                    val holidayHeader = requireView().findViewById<LinearLayout>(R.id.layoutHolidayHeader)

                    recycler.adapter = CalendarAdapter(mappedList) { selectedMonth ->
                        // 1. Load holidays
                        loadHolidays(selectedMonth)

                        // 2. Switch to holiday header
                        header.visibility = View.GONE
                        holidayHeader.visibility = View.VISIBLE

                        // 3. Adjust buttons (like the Holiday tab)
                        btnFilter.visibility = View.GONE
                        btnAdd.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "API Error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ LOAD HOLIDAYS
    private fun loadHolidays(month: String?) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getCalendar(
                    "calender",
                    "2026",
                    month   // 🔥 null means ALL months
                )

                if (response.isSuccessful) {

                    val holidayList = response.body()?.holidays_list ?: emptyList()

                    val mapped = holidayList.map {
                        HolidayModel(
                            it.year ?: "",
                            it.month ?: "",
                            it.date ?: "",
                            it.description ?: "",
                            it.remarks ?: ""
                        )
                    }

                    recycler.adapter = HolidayAdapter(mapped)

                } else {
                    Toast.makeText(requireContext(), "Holiday API Error", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

}
