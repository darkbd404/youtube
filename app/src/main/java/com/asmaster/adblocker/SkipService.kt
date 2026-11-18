package com.asmaster.adblocker

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Looks ONLY at the official YouTube app window, finds a visible, enabled,
 * clickable "Skip Ads" control and taps it. Nothing is read outside YouTube,
 * stored, or sent anywhere.
 */
class SkipService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Volatile private var enabled = true
    private var lastClick = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        scope.launch { Repo.prefs(applicationContext).collect { enabled = it.auto } }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!enabled || event == null) return
        if (event.packageName?.toString() != YT) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastClick < COOLDOWN_MS) return
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != YT) return
        val node = SkipDetector.find(root) ?: return
        if (SkipDetector.click(node)) {
            lastClick = now
            scope.launch { Repo.recordSkip(applicationContext) }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object { private const val COOLDOWN_MS = 1500L }
}

object SkipDetector {
    private val viewIds = listOf(
        "com.google.android.youtube:id/skip_ad_button",
        "com.google.android.youtube:id/skip_ad_button_container"
    )

    fun find(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (id in viewIds) {
            val list = root.findAccessibilityNodeInfosByViewId(id)
            list?.firstOrNull { usable(it) }?.let { return it }
        }
        return search(root, 0)
    }

    private fun search(n: AccessibilityNodeInfo?, depth: Int): AccessibilityNodeInfo? {
        if (n == null || depth > 30) return null
        if (usable(n) && (isSkip(n.text?.toString()) || isSkip(n.contentDescription?.toString()))) return n
        for (i in 0 until n.childCount) {
            search(n.getChild(i), depth + 1)?.let { return it }
        }
        return null
    }

    private fun usable(n: AccessibilityNodeInfo) = n.isVisibleToUser && n.isEnabled

    /** Accepts "Skip", "Skip Ad(s)", Bangla equivalents. Rejects countdowns like "Skip ad in 5". */
    fun isSkip(s: String?): Boolean {
        val x = s?.trim()?.lowercase() ?: return false
        if (x.isEmpty() || x.length > 40) return false
        if (x.any { Character.isDigit(it) }) return false
        if (x == "skip" || x == "skip ad" || x == "skip ads" || x == "skip ad ›") return true
        if (x.startsWith("skip ad")) return true
        if (x.contains("বিজ্ঞাপন") && (x.contains("এড়িয়ে") || x.contains("স্কিপ"))) return true
        return x == "স্কিপ" || x == "এড়িয়ে যান"
    }

    /** Clicks the node or the nearest clickable parent; true only if Android accepted the click. */
    fun click(node: AccessibilityNodeInfo): Boolean {
        var n: AccessibilityNodeInfo? = node
        var i = 0
        while (n != null && i < 6) {
            if (n.isClickable && n.isEnabled) return n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            n = n.parent
            i++
        }
        return false
    }
}
