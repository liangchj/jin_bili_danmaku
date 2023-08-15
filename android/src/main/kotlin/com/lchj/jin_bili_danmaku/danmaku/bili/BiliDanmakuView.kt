package com.lchj.jin_bili_danmaku.danmaku.bili

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import com.lchj.jin_bili_danmaku.DanmakuConstant
import com.lchj.jin_bili_danmaku.danmaku.IJinDanmakuView
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.controller.IDanmakuView
import master.flame.danmaku.danmaku.loader.ILoader
import master.flame.danmaku.danmaku.loader.IllegalDataException
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import org.apache.commons.collections4.MapUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * 哔哩哔哩弹幕VIEW（烈焰弹幕使）
 */
class BiliDanmakuView(
    context: Context?,
    private val danmakuUrl: String,
    args: Map<String, Any>
) : IJinDanmakuView {
    companion object {
        // 弹幕速度列表，数字越小越快，越大的越慢
        private val danmakuSpeedList: List<Float> = DanmakuConstant.DANMAKU_SPEED_LIST
        private const val danmakuSpeedListTotal: Int = DanmakuConstant.DANMAKU_SPEED_LIST_TOTAL
        // 显示区域["1/4屏", "半屏", "3/4屏", "不重叠", "无限"]，选择下标，默认半屏（下标1）
        // "不重叠", "无限" 显示区域都是满屏，仅重叠不一致
        private val danmakuDisplayAreaList: List<Float> = DanmakuConstant.DANMAKU_DISPLAY_AREA_LIST
        private const val danmakuDisplayAreaListTotal: Int = DanmakuConstant.DANMAKU_DISPLAY_AREA_LIST_TOTAL
    }
    private var mDanmakuView : DanmakuView
    //创建弹幕上下文
    private val mContext : DanmakuContext = DanmakuContext.create()
    // 弹幕解析器
    private var mParser: BaseDanmakuParser? = null

    // 弹幕配置

    // 设置是否允许重叠
    private var allowOverlap: Boolean = true
    // 设置描边样式
    private var danmakuStyleStroken: Float = 3f

    // 弹幕透明度
    private var danmakuAlphaRatio : Int = 100
    // 显示区域
    private var danmakuDisplayAreaIndex: Int = 4
    // 弹幕字号（百分比）
    private var danmakuFontSizeRatio : Int = 100
    // 弹幕速度
    private var danmakuSpeedIndex : Int = 2

    // 解析完是否直接启动
    private var isStart: Boolean = true
    // 是否显示FPS
    private var isShowFPS: Boolean = false
    // 是否显示缓存信息
    private var isShowCache: Boolean = false

    /**
     * 弹幕显示隐藏设置
     */
    // 是否显示顶部弹幕
    private var fixedTopDanmakuVisibility: Boolean = true
    // 是否显示底部弹幕
    private var fixedBottomDanmakuVisibility: Boolean = true
    // 是否显示滚动弹幕
    private var rollDanmakuVisibility: Boolean = true
    // 是否显示特殊弹幕
    private var specialDanmakuVisibility: Boolean = true
    // 是否启用合并重复弹幕
    private var duplicateMergingEnable: Boolean = false

    // 是否显示彩色弹幕
    private var colorsDanmakuVisibility: Boolean = true

    init {
        danmakuAlphaRatio = MapUtils.getInteger(args, "danmakuAlphaRatio", danmakuAlphaRatio)
        danmakuDisplayAreaIndex = MapUtils.getInteger(args, "danmakuDisplayAreaIndex", danmakuDisplayAreaIndex)
        if (danmakuDisplayAreaIndex >= danmakuDisplayAreaListTotal) {
            danmakuDisplayAreaIndex = danmakuDisplayAreaListTotal - 1
        }
        if (danmakuDisplayAreaIndex == 3 || danmakuDisplayAreaIndex == 4) {
            allowOverlap = danmakuDisplayAreaIndex == 4
        } else {
            allowOverlap = true
        }
        danmakuFontSizeRatio = MapUtils.getInteger(args, "danmakuFontSizeRatio", danmakuFontSizeRatio)
        danmakuSpeedIndex = MapUtils.getInteger(args, "danmakuSpeedIndex", danmakuSpeedIndex)
        if (danmakuSpeedIndex >= danmakuSpeedListTotal) {
            danmakuSpeedIndex = danmakuSpeedListTotal - 1
        }
        isStart = MapUtils.getBoolean(args, "isStart", isStart)
        isShowFPS = MapUtils.getBoolean(args, "isShowFPS", isShowFPS)
        isShowCache = MapUtils.getBoolean(args, "isShowCache", isShowCache)
        duplicateMergingEnable = MapUtils.getBoolean(args, "duplicateMergingEnabled", duplicateMergingEnable)
        fixedTopDanmakuVisibility = MapUtils.getBoolean(args, "fixedTopDanmakuVisibility", fixedTopDanmakuVisibility)
        rollDanmakuVisibility = MapUtils.getBoolean(args, "rollDanmakuVisibility", rollDanmakuVisibility)
        fixedBottomDanmakuVisibility = MapUtils.getBoolean(args, "fixedBottomDanmakuVisibility", fixedBottomDanmakuVisibility)
        specialDanmakuVisibility = MapUtils.getBoolean(args, "specialDanmakuVisibility", specialDanmakuVisibility)
        colorsDanmakuVisibility = MapUtils.getBoolean(args, "colorsDanmakuVisibility", colorsDanmakuVisibility)

        mDanmakuView = DanmakuView(context)
        setSetting(context)
    }
    override fun getView(): View {
        return mDanmakuView
    }

    /**
     * 销毁view
     */
    override fun dispose() {
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "销毁view")
        try {
            mDanmakuView.release()
        } catch (e: Exception) {
            Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "销毁view失败")
        }
    }

    /**
     * 弹幕设置
     */
    private fun setSetting(context: Context?) {
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "entry setSetting")

        // 设置是否禁止重叠
        val overlappingEnablePair : Map<Int, Boolean> = hashMapOf(BaseDanmaku.TYPE_SCROLL_RL to allowOverlap, BaseDanmaku.TYPE_FIX_TOP to allowOverlap)

        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, danmakuStyleStroken) // 设置描边样式
            .setDanmakuTransparency(danmakuAlphaRatio / 100.0f)
            .setDuplicateMergingEnabled(duplicateMergingEnable) // 设置是否启用合并重复弹幕
            .setFTDanmakuVisibility(fixedTopDanmakuVisibility) // 是否显示顶部弹幕
            .setFBDanmakuVisibility(fixedBottomDanmakuVisibility) // 是否显示底部弹幕
            .setL2RDanmakuVisibility(rollDanmakuVisibility) // 是否显示左右滚动弹幕
            .setR2LDanmakuVisibility(rollDanmakuVisibility) // 是否显示右左滚动弹幕
            .setSpecialDanmakuVisibility(specialDanmakuVisibility) // 是否显示特殊弹幕
            // 设置弹幕滚动速度
            .setScrollSpeedFactor(danmakuSpeedList[danmakuSpeedIndex]) // 设置弹幕滚动速度系数,只对滚动弹幕有效
            .setScaleTextSize((danmakuFontSizeRatio / 100.0f)) // 弹幕字号
            //设置缓存绘制填充器，默认使用SimpleTextCacheStuffer只支持纯文字显示,
            // 如果需要图文混排请设置SpannedCacheStuffer 如果需要定制其他样式请扩展SimpleTextCacheStuffer|SpannedCacheStuffer
            .setCacheStuffer(SpannedCacheStuffer(), null)
