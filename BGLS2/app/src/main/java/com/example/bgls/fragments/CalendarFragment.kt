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
import com.example.bgls.DataModels.AddHolidayMasterRequest
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
    private var isFilterVisible = false
    private var allCalendarData = mutableListOf<CalendarModel>()
    private lateinit var calendarAdapter: CalendarAdapter
    private var selectedHolidayMonth: String? = null
    private val calendarYear: String
        get() = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnFilter = view.findViewById<Button>(R.id.btnFilter)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val layoutHoliday = view.findViewById<LinearLayout>(R.id.layoutHoliday)

        recycler = view.findViewById(R.id.recyclerCalendar)
        val tabCalendar = view.findViewById<TextView>(R.id.tabCalendar)
        val tabHoliday = view.findViewById<TextView>(R.id.tabHoliday)
        val headerContainer = view.findViewById<FrameLayout>(R.id.frameCalendarHeader)
        val layoutDefaultHeader = view.findViewById<LinearLayout>(R.id.layoutDefaultHeader)
        val layoutFilterHeader = view.findViewById<LinearLayout>(R.id.layoutFilterHeader)
        val holidayHeader = view.findViewById<LinearLayout>(R.id.layoutHolidayHeader)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        btnFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            layoutDefaultHeader.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
            layoutFilterHeader.visibility = if (isFilterVisible) View.VISIBLE else View.GONE
            
            if (isFilterVisible) {
                applyCalendarFilter()
            } else {
                clearColumnFilters()
                applyCalendarFilter()
            }
        }

        setupColumnFilterLogic(view)

        // 🔥 CALENDAR TAB
        tabCalendar.setOnClickListener {

            tabCalendar.setBackgroundResource(R.drawable.tab_selected)
            tabHoliday.setBackgroundResource(R.drawable.tab_unselected)

            btnFilter.visibility = View.VISIBLE
            btnAdd.visibility = View.GONE

            recycler.visibility = View.VISIBLE
            layoutHoliday.visibility = View.GONE
            headerContainer.visibility = View.VISIBLE
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
            headerContainer.visibility = View.GONE
            
            isFilterVisible = false
            layoutDefaultHeader.visibility = View.VISIBLE
            layoutFilterHeader.visibility = View.GONE
            
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
            headerContainer.visibility = View.GONE
            holidayHeader.visibility = View.GONE
            btnAdd.visibility = View.GONE
        }

        // 🔥 SUBMIT
        btnSubmit.setOnClickListener {
            submitHoliday(layoutHoliday, recycler, holidayHeader, btnAdd)
        }

        tabCalendar.performClick()
    }

    // ✅ LOAD CALENDAR
    private fun loadCalendar() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getCalendar("calender", calendarYear, null)

                if (response.isSuccessful) {
                    val calList = response.body()?.calender_list ?: emptyList()
                    val mappedList = calList.map {
                        CalendarModel(it.year?.toString() ?: "", it.month ?: "")
                    }

                    // Get UI references (make them accessible inside the lambda)
                    val btnFilter = requireView().findViewById<Button>(R.id.btnFilter)
                    val btnAdd = requireView().findViewById<Button>(R.id.btnAdd)
                    val headerContainer = requireView().findViewById<FrameLayout>(R.id.frameCalendarHeader)
                    val layoutDefaultHeader = requireView().findViewById<LinearLayout>(R.id.layoutDefaultHeader)
                    val layoutFilterHeader = requireView().findViewById<LinearLayout>(R.id.layoutFilterHeader)
                    val holidayHeader = requireView().findViewById<LinearLayout>(R.id.layoutHolidayHeader)

                    allCalendarData = mappedList.toMutableList()
                    calendarAdapter = CalendarAdapter(allCalendarData) { selectedMonth ->
                        selectedHolidayMonth = selectedMonth
                        // 1. Load holidays
                        loadHolidays(selectedMonth)

                        // 2. Switch to holiday header
                        headerContainer.visibility = View.GONE
                        isFilterVisible = false
                        layoutDefaultHeader.visibility = View.VISIBLE
                        layoutFilterHeader.visibility = View.GONE
                        holidayHeader.visibility = View.VISIBLE

                        // 3. Adjust buttons (like the Holiday tab)
                        btnFilter.visibility = View.GONE
                        btnAdd.visibility = View.VISIBLE
                    }
                    recycler.adapter = calendarAdapter
                    applyCalendarFilter()
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
                val response = RetrofitClient.api.getHolidayMasterList()

                if (response.isSuccessful) {
                    val holidayList = response.body()?.listOfValues ?: emptyList()
                    val filtered = if (month.isNullOrBlank()) {
                        holidayList
                    } else {
                        holidayList.filter { it.calMonth.equals(month, ignoreCase = true) }
                    }
                    val mapped = filtered.map {
                        HolidayModel(
                            it.calYear ?: "",
                            it.calMonth ?: "",
                            it.recordDate ?: "",
                            it.holidayDesc ?: "",
                            it.holidayRemarks ?: ""
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

    private fun setupColumnFilterLogic(view: View) {
        val etFilterYear = view.findViewById<EditText>(R.id.etFilterYear)
        val etFilterMonth = view.findViewById<EditText>(R.id.etFilterMonth)

        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFilterVisible) applyCalendarFilter()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        etFilterYear.addTextChangedListener(textWatcher)
        etFilterMonth.addTextChangedListener(textWatcher)

        val editorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                applyCalendarFilter()
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                true
            } else false
        }

        etFilterYear.setOnEditorActionListener(editorActionListener)
        etFilterMonth.setOnEditorActionListener(editorActionListener)
    }

    private fun applyCalendarFilter() {
        if (!::calendarAdapter.isInitialized) return

        val view = view ?: return
        val etYear = view.findViewById<EditText>(R.id.etFilterYear).text.toString().trim()
        val etMonth = view.findViewById<EditText>(R.id.etFilterMonth).text.toString().trim()

        val filtered = allCalendarData.filter { item ->
            val matchesYear = etYear.isEmpty() || (item.year?.contains(etYear, ignoreCase = true) == true)
            val matchesMonth = etMonth.isEmpty() || (item.month?.contains(etMonth, ignoreCase = true) == true)
            matchesYear && matchesMonth
        }
        calendarAdapter.updateData(filtered)
    }

    private fun clearColumnFilters() {
        view?.findViewById<EditText>(R.id.etFilterYear)?.text?.clear()
        view?.findViewById<EditText>(R.id.etFilterMonth)?.text?.clear()
    }

    private fun submitHoliday(
        layoutHoliday: LinearLayout,
        recycler: RecyclerView,
        holidayHeader: LinearLayout,
        btnAdd: Button
    ) {
        val root = view ?: return
        val orgn = root.findViewById<EditText>(R.id.etOrganizationName).text.toString().trim()
        val location = root.findViewById<EditText>(R.id.etOrganizationType).text.toString().trim()
        val calYear = root.findViewById<EditText>(R.id.etDateOfRegistration).text.toString().trim()
            .ifEmpty { calendarYear }
        val calMonth = root.findViewById<EditText>(R.id.etCertificateReg).text.toString().trim()
            .ifEmpty { selectedHolidayMonth ?: "" }
        val recordDate = root.findViewById<EditText>(R.id.etVatReference).text.toString().trim()
        val holidayDesc = root.findViewById<EditText>(R.id.etNoOfEmployees).text.toString().trim()
        val holidayRemarks = root.findViewById<EditText>(R.id.etAsOn).text.toString().trim()
        val holidayFlg = root.findViewById<EditText>(R.id.etRegOfficeAddr1).text.toString().trim()

        if (recordDate.isEmpty() || holidayDesc.isEmpty()) {
            Toast.makeText(requireContext(), "Date and Description are required", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = AddHolidayMasterRequest(
                    orgn = orgn,
                    location = location,
                    calYear = calYear,
                    calMonth = calMonth,
                    recordDate = formatRecordDateForHolidayMaster(recordDate),
                    holidayDesc = holidayDesc,
                    holidayRemarks = holidayRemarks,
                    holidayFlg = holidayFlg.ifEmpty { "Y" }
                )

                val response = RetrofitClient.api.addHolidayMaster(request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Holiday added successfully", Toast.LENGTH_SHORT).show()
                    layoutHoliday.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                    holidayHeader.visibility = View.VISIBLE
                    btnAdd.visibility = View.VISIBLE
                    loadHolidays(calMonth.ifEmpty { selectedHolidayMonth })
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed: ${response.code()} ${response.errorBody()?.string()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun formatRecordDateForHolidayMaster(input: String): String {
        val sourceFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
            SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH),
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        )
        val targetFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        sourceFormats.forEach { parser ->
            try {
                parser.isLenient = false
                val parsed = parser.parse(input)
                if (parsed != null) return targetFormat.format(parsed)
            } catch (_: Exception) {
            }
        }
        return input
    }

}
