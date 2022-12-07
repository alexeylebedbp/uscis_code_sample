//
//  UIViewController+MainViewController.h
//  mob
//
//  Created by Rostislav on 15.07.2021.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <StoreKit/StoreKit.h>

NS_ASSUME_NONNULL_BEGIN

//@interface RCTMemoryModule: NSObject <RCTBridgeModule>
@interface MainViewController : UIViewController <RCTBridgeModule, SKStoreProductViewControllerDelegate>
- (BOOL *)isMounted;
- (void)setAppIsMounted:(BOOL *)isMounted;
- (void)setup:(RCTRootView *)rootView name:(NSString *)name nibName:(NSString *)nibName;
- (void)splash:(NSString *)name text:(NSString *)text;
- (void)showSplash:(NSString *)name text:(NSString *)text;
- (void)hideSplash:(NSString *)name;
- (void)isShowing:(NSString *)name callback:(RCTResponseSenderBlock)callback;
- (void)changeText:(NSString *)name text:(NSString *)text;
@end

NS_ASSUME_NONNULL_END
