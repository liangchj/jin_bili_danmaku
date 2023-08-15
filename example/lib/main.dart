import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:jin_bili_danmaku/jin_bili_danmaku_view.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var platform = const MethodChannel('JIN_DANMAKU_NATIVE_VIEW');
  late final Widget danmaku;
  CustomViewController? _controller;
  @override
  void initState() {
    super.initState();
  }

  void _onCustomAndroidViewCreated(CustomViewController controller) {
    _controller = controller;
    _controller?.customDataStream.listen((data) {
      //接收到来自Android端的数据
      print('来自Android的数据：$data');
    });
  }
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: JinBiliDanmakuView(danmakuUrl: "/storage/emulated/0/DCIM/1.xml", onViewCreated: _onCustomAndroidViewCreated,),
        ),
      ),
    );
  }
}
