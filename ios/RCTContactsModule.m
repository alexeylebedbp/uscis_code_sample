//
//  ContactsModule.m
//  mob
//
//  Created by Anthony Lin on 10/1/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(ContactsModule, NSObject)
RCT_EXTERN_METHOD(requestContactsPermission)
RCT_EXTERN_METHOD(clearSavedContact)
RCT_EXTERN_METHOD(createContact:(NSString *)firstName lastName:(NSString *)lastName phoneNumber:(NSString *)phoneNumber accessNumber:(NSString *)accessNumber)
@end
