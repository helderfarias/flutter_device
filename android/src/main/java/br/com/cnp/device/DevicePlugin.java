package br.com.cnp.device;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.FeatureInfo;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.BatteryManager;
import android.provider.Settings;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.text.TextUtils;
import android.app.ActivityManager;
import android.util.DisplayMetrics;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import java.security.*;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import android.util.Base64;

public class DevicePlugin implements FlutterPlugin, MethodCallHandler {

  private MethodChannel channel;

  private Context context;

  private Map<String, String> constants;

  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
    this.context = binding.getApplicationContext();
    this.channel = new MethodChannel(binding.getFlutterEngine().getDartExecutor(), "br.com.cnp.flutter.plugins/device");
    this.channel.setMethodCallHandler(this);
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "br.com.cnp.flutter.plugins/device");
    channel.setMethodCallHandler(new DevicePlugin(registrar.context()));
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
    this.channel.setMethodCallHandler(null);
    this.context = null;
  }

  public DevicePlugin() {
    this.constants = generateConstants();
  }

  public DevicePlugin(Context context) {
    this.context = context;
    this.constants = generateConstants();
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getCodeGen")) {
      getCodeGen(result);
      return;
    }

    if (call.method.equals("getPackageInfo")) {
      getPackageInfo(call, result);
      return;
    }

    result.notImplemented();
  }

  private void getCodeGen(Result result) {
    Map<String, String> map = new HashMap<>();
    String plain = UUID.randomUUID().toString();
    String hash = toHash(plain);
    String encoded = Base64.encodeToString(hash.getBytes(), Base64.DEFAULT | Base64.NO_WRAP);
    map.put("plain", plain);
    map.put("hash", encoded.toString());
    result.success(map);
  }

  private void getPackageInfo(MethodCall call, Result result) {
    Map<String, String> map = new HashMap<>();
    map.put("package", this.constants.get("package"));
    map.put("fingerprint", this.constants.get("fingerprint"));
    map.put("version", this.constants.get("versioncode"));
    map.put("X-Platform", "android");
    map.put("X-Android-Package", this.constants.get("package"));
    map.put("X-Android-Cert", this.constants.get("fingerprint"));
    map.put("X-Android-Version", this.constants.get("versioncode"));
    result.success(map);
  }

  private Map<String, String> generateConstants() {
    HashMap<String, String> constants = new HashMap<String, String>();
    PackageManager packageManager = this.context.getPackageManager();
    String packageName = this.context.getPackageName();
    String fingerprint = this.getSignature(packageManager, packageName);
    int versionCode = this.getVersionCode(packageManager, packageName);
    constants.put("package", packageName);
    constants.put("fingerprint", fingerprint);
    constants.put("versioncode", String.valueOf(versionCode));
    return constants;
  }

  private static int getVersionCode(PackageManager pm, String packageName) {
    try {
      PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
      if (packageInfo == null) {
        return 0;
      }
      return packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      return 0;
    }
  }

  private static String getSignature(PackageManager pm, String packageName) {
    try {
      PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
      if (packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.length == 0
          || packageInfo.signatures[0] == null) {
        return null;
      }
      return signatureDigest(packageInfo.signatures[0]);
    } catch (PackageManager.NameNotFoundException e) {
      return null;
    }
  }

  private static String signatureDigest(Signature sig) {
    byte[] signature = sig.toByteArray();
    try {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      byte[] digest = md.digest(signature);
      return bytesToHex(digest);
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
  }

  private static String bytesToHex(byte[] bytes) {
    final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    char[] hexChars = new char[bytes.length * 2];
    int v;
    for (int j = 0; j < bytes.length; j++) {
      v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  private String toHash(String plain) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(plain.getBytes("UTF-8"));
      byte[] digest = md.digest();
      return String.format("%064x", new java.math.BigInteger(1, digest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
