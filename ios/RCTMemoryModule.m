//
//  RCTMemoryModule.m
//  mob
//
//  Created by Anthony Lin on 6/9/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RCTMemoryModule.h"
#include <mach/mach.h>

@implementation RCTMemoryModule

RCT_EXPORT_MODULE(MemoryModule);

RCT_EXPORT_METHOD(getMemoryUsage: (RCTResponseSenderBlock)callback) {
  NSLog(@"getMemoryUsage");
  task_vm_info_data_t vmInfo;
  mach_msg_type_number_t count = TASK_VM_INFO_COUNT;
  kern_return_t err = task_info(mach_task_self(), TASK_VM_INFO, (task_info_t) &vmInfo, &count);
//  if (err != KERN_SUCCESS)
//      return 0;
  
  NSLog(@"Memory vminfo.external %f", vmInfo.external * 0.000001);
  NSLog(@"Memory in use vmInfo.internal (in megabytes): %f", vmInfo.internal * 0.000001);
  
  NSNumber *megabyteDouble = [NSNumber numberWithDouble:vmInfo.internal * 0.000001];

  NSString *megabytes;
  megabytes = [megabyteDouble stringValue];
  
  callback(@[megabytes]);
}

@end
