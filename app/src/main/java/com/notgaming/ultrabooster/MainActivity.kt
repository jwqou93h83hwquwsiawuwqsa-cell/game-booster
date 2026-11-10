package com.notgaming.ultrabooster

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val bg = Color.rgb(8, 8, 8)
    private val white = Color.WHITE
    private val red = Color.rgb(255, 55, 55)

    private lateinit var root: LinearLayout
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 35, 24, 24)
            setBackgroundColor(bg)
        }

        val title = TextView(this).apply {
            text = "NOTGAMING"
            textSize = 27f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "ULTRA BOOSTER  •  GAME CONTROL"
            textSize = 12f
            setTextColor(red)
        }
        root.addView(subtitle)

        addSpace(25)

        addHeading("PERFORMANCE MODE")

        status = TextView(this).apply {
            text = "Current profile: Balanced"
            textSize = 15f
            setTextColor(white)
            setPadding(0, 12, 0, 12)
        }
        root.addView(status)

        addButton("⚡  PERFORMANCE") {
            status.text = "Current profile: Performance"
            Toast.makeText(this, "Performance profile selected", Toast.LENGTH_SHORT).show()
        }

        addButton("◉  BALANCED") {
            status.text = "Current profile: Balanced"
            Toast.makeText(this, "Balanced profile selected", Toast.LENGTH_SHORT).show()
        }

        addButton("☾  POWER SAVING") {
            status.text = "Current profile: Power Saving"
            Toast.makeText(this, "Power Saving profile selected", Toast.LENGTH_SHORT).show()
        }

        addSpace(25)
        addHeading("MY GAMES")

        addButton("↻  SCAN INSTALLED GAMES") {
            showGames()
        }

        addButton("●  SCREEN RECORDER") {
            Toast.makeText(this, "Screen recorder will be added in a later version.", Toast.LENGTH_LONG).show()
        }

        addButton("FPS MONITOR") {
            Toast.makeText(this, "Real FPS monitoring is not available yet.", Toast.LENGTH_LONG).show()
        }

        addSpace(20)

        val footer = TextView(this).apply {
            text = "MADE FOR GAMERS  •  NOTGAMING"
            textSize = 10f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }
        root.addView(footer)

        setContentView(root)
    }

    private fun addHeading(text: String) {
        val view = TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(red)
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(view)
    }

    private fun addSpace(height: Int) {
        val space = View(this)
        root.addView(space, LinearLayout.LayoutParams(1, height))
    }

    private fun addButton(text: String, action: () -> Unit) {
        val button = Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(white)
            setBackgroundColor(Color.rgb(35, 35, 35))
            isAllCaps = false
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            55
        )
        params.setMargins(0, 7, 0, 7)
        root.addView(button, params)
    }

    private fun showGames() {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }

        if (apps.isEmpty()) {
            Toast.makeText(this, "No launchable apps found.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogItems = apps.map {
            it.loadLabel(packageManager).toString()
        }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("SELECT A GAME OR APP")
            .setItems(dialogItems) { _, which ->
                val packageName = apps[which].activityInfo.packageName
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(this, "Cannot launch this app.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }
}
