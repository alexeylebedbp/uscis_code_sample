//
//  RCTUserDefaultsModule.m
//  mob
//
//  Created by Anthony Lin on 4/28/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RCTWidgetModule.h"
#import "WidgetKitHelper-Swift.h"

@implementation RCTWidgetModule

RCT_EXPORT_MODULE(WidgetModule);

RCT_EXPORT_METHOD(update) {
  if(@available(iOS 14, *)){
    WidgetKitHelper * widgetKitHelper = [[WidgetKitHelper alloc] init];
    [widgetKitHelper reloadAllWidgets];
    NSLog(@"ReactNative Called ReloadAllWidget");
  }
}

@end
