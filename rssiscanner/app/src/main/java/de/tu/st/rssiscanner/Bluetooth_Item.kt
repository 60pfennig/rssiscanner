package de.tu.st.rssiscanner

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView

class Bluetooth_Item(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    init {

        val imageView: TextView = findViewById(R.id.deviceName)
        val textView: TextView = findViewById(R.id.deviceRSSI)
    }
}