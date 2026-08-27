package com.xnglo.font

import android.app.Activity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var logView: TextView
    private lateinit var selectedFont: LocalFontOption
    private lateinit var statusView: TextView
    private lateinit var makeDefaultCheckbox: CheckBox
    private lateinit var reapplyOnBootCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedDefaultId = FontPrefs.getDefaultFontId(this)
        selectedFont = LocalFonts.byId(savedDefaultId ?: LocalFonts.DEFAULT_FONT_ID)
            ?: LocalFonts.ALL.first()
        setContentView(buildLayout())
        checkRootAndShowStatus()
    }

    private fun buildLayout(): ScrollView {
        val padding = (16 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(0xFF0B0F19.toInt())
        }

        val title = TextView(this).apply {
            text = "xNgloFont"
            setTextColor(0xFFE2E8F0.toInt())
            textSize = 20f
            setPadding(0, 0, 0, 4)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Set an xNglo font as the Android system font. Requires root (Magisk)."
            setTextColor(0xFF64748B.toInt())
            textSize = 12f
            setPadding(0, 0, 0, padding)
        }
        root.addView(subtitle)

        statusView = TextView(this).apply {
            text = "Checking root access..."
            setTextColor(0xFFF59E0B.toInt())
            textSize = 13f
            setPadding(0, 0, 0, padding)
        }
        root.addView(statusView)

        val label = TextView(this).apply {
            text = "Font"
            setTextColor(0xFF64748B.toInt())
            textSize = 13f
            setPadding(0, 0, 0, 8)
        }
        root.addView(label)

        val spinner = Spinner(this)
        val names = LocalFonts.ALL.map { it.displayName }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        val initialIndex = LocalFonts.ALL.indexOf(selectedFont).let { if (it < 0) 0 else it }
        spinner.setSelection(initialIndex)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedFont = LocalFonts.ALL[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        root.addView(spinner)

        val checkboxPad = (padding * 0.75).toInt()

        makeDefaultCheckbox = CheckBox(this).apply {
            text = "Make this the default font"
            setTextColor(0xFFE2E8F0.toInt())
            isChecked = true
            setPadding(0, checkboxPad, 0, 0)
        }
        root.addView(makeDefaultCheckbox)

        reapplyOnBootCheckbox = CheckBox(this).apply {
            text = "Re-apply after every reboot"
            setTextColor(0xFFE2E8F0.toInt())
            isChecked = true
        }
        root.addView(reapplyOnBootCheckbox)

        val checkboxNote = TextView(this).apply {
            text = "The Magisk overlay itself always persists across reboots automatically. " +
                "This adds a second, independent safety net that re-copies the font from a " +
                "backup kept inside the module on every boot."
            setTextColor(0xFF64748B.toInt())
            textSize = 11f
            setPadding(0, 4, 0, 0)
        }
        root.addView(checkboxNote)

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, padding, 0, padding)
        }

        val applyButton = Button(this).apply {
            text = "Apply as system font"
            setOnClickListener { runInstall() }
        }
        buttonRow.addView(applyButton)

        val removeButton = Button(this).apply {
            text = "Remove"
            setOnClickListener { runUninstall() }
        }
        buttonRow.addView(removeButton)

        val rebootButton = Button(this).apply {
            text = "Reboot"
            setOnClickListener { RootFontInstaller.reboot() }
        }
        buttonRow.addView(rebootButton)

        root.addView(buttonRow)

        val logLabel = TextView(this).apply {
            text = "Log"
            setTextColor(0xFF64748B.toInt())
            textSize = 13f
            setPadding(0, 0, 0, 8)
        }
        root.addView(logLabel)

        logView = TextView(this).apply {
            text = ""
            setTextColor(0xFFE2E8F0.toInt())
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        root.addView(logView)

        val scroll = ScrollView(this)
        scroll.addView(root)
        return scroll
    }

    private fun checkRootAndShowStatus() {
        thread {
            val hasRoot = RootShell.hasRoot()
            runOnUiThread {
                statusView.text = if (hasRoot) {
                    statusView.setTextColor(0xFF22C55E.toInt())
                    "✓ Root access available"
                } else {
                    statusView.setTextColor(0xFFF43F5E.toInt())
                    "✗ No root detected -- this app needs a rooted device (Magisk)"
                }
            }
        }
    }

    private fun appendLog(line: String) {
        runOnUiThread {
            logView.append(line + "\n\n")
        }
    }

    private fun runInstall() {
        val font = selectedFont
        val makeDefault = makeDefaultCheckbox.isChecked
        val reapplyOnBoot = reapplyOnBootCheckbox.isChecked

        appendLog("Installing ${font.displayName}...")
        thread {
            val result = RootFontInstaller.install(this, font, reapplyOnBoot)
            appendLog((if (result.success) "✓ " else "✗ ") + result.message)

            if (result.success && makeDefault) {
                FontPrefs.setDefaultFontId(this, font.id)
                appendLog("✓ Saved ${font.displayName} as the default font")
            }
        }
    }

    private fun runUninstall() {
        val font = selectedFont
        appendLog("Removing xNglo font module for ${font.displayName}...")
        thread {
            val result = RootFontInstaller.uninstall(font.id)
            appendLog((if (result.success) "✓ " else "✗ ") + result.message)
        }
    }
}
