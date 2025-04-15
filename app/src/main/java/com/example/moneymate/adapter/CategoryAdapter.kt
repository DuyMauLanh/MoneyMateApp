package com.example.moneymate.adapter

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymate.R
import com.example.moneymate.model.Category

class CategoryAdapter(
    private val categories: MutableList<Category>,
    private val onCategoryClick: (Category) -> Unit,
    private val onAddCategoryClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_CATEGORY = 0
    private val VIEW_TYPE_ADD = 1
    private var selectedItemPosition = -1

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val nameView: TextView = itemView.findViewById(R.id.categoryName)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(category: Category, position: Int) {
            category.icon?.let { iconView.setImageResource(it) }
            nameView.text = category.name
            
            // Show/hide delete button based on selection
            deleteButton.visibility = if (position == selectedItemPosition) View.VISIBLE else View.GONE
            
            // Set up click listeners
            itemView.setOnClickListener { 
                if (selectedItemPosition == -1) {
                    onCategoryClick(category)
                } else {
                    // If in delete mode, clicking anywhere else exits delete mode
                    updateSelectedPosition(-1)
                }
            }
            
            itemView.setOnLongClickListener { 
                updateSelectedPosition(if (selectedItemPosition == position) -1 else position)
                true
            }
            
            deleteButton.setOnClickListener {
                if (position < categories.size) {
//                    categories.removeAt(position)
                    notifyItemRemoved(position)
                    updateSelectedPosition(-1)
                }
            }
        }
    }

    inner class AddCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { 
                updateSelectedPosition(-1)
                onAddCategoryClick() 
            }
        }
    }

    private fun updateSelectedPosition(position: Int) {
        val oldPosition = selectedItemPosition
        selectedItemPosition = position
        if (oldPosition != -1) notifyItemChanged(oldPosition)
        if (position != -1) notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category, parent, false)
                CategoryViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_category, parent, false)
                AddCategoryViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> holder.bind(categories[position], position)
            is AddCategoryViewHolder -> { /* No binding needed */ }
        }
    }

    override fun getItemCount(): Int = categories.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position < categories.size) VIEW_TYPE_CATEGORY else VIEW_TYPE_ADD
    }

    fun addCategory(category: Category) {
        (categories as MutableList).add(category) // Ép kiểu vì `categories` là List
        notifyItemInserted(categories.size - 1)
    }
} 