package com.beycude.cevirgec

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.floor

class MainActivity : Activity() {

    private lateinit var ayar: SharedPreferences
    private val sekmeler = ArrayList<View>()
    private val sekmeDugmeleri = ArrayList<TextView>()

    companion object {
        const val TROY_ONS_G = 31.1034768
        const val ONS_G = 28.349523125
        const val US_GALON_L = 3.785411784
        const val ING_GALON_L = 4.54609
        const val VARIL_L = 158.987294928
        const val FT3_L = 28.316846592
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ayar = getSharedPreferences("cevirgec", Context.MODE_PRIVATE)

        val kok = LinearLayout(this)
        kok.orientation = LinearLayout.VERTICAL
        kok.setBackgroundColor(Renk.ZEMIN)

        // Üst başlık
        val ust = LinearLayout(this)
        ust.orientation = LinearLayout.HORIZONTAL
        ust.gravity = Gravity.CENTER_VERTICAL
        ust.setPadding(dp(16), dp(14), dp(16), dp(6))
        ust.addView(yazi(this, "⇄ ", 24f, Renk.VURGU, true))
        ust.addView(yazi(this, "Çevirgeç", 24f, Renk.YAZI, true))
        kok.addView(ust)

        // Sekme çubuğu
        val sekmeKaydir = HorizontalScrollView(this)
        sekmeKaydir.isHorizontalScrollBarEnabled = false
        val sekmeSira = LinearLayout(this)
        sekmeSira.orientation = LinearLayout.HORIZONTAL
        sekmeSira.setPadding(dp(12), dp(4), dp(12), dp(8))
        sekmeKaydir.addView(sekmeSira)
        kok.addView(sekmeKaydir)

        // İçerik
        val icerik = LinearLayout(this)
        icerik.orientation = LinearLayout.VERTICAL
        kok.addView(icerik, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        val adlar = listOf("Deniz", "Yakıt m³↔ton", "Altın", "Araç / Tekne", "Diğer")
        val kurucular = listOf<(LinearLayout) -> Unit>(
            { denizSekmesi(it) },
            { yakitSekmesi(it) },
            { altinSekmesi(it) },
            { aracSekmesi(it) },
            { digerSekmesi(it) }
        )

        for ((i, ad) in adlar.withIndex()) {
            val d = yazi(this, ad, 14.5f, Renk.YAZI, true)
            d.setPadding(dp(14), dp(8), dp(14), dp(8))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(dp(4), 0, dp(4), 0)
            d.setOnClickListener { sekmeSec(i) }
            sekmeDugmeleri.add(d)
            sekmeSira.addView(d, lp)

            val kaydir = ScrollView(this)
            kaydir.isFillViewport = true
            val ic = LinearLayout(this)
            ic.orientation = LinearLayout.VERTICAL
            ic.setPadding(dp(12), dp(4), dp(12), dp(24))
            kaydir.addView(ic)
            kurucular[i](ic)
            val alt = yazi(this, "Çevirgeç v1.0 • internetsiz çalışır, fiyatlar elle girilir", 12f, Renk.SOLUK)
            alt.gravity = Gravity.CENTER
            alt.setPadding(0, dp(8), 0, 0)
            ic.addView(alt)
            sekmeler.add(kaydir)
            icerik.addView(kaydir, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
        }

        setContentView(kok)
        sekmeSec(ayar.getInt("sekme", 0).coerceIn(0, adlar.size - 1))
    }

    private fun sekmeSec(i: Int) {
        for ((j, s) in sekmeler.withIndex()) s.visibility = if (j == i) View.VISIBLE else View.GONE
        for ((j, d) in sekmeDugmeleri.withIndex()) {
            if (j == i) {
                d.background = yuvarlak(Renk.VURGU, dp(20).toFloat())
                d.setTextColor(Renk.ZEMIN)
            } else {
                d.background = yuvarlak(Renk.KART, dp(20).toFloat())
                d.setTextColor(Renk.YAZI)
            }
        }
        ayar.edit().putInt("sekme", i).apply()
    }

    // ---------- Kayıtlı (hatırlanan) kutu ----------
    private fun kayitliKutu(anahtar: String, varsayilan: String = "", ipucu: String = ""): EditText {
        val k = sayiKutusu(this, ipucu)
        k.setText(ayar.getString(anahtar, varsayilan))
        k.degisince { ayar.edit().putString(anahtar, k.text.toString()).apply() }
        return k
    }

    private fun deger(k: EditText): Double? = sayiOku(k.text.toString())

    // =====================================================================
    // DENİZ
    // =====================================================================
    private fun denizSekmesi(ana: LinearLayout) {
        val c = this

        val uz = kart(c, ana, "Uzunluk / Mesafe",
            "1 deniz mili = 1852 m • 1 gomina = 1/10 mil = 185,2 m • 1 kulaç = 6 ft • 1 kilit (shackle) = 15 kulaç = 27,43 m")
        Donusturucu(c, uz, listOf(
            carpanli("Deniz mili (nm)", 1852.0),
            carpanli("Gomina (cable)", 185.2),
            carpanli("Kilit (shackle)", 27.432),
            carpanli("Kulaç (fathom)", 1.8288),
            carpanli("Metre", 1.0),
            carpanli("Kilometre", 1000.0),
            carpanli("Kara mili", 1609.344),
            carpanli("Yarda", 0.9144),
            carpanli("Feet (ft)", 0.3048),
            carpanli("İnç", 0.0254)
        ))

        val hz = kart(c, ana, "Hız", "1 knot = 1 deniz mili/saat = 1,852 km/s")
        lateinit var bofor: TextView
        Donusturucu(c, hz, listOf(
            carpanli("Knot (kn)", 1852.0 / 3600.0),
            carpanli("km/saat", 1.0 / 3.6),
            carpanli("m/saniye", 1.0),
            carpanli("Mil/saat (mph)", 0.44704),
            carpanli("ft/saniye", 0.3048)
        )) { ms ->
            bofor.text = if (ms == null) "—" else beaufort(ms * 3600.0 / 1852.0)
        }
        bofor = sonucSatiri(c, hz, "Rüzgâr olsaydı (Bofor)", true)

        val hc = kart(c, ana, "Hacim",
            "DİKKAT: ABD galonu (3,785 L) ile İngiliz galonu (4,546 L) farklıdır. Petrol varili = 159 L")
        Donusturucu(c, hc, listOf(
            carpanli("Litre", 1.0),
            carpanli("m³", 1000.0),
            carpanli("ABD galonu (US gal)", US_GALON_L),
            carpanli("İngiliz galonu (Imp gal)", ING_GALON_L),
            carpanli("Varil (bbl)", VARIL_L),
            carpanli("Kübik feet (ft³)", FT3_L)
        ))

        val ag = kart(c, ana, "Ağırlık",
            "Metrik ton = 1000 kg • Long ton (İngiliz) = 1016 kg • Short ton (ABD) = 907 kg")
        Donusturucu(c, ag, listOf(
            carpanli("Metrik ton (t)", 1000.0),
            carpanli("Long ton (LT)", 1016.0469088),
            carpanli("Short ton (ST)", 907.18474),
            carpanli("Kilogram", 1.0),
            carpanli("Libre (lb)", 0.45359237)
        ))

        draftKarti(ana)
    }

    private fun beaufort(kn: Double): String {
        val sinirlar = doubleArrayOf(1.0, 4.0, 7.0, 11.0, 17.0, 22.0, 28.0, 34.0, 41.0, 48.0, 56.0, 64.0)
        val adlar = arrayOf("Sakin", "Esinti", "Hafif rüzgâr", "Tatlı rüzgâr", "Mutedil rüzgâr",
            "Sert rüzgâr", "Kuvvetli rüzgâr", "Fırtınamsı", "Fırtına", "Kuvvetli fırtına",
            "Tam fırtına", "Çok şiddetli fırtına", "Kasırga")
        var b = 12
        for (i in sinirlar.indices) {
            if (kn < sinirlar[i]) { b = i; break }
        }
        return "$b — ${adlar[b]}"
    }

    private fun draftKarti(ana: LinearLayout) {
        val c = this
        val k = kart(c, ana, "Draft: feet + inç ↔ metre", "Draft markaları için: 12 ft 6 in gibi")
        val ft = sayiKutusu(c)
        val inc = sayiKutusu(c)
        val m = sayiKutusu(c)
        satir(c, k, "Feet", ft)
        satir(c, k, "İnç", inc)
        satir(c, k, "Metre", m)
        var mesgul = false
        val fiDegisti = {
            if (!mesgul) {
                val f = deger(ft)
                val i = deger(inc)
                mesgul = true
                if (f == null && i == null) m.setText("")
                else m.setText(sayiYaz(((f ?: 0.0) * 12.0 + (i ?: 0.0)) * 0.0254))
                mesgul = false
            }
        }
        ft.degisince(fiDegisti)
        inc.degisince(fiDegisti)
        m.degisince {
            if (!mesgul) {
                val v = deger(m)
                mesgul = true
                if (v == null) {
                    ft.setText(""); inc.setText("")
                } else {
                    var toplamInc = Math.round(v / 0.0254 * 100.0) / 100.0
                    val neg = toplamInc < 0
                    if (neg) toplamInc = -toplamInc
                    val f = floor(toplamInc / 12.0)
                    val i = Math.round((toplamInc - f * 12.0) * 100.0) / 100.0
                    ft.setText(sayiYaz(if (neg) -f else f))
                    inc.setText(sayiYaz(i))
                }
                mesgul = false
            }
        }
    }

    // =====================================================================
    // YAKIT m³ ↔ TON
    // =====================================================================
    private fun yakitSekmesi(ana: LinearLayout) {
        val c = this
        val yk = kart(c, ana, "Yoğunluk (kg/L = t/m³)",
            "Ölçüm sıcaklığındaki yoğunluğu girersen ton doğru çıkar. BDN'deki 15°C yoğunluğu kullanılırsa sıcaklık düzeltmesi (VCF) ayrıca yapılmalı.")
        val yogunluk = kayitliKutu("yogunluk", "0,85")
        satir(c, yk, "Yoğunluk", yogunluk)

        val hazir = listOf("MGO" to "0,85", "MDO" to "0,89", "IFO" to "0,96", "Benzin" to "0,74", "Motorin" to "0,835")
        Secici(c, yk, hazir.map { it.first }, -1) { i -> yogunluk.setText(hazir[i].second) }
        yk.addView(yazi(c, "MGO 0,85 • MDO 0,89 • IFO/HFO 0,94–0,99 • Benzin 0,74 • Motorin 0,835 (ortalama değerler)", 12f, Renk.SOLUK))

        fun d(): Double {
            val v = deger(yogunluk)
            return if (v == null || v <= 0.0) Double.NaN else v
        }

        val ck = kart(c, ana, "Hacim ↔ Ağırlık", "Herhangi birine yaz, diğerleri hesaplanır")
        val donus = Donusturucu(c, ck, listOf(
            carpanli("Litre", 1.0),
            carpanli("m³", 1000.0),
            carpanli("ABD galonu", US_GALON_L),
            carpanli("İngiliz galonu", ING_GALON_L),
            carpanli("Varil (bbl)", VARIL_L),
            Birim("Metrik ton", { it * 1000.0 / d() }, { it * d() / 1000.0 }),
            Birim("Kilogram", { it / d() }, { it * d() }),
            Birim("Long ton", { it * 1016.0469088 / d() }, { it * d() / 1016.0469088 })
        ))
        yogunluk.degisince { donus.yenile() }
    }

    // =====================================================================
    // ALTIN
    // =====================================================================
    private fun altinSekmesi(ana: LinearLayout) {
        val c = this
        val fk = kart(c, ana, "Fiyatlar (elle gir)", "Girdiğin değerler hatırlanır")
        val ons = kayitliKutu("ons_usd", "", "ör. 2650")
        val kur = kayitliKutu("usd_tl", "", "ör. 34,5")
        satir(c, fk, "Ons altın (USD)", ons)
        satir(c, fk, "Dolar kuru (₺)", kur)

        val sk = kart(c, ana, "Hesaplanan has değerler",
            "1 troy ons = 31,1035 g. Kuyumcu işçilik ve alış-satış farkı dahil değildir.")
        val gramUsd = sonucSatiri(c, sk, "Gram altın (USD)")
        val gram24 = sonucSatiri(c, sk, "Gram 24 ayar", true)
        val gram22 = sonucSatiri(c, sk, "Gram 22 ayar")
        val gram18 = sonucSatiri(c, sk, "Gram 18 ayar")
        val gram14 = sonucSatiri(c, sk, "Gram 14 ayar")
        val ceyrek = sonucSatiri(c, sk, "Çeyrek (1,75 g, 22 ayar)")
        val yarim = sonucSatiri(c, sk, "Yarım (3,50 g)")
        val tam = sonucSatiri(c, sk, "Tam (7,00 g)")
        val cumh = sonucSatiri(c, sk, "Cumhuriyet (7,216 g)")
        val kulce = sonucSatiri(c, sk, "1 kg külçe")

        val ek = kart(c, ana, "Elimdeki altın", "Gramı ve ayarı gir, değeri çıksın")
        val elGram = kayitliKutu("el_gram", "", "gram")
        satir(c, ek, "Ağırlık (g)", elGram)
        val ayarlar = listOf("24" to 0.995, "22" to 0.916, "18" to 0.750, "14" to 0.585)
        var ayarSec = ayar.getInt("el_ayar", 0).coerceIn(0, 3)
        lateinit var hesapla: () -> Unit
        Secici(c, ek, ayarlar.map { it.first + " ayar" }, ayarSec) { i ->
            ayarSec = i
            ayar.edit().putInt("el_ayar", i).apply()
            hesapla()
        }
        val elDeger = sonucSatiri(c, ek, "Değeri", true)
        val elHas = sonucSatiri(c, ek, "Has altın karşılığı")

        hesapla = {
            val o = deger(ons)
            val k = deger(kur)
            if (o == null || k == null) {
                for (t in listOf(gramUsd, gram24, gram22, gram18, gram14, ceyrek, yarim, tam, cumh, kulce, elDeger)) t.text = "—"
            } else {
                val g = o * k / TROY_ONS_G // has (24 ayar, 999,9) gram TL
                gramUsd.text = sayiGoster(o / TROY_ONS_G, 2) + " $"
                gram24.text = tl(g)
                gram22.text = tl(g * 0.916)
                gram18.text = tl(g * 0.750)
                gram14.text = tl(g * 0.585)
                ceyrek.text = tl(g * 1.75 * 0.916)
                yarim.text = tl(g * 3.50 * 0.916)
                tam.text = tl(g * 7.00 * 0.916)
                cumh.text = tl(g * 7.216 * 0.916)
                kulce.text = tl(g * 1000.0)
            }
            val eg = deger(elGram)
            if (eg == null) {
                elDeger.text = "—"; elHas.text = "—"
            } else {
                val has = eg * ayarlar[ayarSec].second
                elHas.text = sayiGoster(has, 3) + " g"
                elDeger.text = if (o == null || k == null) "—" else tl(has * o * k / TROY_ONS_G)
            }
        }
        ons.degisince { hesapla() }
        kur.degisince { hesapla() }
        elGram.degisince { hesapla() }
        hesapla()

        val dk = kart(c, ana, "Ağırlık çevirici",
            "Altında her zaman TROY ons kullanılır. Normal (avoirdupois) ons 28,35 g'dır, altında kullanılmaz.")
        Donusturucu(c, dk, listOf(
            carpanli("Troy ons (ozt)", TROY_ONS_G),
            carpanli("Gram", 1.0),
            carpanli("Kilogram", 1000.0),
            carpanli("Çeyrek adet (1,75 g)", 1.75),
            carpanli("Normal ons (oz)", ONS_G)
        ))
    }

    // =====================================================================
    // ARAÇ / TEKNE / GEMİ
    // =====================================================================
    private fun aracSekmesi(ana: LinearLayout) {
        val c = this
        val fk = kart(c, ana, "Akaryakıt fiyatları (₺/L)", "Elle gir, hatırlanır")
        val benzin = kayitliKutu("f_benzin", "", "₺/L")
        val motorin = kayitliKutu("f_motorin", "", "₺/L")
        val lpg = kayitliKutu("f_lpg", "", "₺/L")
        satir(c, fk, "Benzin", benzin)
        satir(c, fk, "Motorin (mazot)", motorin)
        satir(c, fk, "LPG", lpg)
        val fiyatKutulari = listOf(benzin, motorin, lpg)

        // --- Araç ---
        val ak = kart(c, ana, "Araç yolculuğu", "Ne kadar yakar, kaça mal olur")
        var aYakit = ayar.getInt("a_yakit", 1).coerceIn(0, 2)
        lateinit var aHesap: () -> Unit
        Secici(c, ak, listOf("Benzin", "Motorin", "LPG"), aYakit) { i ->
            aYakit = i; ayar.edit().putInt("a_yakit", i).apply(); aHesap()
        }
        val tuketim = kayitliKutu("a_tuketim", "", "ör. 6,5")
        val mesafe = kayitliKutu("a_mesafe", "", "km")
        val kisi = kayitliKutu("a_kisi", "1")
        satir(c, ak, "Tüketim (L/100 km)", tuketim)
        satir(c, ak, "Mesafe (km)", mesafe)
        var gidisDonus = ayar.getBoolean("a_gd", false)
        Secici(c, ak, listOf("Tek yön", "Gidiş-dönüş"), if (gidisDonus) 1 else 0) { i ->
            gidisDonus = i == 1; ayar.edit().putBoolean("a_gd", gidisDonus).apply(); aHesap()
        }
        satir(c, ak, "Kişi sayısı", kisi)
        val aLitre = sonucSatiri(c, ak, "Yakıt")
        val aTutar = sonucSatiri(c, ak, "Tutar", true)
        val aKisi = sonucSatiri(c, ak, "Kişi başı")
        val aKm = sonucSatiri(c, ak, "Km başı")

        aHesap = {
            val t = deger(tuketim)
            var km = deger(mesafe)
            val f = deger(fiyatKutulari[aYakit])
            val n = deger(kisi)
            if (km != null && gidisDonus) km *= 2.0
            if (t == null || km == null) {
                aLitre.text = "—"; aTutar.text = "—"; aKisi.text = "—"; aKm.text = "—"
            } else {
                val l = t * km / 100.0
                aLitre.text = sayiGoster(l, 2) + " L  (" + sayiGoster(km, 0) + " km)"
                if (f == null) {
                    aTutar.text = "fiyat gir"; aKisi.text = "—"; aKm.text = "—"
                } else {
                    val tutar = l * f
                    aTutar.text = tl(tutar)
                    aKisi.text = if (n != null && n >= 1.0) tl(tutar / n) else "—"
                    aKm.text = if (km > 0) tl(tutar / km) else "—"
                }
            }
        }
        for (k in listOf(tuketim, mesafe, kisi, benzin, motorin, lpg)) k.degisince { aHesap() }
        aHesap()

        // --- Tekne ---
        val tk = kart(c, ana, "Tekne seferi",
            "Süreyi doğrudan gir ya da mesafe (nm) ve hız (knot) gir, süre hesaplansın")
        var tYakit = ayar.getInt("t_yakit", 1).coerceIn(0, 1)
        lateinit var tHesap: () -> Unit
        Secici(c, tk, listOf("Benzin", "Motorin"), tYakit) { i ->
            tYakit = i; ayar.edit().putInt("t_yakit", i).apply(); tHesap()
        }
        val lSaat = kayitliKutu("t_lsaat", "", "L/saat")
        val sure = kayitliKutu("t_sure", "", "saat")
        val nm = kayitliKutu("t_nm", "", "nm")
        val kn = kayitliKutu("t_kn", "", "knot")
        satir(c, tk, "Tüketim (L/saat)", lSaat)
        satir(c, tk, "Süre (saat)", sure)
        satir(c, tk, "veya Mesafe (nm)", nm)
        satir(c, tk, "ve Hız (knot)", kn)
        val tSure = sonucSatiri(c, tk, "Seyir süresi")
        val tLitre = sonucSatiri(c, tk, "Yakıt")
        val tTutar = sonucSatiri(c, tk, "Tutar", true)
        val tMil = sonucSatiri(c, tk, "Mil başı")

        tHesap = {
            val ls = deger(lSaat)
            val m = deger(nm)
            val h = deger(kn)
            val s: Double? = if (m != null && h != null && h > 0) m / h else deger(sure)
            val f = deger(fiyatKutulari[tYakit])
            tSure.text = if (s == null) "—" else saatYaz(s)
            if (ls == null || s == null) {
                tLitre.text = "—"; tTutar.text = "—"; tMil.text = "—"
            } else {
                val l = ls * s
                tLitre.text = sayiGoster(l, 1) + " L"
                if (f == null) {
                    tTutar.text = "fiyat gir"; tMil.text = "—"
                } else {
                    tTutar.text = tl(l * f)
                    tMil.text = if (m != null && m > 0 && h != null && h > 0) tl(l * f / m) else "—"
                }
            }
        }
        for (k in listOf(lSaat, sure, nm, kn, benzin, motorin)) k.degisince { tHesap() }
        tHesap()

        // --- Gemi ---
        val gk = kart(c, ana, "Gemi seferi (ton/gün)",
            "Günlük tüketim tonla. Gün sayısını gir ya da mesafe ve hızdan hesaplansın")
        val tGun = kayitliKutu("g_tgun", "", "t/gün")
        val gun = kayitliKutu("g_gun", "", "gün")
        val gNm = kayitliKutu("g_nm", "", "nm")
        val gKn = kayitliKutu("g_kn", "", "knot")
        val tonFiyat = kayitliKutu("g_tonusd", "", "USD/t")
        val gKur = kayitliKutu("usd_tl", "", "₺")
        satir(c, gk, "Tüketim (t/gün)", tGun)
        satir(c, gk, "Süre (gün)", gun)
        satir(c, gk, "veya Mesafe (nm)", gNm)
        satir(c, gk, "ve Hız (knot)", gKn)
        satir(c, gk, "Yakıt fiyatı (USD/t)", tonFiyat)
        satir(c, gk, "Dolar kuru (₺)", gKur)
        val gSure = sonucSatiri(c, gk, "Seyir süresi")
        val gTon = sonucSatiri(c, gk, "Yakıt")
        val gUsd = sonucSatiri(c, gk, "Tutar (USD)", true)
        val gTl = sonucSatiri(c, gk, "Tutar (₺)")

        val gHesap = {
            val tg = deger(tGun)
            val m = deger(gNm)
            val h = deger(gKn)
            val g: Double? = if (m != null && h != null && h > 0) m / h / 24.0 else deger(gun)
            val fp = deger(tonFiyat)
            val kr = deger(gKur)
            gSure.text = if (g == null) "—" else sayiGoster(g, 2) + " gün (" + saatYaz(g * 24.0) + ")"
            if (tg == null || g == null) {
                gTon.text = "—"; gUsd.text = "—"; gTl.text = "—"
            } else {
                val ton = tg * g
                gTon.text = sayiGoster(ton, 2) + " t"
                if (fp == null) {
                    gUsd.text = "fiyat gir"; gTl.text = "—"
                } else {
                    gUsd.text = sayiGoster(ton * fp, 2) + " $"
                    gTl.text = if (kr == null) "—" else tl(ton * fp * kr)
                }
            }
        }
        for (k in listOf(tGun, gun, gNm, gKn, tonFiyat, gKur)) k.degisince { gHesap() }
        gHesap()
    }

    private fun saatYaz(saat: Double): String {
        if (saat.isNaN() || saat.isInfinite() || saat < 0) return "—"
        val toplamDk = Math.round(saat * 60.0)
        val s = toplamDk / 60
        val d = toplamDk % 60
        return "$s sa $d dk"
    }

    // =====================================================================
    // DİĞER
    // =====================================================================
    private fun digerSekmesi(ana: LinearLayout) {
        val c = this

        val bs = kart(c, ana, "Basınç", "Lastik, kompresör, hidrolik… 1 bar ≈ 14,5 psi")
        Donusturucu(c, bs, listOf(
            carpanli("bar", 1.0),
            carpanli("psi", 0.0689475729),
            carpanli("kg/cm²", 0.980665),
            carpanli("atm", 1.01325),
            carpanli("kPa", 0.01),
            carpanli("MPa", 10.0),
            carpanli("hPa / mbar", 0.001)
        ))

        val sc = kart(c, ana, "Sıcaklık")
        Donusturucu(c, sc, listOf(
            Birim("°C (Santigrat)", { it }, { it }),
            Birim("°F (Fahrenhayt)", { (it - 32.0) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32.0 }),
            Birim("K (Kelvin)", { it - 273.15 }, { it + 273.15 })
        ))

        val gc = kart(c, ana, "Motor gücü",
            "HP (İngiliz beygir) = 0,7457 kW • PS / BG (metrik beygir) = 0,7355 kW")
        Donusturucu(c, gc, listOf(
            carpanli("kW", 1.0),
            carpanli("HP (bhp)", 0.745699872),
            carpanli("PS / BG (metrik)", 0.73549875)
        ))

        val al = kart(c, ana, "Alan")
        Donusturucu(c, al, listOf(
            carpanli("m²", 1.0),
            carpanli("ft²", 0.09290304),
            carpanli("Dönüm", 1000.0),
            carpanli("Hektar", 10000.0)
        ))
    }
}
