package com.example.bgls.ChartOfAccounts

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.TabTransactionModel
import com.example.bgls.R

class TabTransactionAdapter(
    private val context: Context,
    private val initialList: List<TabTransactionModel>
) : RecyclerView.Adapter<TabTransactionAdapter.ViewHolder>() {

    private var fullList: List<TabTransactionModel> = initialList
    private val list = initialList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvEvent: TextView = view.findViewById(R.id.tvEvent)
        val tvDebitAccNo: TextView = view.findViewById(R.id.tvDebitAccNo)
        val tvDebitAccName: TextView = view.findViewById(R.id.tvDebitAccName)
        val tvCreditAccNo: TextView = view.findViewById(R.id.tvCreditAccNo)
        val tvCreditAccName: TextView = view.findViewById(R.id.tvCreditAccName)
        val tvTranParticular: TextView = view.findViewById(R.id.tvTranParticular)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvAction: TextView = view.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvId.text = item.id
        holder.tvEvent.text = item.event
        holder.tvDebitAccNo.text = item.debitAccNo
        holder.tvDebitAccName.text = item.debitAccName
        holder.tvCreditAccNo.text = item.creditAccNo
        holder.tvCreditAccName.text = item.creditAccName
        holder.tvTranParticular.text = item.tranParticular
        holder.tvType.text = item.type
        
        // Zebra striping
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

        // Event Click -> Navigate to TransactionAccountViewActivity (VIEW MODE)
        holder.tvEvent.paintFlags = holder.tvEvent.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        holder.tvEvent.setTextColor(Color.parseColor("#2196F3"))
        holder.tvEvent.setOnClickListener {
            val intent = android.content.Intent(context, TransactionAccountViewActivity::class.java)
            intent.putExtra("MODE", "VIEW")
            intent.putExtra("ID", item.id)
            context.startActivity(intent)
        }

        // Action Dropdown -> Edit, Delete
        holder.tvAction.text = "Action ▼"
        holder.tvAction.setTextColor(Color.parseColor("#2196F3"))
        holder.tvAction.setOnClickListener {
            val popup = android.widget.PopupMenu(context, holder.tvAction)
            popup.menu.add("Edit")
            popup.menu.add("Delete")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Edit" -> {
                        val intent = android.content.Intent(context, TransactionAccountModifyActivity::class.java)
                        intent.putExtra("ID", item.id)
                        context.startActivity(intent)
                        true
                    }
                    "Delete" -> {
                        android.app.AlertDialog.Builder(context)
                            .setTitle("Delete Account")
                            .setMessage("Are you sure you want to delete this Transaction Account?")
                            .setPositiveButton("Yes") { _, _ ->
                                val pos = holder.adapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    val itemToDelete = list[pos]
                                    val idToDel = itemToDelete.id?.toLongOrNull()
                                    if (idToDel != null) {
                                        com.example.bgls.Retrofit.RetrofitClient.api.deleteTransactionAccount(idToDel)
                                            .enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                                                override fun onResponse(
                                                    call: retrofit2.Call<okhttp3.ResponseBody>,
                                                    response: retrofit2.Response<okhttp3.ResponseBody>
                                                ) {
                                                    if (response.isSuccessful) {
                                                        val mutableList = fullList.toMutableList()
                                                        mutableList.remove(itemToDelete)
                                                        updateList(mutableList)
                                                        android.widget.Toast.makeText(context, "Deleted Successfully", android.widget.Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        android.widget.Toast.makeText(context, "Delete failed: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                                                    android.widget.Toast.makeText(context, "Network error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                    } else {
                                        android.widget.Toast.makeText(context, "Invalid Account ID", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setNegativeButton("No", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<TabTransactionModel>) {
        fullList = newList
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(id: String, event: String, debitNo: String, debitName: String, creditNo: String, creditName: String, particular: String, type: String) {
        val f1 = id.lowercase()
        val f2 = event.lowercase()
        val f3 = debitNo.lowercase()
        val f4 = debitName.lowercase()
        val f5 = creditNo.lowercase()
        val f6 = creditName.lowercase()
        val f7 = particular.lowercase()
        val f8 = type.lowercase()

        val filtered = fullList.filter {
            (f1.isEmpty() || it.id.lowercase().contains(f1)) &&
            (f2.isEmpty() || it.event.lowercase().contains(f2)) &&
            (f3.isEmpty() || it.debitAccNo.lowercase().contains(f3)) &&
            (f4.isEmpty() || it.debitAccName.lowercase().contains(f4)) &&
            (f5.isEmpty() || it.creditAccNo.lowercase().contains(f5)) &&
            (f6.isEmpty() || it.creditAccName.lowercase().contains(f6)) &&
            (f7.isEmpty() || it.tranParticular.lowercase().contains(f7)) &&
            (f8.isEmpty() || it.type.lowercase().contains(f8))
        }

        list.clear()
        list.addAll(filtered)
        notifyDataSetChanged()
    }
}


