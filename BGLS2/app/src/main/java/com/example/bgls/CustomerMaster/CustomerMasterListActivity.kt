package com.example.bgls.CustomerMaster

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.CustomerMaster.CustomerMasterAdapter
import com.example.bgls.CustomerMaster.CustomerMasterViewActivity
import com.example.bgls.DataModels.CustomerMaster
import com.example.bgls.R

class CustomerMasterListActivity : AppCompatActivity() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerMasterAdapter

    // ─── Pagination ───
    private val pageSize = 16
    private var currentPage = 1
    private var totalPages = 1

    // ─── Full dummy data — replace with API later ───
    private val allCustomers = mutableListOf(
        CustomerMaster(
            "1",
            "27917600",
            "MERCY NYANGOGE MACHUKA",
            "11-06-1989",
            "NAIROBI HEAD OFFICE",
            "254725661248",
            "mercymachuka24@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "2",
            "22187093",
            "BEATRICE KEMUNTO OBWOCHA",
            "16-06-1981",
            "NAIROBI HEAD OFFICE",
            "254721169780",
            "beatrice.obwocha@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "3",
            "13667114",
            "JACKLYNE OBEGI",
            "30-09-1976",
            "NAIROBI HEAD OFFICE",
            "254726678050",
            "jackieobegi@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "4",
            "22619397",
            "NJOGU YUNA NYATHIRIGA",
            "01-01-1990",
            "NAIROBI HEAD OFFICE",
            "254722663889",
            "nyathiriga@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "5",
            "10957906",
            "MWANASITI MOHAMED HASSAN",
            "01-01-1970",
            "NAIROBI HEAD OFFICE",
            "254720789082",
            "catherinemzungu4@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "6",
            "11417859",
            "WYCLIFFE NDEKWE",
            "28-05-1972",
            "NAIROBI HEAD OFFICE",
            "254722243267",
            "wandekwe@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "7",
            "CUST0000040801",
            "HARISH KALYAN",
            "21-03-1998",
            "Al Salam Bank Seychelles Limited",
            "3684308",
            "harishkalyan@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "8",
            "CUST0000041101",
            "SUNIL KUMAR",
            "21-03-1998",
            "Al Salam Bank Seychelles Limited",
            "5887958",
            "sunilkumar@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "9",
            "CUST0000041401",
            "RAJILAKSHMI",
            "28-01-2003",
            "Al Salam Bank Seychelles Limited",
            "5744541",
            "raji@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "10",
            "CUST0000039401",
            "PON PRASANTH",
            "21-03-1998",
            "Al Salam Bank Seychelles Limited",
            "5659769",
            "ponprasanth321@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "11",
            "23598125",
            "ELIZABETH NYAMBURA HOSEAH",
            "29-10-1984",
            "NAIROBI HEAD OFFICE",
            "254721480542",
            "liznyambura59@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "12",
            "32886653",
            "NDIWA PAUL",
            "11-12-1995",
            "NAIROBI HEAD OFFICE",
            "254703815518",
            "paulndiwa95@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "13",
            "30118172",
            "CHERUIYOT ISAAC",
            "02-06-1993",
            "NAIROBI HEAD OFFICE",
            "254727938049",
            "cherioyotisaac170@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "14",
            "36070502",
            "HESBON MUSILI",
            "31-12-1899",
            "NAIROBI HEAD OFFICE",
            "254703321017",
            "musilihesbon@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "15",
            "30982540",
            "SIMOTWO TOM",
            "03-12-1994",
            "NAIROBI HEAD OFFICE",
            "254728724194",
            "simotwot@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "16",
            "38441246",
            "HILLARY AMACHE LUSENO",
            "14-03-2001",
            "NAIROBI HEAD OFFICE",
            "254797828762",
            "hluseno695@gmail.com",
            "ACTIVE"
        ),
        // Add more rows for pagination demo
        CustomerMaster(
            "17",
            "29100001",
            "JOHN DOE",
            "10-05-1985",
            "NAIROBI HEAD OFFICE",
            "254700000001",
            "johndoe@gmail.com",
            "ACTIVE"
        ),
        CustomerMaster(
            "18",
            "29100002",
            "JANE DOE",
            "22-08-1990",
            "NAIROBI HEAD OFFICE",
            "254700000002",
            "janedoe@gmail.com",
            "INACTIVE"
        )
    )

    private var filteredCustomers = allCustomers.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_master_list)

        initViews()
        setupSpinners()
        setupRecyclerView()
        setupPagination()
        setupDownload()
        loadPage(1)
    }

    private fun initViews() {
        spinnerFilter  = findViewById(R.id.spinnerFilter)
        spinnerStatus  = findViewById(R.id.spinnerStatus)
        btnDownload    = findViewById(R.id.btnDownload)
        btnPrev        = findViewById(R.id.btnPrev)
        btnNext        = findViewById(R.id.btnNext)
        tvPageInfo     = findViewById(R.id.tvPageInfo)
        recyclerView   = findViewById(R.id.recyclerViewCustomers)
    }

    private fun setupSpinners() {
        // Filter spinner
        val filterOptions = listOf("Select Filter", "Customer Id", "Customer Name", "Mobile No", "Email")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter

        // Status spinner
        val statusOptions = listOf("Select Status", "ACTIVE", "INACTIVE", "PENDING")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        // Filter by status when changed
        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selected = statusOptions[pos]
                filteredCustomers = if (selected == "Select Status") {
                    allCustomers.toMutableList()
                } else {
                    allCustomers.filter { it.status == selected }.toMutableList()
                }
                loadPage(1)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun setupRecyclerView() {
        adapter = CustomerMasterAdapter(this, emptyList()) { customer ->
            // Navigate to Customer Detail screen
            val intent = Intent(this, CustomerMasterViewActivity::class.java)
            intent.putExtra("customerId", customer.customerId)
            intent.putExtra("customerName", customer.customerName)
            intent.putExtra("mobile", customer.mobileNo)
            intent.putExtra("email", customer.email)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    // ─── Load a specific page ───
    private fun loadPage(page: Int) {
        totalPages = Math.max(1, Math.ceil(filteredCustomers.size.toDouble() / pageSize).toInt())
        currentPage = page.coerceIn(1, totalPages)

        val fromIndex = (currentPage - 1) * pageSize
        val toIndex = minOf(fromIndex + pageSize, filteredCustomers.size)
        val pageData = if (fromIndex < filteredCustomers.size) {
            filteredCustomers.subList(fromIndex, toIndex)
        } else emptyList()

        adapter.updateList(pageData)
        tvPageInfo.text = "Page $currentPage of $totalPages"

        // Disable Prev on first page, Next on last page
        btnPrev.isEnabled = currentPage > 1
        btnPrev.alpha = if (currentPage > 1) 1f else 0.5f
        btnNext.isEnabled = currentPage < totalPages
        btnNext.alpha = if (currentPage < totalPages) 1f else 0.5f
    }

    private fun setupPagination() {
        btnPrev.setOnClickListener {
            if (currentPage > 1) loadPage(currentPage - 1)
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) loadPage(currentPage + 1)
        }
    }

    private fun setupDownload() {
        btnDownload.setOnClickListener {
            // TODO: Export list to CSV/Excel
            Toast.makeText(this, "Downloading customer list...", Toast.LENGTH_SHORT).show()
        }
    }
}