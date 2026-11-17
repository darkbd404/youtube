package com.asmaster.adblocker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val Emerald = Color(0xFF10B981)
val Navy = Color(0xFF0B1426)
val NavyCard = Color(0xFF14213D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

fun scheme(dark: Boolean): ColorScheme =
    if (dark) darkColorScheme(
        primary = Emerald, background = Navy, surface = NavyCard,
        onBackground = Color.White, onSurface = Color.White, onPrimary = Color.Black,
        surfaceVariant = NavyCard
    ) else lightColorScheme(
        primary = Color(0xFF059669), background = Color(0xFFF4F7FB), surface = Color.White,
        onBackground = Color(0xFF0B1426), onSurface = Color(0xFF0B1426), surfaceVariant = Color.White
    )

fun a11yEnabled(c: Context): Boolean {
    val v = Settings.Secure.getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    return v.split(":").any { it.contains(c.packageName + "/") && it.contains("SkipService") }
}

fun ytInstalled(c: Context): Boolean = try {
    c.packageManager.getPackageInfo(YT, 0); true
} catch (e: Exception) { false }

@Composable
fun App() {
    val ctx = LocalContext.current
    val p by remember { Repo.prefs(ctx) }.collectAsState(initial = Prefs())
    val stats by remember { Repo.stats(ctx) }.collectAsState(initial = emptyMap())
    var tab by remember { mutableIntStateOf(0) }
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }
    val lang = p.lang
    MaterialTheme(colorScheme = scheme(p.theme == "dark")) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val items: List<Pair<String, ImageVector>> = listOf(
                        "home" to Icons.Filled.Home, "stats" to Icons.Filled.DateRange,
                        "dns" to Icons.Filled.Lock, "settings" to Icons.Filled.Settings
                    )
                    items.forEachIndexed { i, (k, icon) ->
                        NavigationBarItem(
                            selected = tab == i, onClick = { tab = i },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(tr(lang, k), fontSize = 11.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black, indicatorColor = Emerald
                            )
                        )
                    }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                Crossfade(targetState = tab, animationSpec = tween(if (p.anim) 300 else 0), label = "tab") { t ->
                    when (t) {
                        0 -> HomeTab(p, stats, tick)
                        1 -> StatsTab(p, stats)
                        2 -> DnsTab(p, tick)
                        else -> SettingsTab(p, tick)
                    }
                }
            }
        }
    }
}

@Composable
fun Page(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) { content() }
}

@Composable
fun Panel(modifier: Modifier = Modifier, border: Boolean = false, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (border) BorderStroke(2.dp, Emerald) else null
    ) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() } }
}

