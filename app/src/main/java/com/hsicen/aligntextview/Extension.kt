package com.hsicen.aligntextview

import android.content.res.Resources
import android.util.TypedValue
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 作者：hsicen  2020/3/19 14:15
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：扩展工具类
 */

@UseExperimental(ExperimentalContracts::class)
inline fun Boolean?.yes(block: () -> Unit): Boolean? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (this == true) block()
    return this
}

@UseExperimental(ExperimentalContracts::class)
inline fun Boolean?.no(block: () -> Unit): Boolean? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (this != true) block()
    return this
}

val Float.px: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val String.regex: Regex
    get() = Regex(this)

val String.isSymbol: Boolean
    get() {
        var tmp = this
        tmp = tmp.replace("\\p{P}".regex, "")
        return this.length != tmp.length
    }

val String.isCN: Boolean
    get() {
        val bytes = this.toByteArray(Charsets.UTF_8)
        return bytes.size != this.length
    }



