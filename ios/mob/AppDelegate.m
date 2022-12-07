#import "AppDelegate.h"

#import <React/RCTBridge.h>
#import <React/RCTBundleURLProvider.h>
#import <RNCPushNotificationIOS.h>
#import <React/RCTRootView.h>
#import <PushKit/PushKit.h>
#import "RNVoipPushNotificationManager.h"
#import "RNCallKeep.h"
#import <UserNotifications/UserNotifications.h>
#import <AudioToolbox/AudioToolbox.h>
#import <RNCAsyncStorage/RNCAsyncStorage.h>
#import <UNIRest.h>
#import "MainViewController.h"
#import <React/RCTLinkingManager.h>

#import "WidgetKitHelper-Swift.h"

#include "RCTCallModule.h"

@import UIKit;
@import Firebase;

@implementation AppDelegate

UIBackgroundTaskIdentifier backgroundTask;
NSDictionary *lastVOIPPayload;
NSDictionary *lastChatPayload;
MainViewController *mainViewController;
NSTimer *TimeOfActiveUser;
NSString *lastUUID;
NSString *lastCallerName;
int lastTotalQueueTime;
NSDate *receivingTime;
int timerCounter;

-(void) writeLogToFile:(NSString *)aString{
  NSUserDefaults *userDefaults = [[NSUserDefaults alloc] initWithSuiteName:@"group.com.brightpattern.mobile"];
  NSString *value = [userDefaults objectForKey:@"logsAreEnabled"];
  if ([value isEqualToString:@"enabled"]) {
    NSString *filePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSArray* dir = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:filePath error:NULL];
    __block long currentLogFile = 0;
    [dir enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        NSString *filenameWithExtension = (NSString *)obj;
        NSString *filename = [[filenameWithExtension lastPathComponent] stringByDeletingPathExtension];
        NSCharacterSet* notDigits = [[NSCharacterSet decimalDigitCharacterSet] invertedSet];
        if ([filename rangeOfCharacterFromSet:notDigits].location == NSNotFound)
        {
            long filenameLong = [filename longLongValue];
            if (filenameLong > currentLogFile) {
                currentLogFile = filenameLong;
            }
        }
    }];
    
    if (currentLogFile == 0) return;
    NSString *fileName = [NSString stringWithFormat:@"/%ld.log", currentLogFile];
    NSString *fileAtPath = [filePath stringByAppendingString:fileName];
    
    NSDateFormatter *dateFormatter=[[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss:SSS "];
    NSString *dateString = [dateFormatter stringFromDate:[NSDate date]];
    NSString *stringWithDate = [dateString stringByAppendingString:aString];
    NSString *stringWithNewLine = [NSString stringWithFormat:@"\r%@", stringWithDate];
    
    NSFileHandle *fileHandle = [NSFileHandle fileHandleForWritingAtPath:fileAtPath];
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:fileAtPath]) {
        [[NSFileManager defaultManager] createFileAtPath:fileAtPath contents:nil attributes:nil];
        [fileHandle seekToEndOfFile];
        [fileHandle writeData:[stringWithNewLine dataUsingEncoding:NSUTF8StringEncoding]];
    } else {
        NSString *contents = [NSString stringWithContentsOfFile:fileAtPath encoding:NSUTF8StringEncoding error:nil];
        contents = [contents stringByAppendingString:stringWithNewLine];
        [fileHandle seekToEndOfFile];
        [fileHandle writeData:[stringWithNewLine dataUsingEncoding:NSUTF8StringEncoding]];
    }
  }
}

- (void)redirectLogToDocuments
{
     NSArray *allPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
     NSString *documentsDirectory = [allPaths objectAtIndex:0];
     NSString *pathForLog = [documentsDirectory stringByAppendingPathComponent:@"logs.txt"];

     freopen([pathForLog cStringUsingEncoding:NSASCIIStringEncoding], "a+", stderr);
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  
  RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];

  [RNVoipPushNotificationManager voipRegistration];

  RCTRootView *rootView = [[RCTRootView alloc] initWithBridge:bridge
                                                   moduleName:@"mob"
                                            initialProperties:nil];

  rootView.backgroundColor = [[UIColor alloc] initWithRed:1.0f green:1.0f blue:1.0f alpha:1];

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  mainViewController = [[MainViewController alloc] init];
  [mainViewController setup:rootView name:@"splash" nibName:@"SplashView"];
  [mainViewController setup:rootView name:@"connecting" nibName:@"SplashView"];
  if (![mainViewController isMounted]) {
    [mainViewController showSplash:@"splash" text:@"Building the app..."];
  }
  self.window.rootViewController = mainViewController;
  [self.window makeKeyAndVisible];
  UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
  center.delegate = self;
  [FIRApp configure]; // Firebase
  return YES;
}

