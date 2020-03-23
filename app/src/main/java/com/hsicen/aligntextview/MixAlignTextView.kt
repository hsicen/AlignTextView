package com.hsicen.aligntextview

import android.content.Context
import android.graphics.Canvas
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * 作者：hsicen  2020/3/17 16:54
 * 邮箱：codinghuang@163.com
 * 作用：中英文混排，两端对齐，自动截断的TextView
 * 描述：先将文字按照段为单位进行拆分，然后再将没一段按照汉字或者单词为最小单位进行拆分
 * 将得到的按段为单位的结合进行遍历，将每一段以行为单位进行拆分，这里为关键点，断行的逻辑都在这里处理
 *
 *将最后的到的以段为大单位，行为中单位，汉字或者单词为最小单位的数据进行绘制
 */
class MixAlignTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    //段落间距
    private val mParagraphSpace: Int
    private var mBaseLineY = 0f
    private var mViewWidth = 0
    private var mLineCount = 0

    //所有段的单词集合
    private val mParagraphWordsList by lazy { ArrayList<List<String>>() }

    //所有段的行集合
    private val mParagraphLineList by lazy { ArrayList<List<List<String>>>() }
    private val mVowels by lazy { listOf("A", "E", "I", "O", "U", "a", "e", "i", "o", "u") }
    private val mDrawText by lazy { StringBuilder() }

    init {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.MixAlignTextView)
        mParagraphSpace = typeArray.getDimension(
            R.styleable.MixAlignTextView_paragraphSpace, 15f.px
        ).toInt()
        typeArray.recycle()
    }

    //重写onMeasure，修改已有View的尺寸
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        //重置数据
        mParagraphWordsList.clear()
        mParagraphLineList.clear()
        mBaseLineY = 0f

        //获取最新数据
        mViewWidth = measuredWidth - paddingLeft - paddingRight
        getParagraphWordsList()
        getParagraphLineList()

        //设置尺寸
        setMeasuredDimension(
            mViewWidth, (mParagraphWordsList.size - 1) * mParagraphSpace + mLineCount * lineHeight
        )
    }

    override fun onDraw(canvas: Canvas?) {
        layout ?: return
        canvas ?: return

        fun drawEndText(line: List<String>, mPaint: TextPaint) {
            mDrawText.clear()

            for (item in line) {
                mDrawText.append(item)
            }

            //默认左对齐 （后续考虑右对齐和居中对齐）
            canvas.drawText(mDrawText.toString(), 0f, mBaseLineY, mPaint)
        }

        fun drawScaleText(line: List<String>, mPaint: TextPaint) {
            mDrawText.clear()

            for (item in line) {
                mDrawText.append(item)
            }

            val strWidth = StaticLayout.getDesiredWidth(mDrawText.toString(), mPaint)
            val d = (mViewWidth - strWidth) / (line.size - 1)
            var cw = 0f

            for (item in line) {
                canvas.drawText(item, cw, mBaseLineY, mPaint)
                cw += StaticLayout.getDesiredWidth(item + "", mPaint) + d
            }
        }

        fun drawText(mPaint: TextPaint) {
            mParagraphLineList.forEach { lineList ->
                lineList.forEachIndexed { index, line ->
                    (index == lineList.size - 1).yes {
                        drawEndText(line, mPaint)
                    }.no { drawScaleText(line, mPaint) }
                    mBaseLineY += lineHeight
                }
                mBaseLineY += mParagraphSpace
            }
        }

        val mPaint = paint
        mPaint.color = currentTextColor
        mPaint.drawableState = drawableState

        mBaseLineY = textSize + paddingTop
        drawText(mPaint)
    }

    private fun getParagraphWordsList() {
        //去掉多余的空格
        val content = text.toString().replace("  ".regex, "").replace("   ".regex, "")
            .replace("\r".regex, "").trim()
        val paragraphs = content.split("\n".regex)

        for (item in paragraphs) {
            if (item.isNotEmpty())
                mParagraphWordsList.add(getWorldsList(item))
        }
    }

    /*** 分解段落为最小单词单元*/
    @Synchronized
    private fun getWorldsList(item: String): List<String> {
        val worldsList = ArrayList<String>()
        val sb = StringBuilder()

        for (char in item) {
            val str = char.toString()
            str.isNotBlank().yes {
                str.isSymbol.yes {
                    val notEmpty = sb.isNotEmpty()
                    sb.append(str)
                    notEmpty.yes {
                        worldsList.add(sb.toString().trim())
                        sb.clear()
                    }
                }.no {
                    str.isCN.yes {
                        worldsList.add(sb.toString().trim())
                        sb.clear()
                    }
                    sb.append(str)
                }
            }.no {
                sb.isNotEmpty().yes {
                    worldsList.add(sb.toString().trim())
                    sb.clear()
                }
            }
        }

        if (sb.isNotEmpty()) {
            worldsList.add(sb.toString())
            sb.clear()
        }

        return worldsList
    }

    /*** 分解段落为行单元（关键点）*/
    private fun getParagraphLineList() {
        fun addLines(lines: ArrayList<List<String>>?, line: ArrayList<String>?) {
            if (null == lines || null == line) return

            val tmp = ArrayList<String>(line)
            lines.add(tmp)
            line.clear()
        }

        fun getLineList(paragraphStr: List<String>): List<List<String>> {
            val lines = ArrayList<List<String>>()
            val line = ArrayList<String>()

            val sb = StringBuilder()
            var lineWidth: Float
            var cutWord = ""  //单词截取标记

            paragraphStr.forEachIndexed { index, word ->
                cutWord.isNotEmpty().yes { //判断上一个单词是否截留
                    sb.append(cutWord)
                    line.add(cutWord)

                    cutWord.isCN.no { sb.append(STR_BLANK) }
                    cutWord = ""
                }

                //判断当前单词是否加空格
                word.isCN.yes { sb.append(word) }
                    .no {
                        ((index + 1) < paragraphStr.size).yes {
                            val nextWord = paragraphStr[index + 1]
                            nextWord.isCN.yes { sb.append(word) }
                                .no { sb.append(word).append(STR_BLANK) }
                        }.no { sb.append(word) }
                    }

                line.add(word)
                lineWidth = StaticLayout.getDesiredWidth(sb.toString(), paint)

                //截断处理
                (lineWidth > mViewWidth).yes {
                    val lineLastWord = line.last()
                    line.removeAt(line.lastIndex)

                    lineLastWord.isCN.yes {
                        addLines(lines, line)
                        cutWord = lineLastWord
                    }.no {
                        val lastWordLength = lineLastWord.length

                        (lastWordLength <= 3).yes { //最后一个单词小于3 不截断
                            addLines(lines, line)
                            cutWord = lineLastWord
                        }.no {
                            val cutForwardStr = sb.substring(0, sb.length - lastWordLength - 1)
                            sb.clear().append(cutForwardStr).append(STR_BLANK)
                            var cutIndex: Int

                            lineLastWord.forEachIndexed charLoop@{ charIndex, char ->
                                sb.append(char.toString())

                                //走元音截断逻辑
                                mVowels.contains(char.toString()).yes {
                                    ((charIndex + 1) < lastWordLength).yes {
                                        val lastNextChar = lineLastWord[charIndex + 1]
                                        sb.append(lastNextChar.toString())
                                        lineWidth =
                                            StaticLayout.getDesiredWidth(sb.toString(), paint)
                                        cutIndex = charIndex

                                        (lineWidth > mViewWidth).yes {
                                            (cutIndex > 2 && cutIndex <= lastWordLength - 2).yes {
                                                line.add(lineLastWord.substring(0, cutIndex + 2) + "-")
                                                addLines(lines, line)
                                                cutWord = lineLastWord.substring(cutIndex, lastWordLength)
                                            }.no {
                                                addLines(lines, line)
                                                cutWord = lineLastWord
                                            }
                                            return@charLoop
                                        }
                                    }.no {
                                        addLines(lines, line)
                                        cutWord = lineLastWord
                                        return@charLoop
                                    }
                                }

                                //走默认截断逻辑
                                lineWidth = StaticLayout.getDesiredWidth(sb.toString(), paint)
                                (lineWidth > mViewWidth).yes {

                                    ((charIndex > 2) && charIndex <= lastWordLength - 2).yes {
                                        line.add(lineLastWord.substring(0, charIndex) + "-")
                                        addLines(lines, line)
                                        cutWord = lineLastWord.substring(charIndex, lastWordLength)
                                    }.no {
                                        addLines(lines, line)
                                        cutWord = lineLastWord
                                    }

                                    return@charLoop
                                }
                            }
                        }
                    }

                    sb.clear()
                }


                (line.size > 0 && index == paragraphStr.size - 1).yes {
                    addLines(lines, line)
                }
            }

            if (cutWord.isNotEmpty()) {
                line.add(cutWord)
                addLines(lines, line)
            }

            mLineCount += lines.size
            return lines
        }

        for (paragraphStr in mParagraphWordsList) {
            mParagraphLineList.add(getLineList(paragraphStr))
        }
    }

    companion object {
        const val STR_BLANK = " "
    }

}
