package com.beycude.cevirgec

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

// ---------- Renkler ----------
object Renk {
    val ZEMIN = Color.parseColor("#0B1F33")
    val KART = Color.parseColor("#12304D")
    val KUTU = Color.parseColor("#1C4266")
    val VURGU = Color.parseColor("#F2B33D")
    val YAZI = Color.WHITE
    val SOLUK = Color.parseColor("#9FB6CC")
}

private val TR = Locale("tr", "TR")
private val TR_SEMBOL = DecimalFormatSymbols(TR)

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

// ---------- Sayı okuma / yazma ----------

/** Hem "1,5" hem "1.5" kabul eder. "1.234,5" gibi yazılırsa noktaları binlik sayar. */
fun sayiOku(s: String): Double? {
    var t = s.trim().replace(" ", "")
    if (t.isEmpty() || t == "-" || t == "," || t == ".") return null
    if (t.contains(',') && t.contains('.')) t = t.replace(".", "")
    t = t.replace(',', '.')
    return t.toDoubleOrNull()
}

/** Kutulara yazılan değer: virgüllü, binlik ayırıcısız (tekrar düzenlenebilsin diye). */
fun sayiYaz(v: Double): String {
    if (v.isNaN() || v.isInfinite()) return ""
    val a = abs(v)
    val ondalik = when {
        a == 0.0 -> 0
        a >= 1000 -> 2
        a >= 1 -> 4
        a >= 0.01 -> 6
        else -> 8
    }
    val df = DecimalFormat("0", TR_SEMBOL)
    df.maximumFractionDigits = ondalik
    df.isGroupingUsed = false
    return df.format(v)
}

/** Sonuç satırları: binlik ayırıcılı, sabit ondalık. */
fun sayiGoster(v: Double, ondalik: Int = 2): String {
    if (v.isNaN() || v.isInfinite()) return "—"
    val df = DecimalFormat("#,##0", TR_SEMBOL)
    df.minimumFractionDigits = ondalik
    df.maximumFractionDigits = ondalik
    return df.format(v)
}

fun tl(v: Double): String = sayiGoster(v, 2) + " ₺"

// ---------- Görsel parçalar ----------

fun EditText.degisince(f: () -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun afterTextChanged(s: Editable?) { f() }
    })
}

fun yuvarlak(renk: Int, yaricap: Float): GradientDrawable {
    val g = GradientDrawable()
    g.setColor(renk)
    g.cornerRadius = yaricap
    return g
}

fun sayiKutusu(ctx: Context, ipucu: String = ""): EditText {
    val e = EditText(ctx)
    e.inputType = InputType.TYPE_CLASS_NUMBER or
        InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
    e.keyListener = DigitsKeyListener.getInstance("0123456789,.-")
    e.setTextColor(Renk.YAZI)
    e.setHintTextColor(Renk.SOLUK)
    e.hint = ipucu
    e.textSize = 17f
    e.setSingleLine(true)
    e.gravity = Gravity.END or Gravity.CENTER_VERTICAL
    e.background = yuvarlak(Renk.KUTU, ctx.dp(8).toFloat())
    e.setPadding(ctx.dp(10), ctx.dp(8), ctx.dp(10), ctx.dp(8))
    e.setSelectAllOnFocus(true)
    return e
}

fun yazi(ctx: Context, metin: String, boyut: Float = 15f, renk: Int = Renk.YAZI, kalin: Boolean = false): TextView {
    val t = TextView(ctx)
    t.text = metin
    t.textSize = boyut
    t.setTextColor(renk)
    if (kalin) t.setTypeface(t.typeface, Typeface.BOLD)
    return t
}

/** Başlıklı kart oluşturur, ekrana ekler ve döndürür. */
fun kart(ctx: Context, ana: LinearLayout, baslik: String, aciklama: String? = null): LinearLayout {
    val k = LinearLayout(ctx)
    k.orientation = LinearLayout.VERTICAL
    k.background = yuvarlak(Renk.KART, ctx.dp(14).toFloat())
    k.setPadding(ctx.dp(14), ctx.dp(12), ctx.dp(14), ctx.dp(14))
    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    lp.setMargins(0, 0, 0, ctx.dp(12))
    k.layoutParams = lp
    k.addView(yazi(ctx, baslik, 18f, Renk.VURGU, true))
    if (aciklama != null) {
        val a = yazi(ctx, aciklama, 12.5f, Renk.SOLUK)
        a.setPadding(0, ctx.dp(2), 0, ctx.dp(4))
        k.addView(a)
    }
    ana.addView(k)
    return k
}

