package com.example.bgls.Adapter

import android.content.Intent
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.*
import com.example.bgls.DataModels.Branch
import com.example.bgls.OrganizationDetails.DeleteBranchActivity
import com.example.bgls.OrganizationDetails.EditBranchActivity
import com.example.bgls.OrganizationDetails.ViewBranchActivity

class BranchAdapter(private val list: MutableList<Branch>) :
    RecyclerView.Adapter<BranchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSrl: TextView = view.findViewById(R.id.tvSrl)
        val tvCode: TextView = view.findViewById(R.id.tvCode)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSwift: TextView = view.findViewById(R.id.tvSwift)
        val tvHead: TextView = view.findViewById(R.id.tvHead)
        val spinner: Spinner = view.findViewById(R.id.spinnerAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_branch, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.tvSrl.text = item.srlNo.toString()
        holder.tvCode.text = item.branchCode ?: ""
        holder.tvName.text = item.branchName ?: ""
        holder.tvSwift.text = item.swiftCode ?: ""
        holder.tvHead.text = item.branchHead ?: ""

        val actions = listOf("Action","View","Edit", "Delete")

        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_dropdown_item,
            actions
        )

        holder.spinner.adapter = adapter

        holder.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

                    when (actions[pos]) {
                        // Inside BranchAdapter, for "View" action:
                        "View" -> {
                            val intent = Intent(holder.itemView.context, ViewBranchActivity::class.java)
                            intent.putExtra("branch_code", item.branchCode)
                            holder.itemView.context.startActivity(intent)
                        }
                        "Edit" -> {
                            val intent = Intent(holder.itemView.context, EditBranchActivity::class.java)
                            intent.putExtra("branch_code", item.branchCode)
                            holder.itemView.context.startActivity(intent)
                        }

                        "Delete" -> {
                            val intent = Intent(holder.itemView.context, DeleteBranchActivity::class.java)
                            intent.putExtra("code", item.branchCode)
                            intent.putExtra("name", item.branchName)
                            intent.putExtra("head", item.branchHead)
                            intent.putExtra("swift", item.swiftCode)
                            intent.putExtra("designation", item.designation)
                            intent.putExtra("remarks", item.remarks)
                            intent.putExtra("landline", item.landline)
                            intent.putExtra("fax", item.fax)
                            intent.putExtra("mobile", item.mobile)
                            intent.putExtra("contact", item.contactPerson)
                            intent.putExtra("website", item.website)
                            intent.putExtra("email", item.email)
                            intent.putExtra("address1", item.address1)
                            intent.putExtra("address2", item.address2)
                            intent.putExtra("city", item.city)
                            intent.putExtra("state", item.state)
                            intent.putExtra("country", item.country)
                            intent.putExtra("zip", item.zipCode)
                            holder.itemView.context.startActivity(intent)
                        }
                    }

                    holder.spinner.setSelection(0)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }
}
