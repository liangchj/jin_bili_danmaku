package com.lchj.jin_bili_danmaku.danmaku

import android.content.Context
import android.util.Log
import android.view.View
import com.lchj.jin_bili_danmaku.DanmakuConstant
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView


class JinDanmakuView(
    private var context: Context?,
    messenger: BinaryMessenger,
    viewId: Int,
    args: Map<String, Any>?
) :
    PlatformView, MethodChannel.MethodCallHandler {
    private var args: Map<String, Any> = args ?: hashMapOf()


    private var methodChannel: MethodChannel = MethodChannel(messenger, DanmakuConstant.JIN_BILI_DANMAKU_METHOD_CHANNEL)

    init {
        Log.d(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "JinDanmakuView viewId: $viewId")
        methodChannel.setMethodCallHandler(this)
    }
    override fun getView(): View {
        val view = DanmakuViewUtils.getView(context, args)
        sendMessageToFlutter("AndroidViewCreateSuccess",view != null)
        return view ?: View(context)
    }

    private fun sendMessageToFlutter(method: String, msg: Any) {
        methodChannel.invokeMethod(method, msg)
    }

    override fun dispose() {
        DanmakuViewUtils.dispose()
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "sendDanmaku" -> { // 发送弹幕
                try {
                    val danmakuText: String? = call.argument<Long>("danmakuText") as String?
                    if (!danmakuText.isNullOrEmpty()) {
                        DanmakuViewUtils.sendDanmaku(false, danmakuText)
                    }
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "sendDanmaku error: $e")
                    result.success(false)
                }
            }
            "startDanmaku" -> { // 启动弹幕
                try {
                    val msStr: String? = call.argument<Long>("time") as String?
                    DanmakuViewUtils.startDanmaku(if (!msStr.isNullOrEmpty()) msStr.toLong() else null)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "startDanmaku error: $e")
                    result.success(false)
                }
            }
            "pauseDanmaKu" -> { // 暂停弹幕
                try {
                    DanmakuViewUtils.pauseDanmaKu()
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "pauseDanmaKu error: $e")
                    result.success(false)
                }
            }
            "resumeDanmaku" -> { // 继续弹幕
                try {
                    DanmakuViewUtils.resumeDanmaku()
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "resumeDanmaku error: $e")
                    result.success(false)
                }
            }
            "danmaKuSeekTo" -> { // 跳转弹幕
                try {
                    val ms: String = call.argument<String>("time") as String
                    DanmakuViewUtils.danmaKuSeekTo(ms.toLong())
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "danmaKuSeekTo error: $e")
                    result.success(false)
                }
            }
            "setDanmaKuVisibility" -> { // 显示/隐藏弹幕
                try {
                    var visible: Boolean = call.argument<Boolean>("visible")  as Boolean
                    DanmakuViewUtils.setDanmaKuVisibility(visible)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setDanmaKuVisibility error: $e")
                    result.success(false)
                }
            }
            "setDanmakuAlphaRatio" -> { // 设置弹幕透明的（百分比）
                try {
                    val danmakuAlphaRatio: Int = call.argument<Int>("danmakuAlphaRatio") as Int
                    DanmakuViewUtils.setDanmakuAlphaRatio(danmakuAlphaRatio)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setDanmakuAlphaRatio error: $e")
                    result.success(false)
                }
            }
            "setDanmakuDisplayArea" -> { // 设置弹幕显示区域
                try {
                    val danmakuDisplayAreaIndex: Int = call.argument<Int>("danmakuDisplayAreaIndex") as Int
                    DanmakuViewUtils.setDanmakuDisplayArea(danmakuDisplayAreaIndex)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setDanmakuDisplayArea error: $e")
                    result.success(false)
                }
            }
            "setDanmakuScaleTextSize" -> { // 设置字体大小（百分比）
                try {
                    val danmakuFontSizeRatio: Int = call.argument<Int>("danmakuFontSizeRatio") as Int
                    Log.d(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setDanmakuScaleTextSize danmakuFontSizeRatio: $danmakuFontSizeRatio")
                    DanmakuViewUtils.setDanmakuScaleTextSize(danmakuFontSizeRatio)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setDanmakuScaleTextSize error: $e")
                    result.success(false)
                }
            }
            "setDanmakuSpeed" -> { // 设置滚动速度
                try {
                    //播放速度
                    val playSpeed = (call.argument<Long>("playSpeed") as Double?)?.toFloat() ?: 1.0f
                    // 速度下标
                    val danmakuSpeedIndex = (call.argument<Long>("danmakuSpeedIndex")) as Int
                    DanmakuViewUtils.setDanmakuSpeed(danmakuSpeedIndex, playSpeed)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setDanmakuSpeed error: $e")
                    result.success(false)
                }
            }
            "setDuplicateMergingEnabled" -> { // 设置是否启用合并重复弹幕
                try {
                    var flag: Boolean = call.argument<Boolean>("flag") as Boolean
                    DanmakuViewUtils.setDuplicateMergingEnabled(flag)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setDuplicateMergingEnabled error: $e")
                    result.success(false)
                }
            }
            "setFixedTopDanmakuVisibility" -> { // 设置是否显示顶部固定弹幕
                try {
                    var visible: Boolean = call.argument<Boolean>("visible")  as Boolean
                    DanmakuViewUtils.setFixedTopDanmakuVisibility(visible)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setFixedTopDanmakuVisibility error: $e")
                    result.success(false)
                }
            }
            "setRollDanmakuVisibility" -> { // 设置是否显示滚动弹幕
                try {
                    var visible: Boolean = call.argument<Boolean>("visible")  as Boolean
                    Log.d(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setRollDanmakuVisibility visible: $visible")
                    DanmakuViewUtils.setRollDanmakuVisibility(visible)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setRollDanmakuVisibility error: $e")
                    result.success(false)
                }
            }
            "setFixedBottomDanmakuVisibility" -> { // 设置是否显示底部固定弹幕
                try {
                    var visible: Boolean = call.argument<Boolean>("visible")  as Boolean
                    DanmakuViewUtils.setFixedBottomDanmakuVisibility(visible)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setFixedBottomDanmakuVisibility error: $e")
                    result.success(false)
                }
            }
            "setSpecialDanmakuVisibility" -> { // 设置是否显示特殊弹幕
                try {
                    var visible: Boolean = call.argument<Boolean>("visible")  as Boolean
                    DanmakuViewUtils.setSpecialDanmakuVisibility(visible)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setSpecialDanmakuVisibility error: $e")
                    result.success(false)
                }
            }
            "setColorsDanmakuVisibility" -> { // 是否显示彩色弹幕
                try {
                    var visible: Boolean = call.argument<Boolean>("visible")  as Boolean
                    DanmakuViewUtils.setColorsDanmakuVisibility(visible)
                    result.success(true)
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.DANMAKU_VIEW_LOG_TAG, "setColorsDanmakuVisibility error: $e")
                    result.success(false)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

}

// 弹幕view默认有获取和销毁方法
interface IJinDanmakuView {
    fun getView(): View
    fun dispose()

    /**
     * 开始弹幕
     */
    fun startDanmaku(position: Long?)

    /**
     * 暂停弹幕
     */
    fun pauseDanmaKu()

    /**
     * 继续弹幕
     */
    fun resumeDanmaku()

    /**
     * 发送弹幕
     */
    fun sendDanmaku(isLive: Boolean, text: String)

    /**
     * 获取当前弹幕时间
     */
    fun danmakuCurrentTime(): Long

    /**
     * 弹幕跳转
     */
    fun danmaKuSeekTo(position: Long)

    /**
     * 显示或隐藏
     */
    fun setDanmaKuVisibility(visible: Boolean)

    /***
     * 设置弹幕透明的（百分比）
     */
    fun setDanmakuAlphaRatio(danmakuAlphaRatio: Int)

    /**
     * 设置显示区域（区域下标）
     */
    fun setDanmakuDisplayArea(danmakuDisplayAreaIndex: Int)

    /**
     * 设置弹幕文字大小（百分比）
     */
    fun setDanmakuScaleTextSize(danmakuFontSizeRatio: Int)

    /**
     * 设置弹幕滚动速度
     */
    fun setDanmakuSpeed(danmakuSpeedIndex: Int, playSpeed: Float)

    /**
     * 设置是否启用合并重复弹幕
     */
    fun setDuplicateMergingEnabled(merge: Boolean)

    /**
     * 设置是否显示顶部固定弹幕
     */
    fun setFixedTopDanmakuVisibility(visible: Boolean)

    /**
     * 设置是否显示滚动弹幕
     */
    fun setRollDanmakuVisibility(visible: Boolean)

    /**
     * 设置是否显示底部固定弹幕
     */
    fun setFixedBottomDanmakuVisibility(visible: Boolean)

    /**
     * 设置是否显示特殊弹幕
     */
    fun setSpecialDanmakuVisibility(visible: Boolean)

    /**
     * 是否显示彩色弹幕
     */
    fun setColorsDanmakuVisibility(visible: Boolean)
}