@Composable
fun Shield(on: Boolean, anim: Boolean, sz: Dp = 130.dp) {
    val inf = rememberInfiniteTransition(label = "pulse")
    val sc by inf.animateFloat(
        initialValue = 1f, targetValue = if (anim && on) 1.08f else 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val col = if (on) Emerald else Color.Gray
    Canvas(Modifier.size(sz).scale(sc)) {
        val w = size.width
        val h = size.height
        val shield = Path().apply {
            moveTo(w * .5f, h * .04f)
            lineTo(w * .9f, h * .2f)
            lineTo(w * .9f, h * .5f)
            cubicTo(w * .9f, h * .75f, w * .7f, h * .9f, w * .5f, h * .97f)
            cubicTo(w * .3f, h * .9f, w * .1f, h * .75f, w * .1f, h * .5f)
            lineTo(w * .1f, h * .2f)
            close()
        }
        drawPath(shield, Brush.verticalGradient(listOf(col, col.copy(alpha = .45f))))
        drawPath(shield, Color.White.copy(alpha = .85f), style = Stroke(w * .03f))
        val check = Path().apply {
            moveTo(w * .33f, h * .5f); lineTo(w * .46f, h * .63f); lineTo(w * .68f, h * .37f)
        }
        drawPath(check, Color.White, style = Stroke(w * .07f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun Counter(v: Int, anim: Boolean, size: Int = 28) {
    val a by animateIntAsState(v, tween(if (anim) 800 else 0), label = "count")
    Text(a.toString(), fontSize = size.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
fun StatCard(title: String, v: Int, anim: Boolean, modifier: Modifier) {
    Panel(modifier) {
        Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
        Counter(v, anim)
    }
}

@Composable
fun HomeTab(p: Prefs, stats: Map<String, Int>, tick: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val lang = p.lang
    val a11y = remember(tick) { a11yEnabled(ctx) }
    val yt = remember(tick) { ytInstalled(ctx) }
    val active = a11y && p.auto
    Page {
        Panel(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Shield(active, p.anim)
                Spacer(Modifier.height(8.dp))
                Text("AS MASTER Ad Blocker", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    tr(lang, if (active) "prot_on" else if (!a11y) "prot_need" else "prot_off"),
                    color = if (active) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr(lang, "auto"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(tr(lang, if (p.auto) "auto_on" else "auto_off"), fontSize = 12.sp)
                }
                Switch(
                    checked = p.auto,
                    onCheckedChange = { scope.launch { Repo.put(ctx, K.AUTO, it) } },
                    modifier = Modifier.scale(1.5f).padding(end = 12.dp),
                    colors = SwitchDefaults.colors(checkedTrackColor = Emerald, checkedThumbColor = Color.White)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(tr(lang, "today"), stats[dayKey()] ?: 0, p.anim, Modifier.weight(1f))
            StatCard(tr(lang, "total"), stats.values.sum(), p.anim, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(tr(lang, "week"), stats.sumDays(7), p.anim, Modifier.weight(1f))
            StatCard(tr(lang, "month"), stats.sumDays(30), p.anim, Modifier.weight(1f))
        }
        Panel(Modifier.fillMaxWidth()) {
            Text(tr(lang, if (a11y) "a11y_on" else "a11y_off"), fontWeight = FontWeight.Bold,
                color = if (a11y) MaterialTheme.colorScheme.primary else Color(0xFFEF4444))
            Text(tr(lang, if (yt) "yt_ok" else "yt_no"))
            Text(tr(lang, "a11y_why"), fontSize = 12.sp)
            if (!a11y) {
                Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.Black)) {
                    Text(tr(lang, "a11y_btn"))
                }
            }
            OutlinedButton(onClick = { openYouTube(ctx) }, modifier = Modifier.fillMaxWidth()) {
                Text(tr(lang, "open_yt"))
            }
        }
    }
}

fun openYouTube(ctx: Context) {
    val i = ctx.packageManager.getLaunchIntentForPackage(YT)
        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$YT"))
    ctx.startActivity(i)
}

@Composable
fun StatsTab(p: Prefs, stats: Map<String, Int>) {
    val lang = p.lang
    val last7 = (6 downTo 0).map { dayKey(-it) }
    val max = (last7.maxOf { stats[it] ?: 0 }).coerceAtLeast(1)
    Page {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(tr(lang, "today"), stats[dayKey()] ?: 0, p.anim, Modifier.weight(1f))
            StatCard(tr(lang, "yesterday"), stats[dayKey(-1)] ?: 0, p.anim, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(tr(lang, "week"), stats.sumDays(7), p.anim, Modifier.weight(1f))
            StatCard(tr(lang, "month"), stats.sumDays(30), p.anim, Modifier.weight(1f))
        }
        StatCard(tr(lang, "total"), stats.values.sum(), p.anim, Modifier.fillMaxWidth())
        Panel(Modifier.fillMaxWidth()) {
            Text(tr(lang, "chart"), fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom) {
                last7.forEach { d ->
                    val n = stats[d] ?: 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Text(n.toString(), fontSize = 10.sp)
                        Box(Modifier.size(22.dp, (90f * n / max + 4f).dp)
                            .background(Emerald, RoundedCornerShape(6.dp)))
                        Text(d.takeLast(5), fontSize = 9.sp)
                    }
                }
            }
        }
        Panel(Modifier.fillMaxWidth()) {
            Text(tr(lang, "history"), fontWeight = FontWeight.Bold)
            val rows = stats.entries.filter { it.value > 0 }.sortedByDescending { it.key }.take(60)
            if (rows.isEmpty()) Text(tr(lang, "no_history"), fontSize = 12.sp)
            rows.forEach {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(it.key); Text(it.value.toString(), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

data class Host(val host: String, val en: String, val bn: String)

val providers: List<Pair<String, List<Host>>> = listOf(
    "AdGuard" to listOf(
        Host("dns.adguard-dns.com", "Blocks ads and trackers", "বিজ্ঞাপন ও ট্র্যাকার ব্লক করে"),
        Host("family.adguard-dns.com", "Ads, trackers + family protection", "বিজ্ঞাপন, ট্র্যাকার + ফ্যামিলি সুরক্ষা"),
        Host("unfiltered.adguard-dns.com", "No filtering", "কোনো ফিল্টার নেই")
    ),
    "Mullvad" to listOf(
        Host("adblock.dns.mullvad.net", "Ads and trackers", "বিজ্ঞাপন ও ট্র্যাকার"),
        Host("extended.dns.mullvad.net", "Extended blocking", "বর্ধিত ব্লকিং"),
        Host("all.dns.mullvad.net", "Maximum blocking", "সর্বোচ্চ ব্লকিং")
    ),
    "Cloudflare" to listOf(
        Host("one.one.one.one", "Fast, no filtering (1.1.1.1)", "দ্রুত, ফিল্টার নেই (1.1.1.1)"),
        Host("security.cloudflare-dns.com", "Malware blocking (1.1.1.2)", "ম্যালওয়্যার ব্লক (1.1.1.2)"),
        Host("family.cloudflare-dns.com", "Malware + adult blocking (1.1.1.3)", "ম্যালওয়্যার + অ্যাডাল্ট ব্লক (1.1.1.3)")
    )
)

val ipRegex = Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")

@Composable
fun DnsTab(p: Prefs, tick: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val clip = LocalClipboardManager.current
    val lang = p.lang
    var custom by remember(p.customDns) { mutableStateOf(p.customDns) }
    var bad by remember { mutableStateOf(false) }
    val current = remember(tick) {
        val mode = Settings.Global.getString(ctx.contentResolver, "private_dns_mode")
        val spec = Settings.Global.getString(ctx.contentResolver, "private_dns_specifier")
        when (mode) {
            "hostname" -> spec ?: "-"
            "opportunistic" -> tr(lang, "mode_auto")
            "off" -> tr(lang, "mode_off")
            else -> "-"
        }
    }
    fun copy(h: String) {
        clip.setText(AnnotatedString(h))
        Toast.makeText(ctx, tr(lang, "copied"), Toast.LENGTH_SHORT).show()
    }
    @Composable
    fun hostRow(h: Host) {
        val sel = p.dnsSel == h.host
        Panel(Modifier.fillMaxWidth(), border = sel) {
            Text(h.host, fontWeight = FontWeight.Bold)
            Text(if (lang == "bn") h.bn else h.en, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { copy(h.host) }) { Text(tr(lang, "copy")) }
                Button(
                    onClick = { scope.launch { Repo.put(ctx, K.DNS_SEL, h.host) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.Black)
                ) { Text(tr(lang, if (sel) "selected" else "select")) }
            }
        }
    }
    Page {
        Panel(Modifier.fillMaxWidth()) {
            Text(tr(lang, "cur_dns"), fontWeight = FontWeight.Bold)
            Text(current, color = MaterialTheme.colorScheme.primary)
            Text(tr(lang, "sel_dns") + ": " + (p.dnsSel.ifEmpty { tr(lang, "none") }), fontSize = 12.sp)
            Text(tr(lang, "dns_steps"), fontSize = 12.sp)
            Button(
                onClick = { ctx.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.Black)
            ) { Text(tr(lang, "open_dns")) }
        }
        providers.forEach { (name, hosts) ->
            Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            hosts.forEach { hostRow(it) }
        }
        Text("NextDNS", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Panel(Modifier.fillMaxWidth()) {
            Text(tr(lang, "nextdns"), fontSize = 12.sp)
            OutlinedTextField(
                value = custom, onValueChange = { custom = it; bad = false },
                label = { Text(tr(lang, "custom_host"), fontSize = 11.sp) },
                singleLine = true, isError = bad, modifier = Modifier.fillMaxWidth()
            )
            if (bad) Text(tr(lang, "invalid"), color = Color(0xFFEF4444), fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val h = custom.trim().lowercase()
                        if (h.contains(".") && !h.contains(" ") && !ipRegex.matches(h)) {
                            scope.launch { Repo.put(ctx, K.CUSTOM_DNS, h); Repo.put(ctx, K.DNS_SEL, h) }
                        } else bad = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.Black)
                ) { Text(tr(lang, "save")) }
                OutlinedButton(onClick = { if (custom.isNotBlank()) copy(custom.trim()) }) { Text(tr(lang, "copy")) }
            }
        }
        Panel(Modifier.fillMaxWidth()) {
            Text(tr(lang, "dns_restore"), fontSize = 12.sp)
            Text(tr(lang, "dns_priv"), fontSize = 12.sp)
            Text(tr(lang, "dns_note"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsTab(p: Prefs, tick: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val lang = p.lang
    var askReset by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    val a11y = remember(tick) { a11yEnabled(ctx) }
    @Composable
    fun choice(title: String, a: Pair<String, String>, b: Pair<String, String>, cur: String, set: (String) -> Unit) {
        Panel(Modifier.fillMaxWidth()) {
            Text(title, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(a, b).forEach { (v, label) ->
                    if (cur == v) Button(onClick = { set(v) },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.Black)) { Text(label) }
                    else OutlinedButton(onClick = { set(v) }) { Text(label) }
                }
            }
        }
    }
    Page {
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr(lang, "auto"), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(checked = p.auto, onCheckedChange = { scope.launch { Repo.put(ctx, K.AUTO, it) } },
                    colors = SwitchDefaults.colors(checkedTrackColor = Emerald, checkedThumbColor = Color.White))
            }
        }
        choice(tr(lang, "language"), "bn" to "বাংলা", "en" to "English", p.lang) { scope.launch { Repo.put(ctx, K.LANG, it) } }
        choice(tr(lang, "theme"), "dark" to tr(lang, "dark"), "light" to tr(lang, "light"), p.theme) { scope.launch { Repo.put(ctx, K.THEME, it) } }
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr(lang, "anim"), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(checked = p.anim, onCheckedChange = { scope.launch { Repo.put(ctx, K.ANIM, it) } },
                    colors = SwitchDefaults.colors(checkedTrackColor = Emerald, checkedThumbColor = Color.White))
            }
        }
        Panel(Modifier.fillMaxWidth()) {
            Text(tr(lang, if (a11y) "a11y_on" else "a11y_off"), fontWeight = FontWeight.Bold,
                color = if (a11y) MaterialTheme.colorScheme.primary else Color(0xFFEF4444))
            OutlinedButton(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()) { Text(tr(lang, "a11y_btn")) }
        }
        Button(onClick = { askReset = true }, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Text(tr(lang, "reset")) }
        OutlinedButton(onClick = { showPrivacy = true }, modifier = Modifier.fillMaxWidth()) { Text(tr(lang, "privacy")) }
        Panel(Modifier.fillMaxWidth()) {
            Text(tr(lang, "about"), fontWeight = FontWeight.Bold)
            Text(tr(lang, "about_text"), fontSize = 12.sp)
            Text(tr(lang, "version") + ": 1.0.0", fontSize = 12.sp)
        }
    }
    if (askReset) AlertDialog(
        onDismissRequest = { askReset = false },
        title = { Text(tr(lang, "reset")) }, text = { Text(tr(lang, "reset_q")) },
        confirmButton = { TextButton(onClick = { askReset = false; scope.launch { Repo.resetStats(ctx) } }) { Text(tr(lang, "yes")) } },
        dismissButton = { TextButton(onClick = { askReset = false }) { Text(tr(lang, "cancel")) } }
    )
    if (showPrivacy) AlertDialog(
        onDismissRequest = { showPrivacy = false },
        title = { Text(tr(lang, "privacy")) }, text = { Text(tr(lang, "privacy_text"), fontSize = 13.sp) },
        confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text(tr(lang, "close")) } }
    )
}
