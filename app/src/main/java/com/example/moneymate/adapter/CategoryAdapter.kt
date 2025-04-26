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
    private var currentType: String = "income" // Default to income
    private var filteredCategories: List<Category> = categories.filter { it.type == currentType }
    private var deleteMode = -1 // Track which item is in delete mode

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val nameView: TextView = itemView.findViewById(R.id.categoryName)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(category: Category) {
            iconView.setImageResource(category.icon ?: R.drawable.ic_other)
            nameView.text = category.name
            
            // Hide delete button by default and only show for non-default categories
            deleteButton.visibility = if (adapterPosition == deleteMode && !category.is_default) View.VISIBLE else View.GONE
            
            // Set up click listeners
            itemView.setOnClickListener {
                if (deleteMode == adapterPosition) {
                    // If in delete mode, exit delete mode
                    deleteMode = -1
                    notifyItemChanged(adapterPosition)
                } else {
                    // If not in delete mode, handle normal click
                    onCategoryClick(category)
                    selectedItemPosition = adapterPosition
                    notifyDataSetChanged()
                }
            }

            // Long press to enter delete mode
            itemView.setOnLongClickListener { 
                if (!category.is_default) {
                    val oldDeleteMode = deleteMode
                    deleteMode = adapterPosition
                    notifyItemChanged(oldDeleteMode) // Hide old delete button
                    notifyItemChanged(deleteMode)    // Show new delete button
                }
                true
            }

            deleteButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val position = categories.indexOf(category)
                    if (position != -1) {
                        categories.removeAt(position)
                        deleteMode = -1
                        filteredCategories = categories.filter { it.type == currentType }
                        notifyDataSetChanged()
                    }
                }
            }

            itemView.isSelected = adapterPosition == selectedItemPosition
        }
    }

    inner class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { onAddCategoryClick() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category, parent, false)
                CategoryViewHolder(view)
            }
            VIEW_TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_category, parent, false)
                AddViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> holder.bind(filteredCategories[position])
            is AddViewHolder -> {} // No binding needed for add button
        }
    }

    override fun getItemCount(): Int = filteredCategories.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == filteredCategories.size) VIEW_TYPE_ADD else VIEW_TYPE_CATEGORY
    }

    fun updateType(type: String) {
        currentType = type
        deleteMode = -1 // Reset delete mode when switching types
        filteredCategories = categories.filter { it.type == currentType }
        notifyDataSetChanged()
    }

    fun addCategory(category: Category) {
        categories.add(category)
        if (category.type == currentType) {
            filteredCategories = categories.filter { it.type == currentType }
            notifyDataSetChanged()
        }
    }
} 