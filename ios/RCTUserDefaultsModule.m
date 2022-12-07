//
//  RCTUserDefaultsModule.m
//  mob
//
//  Created by Anthony Lin on 4/28/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RCTUserDefaultsModule.h"

@implementation RCTUserDefaultsModule

RCT_EXPORT_MODULE(UserDefaultsModule);

RCT_EXPORT_METHOD(saveToUserDefaults: (NSString*)key :(NSString*)value) {
  NSUserDefaults *userDefaults = [[NSUserDefaults alloc] initWithSuiteName:@"group.com.brightpattern.mobile"];
  [userDefaults setObject:value forKey:key];
}

@end
