#import "DevicePlugin.h"
#include <CommonCrypto/CommonDigest.h>

@implementation DevicePlugin

typedef unsigned char (*DIGEST_FUNCTION)(const void *data, CC_LONG len, unsigned char *md);

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"br.com.cnp.flutter.plugins/device"
            binaryMessenger:[registrar messenger]];
  DevicePlugin* instance = [[DevicePlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getCodeGen" isEqualToString:call.method]) {
    [self getCodeGen:result];
    return;
  }

  if ([@"getPackageInfo" isEqualToString:call.method]) {
    [self getPackageInfo:result];
    return;
  }

  result(FlutterMethodNotImplemented);
}

- (void) getCodeGen:(FlutterResult)result
{
  NSMutableDictionary *map = [[NSMutableDictionary alloc] init];
  NSUUID *uuid = [NSUUID UUID];
  NSString *toPlain = [uuid UUIDString];
  NSData *toHash = [[self calcHash:toPlain withDigestFunction:CC_SHA256 withDigestLength: CC_SHA256_DIGEST_LENGTH] dataUsingEncoding:NSUTF8StringEncoding];
  map[@"plain"] = toPlain;
  map[@"hash"] = [toHash base64EncodedStringWithOptions:0];
  result(map);
}

- (void) getPackageInfo:(FlutterResult)result
{
  NSMutableDictionary *map = [[NSMutableDictionary alloc] init];
  map[@"package"] = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleIdentifierKey];
  map[@"version"] = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)@"CFBundleShortVersionString"];
  result(map);
}

- (NSString*) calcHash: (NSString*) subject withDigestFunction: (DIGEST_FUNCTION) digest withDigestLength: (int) digestLength {
  const char* str = [subject UTF8String];
  unsigned char result[digestLength];
  digest(str, strlen(str), result);

  NSMutableString *ret = [NSMutableString stringWithCapacity:digestLength * 2];
  for(int i = 0; i < digestLength; i++)
  {
    [ret appendFormat:@"%02x",result[i]];
  }

  return ret;
}

@end
