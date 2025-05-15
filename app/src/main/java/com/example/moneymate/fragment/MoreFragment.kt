package com.example.moneymate.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.moneymate.R
import java.util.Locale

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
        loadSavedSettings()
        return view
    }

    private fun setupLanguageSelection() {
        languageSelector.setOnClickListener {
            val languages = arrayOf("Tiếng Việt", "English")
            val languageCodes = arrayOf("vi", "en")
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.ch_n_ng_n_ng))
                .setItems(languages) { _, which ->
                    selectedLanguageText.text = languages[which]
                    setAppLanguage(languageCodes[which])
                }
                .show()

        }
    }

    private fun setupThemeSelection() {
        themeSelector.setOnClickListener {
            val themes = arrayOf(getString(R.string.s_ng), getString(R.string.t_i),
                getString(R.string.theo_h_th_ng))
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.ch_n_m_u_s_c))
                .setItems(themes) { _, which ->
                    selectedThemeText.text = themes[which]
                    when (which) {
                        0 -> setAppTheme(AppCompatDelegate.MODE_NIGHT_NO)
                        1 -> setAppTheme(AppCompatDelegate.MODE_NIGHT_YES)
                        2 -> setAppTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
                .show()
        }
    }

    private fun setAppTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().putInt("theme_mode", themeMode).apply()
    }
    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = requireActivity().resources.configuration
        config.setLocale(locale)

        requireActivity().apply {
            resources.updateConfiguration(config, resources.displayMetrics)
            getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit().putString("lang", languageCode).apply()
            recreate() // Khởi động lại để áp dụng ngôn ngữ
        }
    }

    private fun loadSavedSettings() {
        val prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)

        // Hiển thị ngôn ngữ đã chọn
        val savedLangCode = prefs.getString("lang", Locale.getDefault().language)
        selectedLanguageText.text = when (savedLangCode) {
            "vi" -> "Tiếng Việt"
            "en" -> "English"
            else -> "English"
        }

        // Hiển thị theme đã chọn
        val savedThemeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        selectedThemeText.text = when (savedThemeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.s_ng)
            AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.t_i)
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> getString(R.string.theo_h_th_ng)
            else -> getString(R.string.theo_h_th_ng)
        }
    }


} 