- (BOOL)application:(UIApplication *)application
   openURL:(NSURL *)url
   options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options
{
  return [RCTLinkingManager application:application openURL:url options:options];
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index" fallbackResource:nil];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

// Required to register for notifications
- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings
{
  [RNCPushNotificationIOS didRegisterUserNotificationSettings:notificationSettings];
}
// Required for the register event.
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
  // Create custom notification actions
  UNNotificationAction* acceptAction = [UNNotificationAction
        actionWithIdentifier:@"accept.action"
        title:@"Accept"
        options:UNNotificationActionOptionForeground];
  
  UNNotificationAction* acceptAndOpenAction = [UNNotificationAction
        actionWithIdentifier:@"acceptAndOpen.action"
        title:@"Accept and Open"
        options:UNNotificationActionOptionForeground];
  
  UNNotificationAction* declineAction = [UNNotificationAction
        actionWithIdentifier:@"decline.action"
        title:@"Decline"
        options:UNNotificationActionOptionDestructive];
  
  UNNotificationCategory* initialMsgCategory = [UNNotificationCategory
        categoryWithIdentifier:@"INITIAL_MSG"
        actions:@[acceptAndOpenAction, acceptAction, declineAction]
        intentIdentifiers:@[]
        hiddenPreviewsBodyPlaceholder:@"New Chat Session"
        options:UNNotificationCategoryOptionHiddenPreviewsShowTitle];
   
  UNNotificationCategory* sessionMsgCategory = [UNNotificationCategory
        categoryWithIdentifier:@"SESSION_MSG"
        actions:@[]
        intentIdentifiers:@[]
        hiddenPreviewsBodyPlaceholder:@"Message"
        options:UNNotificationCategoryOptionHiddenPreviewsShowTitle];

  UNNotificationCategory* workitemCategory = [UNNotificationCategory
        categoryWithIdentifier:@"WORKITEM"
        actions:@[acceptAction, declineAction]
        intentIdentifiers:@[]
        hiddenPreviewsBodyPlaceholder:@"Preview Campaign"
        options:UNNotificationCategoryOptionHiddenPreviewsShowTitle];
    
    UNNotificationCategory* internalMsgCategory = [UNNotificationCategory
        categoryWithIdentifier:@"INTERNAL_MSG"
        actions:@[]
        intentIdentifiers:@[]
        hiddenPreviewsBodyPlaceholder:@"Message"
        options:UNNotificationCategoryOptionHiddenPreviewsShowTitle];
    
    // Register the notification categories.
    UNUserNotificationCenter* center = [UNUserNotificationCenter currentNotificationCenter];
    [center setNotificationCategories:[NSSet setWithObjects:initialMsgCategory, sessionMsgCategory, internalMsgCategory, workitemCategory, nil]];
  
  [RNCPushNotificationIOS didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)applicationWillTerminate:(NSNotification *)notification {
    NSString *logMessage = @"applicationWillTerminate";
    NSLog(logMessage);
    [self writeLogToFile: logMessage];
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center removeAllDeliveredNotifications];
}

- (void)applicationDidEnterBackground:(NSNotification *)notification {
    NSString *logMessage = @"applicationDidEnterBackground";
    NSLog(logMessage);
    [self writeLogToFile: logMessage];
}

- (void)applicationWillResignActive:(NSNotification *)notification {
    NSString *logMessage = @"applicationWillResignActive";
    NSLog(logMessage);
    [self writeLogToFile: logMessage];
}

- (NSString *) getFormattedDataTime {
  NSDateFormatter * formatter =  [[NSDateFormatter alloc] init];
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en"];
  [formatter setLocale:locale];
  [formatter setDateFormat:@"MMMM d' at 'h:mm a"];
  return [formatter stringFromDate:[NSDate date]];
}

