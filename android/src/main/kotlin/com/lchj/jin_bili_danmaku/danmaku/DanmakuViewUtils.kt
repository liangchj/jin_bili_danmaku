package com.lchj.jin_bili_danmaku.danmaku

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import com.lchj.jin_bili_danmaku.DanmakuConstant
import com.lchj.jin_bili_danmaku.danmaku.bili.BiliDanmakuView
import org.apache.commons.collections4.MapUtils


@SuppressLint("StaticFieldLeak")
object DanmakuViewUtils {
    private var biliDanmakuView : BiliDanmakuView? = null
    // AndroidView
    private var danmakuView : View? = null
    private var danmakuUrl : String = ""
    /**
     * 获取弹幕VIEW
     */
    fun getView(context: Context?, args: Map<String, Any>): View? {
        Log.d(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "getView")
        // 是否重新创建
        val rebuild: Boolean = MapUtils.getBoolean(args, "rebuild", false)
        danmakuUrl = MapUtils.getString(args, "danmakuUrl", "")

        // 弹幕路径为空时返回空的view
        if (danmakuUrl.isEmpty()) {
            dispose()
            return null // View(context)
        }
        Log.d(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmakuUrl: $danmakuUrl")
        // 直接重绘/还未创建view/弹幕地址不一致
        if (rebuild || biliDanmakuView == null || this.danmakuUrl != danmakuUrl) {
            biliDanmakuViewDispose() // 先将之前创建的清除
            danmakuView = null
            biliDanmakuView = BiliDanmakuView(context, danmakuUrl, args)
        }
        danmakuView = biliDanmakuView?.getView()
        return danmakuView //?: View(context)
    }

    /**
     * 销毁弹幕VIEW
     */
    fun dispose() {
        Log.d(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "销毁view")
        biliDanmakuViewDispose()
        biliDanmakuView = null
        danmakuView  = null
        danmakuUrl  = ""
    }

    /**
     * 销毁哔哩哔哩弹幕VIEW
     */
    private fun biliDanmakuViewDispose() {
        if (biliDanmakuView != null) {
            biliDanmakuView?.dispose()
        }
    }


    /**
     * 开始弹幕
     */
    fun startDanmaku(position: Long?) {
        try {
            biliDanmakuView?.startDanmaku(position)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "startDanmaku 失败：$e")
        }
    }
    /**
     * 暂停弹幕
     */
    fun pauseDanmaKu() {
        try {
            biliDanmakuView?.pauseDanmaKu()
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "pauseDanmaKu 失败：$e")
        }
    }

    /**
     * 继续弹幕
     */
    fun resumeDanmaku() {
        try {
            biliDanmakuView?.resumeDanmaku()
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "resumeDanmaku 失败：$e")
        }
    }

    /**
     * 发送弹幕
     */
    fun sendDanmaku(isLive: Boolean, text: String) {
        try {
            biliDanmakuView?.sendDanmaku(isLive, text)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "sendDanmaku 失败：$e")
        }
    }

    /**
     * 获取当前弹幕时间
     */
    fun danmakuCurrentTime(): Long? {
        var currentTime: Long? = null
        try {
            currentTime = biliDanmakuView?.danmakuCurrentTime()
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmakuCurrentTime 失败：$e")
        } finally {
            return currentTime
        }
    }

    /**
     * 弹幕跳转
     */
    fun danmaKuSeekTo(position: Long) {
        try {
            biliDanmakuView?.danmaKuSeekTo(position)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmaKuSeekTo 失败：$e")
        }
    }

    /**
     * 显示或隐藏
     */
    fun setDanmaKuVisibility(visible: Boolean) {
        try {
            biliDanmakuView?.setDanmaKuVisibility(visible)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmaKuVisibility 失败：$e")
        }
    }

    /**
     * 设置弹幕透明的（百分比）
     */
    fun setDanmakuAlphaRatio(danmakuAlphaRatio: Int) {
        try {
            biliDanmakuView?.setDanmakuAlphaRatio(danmakuAlphaRatio)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuAlphaRatio 失败：$e")
        }
    }
    /**
     * 设置显示区域（区域下标）
     */
    fun setDanmakuDisplayArea(danmakuDisplayAreaIndex: Int) {
        try {
            biliDanmakuView?.setDanmakuDisplayArea(danmakuDisplayAreaIndex)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuDisplayArea 失败：$e")
        }
    }
    /**
     * 设置弹幕文字大小（百分比）
     */
    fun setDanmakuScaleTextSize(danmakuFontSizeRatio: Int) {
        Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuScaleTextSize util fontSize： $danmakuFontSizeRatio")
        if (danmakuFontSizeRatio == null) {
            return
        }
        try {
            biliDanmakuView?.setDanmakuScaleTextSize(danmakuFontSizeRatio)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuScaleTextSize 失败：$e")
        }
    }

    /**
     * 设置弹幕滚动速度
     */
    fun setDanmakuSpeed(danmakuSpeedIndex: Int, playSpeed: Float) {
        try {
            biliDanmakuView?.setDanmakuSpeed(danmakuSpeedIndex, playSpeed)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuSpeed 失败：$e")
        }
    }

    /**
     * 设置是否启用合并重复弹幕
     */
    fun setDuplicateMergingEnabled(merge: Boolean) {
        try {
            biliDanmakuView?.setDuplicateMergingEnabled(merge)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDuplicateMergingEnabled 失败：$e")
        }
    }


    /**
     * 设置是否显示顶部固定弹幕
     */
    fun setFixedTopDanmakuVisibility(visible: Boolean) {
        try {
            biliDanmakuView?.setFixedTopDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setFixedTopDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 设置是否显示滚动弹幕
     */
    fun setRollDanmakuVisibility(visible: Boolean) {
        try {
            Log.d(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setRollDanmakuVisibility util entry, biliDanmakuView: $biliDanmakuView")
            biliDanmakuView?.setRollDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setRollDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 设置是否显示底部固定弹幕
     */
    fun setFixedBottomDanmakuVisibility(visible: Boolean) {
        try {
            biliDanmakuView?.setFixedBottomDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setFixedBottomDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 设置是否显示特殊弹幕
     */
    fun setSpecialDanmakuVisibility(visible: Boolean) {
        try {
            biliDanmakuView?.setSpecialDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setSpecialDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 是否显示彩色弹幕
     */
    fun setColorsDanmakuVisibility(visible: Boolean) {
        try {
            biliDanmakuView?.setColorsDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setColorsDanmakuVisibility 失败：$e")
        }
    }

}