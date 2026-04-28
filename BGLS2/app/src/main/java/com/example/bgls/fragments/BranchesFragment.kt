package com.example.bgls.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.example.bgls.*
import com.example.bgls.Adapter.BranchAdapter
import com.example.bgls.DataModels.Branch
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch


class BranchesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var branchAdapter: BranchAdapter
    private lateinit var btnAdd: Button

    private val branchList = mutableListOf<Branch>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_branches, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView = view.findViewById(R.id.recyclerView)
        btnAdd = view.findViewById(R.id.btnAdd)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        branchAdapter = BranchAdapter(branchList)
        recyclerView.adapter = branchAdapter

        loadBranches()

        btnAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddBranchActivity::class.java))
        }
    }

    private fun loadBranches() {

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getOrganizationDetails()

                if (response.isSuccessful) {

                    val branches = response.body()?.OrgBranch ?: emptyList()

                    branchList.clear()

                    branches.forEachIndexed { index, b ->
                        branchList.add(
                            Branch(
                                srlNo = index + 1,
                                branchCode = b.branchCode ?: "",
                                branchName = b.branchName ?: "",
                                swiftCode = b.swiftCode ?: "",
                                branchHead = b.branchHead ?: ""
                            )
                        )
                    }


                    branchAdapter.notifyDataSetChanged()

                } else {
                    Toast.makeText(requireContext(), "API Error", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
