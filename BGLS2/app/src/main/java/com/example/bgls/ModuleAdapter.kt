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
       // val tvBadge: TextView = view.findViewById(R.id.tvBadge)
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
       // val mainColor = Color.parseColor(module.color)
        val mainColor = Color.parseColor("#38A9CB")
        holder.tvIcon.text = module.icon
        holder.tvTitle.text = module.title
            .lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }

        // Create light circular background for the icon
//        val alphaColor = Color.argb(30, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor))
//        val circleBg = GradientDrawable().apply {
//            shape = GradientDrawable.OVAL
//            setColor(alphaColor)
//        }
//        holder.iconBg.background = circleBg
// Bright vibrant background for icons
        // Light blue theme
        val bgColor = Color.parseColor("#E3F2FD")
        val borderColor = Color.parseColor("#42A5F5")

// Light blue circular background
        val circleBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(bgColor)
            setStroke(3, borderColor)
        }

        holder.iconBg.background = circleBg

// Bright icon styling
        holder.tvIcon.apply {
            text = module.icon
            textSize = 34f
            setTextColor(Color.parseColor("#1565C0"))

            setShadowLayer(
                4f,
                0f,
                1f,
                Color.parseColor("#33000000")
            )
        }
        holder.tvTitle.isAllCaps = false
        holder.tvTitle.text = module.title
            .lowercase()
            .split(" ")
            .joinToString(" ") {
                it.replaceFirstChar { ch -> ch.uppercase() }
            }
// Make icon brighter and bigger
        holder.tvIcon.textSize = 30f
        holder.tvIcon.setShadowLayer(
            6f,
            0f,
            2f,
            Color.parseColor("#55000000")
        )
        // Handle sub-item badge
        if (module.subItems.size > 1) {
//            holder.tvBadge.visibility = View.VISIBLE
//            holder.tvBadge.text = "${module.subItems.size} Items"
//            holder.tvBadge.setTextColor(mainColor)

            val badgeBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f // Pill shape
                setColor(Color.WHITE)
                setStroke(2, mainColor)
            }
            //holder.tvBadge.background = badgeBg
        } else {
           // holder.tvBadge.visibility = View.INVISIBLE // Keeps layout stable compared to GONE
        }

        holder.itemView.setOnClickListener {
            onClick(module, it)
        }
    }

    override fun getItemCount(): Int = modules.size
}
