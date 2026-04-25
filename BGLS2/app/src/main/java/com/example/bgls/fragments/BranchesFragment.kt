package com.example.bgls.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.BranchAdapter
import com.example.bgls.AddBranchActivity
import com.example.bgls.DataModels.Branch
import com.example.bgls.R

class BranchesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var branchAdapter: BranchAdapter
    private lateinit var btnAdd: Button

    private val branchList = mutableListOf<Branch>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_branches, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        btnAdd = view.findViewById(R.id.btnAdd)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        branchAdapter = BranchAdapter(branchList)
        recyclerView.adapter = branchAdapter

        loadBranches()

        // ✅ FIXED ADD BUTTON CLICK
        btnAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddBranchActivity::class.java)
            startActivity(intent)
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