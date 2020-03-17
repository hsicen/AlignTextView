package com.hsicen.aligntextview

import android.content.Context
import android.graphics.Canvas
import android.text.StaticLayout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * 作者：hsicen  2020/3/16 15:27
 * 邮箱：codinghuang@163.com
 * 作用：两端对齐的TextView
 * 描述：利用StaticLayout测量，依次测量一行文字的每个字符的宽度
 * 利用StaticLayout测量这一行文字的宽度，然后用总的宽度/文字的长度 = 每个字符的宽度
 * 然后把这一行文字绘制在这个View的宽度上
 *
 * 优点：不会出现每一行文字末尾的较长宽度的空白
 * 缺点：有的时候文字的间距会增大
 */
class AlignTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    //当只有一行文字时，是否两端对齐
    private val alignOnlyOneLine: Boolean

    init {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.AlignTextView)
        alignOnlyOneLine = typeArray.getBoolean(R.styleable.AlignTextView_alignWhileOneLine, false)
        typeArray.recycle()
        setTextColor(currentTextColor)
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        paint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        //只绘制String
        if (text !is String) {
            super.onDraw(canvas)
            return
        }

        //依次绘制每一行文字
        for (lineItem in 0 until layout.lineCount) {
            val lineBase = layout.getLineBaseline(lineItem) + paddingTop
            val lineStart = layout.getLineStart(lineItem)
            val lineEnd = layout.getLineEnd(lineItem)

            val lineText = text.substring(lineStart, lineEnd)
            if (alignOnlyOneLine && layout.lineCount == 1) {
                //测量这一行文字在View中占据的宽度
                val lineWidth = StaticLayout.getDesiredWidth(
                    text, lineStart, lineEnd, paint
                )
                //均匀的绘制这一行文字
                drawScaleText(canvas, lineText, lineBase.toFloat(), lineWidth)
            } else if (lineItem == layout.lineCount - 1) {
                canvas.drawText(lineText, paddingLeft.toFloat(), lineBase.toFloat(), paint)
            } else {
                val lineWidth = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, paint)
                drawScaleText(canvas, lineText, lineBase.toFloat(), lineWidth)
            }
        }
    }

    //均匀的绘制这一行文字的每个字符
    private fun drawScaleText(
        canvas: Canvas, lineText: String, baseLineY: Float, lineWidth: Float
    ) {
        if (lineText.isEmpty()) return

        var start = paddingLeft.toFloat() //记录开始绘制x坐标
        val length = lineText.length - 1

        //最后一个字符为强制换行符或者只有一个字符
        val forceNextLine = lineText[length].toInt() == 10
        if (forceNextLine || 0 == length) {
            canvas.drawText(lineText, start, baseLineY, paint)
            return
        }

        //计算出每个字符之间的间距
        val d = (measuredWidth - lineWidth - paddingStart - paddingEnd) / length
        //依次绘制每个字符
        for (element in lineText) {
            val char = element.toString()
            val cw = StaticLayout.getDesiredWidth(char, paint) //计算出当前字符的宽度
            canvas.drawText(char, start, baseLineY, paint)
            start += cw + d
        }
    }

}