package com.notgaming.ultrabooster

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 10, 16)
    private val panel = Color.rgb(18, 24, 35)
    private val panelAlt = Color.rgb(25, 33, 47)
    private val white = Color.WHITE
    private val muted = Color.rgb(151, 164, 183)
    private val accent = Color.rgb(0, 196, 255)

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var profileStatus: TextView
    private var selectedProfile = "Balanced"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedProfile = getPreferences(Context.MODE_PRIVATE).getString("profile", "Balanced") ?: "Balanced"
        buildShell()
        showDashboard()
    }

    private fun buildShell() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(18), dp(22), dp(18), dp(12))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mark = TextView(this).apply {
            text = "N"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(bg)
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(accent, 18)
        }
        header.addView(mark, LinearLayout.LayoutParams(dp(52), dp(52)))

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        brand.addView(label("NOTGAMING", 21f, white, true))
        brand.addView(label("ULTRA BOOSTER  •  BETA", 10f, accent, true))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(header)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(0, dp(18), 0, dp(12))
        }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(panel, 18)
            setPadding(dp(4), dp(5), dp(4), dp(5))
        }
        listOf("Home", "Games", "Stats", "Settings").forEachIndexed { index, name ->
            val button = Button(this).apply {
                text = name
                textSize = 10f
                isAllCaps = false
                setTextColor(if (index == 0) accent else muted)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    when (index) {
                        0 -> showDashboard()
                        1 -> showGames()
                        2 -> showStats()
                        else -> showSettings()
                    }
                }
            }
            nav.addView(button, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        root.addView(nav, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }

    private fun showDashboard() {
        content.removeAllViews()
        addSectionTitle("YOUR GAMING HUB", "A simple control centre for your games")
        val hero = card()
        hero.addView(label("CURRENT PROFILE", 11f, muted, true))
        profileStatus = label(selectedProfile.uppercase(Locale.ROOT), 25f, accent, true)
        hero.addView(profileStatus)
        hero.addView(label("Choose a profile before launching a game.", 12f, muted))
        content.addView(hero)
        addGap(14)
        addSectionTitle("PERFORMANCE MODE", "Select how you want to use your phone")
        val profiles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        listOf(
            Triple("⚡  PERFORMANCE", "Performance", "Prioritise your gaming session"),
            Triple("◈  BALANCED", "Balanced", "Everyday balance"),
            Triple("☾  POWER SAVING", "Power Saving", "Prefer lower power use")
        ).forEach { item ->
            profiles.addView(actionButton(item.first, item.third, selectedProfile == item.second) {
                selectedProfile = item.second
                getPreferences(Context.MODE_PRIVATE).edit().putString("profile", selectedProfile).apply()
                showDashboard()
                Toast.makeText(this, "$selectedProfile profile selected", Toast.LENGTH_SHORT).show()
            })
        }
        content.addView(profiles)
        addGap(14)
        addSectionTitle("QUICK LAUNCH", "Open a game from your installed apps")
        val gameCard = card()
        gameCard.addView(label("GAME CENTER", 16f, white, true))
        gameCard.addView(label("Choose a game and launch it from here. Your selected profile is remembered in the app.", 12f, muted))
        gameCard.addView(actionButton("SCAN & SELECT GAME", "Find launchable apps on this phone", false) { showGames() })
        content.addView(gameCard)
        addGap(10)
        content.addView(label("Note: profiles are currently saved in NOTGAMING only. Android does not let a normal app force CPU/GPU speeds or guarantee extra FPS.", 10f, muted))
    }

    private fun showGames() {
        content.removeAllViews()
        addSectionTitle("GAME CENTER", "Select an installed game or app")
        content.addView(actionButton("SCAN INSTALLED APPS", "Show apps that can be opened", true) {
            val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = packageManager.queryIntentActivities(intent, 0)
                .filter { it.activityInfo?.packageName != packageName }
                .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.ROOT) }
            if (apps.isEmpty()) {
                Toast.makeText(this, "No launchable apps found.", Toast.LENGTH_SHORT).show()
                return@actionButton
            }
            val names = apps.map { it.loadLabel(packageManager).toString() }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("SELECT A GAME OR APP")
                .setItems(names) { _, which ->
                    val packageToLaunch = apps[which].activityInfo.packageName
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageToLaunch)
                    if (launchIntent != null) {
                        Toast.makeText(this, "$selectedProfile profile selected in NOTGAMING", Toast.LENGTH_SHORT).show()
                        startActivity(launchIntent)
                    } else {
                        Toast.makeText(this, "Cannot launch this app.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("CANCEL", null)
                .show()
        })
        addGap(12)
        content.addView(label("The selected profile is remembered, but this version does not change Android system performance settings automatically.", 11f, muted))
    }

    private fun showStats() {
        content.removeAllViews()
        addSectionTitle("DEVICE OVERVIEW", "Live information available to this app")
        val memory = ActivityManager.MemoryInfo()
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memory)
        val totalMb = memory.totalMem / (1024L * 1024L)
        val availableMb = memory.availMem / (1024L * 1024L)
        val usedMb = (totalMb - availableMb).coerceAtLeast(0)
        val stats = card()
        stats.addView(label("DEVICE", 11f, muted, true))
        stats.addView(label(Build.MODEL ?: "Android device", 18f, white, true))
        stats.addView(label("Android ${Build.VERSION.RELEASE}  •  API ${Build.VERSION.SDK_INT}", 12f, muted))
        addGap(14, stats)
        stats.addView(label("MEMORY", 11f, muted, true))
        stats.addView(label("${usedMb} MB used / ${totalMb} MB total", 18f, accent, true))
        stats.addView(label("${availableMb} MB currently available", 12f, muted))
        content.addView(stats)
        addGap(12)
        content.addView(label("CPU temperature and true per-game FPS are not exposed consistently to ordinary Android apps. We won't display made-up readings.", 11f, muted))
    }

    private fun showSettings() {
        content.removeAllViews()
        addSectionTitle("SETTINGS", "NOTGAMING ULTRA BOOSTER")
        val box = card()
        box.addView(label("APP VERSION", 11f, muted, true))
        box.addView(label("1.1 BETA", 18f, accent, true))
        addGap(12, box)
        box.addView(label("ABOUT", 11f, muted, true))
        box.addView(label("A lightweight game launcher and device information dashboard.", 13f, white))
        box.addView(actionButton("RESET PROFILE TO BALANCED", "Change the saved profile", false) {
            selectedProfile = "Balanced"
            getPreferences(Context.MODE_PRIVATE).edit().putString("profile", selectedProfile).apply()
            Toast.makeText(this, "Profile reset to Balanced", Toast.LENGTH_SHORT).show()
            showSettings()
        })
        content.addView(box)
    }

    private fun addSectionTitle(title: String, subtitle: String) {
        content.addView(label(title, 18f, white, true))
        content.addView(label(subtitle, 12f, muted))
        addGap(14)
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(panel, 18)
        setPadding(dp(16), dp(16), dp(16), dp(16))
    }

    private fun actionButton(title: String, subtitle: String, active: Boolean, action: () -> Unit): LinearLayout {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(if (active) Color.rgb(15, 48, 65) else panelAlt, 14)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setOnClickListener { action() }
        }
        item.addView(label(title, 14f, if (active) accent else white, true))
        item.addView(label(subtitle, 11f, muted))
        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(8)
        item.layoutParams = params
        return item
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(2), 0, dp(2))
        }

    private fun rounded(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun addGap(height: Int, parent: LinearLayout = content) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
