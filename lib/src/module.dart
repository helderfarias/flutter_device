import 'dart:async';

import 'package:flutter/services.dart';

class Device {
  static const MethodChannel _channel = const MethodChannel('br.com.cnp.flutter.plugins/device');

  static Future<Map<String, String>> getCodeGen() async {
    return await _channel.invokeMapMethod<String, String>('getCodeGen');
  }

  static Future<Map<String, String>> getPackageInfo() async {
    return await _channel.invokeMapMethod<String, String>('getPackageInfo');
  }
}
