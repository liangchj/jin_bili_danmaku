import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
typedef OnViewCreated = Function(CustomViewController);

class JinBiliDanmakuView extends StatefulWidget {
  const JinBiliDanmakuView(
      {Key? key,
      required this.danmakuUrl,
      this.isShowFPS = false,
      this.isShowCache = false,
      this.colorsDanmakuVisibility = true,
      this.extendParams, required this.onViewCreated})
      : super(key: key);
  final OnViewCreated onViewCreated;
  final String danmakuUrl;
  final bool isShowFPS;
  final bool isShowCache;
  final bool colorsDanmakuVisibility;
  final Map<String, dynamic>? extendParams;

  @override
  State<JinBiliDanmakuView> createState() => _JinBiliDanmakuViewState();
}

class _JinBiliDanmakuViewState extends State<JinBiliDanmakuView> {
  late MethodChannel _channel;
  late Map<String, dynamic> params;
  @override
  void initState() {
    // TODO: implement initState
    params = {
      'danmakuUrl': widget.danmakuUrl,
      "isShowFPS": widget.isShowFPS,
      "isShowCache": widget.isShowCache,
      "colorsDanmakuVisibility": widget.colorsDanmakuVisibility
    };
    if (widget.extendParams != null) {
      params.addAll(widget.extendParams!);
    }
    super.initState();
  }
  @override
  Widget build(BuildContext context) {
    return AndroidView(viewType: "ANDROID/JIN_BILI_DANMAKU_VIEW_ID",
      creationParams: params,
      onPlatformViewCreated: _onPlatformViewCreated,
      creationParamsCodec: const StandardMessageCodec(),
      hitTestBehavior: PlatformViewHitTestBehavior.transparent,
    );
  }

  void _onPlatformViewCreated(int id) {
    _channel = const MethodChannel('JIN_BILI_DANMAKU_METHOD_CHANNEL');
    final controller = CustomViewController._(
      _channel,
    );
    widget.onViewCreated(controller);
  }
}

class CustomViewController {
  final MethodChannel _channel;
  final StreamController<dynamic> _controller = StreamController<dynamic>();

  CustomViewController._(
      this._channel,
      ) {
    _channel.setMethodCallHandler(
          (call) async {
        switch (call.method) {
          case 'AndroidViewCreateSuccess':
          // 从native端获取数据
            final result = call.arguments as bool;
            print("返回结果:$result");
            _controller.sink.add(result);
            break;
        }
      },
    );
  }

  Stream<dynamic> get customDataStream => _controller.stream;
  Future<bool?> sendDanmaku(String danmakuText) async {
    return await _channel.invokeMethod<bool>('startDanmaku', {'danmakuText': danmakuText});
  }



  Future<bool?> startDanmaku(String? startTime) async {
    return await _channel.invokeMethod<bool>('startDanmaku', {'time': startTime});
  }


  Future<bool?> pauseDanmaKu() async {
    return await _channel.invokeMethod<bool>('pauseDanmaKu');
  }


  Future<bool?> resumeDanmaku() async {
    return await _channel.invokeMethod<bool>('resumeDanmaku');
  }


  Future<bool?> danmaKuSeekTo(String? time) async {
    return await _channel.invokeMethod<bool>('danmaKuSeekTo', {'time': time});
  }


  Future<bool?> setDanmaKuVisibility(bool visible) async {
    return await _channel.invokeMethod<bool>('setDanmaKuVisibility', {'visible': visible});
  }


  Future<bool?> setDanmakuAlphaRatio(int danmakuAlphaRatio) async {
    return await _channel.invokeMethod<bool>('setDanmakuAlphaRatio', {'danmakuAlphaRatio': danmakuAlphaRatio});
  }


  Future<bool?> setDanmakuDisplayArea(int danmakuDisplayAreaIndex) async {
    return await _channel.invokeMethod<bool>('setDanmakuDisplayArea', {'danmakuDisplayAreaIndex': danmakuDisplayAreaIndex});
  }


  Future<bool?> setDanmakuScaleTextSize(int danmakuFontSizeRatio) async {
    return await _channel.invokeMethod<bool>('setDanmakuScaleTextSize', {'danmakuFontSizeRatio': danmakuFontSizeRatio});
  }


  Future<bool?> setDanmakuSpeed(int danmakuSpeedIndex, double playSpeed) async {
    return await _channel.invokeMethod<bool>('setDanmakuSpeed', {'danmakuSpeedIndex': danmakuSpeedIndex, "playSpeed": playSpeed});
  }


  Future<bool?> setDuplicateMergingEnabled(bool flag) async {
    return await _channel.invokeMethod<bool>('setDuplicateMergingEnabled', {'flag': flag});
  }


  Future<bool?> setFixedTopDanmakuVisibility(bool visible) async {
    return await _channel.invokeMethod<bool>('setFixedTopDanmakuVisibility', {'visible': visible});
  }


  Future<bool?> setRollDanmakuVisibility(bool visible) async {
    return await _channel.invokeMethod<bool>('setRollDanmakuVisibility', {'visible': visible});
  }


  Future<bool?> setFixedBottomDanmakuVisibility(bool visible) async {
    return await _channel.invokeMethod<bool>('setFixedBottomDanmakuVisibility', {'visible': visible});
  }


  Future<bool?> setSpecialDanmakuVisibility(bool visible) async {
    return await _channel.invokeMethod<bool>('setSpecialDanmakuVisibility', {'visible': visible});
  }


  Future<bool?> setColorsDanmakuVisibility(bool visible) async {
    return await _channel.invokeMethod<bool>('setColorsDanmakuVisibility', {'visible': visible});
  }
}