/** Etiket + kutu satırı. */
fun satir(ctx: Context, kart: LinearLayout, etiket: String, kutu: android.view.View) {
    val r = LinearLayout(ctx)
    r.orientation = LinearLayout.HORIZONTAL
    r.gravity = Gravity.CENTER_VERTICAL
    r.setPadding(0, ctx.dp(5), 0, 0)
    val e = yazi(ctx, etiket, 15f)
    r.addView(e, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
    r.addView(kutu, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.15f))
    kart.addView(r)
}

/** Etiket + sonuç yazısı satırı; sonuç TextView'ını döndürür. */
fun sonucSatiri(ctx: Context, kart: LinearLayout, etiket: String, buyuk: Boolean = false): TextView {
    val r = LinearLayout(ctx)
    r.orientation = LinearLayout.HORIZONTAL
    r.gravity = Gravity.CENTER_VERTICAL
    r.setPadding(0, ctx.dp(6), 0, 0)
    r.addView(yazi(ctx, etiket, 15f, Renk.SOLUK), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
    val v = yazi(ctx, "—", if (buyuk) 19f else 16f, if (buyuk) Renk.VURGU else Renk.YAZI, true)
    v.gravity = Gravity.END
    r.addView(v, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.15f))
    kart.addView(r)
    return v
}

/** Yan yana seçim düğmeleri. */
class Secici(ctx: Context, kart: LinearLayout, secenekler: List<String>, var secili: Int, val secince: (Int) -> Unit) {
    private val dugmeler = ArrayList<TextView>()
    private val c = ctx

    init {
        val r = LinearLayout(ctx)
        r.orientation = LinearLayout.HORIZONTAL
        r.setPadding(0, ctx.dp(6), 0, ctx.dp(2))
        for ((i, s) in secenekler.withIndex()) {
            val d = yazi(ctx, s, 13.5f, Renk.YAZI, true)
            d.gravity = Gravity.CENTER
            d.setPadding(ctx.dp(4), ctx.dp(8), ctx.dp(4), ctx.dp(8))
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            lp.setMargins(if (i == 0) 0 else ctx.dp(4), 0, 0, 0)
            d.setOnClickListener {
                secili = i
                boya()
                secince(i)
            }
            dugmeler.add(d)
            r.addView(d, lp)
        }
        kart.addView(r)
        boya()
    }

    private fun boya() {
        for ((i, d) in dugmeler.withIndex()) {
            if (i == secili) {
                d.background = yuvarlak(Renk.VURGU, c.dp(8).toFloat())
                d.setTextColor(Renk.ZEMIN)
            } else {
                d.background = yuvarlak(Renk.KUTU, c.dp(8).toFloat())
                d.setTextColor(Renk.YAZI)
            }
        }
    }
}

// ---------- Birim dönüştürücü ----------

class Birim(val ad: String, val tabana: (Double) -> Double, val tabandan: (Double) -> Double)

/** Taban birime göre çarpanla tanımlı birim: 1 [ad] = f taban. */
fun carpanli(ad: String, f: Double) = Birim(ad, { it * f }, { it / f })

/**
 * Bir gruptaki tüm birimleri alt alta gösterir; herhangi birine yazınca diğerleri anında güncellenir.
 */
class Donusturucu(
    private val ctx: Context,
    kart: LinearLayout,
    private val birimler: List<Birim>,
    private val tabanDegisti: ((Double?) -> Unit)? = null
) {
    private val kutular = ArrayList<EditText>()
    private var guncelleniyor = false
    private var sonKutu = -1

    init {
        for ((i, b) in birimler.withIndex()) {
            val k = sayiKutusu(ctx)
            k.degisince {
                if (!guncelleniyor) {
                    sonKutu = i
                    hesapla(i)
                }
            }
            kutular.add(k)
            satir(ctx, kart, b.ad, k)
        }
    }

    private fun hesapla(i: Int) {
        val v = sayiOku(kutular[i].text.toString())
        guncelleniyor = true
        if (v == null) {
            for (j in kutular.indices) if (j != i) kutular[j].setText("")
            tabanDegisti?.invoke(null)
        } else {
            val taban = birimler[i].tabana(v)
            for (j in kutular.indices) if (j != i) kutular[j].setText(sayiYaz(birimler[j].tabandan(taban)))
            tabanDegisti?.invoke(taban)
        }
        guncelleniyor = false
    }

    /** Dışarıdaki bir değer (ör. yoğunluk) değişince son yazılan kutudan yeniden hesapla. */
    fun yenile() {
        if (sonKutu >= 0) hesapla(sonKutu)
    }
}