- (NSMutableDictionary *)formatNotificationPayload:(NSDictionary *)userInfo {
  NSString *body = @"";
  NSString *name = @"";
  NSString *itemId = @"";
  NSString *userId = @"";
  NSString *category = @"";
  NSString *localCategory = @"";
  NSString *acdState = @"";
  NSString *timestamp = @"";
  NSString *firstName = @"";
  NSString *lastName = @"";
  NSString *mediaType = @"";
  NSString *phoneNumber = @"";

  NSString *notificationCategory = [userInfo objectForKey:@"aps"][@"category"];
  
  if (notificationCategory != nil) category = notificationCategory;
    
  NSString *title = [userInfo objectForKey:@"aps"][@"alert"][@"title"];
  if (title != nil) name = title;
    
  if (
    [notificationCategory isEqualToString:@"SESSION_MSG"] ||
    [notificationCategory isEqualToString: @"INITIAL_MSG"] ||
    [notificationCategory isEqualToString: @"WORKITEM"]
  ) {
    NSString *notificationItemId = userInfo[@"item_id"];
    if (notificationItemId != nil) itemId = notificationItemId;
  }
  
  if ([notificationCategory isEqualToString: @"INTERNAL_MSG"]) {
    NSString *notificationUserId = userInfo[@"user_id"];
    if (notificationUserId != nil) userId = notificationUserId;
  }
  
  if ([notificationCategory isEqualToString: @"MISSED_INTERACTION"] || [notificationCategory isEqualToString: @"INBOUND_CALL_EXTERNAL"]) {
    NSString *notifItemId = [userInfo objectForKey:@"aps"][@"data"][@"item_id"];
    if (notifItemId != nil) itemId = notifItemId;
  }
  
  if ([notificationCategory isEqualToString: @"MISSED_INTERACTION"]) {
    NSString *notifTimestamp = [userInfo objectForKey:@"aps"][@"data"][@"timestamp"];
    if (notifTimestamp != nil) timestamp = notifTimestamp;
    
    NSString *notifFirstName = [userInfo objectForKey:@"aps"][@"data"][@"first_name"];
    if (notifFirstName != nil) firstName = notifFirstName;
    
    NSString *notifLastName = [userInfo objectForKey:@"aps"][@"data"][@"last_name"];
    if (notifLastName != nil) lastName = notifLastName;
    
    NSString *notifMediaType = [userInfo objectForKey:@"aps"][@"data"][@"media_type"];
    if (notifMediaType != nil) mediaType = notifMediaType;
    
    NSString *notifPhone = [userInfo objectForKey:@"aps"][@"data"][@"phonenumber"];
    if (notifPhone != nil) phoneNumber = notifPhone;
  }
  
  if ([notificationCategory isEqualToString: @"AGENT_CHANGE_STATE"]) {
    NSString *state = [userInfo objectForKey:@"aps"][@"data"][@"acd_state"];
    if (state != nil) acdState = state;
  }
  
  NSString *notificationBody = [userInfo objectForKey:@"aps"][@"alert"][@"body"];
  if (notificationBody != nil) body = notificationBody;
  
  if (userInfo[@"localCategory"] != nil) {
    localCategory = userInfo[@"localCategory"];
    if ([localCategory isEqualToString: @"SESSION_MSG"]) {
      NSString *localNotifItemId = userInfo[@"item_id"];
      if (localNotifItemId != nil) itemId = localNotifItemId;
    } else if ([localCategory isEqualToString: @"INTERNAL_MSG"]) {
      NSString *localNotifUserId = userInfo[@"user_id"];
      if (localNotifUserId != nil) userId = localNotifUserId;
    }
  }
  
  NSMutableDictionary   *dict = [NSMutableDictionary new];
  [dict setDictionary: @{
    @"notification": userInfo,
    @"name": name,
    @"text": body,
    @"category": category,
    @"userId": userId,
    @"itemId": itemId,
    @"localCategory": localCategory,
    @"acdState": acdState,
    @"mediaType": mediaType,
    @"firstName": firstName,
    @"lastName": lastName,
    @"timestamp": timestamp,
    @"phoneNumber": phoneNumber,
  }];
  
  
  return dict;
}

