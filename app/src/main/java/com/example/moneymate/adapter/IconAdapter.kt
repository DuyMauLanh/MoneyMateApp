package com.example.moneymate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymate.R

class IconAdapter(
    private val icons: List<Int>,
    private val onIconSelected: (Int) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    private var selectedPosition = -1

    inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconView)

        fun bind(iconRes: Int, position: Int) {
            iconView.setImageResource(iconRes)
            itemView.isSelected = position == selectedPosition
            itemView.setBackgroundResource(
                if (position == selectedPosition) R.drawable.selected_icon_background
                else android.R.color.transparent
            )
            
            itemView.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onIconSelected(iconRes)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icon_selection, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(icons[position], position)
    }

    override fun getItemCount(): Int = icons.size
} 