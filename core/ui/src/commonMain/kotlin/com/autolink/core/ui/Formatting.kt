package com.autolink.core.ui

import kotlin.math.abs
import kotlin.math.roundToLong

/** Formats with one decimal without java.text (common code). */
fun Float.oneDecimal(): String {
    val tenths = (this * 10).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    val a = abs(tenths)
    return "$sign${a / 10}.${a % 10}"
}

/** 1250 -> "1.3 s", 400 -> "0.4 s". */
fun Long.asSecondsAgo(): String = (this / 1000f).oneDecimal() + " s"
