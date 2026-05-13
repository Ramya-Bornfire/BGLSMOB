package com.example.bgls

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.MainActivity.Module
import com.example.bgls.R

class ModuleAdapter(
    private val modules: List<Module>,
    private val cellW: Int,
    private val cellH: Int,
    private val onClick: (Module, View) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    class ModuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.llContainer)
        val iconBg: FrameLayout = view.findViewById(R.id.flIconBg)
        val tvIcon: TextView = view.findViewById(R.id.tvIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvBadge: TextView = view.findViewById(R.id.tvBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_module, parent, false)
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        // Account for margins so it fits perfectly
        layoutParams.width = cellW - layoutParams.leftMargin - layoutParams.rightMargin
        layoutParams.height = cellH - layoutParams.topMargin - layoutParams.bottomMargin
        view.layoutParams = layoutParams
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        val mainColor = Color.parseColor(module.color)

        holder.tvIcon.text = module.icon
        holder.tvTitle.text = module.title

        // Create light circular background for the icon
        val alphaColor = Color.argb(30, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor))
        val circleBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(alphaColor)
        }
        holder.iconBg.background = circleBg

        // Handle sub-item badge
        if (module.subItems.size > 1) {
            holder.tvBadge.visibility = View.VISIBLE
            holder.tvBadge.text = "${module.subItems.size} Items"
            holder.tvBadge.setTextColor(mainColor)

            val badgeBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f // Pill shape
                setColor(Color.WHITE)
                setStroke(2, mainColor)
            }
            holder.tvBadge.background = badgeBg
        } else {
            holder.tvBadge.visibility = View.INVISIBLE // Keeps layout stable compared to GONE
        }

        holder.itemView.setOnClickListener {
            onClick(module, it)
        }
    }

    override fun getItemCount(): Int = modules.size
}