- (void) clearInitialMsgNotification:(NSString*)itemId {
  [[UNUserNotificationCenter currentNotificationCenter] getDeliveredNotificationsWithCompletionHandler:^(NSArray<UNNotification *> * _Nonnull notifications) {
            for (UNNotification *notification in notifications) {
              if ([notification.request.content.categoryIdentifier isEqualToString:@"INITIAL_MSG"]) {
                NSString *notificationItemId = notification.request.content.userInfo[@"item_id"];
                  if ([notificationItemId isEqualToString: itemId]) {
                    [[UNUserNotificationCenter currentNotificationCenter] removeDeliveredNotificationsWithIdentifiers:@[notification.request.identifier]];
                  }
                }
             }
          }];
}

// Required for the notification event. You must call the completion handler after handling the remote notification.
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
  NSString *logMessage = [NSString stringWithFormat: @"APPDELEGATE: didReceiveRemoteNotification: debug %@", userInfo[@"aps"][@"alert"]];
  NSLog(logMessage);
  [self writeLogToFile: logMessage];
  
  NSString *notificationAlert = [userInfo objectForKey:@"aps"][@"alert"];
  UIApplicationState state = [[UIApplication sharedApplication] applicationState];
  
  if (notificationAlert != nil && state != UIApplicationStateActive) {
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
    AudioServicesPlaySystemSound(1315);
  }
  
  NSMutableDictionary *mutableUserInfo = [userInfo mutableCopy];
  
  NSString *notificationCategory = [userInfo objectForKey:@"aps"][@"category"];
  
  if ([notificationCategory isEqualToString:@"INITIAL_MSG"]) {
    lastChatPayload = userInfo;
  }
  
  if ([notificationCategory isEqualToString: @"MISSED_INTERACTION"]) {
      NSString *missedItemId = [userInfo objectForKey:@"aps"][@"data"][@"item_id"];
      mutableUserInfo[@"item_id"] = missedItemId;
        
      Boolean isCallActive = [RNCallKeep isCallActive:missedItemId];
      if (isCallActive) {
        [RNCallKeep endCallStatic:missedItemId];
      }
    
    if (missedItemId != nil) {
      [self clearInitialMsgNotification:missedItemId];
    }
  }
  
  NSMutableDictionary *dict = [self formatNotificationPayload: userInfo];
  
  if ([notificationCategory isEqualToString: @"AGENT_CHANGE_STATE"] &&
      [userInfo objectForKey:@"aps"][@"data"][@"acd_state"] != nil) {
    [self writeLogToFile: @"Attempting Widget Update AppDelegate"];
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:@"group.com.brightpattern.mobile"];
    NSString *oldState = nil;
    NSError *error = nil;
    if([defaults stringForKey:@"widgetData"] != nil){
      id object = [NSJSONSerialization
                   JSONObjectWithData:[[defaults stringForKey:@"widgetData"] dataUsingEncoding:NSUTF8StringEncoding]
                   options:0
                   error:&error
                   ];
      if([object isKindOfClass:[NSDictionary class]]){
        NSDictionary *results = object;
        oldState = results[@"agentState"];
      }
    }
    NSString *json = @"";
    if([userInfo objectForKey:@"aps"][@"data"][@"reason"] != nil){
      json = [NSString stringWithFormat:@"{\"agentState\":\"%@\",\"reason\":\"%@\"}", [userInfo objectForKey:@"aps"][@"data"][@"acd_state"], [userInfo objectForKey:@"aps"][@"data"][@"reason"]];
    }else{
      json = [NSString stringWithFormat:@"{\"agentState\":\"%@\"}", [userInfo objectForKey:@"aps"][@"data"][@"acd_state"]];
    }
    if([[userInfo objectForKey:@"aps"][@"data"][@"acd_state"] isEqualToString:oldState]==false){
      [defaults setObject:json forKey:@"widgetData"];
      [defaults synchronize];
      WidgetKitHelper * widgetKitHelper = [[WidgetKitHelper alloc] init];
      [widgetKitHelper reloadAllWidgets];
      [self writeLogToFile: @"Success Widget Update AppDelegate"];
    }else{
      [self writeLogToFile:@"Failure Widget Update AppDelegate"];
    }
  }
  
  // Only send JS notification object from here if app is inactive or if notification is a silent notification
  // if active, it will be sent from willPresentNotification
  NSString *nameLog = [NSString stringWithFormat:@"[AppDelegate][didReceiveRemoteNotification][state][category]: %ld - %@", state, notificationCategory];
  [self writeLogToFile: nameLog];
  if (state != UIApplicationStateActive) {
      [RNCPushNotificationIOS didReceiveRemoteNotification:dict fetchCompletionHandler:completionHandler];
  } else if ([notificationCategory isEqualToString:@"UPDATE_RECOMMENDED"] || [notificationCategory isEqualToString:@"UPDATE_REQUIRED"]) {
      [RNCPushNotificationIOS didReceiveRemoteNotification:dict fetchCompletionHandler:completionHandler];
  }
}

