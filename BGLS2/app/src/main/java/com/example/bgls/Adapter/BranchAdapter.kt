package com.example.bgls.Adapter

import android.content.Intent
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.*
import com.example.bgls.DataModels.Branch

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

        val actions = listOf("Action", "Edit", "Delete")

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

                        "Edit" -> {
                            val intent = Intent(holder.itemView.context, EditBranchActivity::class.java)

                            intent.putExtra("code", item.branchCode)
                            intent.putExtra("name", item.branchName)
                            intent.putExtra("swift", item.swiftCode)
                            intent.putExtra("head", item.branchHead)

                            holder.itemView.context.startActivity(intent)
                        }

                        "Delete" -> {
                            val intent = Intent(holder.itemView.context, DeleteBranchActivity::class.java)

                            intent.putExtra("code", item.branchCode)
                            intent.putExtra("name", item.branchName)
                            intent.putExtra("swift", item.swiftCode)
                            intent.putExtra("head", item.branchHead)

                            holder.itemView.context.startActivity(intent)
                        }
                    }

                    holder.spinner.setSelection(0)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }
}
