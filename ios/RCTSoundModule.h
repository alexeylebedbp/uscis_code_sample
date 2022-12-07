//
//  RCTSoundModule.h
//  mob
//
//  Created by Anthony Lin on 2/23/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

@interface RCTSoundModule : NSObject <RCTBridgeModule, AVAudioPlayerDelegate>
@end