// Required for the registrationError event.
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
  [RNCPushNotificationIOS didFailToRegisterForRemoteNotificationsWithError:error];
}
// Required for the localNotification event.
- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
  [RNCPushNotificationIOS didReceiveLocalNotification:notification];
}

// Handle updated push credentials
- (void)pushRegistry:(PKPushRegistry *)registry didUpdatePushCredentials:(PKPushCredentials *)credentials forType:(NSString *)type {
  // Register VoIP push token (a property of PKPushCredentials) with server
  [RNVoipPushNotificationManager didUpdatePushCredentials:credentials forType:(NSString *)type];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNTextInputNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler {
  UIApplicationState state = [[UIApplication sharedApplication] applicationState];
  if ([response.actionIdentifier isEqualToString:@"decline.action"]) {
    if ((state == UIApplicationStateBackground || state == UIApplicationStateInactive) && (!backgroundTask || backgroundTask == UIBackgroundTaskInvalid)) {
      backgroundTask = [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^{
        //do all clean up job here gets called few seconds before expiration
        [[UIApplication sharedApplication] endBackgroundTask:backgroundTask];
        backgroundTask = UIBackgroundTaskInvalid;
      }];
    }
  }
  
  NSMutableDictionary   *dict = [self formatNotificationPayload: response.notification.request.content.userInfo];
    
  dict[@"action"] = response.actionIdentifier;
  [RNCPushNotificationIOS didReceiveNotificationResponse:response];
  [RNCPushNotificationIOS didReceiveRemoteNotification:dict];
  completionHandler();
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler {
  NSString *logMessage = [NSString stringWithFormat: @"APPDELEGATE: willPresentNotification: withCompletionHandler %@", notification];
  NSLog(logMessage);
  [self writeLogToFile: logMessage];
  
  UIApplicationState state = [[UIApplication sharedApplication] applicationState];
  
  // Do not show some push-notifications if app is active
  if ([notification.request.content.categoryIdentifier isEqualToString:@"SESSION_MSG"] && state == UIApplicationStateActive) {
    completionHandler(UNNotificationPresentationOptionNone);
  } else if ([notification.request.content.categoryIdentifier isEqualToString:@"INTERNAL_MSG"] && state == UIApplicationStateActive) {
    completionHandler(UNNotificationPresentationOptionNone);
  } else if ([notification.request.content.categoryIdentifier isEqualToString:@"INITIAL_MSG"] && state == UIApplicationStateActive) {
    completionHandler(UNNotificationPresentationOptionNone);
  } else if ([notification.request.content.categoryIdentifier isEqualToString:@"MISSED_INTERACTION"] && state == UIApplicationStateActive) {
    completionHandler(UNNotificationPresentationOptionNone);
  } else if ([notification.request.content.categoryIdentifier isEqualToString:@"AGENT_CHANGE_STATE"] && state == UIApplicationStateActive) {
    completionHandler(UNNotificationPresentationOptionNone);
  } else {
    completionHandler(UNNotificationPresentationOptionSound | UNNotificationPresentationOptionAlert | UNNotificationPresentationOptionBadge);
  }
  
  NSMutableDictionary *dict = [self formatNotificationPayload: notification.request.content.userInfo];
    
  NSString *nameLog = [NSString stringWithFormat:@"[AppDelegate][willPresentNotifiication][category]: %@", notification.request.content.categoryIdentifier];
  [self writeLogToFile: nameLog];
    
  [RNCPushNotificationIOS didReceiveRemoteNotification:dict];
}

- (void) onReject:callUUID endCallAction:(CXEndCallAction *)endCallAction {
  NSString *logMessage = @"ON REJECT APP DELEGATE";
  NSLog(logMessage);
  [self writeLogToFile: logMessage];

  RCTResponseSenderBlock completion = ^(NSArray *response) {
    NSDictionary *dictionary = nil;
    if (response.count > 1) {
      NSArray *response1 = response[1];
      if (response1.count > 0) {
        NSArray *response2 = response1[0];
        if (response2.count > 1) {
          dictionary =
              [NSJSONSerialization JSONObjectWithData:[response2[1] dataUsingEncoding:NSUTF8StringEncoding]
                                              options:NSJSONReadingMutableContainers
                                                error:nil];
        }
      }
    }
    
    NSString *sessionId = nil;
    if (![dictionary isEqual: [NSNull null]]) {
      for (NSString *key in dictionary) {
        NSString *value = dictionary[key];
        if ([key isEqualToString:@"sessionId"]) {
          sessionId = value;
        }
      }
    }
    
    NSString *serverOrigin = nil;
    if (![dictionary isEqual: [NSNull null]]) {
      for (NSString *key in dictionary) {
        NSString *value = dictionary[key];
        if ([key isEqualToString:@"serverOrigin"]) {
          serverOrigin = value;
        }
      }
    }
    
    NSString *tenantUrl = nil;
    if (![dictionary isEqual: [NSNull null]]) {
      for (NSString *key in dictionary) {
        NSString *value = dictionary[key];
        if ([key isEqualToString:@"tenantUrl"]) {
          tenantUrl = value;
        }
      }
    }
    
    if (![sessionId isEqual: [NSNull null]] && (![serverOrigin isEqual: [NSNull null]] || ![tenantUrl isEqual: [NSNull null]])) {
      NSString *serverUrl = [serverOrigin length] != 0 ? serverOrigin : tenantUrl;
      NSString *url = [NSString stringWithFormat:@"https://%@/agentdesktop/mobile/agent_notification_result", serverUrl];
      NSDictionary *headers = @{
        @"Content-Type": @"application/json",
        @"Cookie": [NSString stringWithFormat:@"X-BP-SESSION-ID=%@", sessionId],
      };
      NSDictionary *body = @{
        @"action": @"agent_notification_result",
        @"session_id": sessionId,
        @"item_id": callUUID,
        @"rc": @"1",
      };

      UNIHTTPJsonResponse *response = [[UNIRest postEntity:^(UNIBodyRequest *request) {
        [request setUrl:url];
        [request setHeaders:headers];
        [request setBody:[NSJSONSerialization dataWithJSONObject:body options:0 error:nil]];
      }] asJson];
      
      if (response.code == 200) {
        [endCallAction fulfill];
      }
    }
  };
  
  RNCAsyncStorage *asyncStorage = [[RNCAsyncStorage alloc] init];

  dispatch_async(asyncStorage.methodQueue, ^{
    [asyncStorage
        performSelector:@selector(multiGet:callback:)
        withObject:@[@"@bpinc/ad-local-settings-global"]
        withObject:completion];
  });
}

- (BOOL)application:(UIApplication *)application continueUserActivity:(nonnull NSUserActivity *)userActivity
 restorationHandler:(nonnull void (^)(NSArray<id<UIUserActivityRestoring>> * _Nullable))restorationHandler
{
 return [RCTLinkingManager application:application
                  continueUserActivity:userActivity
                    restorationHandler:restorationHandler];
}

// Handle incoming pushes VOIP
//Payload Diverges From Documentation provided by react-native-voip make sure to rename
//the following values
//number -> handle
//name -> callerName
//item_id -> uuid
//In the following code
//NSString *uuid = payload.dictionaryPayload[@"item_id"];
//NSString *callerName = [NSString stringWithFormat:@"%@ (Connecting...)", payload.dictionaryPayload[@"name"]];
//NSString *handle = payload.dictionaryPayload[@"number"];

- (void)setTimer {
  timerCounter = 0;
  TimeOfActiveUser = [NSTimer
  scheduledTimerWithTimeInterval:1.0
                          target:self
                        selector:@selector(actionTimer)
                        userInfo:nil
                         repeats:YES];
}

- (void)stopTimer
{
    timerCounter = 0;
    [TimeOfActiveUser invalidate];
    TimeOfActiveUser = nil;
}

- (void)actionTimer {
  NSDate *date = [NSDate date];
  // NSTimeInterval timeDiff = [date timeIntervalSinceDate:receivingTime];

  [RNCallKeep updateDisplayName: lastUUID
                     callerName: lastCallerName];

  if (timerCounter % 3 == 0) {
    NSString *formattedTimeDiff = [self getWaitTime:(lastTotalQueueTime)];
    
    [RNCallKeep updateDisplayName: lastUUID
                       callerName: [NSString stringWithFormat: @"Wait time: %@", formattedTimeDiff]];
  }

  timerCounter++;
}

- (NSString *)getWaitTime:(int)time {
  int seconds = time / 1000;
  NSDateComponentsFormatter *formatter = [[NSDateComponentsFormatter alloc] init];
  
  if (seconds < 60) {
    formatter.allowedUnits = NSCalendarUnitMinute | NSCalendarUnitSecond;
  } else if (seconds < 60 * 60) {
    formatter.allowedUnits = NSCalendarUnitMinute | NSCalendarUnitSecond;
  } else {
    formatter.allowedUnits = NSCalendarUnitHour | NSCalendarUnitMinute | NSCalendarUnitSecond;
  }
  
  formatter.zeroFormattingBehavior = NSDateComponentsFormatterZeroFormattingBehaviorNone;
  
  return [formatter stringFromTimeInterval:seconds];
}

- (void)pushRegistry:(PKPushRegistry *)registry didReceiveIncomingPushWithPayload:(PKPushPayload *)payload forType:(PKPushType)type withCompletionHandler:(void (^)(void))completion {
  // --- NOTE: apple forced us to invoke callkit ASAP when we receive voip push
  // --- see: react-native-callkeep
  lastVOIPPayload = payload.dictionaryPayload;

  lastUUID = payload.dictionaryPayload[@"item_id"];
  NSString *uuid = payload.dictionaryPayload[@"item_id"];
  NSString *callerName = [NSString stringWithFormat:@"%@", payload.dictionaryPayload[@"name"]];
  NSString *totalQueueTime = payload.dictionaryPayload[@"total_queue_time"];
  NSString *handle = payload.dictionaryPayload[@"number"];
  NSCharacterSet *set = [NSCharacterSet whitespaceCharacterSet];
  if ([[callerName stringByTrimmingCharactersInSet: set] length] == 0) {
    if ([handle length] != 0) {
      callerName = handle;
    } else {
      callerName = @"Anonymous";
    }
  }

  RCTCallModule *callModule = [[RCTCallModule alloc] init];
  
  [callModule setReceivingTime];
  
  if ([uuid isEqualToString:[callModule getLastFakeCallId]]) {
    NSString *logMessage = [NSString stringWithFormat: @"didReceiveIncomingPushWithPayload VOIP notification is hidden because fake VOIP notification was shown. UUID = %@", uuid];
    NSLog(logMessage);
    [self writeLogToFile: logMessage];

    return;
  }
    
  [RNVoipPushNotificationManager addCompletionHandler:uuid completionHandler:completion];
  [RNVoipPushNotificationManager didReceiveIncomingPushWithPayload:payload forType:(NSString *)type];
  
  [RNCallKeep reportNewIncomingCall: uuid
                             handle: handle
                         handleType: @"generic"
                           hasVideo: NO
                localizedCallerName: callerName
                    supportsHolding: YES
                       supportsDTMF: YES
                   supportsGrouping: YES
                 supportsUngrouping: YES
                        fromPushKit: YES
                            payload: nil
              withCompletionHandler: completion
                           onAccept: ^(NSString *callUUID) {
                              if (![mainViewController isMounted]) {
                                [mainViewController showSplash:@"splash" text:@"Building the app..."];
                              }
                              [self stopTimer];
                           }
                           onReject: ^(NSString *callUUID, CXEndCallAction *endCallAction) {
                             [self onReject:callUUID endCallAction:endCallAction];
                             [self stopTimer];
                           }];

  lastCallerName = callerName;
  lastTotalQueueTime = [totalQueueTime intValue];
  receivingTime = [NSDate date];
  [self setTimer];
}

@end
