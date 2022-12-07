//
//  RCTSoundModule.m
//  mob
//
//  Created by Anthony Lin on 2/23/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import "RCTCallModule.h"
#import <UIKit/UIKit.h>

@implementation RCTCallModule

static NSString *lastFakeCallId;
static long receivingTime;

RCT_EXPORT_MODULE(CallModule);

- (NSString *) getLastFakeCallId{
  return lastFakeCallId;
}

- (void) setReceivingTime{
  receivingTime = (long)[[NSDate date] timeIntervalSince1970];
}

RCT_EXPORT_METHOD(fakeCallShown: (NSString *)callId)
{
  lastFakeCallId = callId;
}

RCT_EXPORT_METHOD(getReceivingTime: (RCTResponseSenderBlock)callback)
{
  callback(@[[NSNumber numberWithLong:receivingTime * 1000]]);
}

@end
