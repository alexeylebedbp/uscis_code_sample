//
//  UIViewController+MainViewController.m
//  mob
//
//  Created by Rostislav on 15.07.2021.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#import <React/RCTBridge.h>
#import <React/RCTBundleURLProvider.h>
#import <React/RCTRootView.h>
#import <StoreKit/StoreKit.h>

#import "RNCallKeep.h"
#import "MainViewController.h"
#import "LottieAnimation-Swift.h"

@implementation MainViewController

BOOL *appIsMounted = false;
NSDictionary *splashViews = nil;

RCT_EXPORT_MODULE(MainViewController);

RCT_EXPORT_METHOD(setAppIsMounted: (BOOL *)isMounted) {
  appIsMounted = isMounted;
}

RCT_EXPORT_METHOD(splash: (NSString *)name text:(NSString *)text) {
  [self _showSplash:name text:text];
}

RCT_EXPORT_METHOD(showSplash: (NSString *)name text:(NSString *)text) {
  UIApplicationState state = [[UIApplication sharedApplication] applicationState];
  if (state == UIApplicationStateActive) return;

  [self _showSplash:name text:text];
}

RCT_EXPORT_METHOD(hideSplash: (NSString *)name) {
  [self _hideSplash:name];
}

RCT_EXPORT_METHOD(isShowing: (NSString *)name callback:(RCTResponseSenderBlock)callback) {
  bool isShowingBool = [splashViews objectForKey:name] != nil;
  callback(@[@(isShowingBool)]);
}

RCT_EXPORT_METHOD(changeText:(NSString *)name text:(NSString *)text) {
  dispatch_async(dispatch_get_main_queue(), ^{
    UIView *splashView = [splashViews objectForKey:name];
    UILabel *textView = (UILabel *)[splashView viewWithTag:1];
    [textView setText:text];
  });
}

RCT_EXPORT_METHOD(openStoreView){
  // Appstoreconnect -> App Information -> Apple ID
  [self openStoreProductViewControllerWithAppleId:1481252289];
}

- (BOOL *)isMounted {
  return appIsMounted;
}

- (void)openStoreProductViewControllerWithAppleId:(NSInteger)appleId {
    dispatch_async(dispatch_get_main_queue(), ^{
      SKStoreProductViewController *storeViewController = [[SKStoreProductViewController alloc] init];

      storeViewController.delegate = self;

      NSNumber *identifier = [NSNumber numberWithInteger:appleId];

      NSDictionary *parameters = @{ SKStoreProductParameterITunesItemIdentifier:identifier };
      UIViewController *controller = [UIApplication sharedApplication].keyWindow.rootViewController;
      [storeViewController loadProductWithParameters:parameters
                                     completionBlock:^(BOOL result, NSError *error) {
                                         if (!result) {
                                             NSLog(@"SKStoreProductViewController: %@", error);
                                         }
                                     }];
      [controller presentViewController:storeViewController animated:YES completion:nil];
    });
}

- (void)productViewControllerDidFinish:(SKStoreProductViewController *)viewController
{
    [viewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)setup:(RCTRootView *)rootView name:(NSString *)name nibName:(NSString *)nibName {
    self.view = rootView;
  
    UIView *splashView = [[[NSBundle mainBundle] loadNibNamed:nibName owner:self options:nil] lastObject];
  
    splashView.hidden = YES;
    
    LottieAnimation *lottieAnimation = [[LottieAnimation alloc] init];
    [lottieAnimation addTo:splashView];

    [self.view addSubview:splashView];

    if (splashViews == nil) {
      splashViews = [[NSMutableDictionary alloc] init];
    }
  
    [splashViews setValue:splashView forKey:name];
}

- (void) _showSplash:(NSString *)name text:(NSString *)text {
    dispatch_async(dispatch_get_main_queue(), ^{
      UIView *splashView = [splashViews objectForKey:name];
      [splashView setHidden:NO];

      UILabel *textView = (UILabel *)[splashView viewWithTag:1];
      [textView setText:text];
    });
}

- (void) _hideSplash:(NSString *)name {
    dispatch_async(dispatch_get_main_queue(), ^{
      UIView *splashView = [splashViews objectForKey:name];
      [splashView setHidden:YES];
    });
}

@end
