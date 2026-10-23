package ir.mehrineh.app

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.InputType
import android.view.*
import android.graphics.Outline
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.Uri
import android.graphics.drawable.Drawable
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var root: LinearLayout
    private lateinit var header: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var bottom: LinearLayout
    private lateinit var scroll: ScrollView
    private val prefs by lazy { getSharedPreferences("mehrineh_v4", MODE_PRIVATE) }
    private var page = "home"
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var voiceMode = false
    private var accent = Color.rgb(239, 47, 104)
    private val navy = Color.rgb(19, 29, 65)
    private val muted = Color.rgb(108, 115, 136)
    private val bg = Color.rgb(252, 249, 253)
    private val white = Color.WHITE
    private val themes = intArrayOf(0xFFEE3368.toInt(), 0xFF2786F5.toInt(), 0xFF12A76C.toInt(), 0xFF8747D7.toInt())
    private val names = mapOf("home" to "مهرینه", "family" to "تماس با خانواده", "sos" to "درخواست کمک", "meds" to "یادآوری دارو", "shop" to "خرید اینترنتی", "assistant" to "دستیار هوشمند", "learn" to "آموزش و یادگیری", "mood" to "اعلام حال", "calendar" to "تقویم و برنامه‌ها", "settings" to "تنظیمات", "apps" to "برنامه‌های مفید")
    private var scale = 1f
    private var selectedMood = ""
    private var introShown = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = white
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        setContentView(R.layout.activity_main)
        root = findViewById(R.id.root); header = findViewById(R.id.header)
        content = findViewById(R.id.content); bottom = findViewById(R.id.bottom); scroll = findViewById(R.id.scroll)
        tts = TextToSpeech(this, this)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 90)
        navigate("home")
        if (!introShown) { introShown = true; root.alpha=0f; root.translationY=dp(14).toFloat(); root.animate().alpha(1f).translationY(0f).setDuration(650).start() }
    }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) { val result = tts?.setLanguage(Locale("fa", "IR")); ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED } }
    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun shape(color: Int, radius: Int = 18, stroke: Int = 0, border: Int = 0): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); if (stroke > 0) setStroke(dp(stroke), border) }
    private fun text(s: String, size: Float = 16f, bold: Boolean = false, color: Int = navy): TextView = TextView(this).apply {
        this.text = s; textSize = size * scale; setTextColor(color); gravity = Gravity.CENTER_VERTICAL; textDirection = View.TEXT_DIRECTION_FIRST_STRONG
        if (bold) setTypeface(null, Typeface.BOLD)
        includeFontPadding = true
    }
    private fun linear(vertical: Boolean = true): LinearLayout = LinearLayout(this).apply { orientation = if (vertical) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL; layoutDirection = View.LAYOUT_DIRECTION_RTL }
    private fun pad(v: View, h: Int = 14, t: Int = 14, b: Int = 14) { v.setPadding(dp(h), dp(t), dp(h), dp(b)) }
    private fun lp(w: Int = -1, h: Int = -2, margin: Int = 0) = LinearLayout.LayoutParams(if (w < 0) w else dp(w), if (h < 0) h else dp(h)).apply { if (margin > 0) setMargins(dp(margin),dp(margin),dp(margin),dp(margin)) }
    private fun gap(n: Int = 10) { content.addView(View(this), lp(1,n)) }
    private fun addTitle(s: String) { content.addView(text(s, 21f, true), lp(-1,-2).apply { topMargin=dp(15); bottomMargin=dp(10) }) }
    private fun icon(name: String, color: Int = navy, size: Int = 25): ImageView = ImageView(this).apply {
        val id = resources.getIdentifier("ic_$name", "drawable", packageName)
        if (id != 0) setImageResource(id)
        imageTintList = android.content.res.ColorStateList.valueOf(color)
        layoutParams = lp(size,size)
        contentDescription = name
    }
    private fun avatar(name: String, size: Int = 64): ImageView = ImageView(this).apply {
        val id = resources.getIdentifier(name, "drawable", packageName)
        if (id != 0) setImageResource(id)
        scaleType = ImageView.ScaleType.CENTER_CROP
        background = shape(0xFFF2E9F0.toInt(), 100)
        outlineProvider = object: ViewOutlineProvider() { override fun getOutline(view: View, outline: Outline) { outline.setOval(0,0,view.width,view.height) } }
        clipToOutline = true
        layoutParams = lp(size,size)
    }
    private fun card(color: Int = white, radius: Int = 20): LinearLayout = linear().apply { background = shape(color,radius); pad(this); elevation=dp(2).toFloat() }
    private fun button(label: String, color: Int = accent, action: () -> Unit): TextView = text(label, 15f,true,white).apply {
        gravity = Gravity.CENTER; background = shape(color, 22); pad(this,18,13,13); setOnClickListener { action() }
    }
    private fun section(label: String, iconName: String, action: () -> Unit): LinearLayout = card().apply {
        orientation = LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
        addView(icon(iconName,accent,27)); addView(text(label,17f,true), LinearLayout.LayoutParams(0,-2,1f).apply { marginStart=dp(12) })
        addView(icon("arrow",muted,20)); setOnClickListener { action() }
    }
    private fun rowCard(label: String, detail: String, iconName: String, tint: Int, action: () -> Unit): LinearLayout = card().apply {
        orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
        val holder = linear(); holder.gravity=Gravity.CENTER; holder.background=shape(tint and 0x00FFFFFF or 0x22000000,14); holder.layoutParams=lp(52,52)
        holder.addView(icon(iconName,tint,29)); addView(holder)
        val col=linear(); col.addView(text(label,17f,true)); col.addView(text(detail,13f,false,muted))
        addView(col,LinearLayout.LayoutParams(0,-2,1f).apply { marginStart=dp(12) }); addView(icon("arrow",muted,18)); setOnClickListener { action() }
    }
    private fun navigate(next: String) {
        page=next; accent=themes[prefs.getInt("theme",0).coerceIn(0,3)]; scale=(prefs.getInt("font_percent",100).coerceIn(85,145))/100f
        header.removeAllViews(); content.removeAllViews(); bottom.removeAllViews(); scroll.scrollTo(0,0)
        if (next=="home") {
            val brand=linear(); brand.addView(text("⌂",32f,true,accent)); brand.addView(text(" مهرینه",27f,true,navy))
            header.addView(brand,LinearLayout.LayoutParams(0,-2,1f)); val alert=icon("bell",navy,27); header.addView(alert); alert.setOnClickListener { navigate("calendar") }
        } else {
            val back=icon("arrow",navy,26); back.rotation=180f; header.addView(back); back.setOnClickListener { navigate("home") }
            val title=text(names[next] ?: "مهرینه",21f,true); title.gravity=Gravity.CENTER
            header.addView(title,LinearLayout.LayoutParams(0,-2,1f)); val home=icon("home",accent,24); header.addView(home); home.setOnClickListener { navigate("home") }
        }
        when(next) { "home"->home(); "family"->family(); "sos"->sos(); "meds"->meds(); "shop"->shop(); "assistant"->assistant(); "learn"->learn(); "mood"->mood(); "calendar"->calendar(); "settings"->settingsPage(); "apps"->apps() }
        bottomNav()
    }
    private fun bottomNav() {
        val items=listOf(Triple("home","خانه","home"),Triple("mood","اعلام حال","heart"),Triple("apps","بیشتر","grid"),Triple("settings","تنظیمات","settings"))
        for ((dest,label,ic) in items) {
            val col=linear(); col.gravity=Gravity.CENTER
            col.addView(icon(ic,if(page==dest) accent else muted,24))
            val t=text(label,11f,page==dest,if(page==dest)accent else muted); t.gravity=Gravity.CENTER; col.addView(t)
            bottom.addView(col,LinearLayout.LayoutParams(0,-1,1f)); col.setOnClickListener { navigate(dest) }
        }
    }
    private fun home() {
        val hero=card(0xFFFFF1F5.toInt(),24); hero.orientation=LinearLayout.HORIZONTAL; hero.gravity=Gravity.CENTER_VERTICAL
        val words=linear(); words.addView(text("سلام مادر عزیزم",23f,true)); words.addView(text("امروز حالتان چطور است؟",15f,false,muted)); words.addView(text("روز خوبی داشته باشید",12f,false,accent))
        hero.addView(words,LinearLayout.LayoutParams(0,-2,1f)); hero.addView(avatar("hero",105)); content.addView(hero); gap(12)
        val tiles=listOf(Triple("تماس با خانواده","phone",0xFF0BAF69.toInt()),Triple("درخواست کمک","shield",0xFFEF3854.toInt()),Triple("یادآوری دارو","pill",0xFF287EF0.toInt()),Triple("خرید اینترنتی","cart",0xFFFF941C.toInt()))
        val dest=listOf("family","sos","meds","shop")
        for (i in 0..1) {
            val r=linear(false)
            for(j in 0..1) {
                val k=i*2+j; val tile=linear(); tile.gravity=Gravity.CENTER; tile.background=shape(tiles[k].third,22); pad(tile,8,20,20)
                val img=icon(tiles[k].second,white,37); tile.addView(img)
                val title=text(tiles[k].first,17f,true,white); title.gravity=Gravity.CENTER; tile.addView(title)
                val sub=text(listOf("تماس سریع با عزیزان","اطلاع‌رسانی به خانواده","مدیریت داروها","تمرین خرید امن")[k],12f,false,white); sub.gravity=Gravity.CENTER; tile.addView(sub)
                r.addView(tile,LinearLayout.LayoutParams(0,dp(147),1f).apply { setMargins(dp(4),dp(4),dp(4),dp(4)) }); tile.setOnClickListener { navigate(dest[k]) }
            }
            content.addView(r)
        }
        gap(7)
        val shortcuts=listOf(Triple("آموزش","book","learn"),Triple("تقویم","calendar","calendar"),Triple("دستیار","chat","assistant"),Triple("برنامه‌ها","grid","apps"))
        val colors=listOf(0xFFF1E5FF.toInt(),0xFFFFE9EC.toInt(),0xFFE2F6EC.toInt(),0xFFEAE7FF.toInt())
        val r=linear(false)
        shortcuts.forEachIndexed { i, t ->
            val box=linear(); box.gravity=Gravity.CENTER; box.background=shape(colors[i],18); pad(box,2,14,14)
            box.addView(icon(t.second,accent,29)); val title=text(t.first,13f,true); title.gravity=Gravity.CENTER; box.addView(title)
            r.addView(box,LinearLayout.LayoutParams(0,dp(100),1f).apply { setMargins(dp(3),dp(3),dp(3),dp(3)) }); box.setOnClickListener { navigate(t.third) }
        }; content.addView(r)
        addTitle("اعضای خانواده")
        val people=linear(false)
        listOf("avatar_daughter" to "دخترم","avatar_son" to "پسرم","avatar_daughterinlaw" to "عروسم","avatar_spouse" to "همسرم").forEach { (img,n) ->
            val box=linear(); box.gravity=Gravity.CENTER; box.addView(avatar(img,58)); val t=text(n,12f,true); t.gravity=Gravity.CENTER; box.addView(t)
            people.addView(box,LinearLayout.LayoutParams(0,-2,1f)); box.setOnClickListener { navigate("family") }
        }; content.addView(people)
        gap(); content.addView(section("ثبت حال امروز", "heart") { navigate("mood") })
    }
    private fun family() {
        val people=listOf(Triple("دخترم","avatar_daughter","daughter"),Triple("پسرم","avatar_son","son"),Triple("عروسم","avatar_daughterinlaw","daughterinlaw"),Triple("همسرم","avatar_spouse","spouse"))
        content.addView(text("برای تماس، یکی از عزیزانتان را انتخاب کنید.",15f,false,muted)); gap()
        people.forEach { (n,img,key) ->
            val box=card(); box.orientation=LinearLayout.HORIZONTAL; box.gravity=Gravity.CENTER_VERTICAL
            box.addView(avatar(img,68)); val col=linear(); col.addView(text(n,19f,true)); col.addView(text("تماس آسان و سریع",13f,false,muted))
            box.addView(col,LinearLayout.LayoutParams(0,-2,1f).apply { marginStart=dp(14) })
            val call=button("تماس",0xFF0BAF69.toInt()) { dial(prefs.getString("number_$key","") ?: "") }; box.addView(call)
            content.addView(box); gap(10)
        }
        content.addView(section("ثبت یا ویرایش شماره اعضای خانواده", "edit") { editNumbers() })
    }
    private fun editNumbers() {
        val keys=arrayOf("daughter","son","daughterinlaw","spouse"); val labels=arrayOf("دخترم","پسرم","عروسم","همسرم")
        val wrapper=linear(); pad(wrapper)
        val edits=keys.mapIndexed { i,k -> EditText(this).apply { hint="شماره ${labels[i]}"; setText(prefs.getString("number_$k","")); inputType=InputType.TYPE_CLASS_PHONE; textSize=17f; wrapper.addView(this,lp(-1,55)) } }
        AlertDialog.Builder(this).setTitle("شماره‌های تماس").setView(wrapper).setPositiveButton("ذخیره") { _,_ -> keys.forEachIndexed { i,k -> prefs.edit().putString("number_$k",edits[i].text.toString()).apply() }; toast("شماره‌ها ذخیره شدند") }.setNegativeButton("انصراف",null).show()
    }
    private fun dial(number: String) { if(number.isBlank()) { toast("ابتدا شماره تماس را در بخش ویرایش وارد کنید"); return }; startActivity(Intent(Intent.ACTION_DIAL,android.net.Uri.parse("tel:${android.net.Uri.encode(number)}"))) }
    private fun sos() {
        gap(28)
        val circle=text("SOS",38f,true,white); circle.gravity=Gravity.CENTER; circle.background=shape(0xFFEF3158.toInt(),100)
        val wrap=linear(); wrap.gravity=Gravity.CENTER; wrap.addView(circle,lp(190,190)); content.addView(wrap)
        gap(22); val info=text("با یک لمس، درخواست کمک ثبت کنید",19f,true); info.gravity=Gravity.CENTER; content.addView(info); gap()
        content.addView(rowCard("اطلاع‌رسانی به خانواده","این نسخه درخواست را روی همین دستگاه ثبت می‌کند","heart",accent) { help() }); gap()
        content.addView(button("ثبت درخواست کمک") { help() }); gap()
        content.addView(section("تماس با خانواده", "phone") { navigate("family") })
        gap(); content.addView(text("توجه: ارسال واقعی درخواست به گوشی خانواده نیازمند اتصال حساب‌ها و سرویس اعلان است. برای شرایط اضطراری از شماره خدمات اضطراری محل زندگی استفاده کنید.",13f,false,muted))
    }
    private fun help() { prefs.edit().putString("last_help",Date().toString()).apply(); AlertDialog.Builder(this).setTitle("درخواست ثبت شد").setMessage("درخواست کمک در این گوشی ثبت شد. ارسال خودکار به گوشی اعضای خانواده در نسخه آزمایشی فعال نیست. آیا می‌خواهید با یکی از اعضای خانواده تماس بگیرید؟").setPositiveButton("تماس") { _,_->navigate("family") }.setNegativeButton("بستن",null).show() }
    private fun meds() {
        addTitle("داروهای امروز")
        val meds=listOf(Triple("قرص فشار خون","۸:۰۰ صبح","pill"),Triple("قرص دیابت","۱۳:۰۰ ظهر","pill"),Triple("مولتی‌ویتامین","۲۰:۰۰ شب","pill"))
        meds.forEachIndexed { i, (n,time,ic) ->
            val key="med_$i"; val done=prefs.getBoolean(key,false)
            val box=card(); box.orientation=LinearLayout.HORIZONTAL; box.gravity=Gravity.CENTER_VERTICAL; box.addView(icon(ic,if(done)0xFF0AA66B.toInt() else accent,31))
            val col=linear(); col.addView(text(n,17f,true)); col.addView(text(time,13f,false,muted)); box.addView(col,LinearLayout.LayoutParams(0,-2,1f).apply { marginStart=dp(12) })
            val cb=CheckBox(this); cb.isChecked=done; cb.buttonTintList=android.content.res.ColorStateList.valueOf(accent); cb.setOnCheckedChangeListener { _,v -> prefs.edit().putBoolean(key,v).apply() }; box.addView(cb)
            content.addView(box); gap(10)
        }
        content.addView(button("افزودن یادآوری دارو") { reminderDialog("دارو") }); gap()
        content.addView(section("شنیدن برنامه دارویی", "volume") { speak("برنامه دارویی امروز: قرص فشار خون ساعت هشت صبح، قرص دیابت ساعت یک بعد از ظهر، مولتی ویتامین ساعت هشت شب") })
    }
    private fun calendar() {
        val now=SimpleDateFormat("EEEE d MMMM yyyy",Locale("fa","IR")).format(Date())
        val top=card(0xFFF0ECFF.toInt()); top.addView(text("امروز",17f,true)); top.addView(text(now,19f,true,accent)); content.addView(top)
        addTitle("یادآوری‌ها و برنامه‌ها")
        val stored=prefs.getString("reminders","") ?: ""
        val lines=stored.split("\n").filter { it.isNotBlank() }
        if(lines.isEmpty()) content.addView(text("هنوز یادآوری شخصی ثبت نکرده‌اید.",15f,false,muted))
        lines.forEach { item -> content.addView(rowCard(item,"ثبت‌شده در این دستگاه","calendar",accent) { speak(item) }); gap(8) }
        gap(); content.addView(button("افزودن یادآوری جدید") { reminderDialog("یادآوری") }); gap()
        content.addView(section("خواندن یادآوری‌ها", "volume") { speak(if(lines.isEmpty())"هنوز یادآوری ندارید" else lines.joinToString(". ")) })
    }
    private fun reminderDialog(kind: String) {
        val wrapper=linear(); pad(wrapper)
        val entry=EditText(this).apply { hint="متن $kind"; textSize=17f; setSingleLine(false) }
        val hours=EditText(this).apply { hint="ساعت (۰ تا ۲۳)"; inputType=InputType.TYPE_CLASS_NUMBER; setText("9") }
        val minutes=EditText(this).apply { hint="دقیقه (۰ تا ۵۹)"; inputType=InputType.TYPE_CLASS_NUMBER; setText("0") }
        wrapper.addView(entry,lp(-1,60));wrapper.addView(hours,lp(-1,55));wrapper.addView(minutes,lp(-1,55))
        AlertDialog.Builder(this).setTitle("یادآوری جدید").setView(wrapper).setPositiveButton("ذخیره") { _,_->
            val msg=entry.text.toString().trim(); val h=hours.text.toString().toIntOrNull(); val m=minutes.text.toString().toIntOrNull()
            if(msg.isBlank()||h==null||m==null||h !in 0..23||m !in 0..59) { toast("متن و ساعت معتبر وارد کنید") } else {
                val line="%02d:%02d - %s".format(Locale.US,h,m,msg)
                val old=prefs.getString("reminders","") ?: ""; prefs.edit().putString("reminders",old+line+"\n").apply()
                scheduleReminder(msg,h,m); navigate(if(page=="meds")"meds" else "calendar"); toast("یادآوری ذخیره شد")
            }
        }.setNegativeButton("انصراف",null).show()
    }
    private fun scheduleReminder(message: String,h:Int,m:Int) {
        val alarm=getSystemService(ALARM_SERVICE) as AlarmManager
        val intent=Intent(this,ReminderReceiver::class.java).putExtra("message",message)
        val pi=PendingIntent.getBroadcast(this,(System.currentTimeMillis()%Int.MAX_VALUE).toInt(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cal=Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,h); set(Calendar.MINUTE,m); set(Calendar.SECOND,0); if(timeInMillis<=System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR,1) }
        try { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.timeInMillis,pi) } catch(_:SecurityException) { toast("مجوز زمان‌بندی اعلان در دستگاه فعال نیست") }
    }
    private fun shop() {
        content.addView(text("خرید اینترنتی را اول در محیط امن مهرینه تمرین کنید.",15f,false,muted)); gap()
        val categories=listOf("مواد غذایی" to "cart","دارو و سلامت" to "pill","پوشاک" to "user","لوازم خانه" to "home")
        categories.forEach { (name,ic) -> content.addView(section(name,ic) { orderDialog(name) }); gap(9) }
        addTitle("سبد خرید آزمایشی من")
        val orders=prefs.getString("orders","") ?: ""
        content.addView(rowCard(if(orders.isBlank())"سبد شما خالی است" else orders.lines().lastOrNull { it.isNotBlank() } ?: "سبد خالی", "فقط تمرینی؛ هیچ مبلغی پرداخت نمی‌شود","cart",0xFFFF941C.toInt()) { orderDialog("مواد غذایی") }); gap()
        content.addView(button("تمرین سفارش دو کیلو گوشت") { confirmOrder("۲ کیلو گوشت") }); gap()
        content.addView(section("آموزش تصویری خرید اینترنتی", "book") { navigate("learn") })
    }
    private fun orderDialog(category: String) {
        val e=EditText(this).apply { hint="چه چیزی می‌خواهید؟"; setText(if(category=="مواد غذایی")"۲ کیلو گوشت" else ""); textSize=17f }
        AlertDialog.Builder(this).setTitle("تمرین خرید: $category").setView(e).setPositiveButton("ادامه") { _,_-> if(e.text.isNotBlank()) confirmOrder(e.text.toString()) }.setNegativeButton("انصراف",null).show()
    }
    private fun confirmOrder(item: String) { AlertDialog.Builder(this).setTitle("تأیید سفارش آزمایشی").setMessage("آیا می‌خواهید «$item» را به سبد خرید تمرینی اضافه کنید؟\nهیچ سفارش واقعی ثبت یا پرداخت نمی‌شود.").setPositiveButton("تأیید") { _,_->prefs.edit().putString("orders",(prefs.getString("orders","")?:"")+item+"\n").apply(); toast("سفارش آزمایشی ثبت شد"); if(page=="shop")navigate("shop") }.setNegativeButton("لغو",null).show() }
    private fun assistant() {
        val slider=HorizontalScrollView(this);slider.isHorizontalScrollBarEnabled=false
        val slides=linear(false)
        val data=listOf(Triple("خرید آسان","بگویید چه چیزی لازم دارید؛ ابتدا سفارش تمرینی می‌سازیم.","lesson_shop"),Triple("یادآوری دارو","زمان داروها را ثبت کنید و صدای یادآوری را بشنوید.","hero"),Triple("تماس با عزیزان","با چند لمس با خانواده تماس بگیرید.","lesson_phone"),Triple("آموزش گوشی","با فیلم‌های کوتاه مهارت‌های تازه یاد بگیرید.","lesson_message"))
        data.forEach { (title,detail,img) -> val tile=card(0xFFF4EFFF.toInt());tile.layoutParams=lp(250,190,5);tile.addView(avatar(img,74));tile.addView(text(title,19f,true));tile.addView(text(detail,13f,false,muted));slides.addView(tile) }
        slider.addView(slides);content.addView(slider);gap(12)
        val greeting=card(0xFFF0EEFF.toInt()); greeting.addView(text("سلام! من دستیار مهرینه هستم",20f,true)); greeting.addView(text("می‌توانید سؤال بپرسید یا روی میکروفن بزنید.",14f,false,muted)); content.addView(greeting)
        addTitle("سؤال‌های پیشنهادی")
        val qs=listOf("وضع حالم را چطور اعلام کنم؟","چطور یادآوری دارو بگذارم؟","چطور با دخترم تماس بگیرم؟","چطور خرید اینترنتی انجام بدهم؟","چطور پیام‌ها را بلند بخوانم؟","چطور از کلاهبرداری اینترنتی دور بمانم؟","برام دو کیلو گوشت بخر","چطور اندازه نوشته را بزرگ کنم؟","چطور از اسنپ استفاده کنم؟","چطور فیلم آپارات ببینم؟","اگر اینترنت قطع شد چه کنم؟","چطور برنامه فیلیمو را باز کنم؟")
        qs.forEach { q -> content.addView(section(q,"chat") { answer(q) }); gap(8) }
        gap()
        val mic=button("صحبت با دستیار",accent) { startVoice() }; content.addView(mic); gap()
        val entry=EditText(this).apply { hint="سؤال خود را بنویسید..."; textSize=17f; background=shape(white,17); pad(this); minHeight=dp(65) }
        content.addView(entry); gap(8); content.addView(button("ارسال پیام") { if(entry.text.isNotBlank()) answer(entry.text.toString()) })
    }
    private fun answer(q: String) {
        val a=when {
            q.contains("گوشت")||q.contains("بخر")||q.contains("سفارش") -> "حتماً. می‌توانم یک سفارش آزمایشی ثبت کنم. برای خرید واقعی باید فروشگاه متصل و پرداخت توسط خودتان تأیید شود."
            q.contains("دارو") -> "به بخش یادآوری دارو بروید، دکمه افزودن را بزنید و ساعت را تعیین کنید."
            q.contains("تماس")||q.contains("دختر") -> "در صفحه تماس با خانواده، شماره عزیزانتان را ثبت کنید و دکمه تماس را بزنید."
            q.contains("حال") -> "در بخش اعلام حال، وضعیت امروز خود را انتخاب و ثبت کنید."
            q.contains("خرید") -> "در بخش خرید اینترنتی، خرید را بدون پرداخت واقعی تمرین کنید."
            q.contains("بخوان")||q.contains("صوت") -> "روی گزینه خواندن صوتی بزنید. برای خواندن فارسی، موتور گفتار فارسی گوشی باید نصب باشد."
            q.contains("کلاهبرداری") -> "رمز یک‌بارمصرف، رمز کارت و اطلاعات بانکی را به افراد ناشناس ندهید و لینک‌های مشکوک را باز نکنید."
            q.contains("اندازه")||q.contains("نوشته") -> "در تنظیمات، نوار اندازه نوشته را به چپ یا راست بکشید."
            q.contains("اسنپ")||q.contains("فیلیمو") -> "از بخش بیشتر، برنامه نصب‌شده را باز کنید. اگر نصب نیست، صفحه فروشگاه را ببینید."
            q.contains("آپارات")||q.contains("فیلم") -> "به بخش آموزش بروید و روی ویدئوی دلخواه بزنید."
            q.contains("اینترنت") -> "اتصال وای‌فای یا داده همراه را بررسی کنید. بخش‌های آفلاین مهرینه همچنان در دسترس هستند."
            else -> "من می‌توانم در تماس با خانواده، یادآوری‌ها، آموزش خرید و تنظیمات مهرینه راهنمایی‌تان کنم."
        }
        val box=card(0xFFF0EEFF.toInt()); box.addView(text("شما: $q",15f,true)); box.addView(text("مهرینه: $a",16f)); content.addView(box,1)
        speak(a)
        if(q.contains("گوشت")||q.contains("بخر")||q.contains("سفارش")) content.addView(button("تأیید سفارش آزمایشی") { confirmOrder("۲ کیلو گوشت") },2)
        scroll.post { scroll.smoothScrollTo(0,0) }
    }
    private fun startVoice() {
        val i=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE,"fa-IR"); putExtra(RecognizerIntent.EXTRA_PROMPT,"سؤالتان را بگویید") }
        try { startActivityForResult(i,101) } catch(_:Exception) { toast("سرویس تشخیص گفتار روی این گوشی در دسترس نیست") }
    }
    @Deprecated("Deprecated in Android") override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) { super.onActivityResult(requestCode,resultCode,data); if(requestCode==101 && resultCode==RESULT_OK) { val words=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); if(!words.isNullOrEmpty()) { if(page!="assistant")navigate("assistant"); answer(words[0]) } } }
    private fun speak(s:String) { if(ttsReady) tts?.speak(s,TextToSpeech.QUEUE_FLUSH,null,"mehrineh") else AlertDialog.Builder(this).setTitle("خواندن صوتی").setMessage("موتور گفتار فارسی روی گوشی آماده نیست. زبان فارسی را در تنظیمات تبدیل متن به گفتار نصب یا فعال کنید.").setPositiveButton("باشه",null).show() }
    private val lessonItems=listOf(
        Triple("تماس با خانواده","lesson_phone","تماس با خانواده"),
        Triple("خرید اینترنتی قدم‌به‌قدم","lesson_shop","خرید اینترنتی"),
        Triple("تشخیص پیام‌های مشکوک","lesson_message","امنیت اینترنت"),
        Triple("تقویم و یادآوری","hero","یادآوری‌ها")
    )
    private fun learn() {
        content.addView(text("هر آموزش یک ویدئوی جداگانه از آپارات دارد. لینک‌ها را می‌توانید برای هر درس ثبت کنید.",15f,false,muted));gap()
        lessonItems.forEachIndexed { index, (title,img,topic) ->
            val box=card();box.orientation=LinearLayout.HORIZONTAL;box.gravity=Gravity.CENTER_VERTICAL
            val thumbnail=avatar(img,92);box.addView(thumbnail)
            val col=linear();col.addView(text(title,17f,true));col.addView(text("آموزش ویدئویی · آپارات",13f,false,muted));
            val link=prefs.getString("aparat_$index","")?:""
            col.addView(text(if(link.isBlank())"لینک هنوز ثبت نشده" else "آماده پخش",12f,false,if(link.isBlank())muted else 0xFF13A56C.toInt()))
            box.addView(col,LinearLayout.LayoutParams(0,-2,1f).apply{marginStart=dp(12)})
            box.addView(icon("video",accent,24));content.addView(box);gap(10)
            box.setOnClickListener { lessonVideo(index,topic) }
        }
    }
    private fun lessonVideo(index:Int,topic:String) {
        val url=prefs.getString("aparat_$index","")?:""
        if(url.isBlank()) {
            val input=EditText(this).apply { hint="https://www.aparat.com/v/xxxxx";inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI;setSingleLine(true) }
            val wrapper=linear();pad(wrapper);wrapper.addView(input)
            AlertDialog.Builder(this).setTitle("لینک آپارات: $topic").setMessage("لینک واقعی ویدئوی همین درس را وارد کنید. هیچ لینک ساختگی در برنامه قرار داده نشده است.")
                .setView(wrapper).setPositiveButton("ذخیره") { _,_-> val v=input.text.toString().trim();if(isAparat(v)) {prefs.edit().putString("aparat_$index",v).apply();lessonVideo(index,topic)}else toast("لینک معتبر آپارات وارد کنید") }
                .setNeutralButton("آموزش متنی") { _,_->lesson(topic) }.setNegativeButton("لغو",null).show();return
        }
        if(!isAparat(url)){toast("آدرس آپارات معتبر نیست");return}
        val id=Regex("(?:/v/|/embed/)([A-Za-z0-9]+)").find(Uri.parse(url).path?:"")?.groupValues?.get(1)
        if(id==null) {toast("لینک مستقیم ویدئوی آپارات لازم است");return}
        val web=WebView(this);web.settings.javaScriptEnabled=true;web.settings.domStorageEnabled=true;web.webViewClient=WebViewClient()
        web.loadUrl("https://www.aparat.com/video/video/embed/videohash/$id/vt/frame")
        val container=android.widget.FrameLayout(this);container.addView(web,android.widget.FrameLayout.LayoutParams(-1,dp(260)))
        val dialog=AlertDialog.Builder(this).setTitle(topic).setView(container).setPositiveButton("بستن",null).setNeutralButton("تغییر لینک") { _,_->prefs.edit().remove("aparat_$index").apply();lessonVideo(index,topic) }.create()
        dialog.setOnDismissListener { web.stopLoading();web.destroy() };dialog.show()
    }
    private fun isAparat(value:String):Boolean = try { val u=Uri.parse(value);u.scheme=="https" && (u.host=="www.aparat.com" || u.host=="aparat.com") && (u.path?:"").startsWith("/v/") } catch(_:Exception){false}
    private fun lesson(topic:String) {
        val steps=when(topic) { "تماس با خانواده"->arrayOf("وارد بخش تماس با خانواده شوید.","شماره فرد مورد نظر را ثبت کنید.","دکمه تماس را لمس کنید.");"خرید اینترنتی"->arrayOf("فروشگاه یا دسته کالا را انتخاب کنید.","کالا را به سبد خرید آزمایشی اضافه کنید.","محتوای سبد را بررسی و تأیید کنید.");"امنیت اینترنت"->arrayOf("به فرستنده پیام دقت کنید.","روی لینک ناشناس نزنید.","رمزهای شخصی را به کسی ندهید.");else->arrayOf("بخش تقویم را باز کنید.","افزودن یادآوری را بزنید.","متن و ساعت را ثبت کنید.") }
        val msg=steps.mapIndexed { i,s -> "${i+1}. $s" }.joinToString("\n\n")
        AlertDialog.Builder(this).setTitle(topic).setMessage(msg).setPositiveButton("متوجه شدم") { _,_->prefs.edit().putInt("lesson_count",prefs.getInt("lesson_count",0)+1).apply() }.setNeutralButton("بلند بخوان") { _,_->speak(msg) }.show()
    }
    private fun mood() {
        addTitle("امروز چه احساسی دارید؟")
        val moods = listOf("عالی","خوب","معمولی","خسته","بد")
        val faces = listOf("😄","🙂","😐","😟","😢")
        val bar=linear(false); bar.gravity=Gravity.CENTER_VERTICAL
        moods.forEachIndexed { i,m ->
            val cell=linear(); cell.gravity=Gravity.CENTER
            val face=text(faces[i],29f); face.gravity=Gravity.CENTER; cell.addView(face)
            val name=text(m,13f,true);name.gravity=Gravity.CENTER;cell.addView(name)
            val active=(selectedMood.ifBlank { prefs.getString("mood","")?:"" }==m)
            cell.background=shape(if(active)0xFFEAF7EF.toInt() else white,15,if(active)2 else 0,0xFF16A86C.toInt())
            pad(cell,2,12,12)
            bar.addView(cell,LinearLayout.LayoutParams(0,dp(92),1f).apply{setMargins(dp(2),0,dp(2),0)})
            cell.setOnClickListener { selectedMood=m; navigate("mood") }
        }
        content.addView(bar);gap(14)
        content.addView(button("ثبت حال من") { saveMood() });gap(14)
        val last=prefs.getString("mood","ثبت نشده")?:"ثبت نشده"
        content.addView(rowCard("آخرین وضعیت ثبت‌شده", "$last · ${prefs.getString("mood_time","—")}","heart",accent) { selectedMood=last.takeIf { it in moods } ?: ""; navigate("mood"); toast("وضعیت خود را انتخاب کنید و ثبت را بزنید") })
        gap(10)
        val chart=card(0xFFFFF0F5.toInt());chart.addView(text("نمای ضربان قلب",17f,true)); chart.addView(text("طرح نمایشی — به حسگر پزشکی متصل نیست",12f,false,muted))
        chart.addView(PulseView(this),lp(-1,92));content.addView(chart)
    }
    private fun saveMood() {
        if(selectedMood.isBlank()) { toast("ابتدا یکی از احساس‌ها را انتخاب کنید");return }
        prefs.edit().putString("mood",selectedMood).putString("mood_time",SimpleDateFormat("yyyy/MM/dd HH:mm",Locale.US).format(Date())).apply()
        toast("حال شما ثبت شد: $selectedMood"); selectedMood="";navigate("mood")
    }
    private class PulseView(ctx:Context):View(ctx) {
        private val paint=android.graphics.Paint(3).apply { color=0xFFEE3368.toInt();style=android.graphics.Paint.Style.STROKE;strokeWidth=5f;strokeJoin=android.graphics.Paint.Join.ROUND }
        override fun onDraw(canvas:android.graphics.Canvas) { super.onDraw(canvas);val w=width.toFloat();val h=height.toFloat();val points=floatArrayOf(0f,.5f,.18f,.5f,.3f,.5f,.42f,.12f,.49f,.85f,.56f,.5f,.75f,.5f,1f,.5f);val path=android.graphics.Path();points.indices.step(2).forEach { i->val x=points[i]*w;val y=points[i+1]*h;if(i==0)path.moveTo(x,y) else path.lineTo(x,y) };canvas.drawPath(path,paint) }
    }
    private fun apps() {
        content.addView(text("میان‌بر برنامه‌های نصب‌شده روی گوشی",15f,false,muted)); gap()
        listOf(Triple("اسنپ","ir.snapp.client","phone"),Triple("فیلیمو","com.aparat.filimo","video"),Triple("بادصبا","com.mobiliha.badesaba","calendar"),Triple("واتساپ","com.whatsapp","chat")).forEach { (n,pkg,ic) ->
            content.addView(section(n,ic) { launchPackage(pkg,n) }); gap(10)
        }
        content.addView(text("اگر برنامه‌ای نصب نباشد، پیام راهنما نمایش داده می‌شود.",13f,false,muted))
    }
    private fun launchPackage(pkg:String,name:String) {
        try {
            val i=packageManager.getLaunchIntentForPackage(pkg)
            if(i!=null) { startActivity(i);return }
            AlertDialog.Builder(this).setTitle("$name پیدا نشد").setMessage("اگر برنامه نصب است، ممکن است شناسه آن در نسخه گوشی شما متفاوت باشد. می‌توانید صفحه آن را در فروشگاه باز کنید.")
                .setPositiveButton("جست‌وجو در فروشگاه") { _,_-> try { startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/search?q="+Uri.encode(name)+"&c=apps"))) } catch(_:Exception){toast("فروشگاه در دسترس نیست")} }
                .setNegativeButton("انصراف",null).show()
        } catch(_:Exception){toast("امکان باز کردن $name وجود ندارد")}
    }
    private fun settingsPage() {
        addTitle("رنگ برنامه")
        val colors=linear(false)
        themes.forEachIndexed { i,c ->
            val v=TextView(this);v.text=if(prefs.getInt("theme",0)==i)"✓" else "";v.gravity=Gravity.CENTER;v.setTextColor(white);v.textSize=23f;v.background=shape(c,50)
            colors.addView(v,LinearLayout.LayoutParams(0,dp(60),1f).apply{setMargins(dp(6),0,dp(6),0)})
            v.setOnClickListener { prefs.edit().putInt("theme",i).apply();navigate("settings") }
        };content.addView(colors)
        addTitle("اندازه نوشته")
        val preview=text("مهرینه همراه مهربان شماست",19f,true);content.addView(preview)
        val value=text("${prefs.getInt("font_percent",100)}٪",15f,false,muted);content.addView(value)
        val slider=SeekBar(this);slider.max=60;slider.progress=prefs.getInt("font_percent",100)-85
        slider.progressTintList=android.content.res.ColorStateList.valueOf(accent)
        slider.thumbTintList=android.content.res.ColorStateList.valueOf(accent)
        slider.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar:SeekBar?,progress:Int,fromUser:Boolean) { val percent=85+progress;value.text="$percent٪";preview.textSize=19f*percent/100f;if(fromUser)prefs.edit().putInt("font_percent",percent).apply() }
            override fun onStartTrackingTouch(bar:SeekBar?){}
            override fun onStopTrackingTouch(bar:SeekBar?) { navigate("settings") }
        });content.addView(slider)
        gap();content.addView(section("آزمایش خواندن متن", "volume") { speak("سلام، به مهرینه خوش آمدید.") });gap()
        content.addView(section("ویرایش شماره‌های تماس", "phone") { editNumbers() })
        gap();content.addView(text("برای اتصال اپ خانواده، راهنمای راه‌اندازی دو برنامه را در فایل README بخوانید.",13f,false,muted))
    }
    private fun toast(s:String) = Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
