//
//  RCTSoundModule.m
//  mob
//
//  Created by Anthony Lin on 2/23/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import "RCTSoundModule.h"
#import <UIKit/UIKit.h>

@implementation RCTSoundModule

NSString *savedSoundCategory;

- (id) init
{
  self = [super init];
  savedSoundCategory = AVAudioSessionCategoryPlayback;
  return self;
}

RCT_EXPORT_MODULE(SoundModule);

RCT_EXPORT_METHOD(checkSoundCategory: (RCTResponseSenderBlock)callback)
{
  callback(@[[NSNull null], savedSoundCategory]);
}

RCT_EXPORT_METHOD(playSoundFromSpeaker) {
  NSError *error;
  AVAudioSession *audioSession = [AVAudioSession sharedInstance];
  [audioSession setCategory: AVAudioSessionCategoryPlayback error:&error];
  
  dispatch_async(dispatch_get_main_queue(), ^{
    [UIDevice currentDevice].proximityMonitoringEnabled = NO;
  });
}

RCT_EXPORT_METHOD(playSoundFromEar) {
  NSError *error;
  AVAudioSession *audioSession = [AVAudioSession sharedInstance];
  [audioSession setCategory: AVAudioSessionCategoryPlayAndRecord error:&error];
  
  dispatch_async(dispatch_get_main_queue(), ^{
    [UIDevice currentDevice].proximityMonitoringEnabled = YES;
  });
}

RCT_EXPORT_METHOD(setSpeakerPhoneMode: (BOOL)isSpeakerPhoneOn) {
  if (isSpeakerPhoneOn) {
      savedSoundCategory = AVAudioSessionCategoryPlayback;
  } else {
      savedSoundCategory = AVAudioSessionCategoryPlayAndRecord;
  }
}

RCT_EXPORT_METHOD(turnOnProximitySensor) {
  dispatch_async(dispatch_get_main_queue(), ^{
    [UIDevice currentDevice].proximityMonitoringEnabled = YES;
  });
}

RCT_EXPORT_METHOD(turnOffProximitySensor) {
  dispatch_async(dispatch_get_main_queue(), ^{
    [UIDevice currentDevice].proximityMonitoringEnabled = NO;
  });
}

@end
