package com.lchj.jin_bili_danmaku

import android.util.Log
import androidx.annotation.NonNull
import com.lchj.jin_bili_danmaku.danmaku.DanmakuViewUtils
import com.lchj.jin_bili_danmaku.danmaku.JinDanmakuViewFactory

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** JinBiliDanmakuPlugin */
class JinBiliDanmakuPlugin: FlutterPlugin {


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    flutterPluginBinding.platformViewRegistry.registerViewFactory(DanmakuConstant.JIN_BILI_DANMAKU_VIEW_ID, JinDanmakuViewFactory(flutterPluginBinding.binaryMessenger))
  }


  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    DanmakuViewUtils.dispose()
  }
}
