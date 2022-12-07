//
//  NotificationViewController.swift
//  PushNotificationContent
//
//  Created by Maxim Vovenko on 7/7/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

import UIKit
import UserNotifications
import UserNotificationsUI

class NotificationViewController: UIViewController, UNNotificationContentExtension {

    @IBOutlet var label: UILabel?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any required interface initialization here.
    }
    
    func didReceive(_ notification: UNNotification) {
      //Suppressing Interface here to prevent user seeing fullscreen image of attachment
      //self.label?.text = notification.request.content.body
    }

}
