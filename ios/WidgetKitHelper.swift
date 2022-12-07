//
//  WidgetModuleHelper.swift
//  mob
//
//  Created by Rostislav on 23.06.2021.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

#if canImport(WidgetKit)
import WidgetKit
#endif

@objc(WidgetKitHelper)
class WidgetKitHelper: NSObject {

    @objc
    func reloadAllWidgets () {
      if #available(iOS 14.0, *) {
          WidgetCenter.shared.reloadAllTimelines()
      }
    }
}
