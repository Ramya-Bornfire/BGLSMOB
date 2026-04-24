package com.example.bgls.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.BranchAdapter
import com.example.bgls.DataModels.Branch
import com.example.bgls.R

class BranchesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var branchAdapter: BranchAdapter
    private lateinit var btnAdd: Button

    // Mutable list to allow dynamic updates
    private val branchList = mutableListOf<Branch>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_branches, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        btnAdd = view.findViewById(R.id.btnAdd)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        branchAdapter = BranchAdapter(branchList)  // pass mutable list
        recyclerView.adapter = branchAdapter

        // Load initial data
        loadBranches()

        // Handle Add button click
        btnAdd.setOnClickListener {
            val newSrl = branchList.size + 1
            val newBranch = Branch(
                srlNo = newSrl,
                code = "BR${String.format("%03d", newSrl)}",
                name = "New Branch $newSrl",
                swift = "SWIFT$newSrl",
                head = "Branch Head $newSrl"
            )
            branchList.add(newBranch)
            branchAdapter.notifyItemInserted(branchList.size - 1)
        }
    }

    private fun loadBranches() {
        branchList.clear()
        branchList.addAll(
            listOf(
                Branch(1, "BR001", "Head Office", "HDFCINBB", "John Doe"),
                Branch(2, "BR002", "Downtown Branch", "HDFCINBB002", "Jane Smith"),
                Branch(3, "BR003", "North Branch", "HDFCINBB003", "Bob Johnson")
            )
        )
        branchAdapter.notifyDataSetChanged()
    }
}