//            .setMaximumLines(maxLInesPair) // 设置最大显示行数
            .setMaximumLines(null) // 设置最大显示行数
            .preventOverlapping(overlappingEnablePair) // 设置防弹幕重叠

        //红色	FE0302	16646914
        //橘红	FF7204	16740868
        //橘黄	FFAA02	16755202
        //淡黄	FFD302	16765698
        //黄色	FFFF00	16776960
        //草绿	A0EE00	10546688
        //绿色	00CD00	52480
        //墨绿	019899	104601
        //紫色	4266BE	4351678
        //青色	89D5FF	9022215
        //品红	CC0273	13369971
        //黑色	222222	2236962
        //灰色	9B9B9B	10197915
        //白色	FFFFFF	16777215
        /*if (colorsDanmakuVisibility) {
            // 0xFFFFFF
            mContext.setColorValueWhiteList(16777215, 16646914, 16740868, 16755202, 16765698, 16776960, 10546688, 52480, 104601,
                4351678, 9022215, 13369971, 2236962, 10197915)
        } else {
            mContext.setColorValueWhiteList(16777215)
        }*/

        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "mDanmakuView not null")
        //mParser = createParser(context!!.openFileInput("C:\\Users\\lcj\\Desktop\\danmu.json"))
        var inStream: InputStream? = null
        if (danmakuUrl.isNotEmpty()) {
            //打开文件
            val file = File(danmakuUrl)
            if (file.exists() && file.isFile) {
                inStream = FileInputStream(file)
            }
        }
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "inStream: ${inStream.toString()}")
        if (inStream != null) {
            mParser = createParser(inStream)
        }

        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "xml："+context!!.resources)
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "mParser：$mParser")
        mDanmakuView.setCallback(object : DrawHandler.Callback {
            override fun updateTimer(timer: DanmakuTimer) {}
            override fun drawingFinished() {}
            override fun danmakuShown(danmaku: BaseDanmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
            }
            override fun prepared() {
                if (isStart) {
                    mDanmakuView.start()
                }
                Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "mDanmakuView start")
            }

        })

        mDanmakuView.onDanmakuClickListener = object : IDanmakuView.OnDanmakuClickListener {
            override fun onDanmakuClick(danmakus: IDanmakus): Boolean {
                Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "onDanmakuClick: danmakus size: ${danmakus.size()}" )
                val latest = danmakus.last()
                if (null != latest) {
                    Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "onDanmakuClick: text of latest danmaku: ${latest.text}")
                    return true
                }
                return false
            }

            override fun onDanmakuLongClick(danmakus: IDanmakus): Boolean {
                return false
            }

            override fun onViewClick(view: IDanmakuView): Boolean {
                Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "点击了弹幕内容：")
                return false
            }
        }
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "mDanmakuView prepare")
        mDanmakuView.prepare(mParser, mContext)
        if (isShowCache) {
            mDanmakuView.showFPS(true)
        }
        if (isShowCache) {
            mDanmakuView.enableDanmakuDrawingCache(true)
        }
    }

    /**
     * 创建解析器
     */
    private fun createParser(stream : InputStream) : BaseDanmakuParser{
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "createParser")
        if (stream == null) {
            Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "stream is null")
            return object : BaseDanmakuParser() {
                override fun parse(): Danmakus {
                    return Danmakus()
                }
            }
        }
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "stream not null，准备读取xml")
        val loader : ILoader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "loader加载：$loader")
        try {
            loader.load(stream)
            Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "读取xml")
        } catch (e : IllegalDataException) {
            Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "解析失败")
            e.printStackTrace()
        }
        val parser : BaseDanmakuParser = BiliDanmakuParser()
        val dataSource = loader.dataSource
        parser.load(dataSource)
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "parser:$parser")
        return parser
    }

    /**
     * 启动弹幕
     */
    override fun startDanmaku(position: Long?) {
        if (mDanmakuView.isPrepared) {
            try {
                if (position == null) {
                    mDanmakuView.start()
                } else {
                    mDanmakuView.start(position)
                }
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "startDanmaku error: $e")
            }
        }
    }

    /**
     * 暂停弹幕
     */
    override fun pauseDanmaKu() {
        if (mDanmakuView.isPrepared && !mDanmakuView.isPaused) {
            try {
                mDanmakuView.pause()
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "pauseDanmaKu error: $e")
            }
        }
    }

    /**
     * 继续弹幕
     */
    override fun resumeDanmaku() {
        if (mDanmakuView.isPrepared && mDanmakuView.isPaused) {
            try {
                mDanmakuView.resume()
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "resumeDanmaku error: $e")
            }
        }
    }

    /**
     * 发送弹幕
     */
    override fun sendDanmaku(isLive: Boolean, text: String) {
        val danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL) ?: return
        danmaku.text = text
        danmaku.padding = 5
        danmaku.priority = 0 // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = isLive
        danmaku.time = mDanmakuView.currentTime + 1200
        danmaku.textSize = 25f * (mParser!!.displayer.density - 0.6f)
        danmaku.textColor = Color.RED
        danmaku.textShadowColor = Color.WHITE
        // danmaku.underlineColor = Color.GREEN;
        danmaku.borderColor = Color.GREEN
        mDanmakuView.addDanmaku(danmaku)
    }

    /**
     * 获取当前弹幕时间
     */
    override fun danmakuCurrentTime() : Long {
        var currentTime: Long? = null
        if (mDanmakuView.isPrepared) {
            currentTime =  mDanmakuView.currentTime
        }
        return currentTime ?: 0
    }

    /**
     * 弹幕跳转
     */
    override fun danmaKuSeekTo(position: Long) {
        if (mDanmakuView.isPrepared) {
            try {
                mDanmakuView.seekTo(position)
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "danmaKuSeekTo error: $e")
            }
        }
    }

    /**
     * 显示或隐藏
     */
    override fun setDanmaKuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                if (visible) {
                    if (!mDanmakuView.isShown) {
                        mDanmakuView.show()
                    }
                } else {
                    if (mDanmakuView.isShown) {
                        mDanmakuView.hide()
                    }
                }
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setDanmaKuVisibility error: $e")
            }
        }
    }

    /***
     * 设置弹幕透明的
     */
    override fun setDanmakuAlphaRatio(danmakuAlphaRatio: Int) {
        if (mDanmakuView.isPrepared) {
            try {
                val alphaRatio = (danmakuAlphaRatio / 100.0f)
                mContext.setDanmakuTransparency(alphaRatio)
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setDanmakuAlphaRatio error: $e")
            }
        }
    }

    /**
     * 设置显示区域（区域下标）
     */
    override fun setDanmakuDisplayArea(danmakuDisplayAreaIndex: Int) {
        TODO("Not yet implemented")
    }

    /**
     * 设置弹幕文字大小（百分比）
     */
    override fun setDanmakuScaleTextSize(danmakuFontSizeRatio: Int) {
        Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setDanmakuScaleTextSize fontSize: ${danmakuFontSizeRatio}, mDanmakuView.isPrepared:${mDanmakuView.isPrepared}")
        if (mDanmakuView.isPrepared) {
            try {
                val fontSizeRatio = (danmakuFontSizeRatio / 100.0f)
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setDanmakuScaleTextSize fontSizeRatio: $fontSizeRatio")
                mContext.setScaleTextSize(fontSizeRatio)
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setDanmakuScaleTextSize error: $e")
            }
        }
    }

    /**
     * 设置弹幕滚动速度
     */
    override fun setDanmakuSpeed(danmakuSpeedIndex: Int, playSpeed: Float) {
        if (mDanmakuView.isPrepared) {
            try {
                var index: Int = danmakuSpeedIndex
                if (danmakuSpeedIndex >= danmakuSpeedListTotal) {
                    index = danmakuSpeedListTotal - 1
                }
                val speed: Float = danmakuSpeedList[index]
                mContext.setScrollSpeedFactor(speed / playSpeed)
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setDanmakuSpeed error: $e")
            }
        }
    }


    /**
     * 设置是否启用合并重复弹幕
     */
    override fun setDuplicateMergingEnabled(merge: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.isDuplicateMergingEnabled = merge
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setDuplicateMergingEnabled error: $e")
            }
        }
    }

    /**
     * 设置是否显示顶部固定弹幕
     */
    override fun setFixedTopDanmakuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.ftDanmakuVisibility = visible
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setFTDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示底部固定弹幕
     */
    override fun setFixedBottomDanmakuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.fbDanmakuVisibility = visible
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setFBDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示滚动弹幕
     */
    override fun setRollDanmakuVisibility(visible: Boolean) {
        Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setL2RDanmakuVisibility visible: $visible, isPrepared: ${mDanmakuView.isPrepared}")
        if (mDanmakuView.isPrepared) {
            Log.d(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setL2RDanmakuVisibility entry")
            try {
                mContext.L2RDanmakuVisibility = visible
                mContext.R2LDanmakuVisibility = visible
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setL2RDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示特殊弹幕
     */
    override fun setSpecialDanmakuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.SpecialDanmakuVisibility = visible
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setSpecialDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 是否显示彩色弹幕
     */
    override fun setColorsDanmakuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                if (visible) {
                    mContext.setColorValueWhiteList(16777215, 16646914, 16740868, 16755202, 16765698, 16776960, 10546688, 52480, 104601,
                        4351678, 9022215, 13369971, 2236962, 10197915)
                } else {
                    mContext.setColorValueWhiteList(16777215)
                }
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(DanmakuConstant.BILI_DANMAKU_LOG_TAG, "setColorsDanmakuVisibility error: $e")
            }
        }
    }

}