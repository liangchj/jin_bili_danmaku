package com.lchj.jin_bili_danmaku.danmaku.bili

import android.graphics.Color
import android.text.TextUtils
import master.flame.danmaku.danmaku.model.AlphaValue
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.Duration
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.SpecialDanmaku
import master.flame.danmaku.danmaku.model.android.DanmakuFactory
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource
import master.flame.danmaku.danmaku.util.DanmakuUtils
import org.json.JSONArray
import org.json.JSONException
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.helpers.XMLReaderFactory
import java.io.IOException
import java.util.Locale

open class BiliDanmakuParser : BaseDanmakuParser() {
    companion object {
        init {
            System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver")
        }
    }

    protected var mDispScaleX = 0f
    protected var mDispScaleY = 0f

    override fun parse(): Danmakus? {
        if (mDataSource != null) {
            val source : AndroidFileSource = mDataSource as AndroidFileSource
            try {
                val xmlReader : XMLReader = XMLReaderFactory.createXMLReader()
                val contentHandler = XmlContentHandler()
                xmlReader.contentHandler = contentHandler
                xmlReader.parse(InputSource(source.data()))
                return contentHandler.result
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    inner class XmlContentHandler : DefaultHandler() {
        private val TRUE_STRING = "true"
        var result: Danmakus? = null
        private var item: BaseDanmaku? = null
        private var completed : Boolean = false
        private var index : Int = 0

        @Throws(SAXException::class)
        override fun startDocument() {
            result = Danmakus(IDanmakus.ST_BY_TIME, false, mContext.baseComparator)
        }

        @Throws(SAXException::class)
        override fun endDocument() {
            completed = true
        }

        @Throws(SAXException::class)
        override fun startElement(
            uri: String?,
            localName: String,
            qName: String?,
            attributes: Attributes
        ) {
            var tagName: String = localName.ifEmpty { qName!! }
            tagName = tagName.lowercase(Locale.getDefault()).trim { it <= ' ' }
            if (tagName == "d") {
                // <d p="23.826000213623,1,25,16777215,1422201084,0,057075e9,757076900">我从未见过如此厚颜无耻之猴</d>
                // 0:时间(弹幕出现时间)
                // 1:类型(1从右至左滚动弹幕|6从左至右滚动弹幕|5顶端固定弹幕|4底端固定弹幕|7高级弹幕|8脚本弹幕)
                // 2:字号
                // 3:颜色
                // 4:时间戳 ?
                // 5:弹幕池id
                // 6:用户hash
                // 7:弹幕id
                val pValue: String = attributes.getValue("p")
                // parse p value to danmaku
                val values: Array<String> = pValue.split(",".toRegex()).toTypedArray()
                if (values.isNotEmpty()) {
                    val time: Long = (parseFloat(values[0]) * 1000).toLong() // 出现时间
                    val type: Int = parseInteger(values[1]) // 弹幕类型
                    val textSize: Float = parseFloat(values[2]) // 字体大小
                    val long1: Long = -0x1000000
                    val long2: Long = -0x1
                    val color: Int = ((long1 or parseLong(values[3])) and long2).toInt() // 颜色
                    // int poolType = parseInteger(values[5]); // 弹幕池类型（忽略
                    item = mContext.mDanmakuFactory.createDanmaku(type, mContext)
                    if (item != null) {
                        item!!.time = time
                        item!!.textSize = textSize * (mDispDensity - 0.6f)
                        item!!.textColor = color
                        item!!.textShadowColor =
                            if (color <= Color.BLACK) Color.WHITE else Color.BLACK
                    }
                }
            }
        }

        @Throws(SAXException::class)
        override fun endElement(uri: String?, localName: String, qName: String?) {
            if (item != null && item!!.text != null) {
                if (item!!.duration != null) {
                    val tagName = localName.ifEmpty { qName!! }
                    if (tagName.equals("d", ignoreCase = true)) {
                        item!!.timer = mTimer
                        item!!.flags = mContext.mGlobalFlagValues
                        val lock = result!!.obtainSynchronizer()
                        synchronized(lock) { result!!.addItem(item) }
                    }
                }
                item = null
            }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            if (item != null) {
                DanmakuUtils.fillText(item, decodeXmlString(String(ch!!, start, length)))
                item!!.index = index++

                // initial specail danmaku data
                val text: String = item!!.text.toString().trim { it <= ' ' }
                if (item!!.type == BaseDanmaku.TYPE_SPECIAL && text.startsWith("[")
                    && text.endsWith("]")
                ) {
                    //text = text.substring(1, text.length() - 1);
                    var textArr: Array<String?>? = null //text.split(",", -1);
                    try {
                        val jsonArray = JSONArray(text)
                        textArr = arrayOfNulls(jsonArray.length())
                        for (i in textArr.indices) {
                            textArr[i] = jsonArray.getString(i)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    if (textArr == null || textArr.size < 5 || TextUtils.isEmpty(textArr[4])) {
                        item = null
                        return
                    }
                    DanmakuUtils.fillText(item, textArr[4])
                    var beginX: Float = parseFloat(textArr[0])
                    var beginY: Float = parseFloat(textArr[1])
                    var endX: Float = beginX
                    var endY: Float = beginY
                    val alphaArr: Array<String> = textArr[2]!!.split("-".toRegex()).toTypedArray()
                    val beginAlpha: Int = (AlphaValue.MAX * parseFloat(alphaArr[0])).toInt()
                    var endAlpha: Int = beginAlpha
                    if (alphaArr.size > 1) {
                        endAlpha = (AlphaValue.MAX * parseFloat(alphaArr[1])).toInt()
                    }
                    val alphaDuraion: Long = (parseFloat(textArr[3]) * 1000).toLong()
                    var translationDuration: Long = alphaDuraion
                    var translationStartDelay: Long = 0
                    var rotateY = 0f
                    var rotateZ = 0f
                    if (textArr.size >= 7) {
                        rotateZ = parseFloat(textArr[5])
                        rotateY = parseFloat(textArr[6])
                    }
                    if (textArr.size >= 11) {
                        endX = parseFloat(textArr[7])
                        endY = parseFloat(textArr[8])
                        if ("" != textArr[9]) {
                            translationDuration = parseInteger(textArr[9]!!).toLong()
                        }
                        if ("" != textArr[10]) {
                            translationStartDelay = parseFloat(textArr[10]).toLong()
                        }
                    }
                    if (isPercentageNumber(textArr[0])) {
                        beginX *= DanmakuFactory.BILI_PLAYER_WIDTH
                    }
                    if (isPercentageNumber(textArr[1])) {
                        beginY *= DanmakuFactory.BILI_PLAYER_HEIGHT
                    }
                    if (textArr.size >= 8 && isPercentageNumber(textArr[7])) {
                        endX *= DanmakuFactory.BILI_PLAYER_WIDTH
                    }
                    if (textArr.size >= 9 && isPercentageNumber(textArr[8])) {
                        endY *= DanmakuFactory.BILI_PLAYER_HEIGHT
                    }
                    item!!.duration = Duration(alphaDuraion)
                    item!!.rotationZ = rotateZ
                    item!!.rotationY = rotateY
                    mContext.mDanmakuFactory.fillTranslationData(
                        item,
                        beginX,
                        beginY,
                        endX,
                        endY,
                        translationDuration,
                        translationStartDelay,
                        mDispScaleX,
                        mDispScaleY
                    )
                    mContext.mDanmakuFactory.fillAlphaData(item, beginAlpha, endAlpha, alphaDuraion)
                    if (textArr.size >= 12) {
                        // 是否有描边
                        if (!TextUtils.isEmpty(textArr[11]) && TRUE_STRING.equals(
                                textArr[11],
                                ignoreCase = true
                            )
                        ) {
                            item!!.textShadowColor = Color.TRANSPARENT
                        }
                    }
                    if (textArr.size >= 13) {
                        //TODO 字体 textArr[12]
                    }
                    if (textArr.size >= 14) {
                        // Linear.easeIn or Quadratic.easeOut
                        (item as SpecialDanmaku).isQuadraticEaseOut = "0" == textArr[13]
                    }
                    if (textArr.size >= 15) {
                        // 路径数据
                        if ("" != textArr[14]) {
                            val motionPathString = textArr[14]!!.substring(1)
                            if (!TextUtils.isEmpty(motionPathString)) {
                                val pointStrArray =
                                    motionPathString.split("L".toRegex()).toTypedArray()
                                if (pointStrArray.isNotEmpty()) {
                                    val points = Array(pointStrArray.size) {
                                        FloatArray(
                                            2
                                        )
                                    }
                                    for (i in pointStrArray.indices) {
                                        val pointArray =
                                            pointStrArray[i].split(",".toRegex()).toTypedArray()
                                        if (pointArray.size >= 2) {
                                            points[i][0] = parseFloat(pointArray[0])
                                            points[i][1] = parseFloat(pointArray[1])
                                        }
                                    }
                                    DanmakuFactory.fillLinePathData(
                                        item, points, mDispScaleX,
                                        mDispScaleY
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun decodeXmlString(title: String): String {
            var titleStr: String = title
            if (title.contains("&amp;")) {
                titleStr = title.replace("&amp;", "&")
            }
            if (title.contains("&quot;")) {
                titleStr = title.replace("&quot;", "\"")
            }
            if (title.contains("&gt;")) {
                titleStr = title.replace("&gt;", ">")
            }
            if (title.contains("&lt;")) {
                titleStr = title.replace("&lt;", "<")
            }
            return titleStr
        }

    }

    private fun isPercentageNumber(number: String?): Boolean {
        //return number >= 0f && number <= 1f;
        return number != null && number.contains(".")
    }

    private fun parseFloat(floatStr: String?): Float {
        return try {
            floatStr!!.toFloat()
        } catch (e: NumberFormatException) {
            0.0f
        }
    }

    private fun parseInteger(intStr: String): Int {
        return try {
            intStr.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun parseLong(longStr: String): Long {
        return try {
            longStr.toLong()
        } catch (e: NumberFormatException) {
            0
        }
    }

    override fun setDisplayer(disp: IDisplayer?): BaseDanmakuParser? {
        super.setDisplayer(disp)
        mDispScaleX = mDispWidth / DanmakuFactory.BILI_PLAYER_WIDTH
        mDispScaleY = mDispHeight / DanmakuFactory.BILI_PLAYER_HEIGHT
        return this
    }
}