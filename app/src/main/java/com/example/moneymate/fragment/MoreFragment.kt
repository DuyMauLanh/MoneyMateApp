package com.example.moneymate.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.moneymate.R

class MoreFragment : Fragment() {

    private lateinit var selectedLanguageText: TextView
    private lateinit var selectedThemeText: TextView
    private lateinit var languageSelector: ImageView
    private lateinit var themeSelector: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        // Initialize views
        selectedLanguageText = view.findViewById(R.id.selectedLanguage)
        selectedThemeText = view.findViewById(R.id.selectedTheme)
        languageSelector = view.findViewById(R.id.languageSelector)
        themeSelector = view.findViewById(R.id.themeSelector)

        setupLanguageSelection()
        setupThemeSelection()

        return view
    }

    private fun setupLanguageSelection() {
        languageSelector.setOnClickListener {
            val languages = arrayOf("Tiếng Việt", "English")
            AlertDialog.Builder(requireContext())
                .setTitle("Chọn ngôn ngữ")
                .setItems(languages) { dialog, which ->
                    selectedLanguageText.text = languages[which]
                    // TODO: Implement language change logic
                }
                .show()
        }
    }

    private fun setupThemeSelection() {
        themeSelector.setOnClickListener {
            val themes = arrayOf("Sáng", "Tối", "Theo hệ thống")
            AlertDialog.Builder(requireContext())
                .setTitle("Chọn màu sắc")
                .setItems(themes) { dialog, which ->
                    selectedThemeText.text = themes[which]
                    // TODO: Implement theme change logic
                }
                .show()
        }
    }
} 