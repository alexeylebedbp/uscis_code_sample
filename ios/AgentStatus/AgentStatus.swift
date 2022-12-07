//
//  AgentStatus.swift
//  AgentStatus
//
//  Created by Rostislav on 21.06.2021.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

import WidgetKit
import SwiftUI

struct SimpleEntry: TimelineEntry {
  let date: Date
  let agentState: String
  let title: String
  let reason: String
}
struct Shared: Codable {
  let agentState: String?
  let timeLastStateChange: Double?
  let title: String?
  let reason: String?
}
struct Provider: TimelineProvider {
  
  let sharedDefaults = UserDefaults.init(suiteName: "group.com.brightpattern.mobile")
  
  func constructEntry() -> (SimpleEntry) {
    var date = Date();
    var agentState = "Error no Data"
    var title = ""
    var reason = ""
    
    do {
      let shared = sharedDefaults?.string(forKey: "widgetData")
      if (shared != nil) {
        let data = try JSONDecoder().decode(Shared.self, from: shared!.data(using: .utf8)!)
        if(data.agentState != nil){
          agentState = data.agentState!
        }
        if(data.timeLastStateChange != nil){
          date = Date(timeIntervalSince1970: data.timeLastStateChange!)
        }
        if(data.reason != nil){
          reason = data.reason!
        }
        if(data.title != nil){
          title=data.title!
        }
      }
    } catch {
      print(error)
    }
    
    do {
      let dateString = String(Int64(Date().timeIntervalSince1970))
      sharedDefaults!.set("{\"Widget_Last_Updated\":\""+dateString+"\"}", forKey: "widgetDataLog")
    }
    
    return SimpleEntry(
      date: date,
      agentState: agentState,
      title: title,
      reason: reason
    )
  }
  
  func constructUpdatePolicy() -> TimelineReloadPolicy {
    var name = ""
    do {
      let shared = sharedDefaults?.string(forKey: "widgetData")
      if (shared != nil) {
        let data = try JSONDecoder().decode(Shared.self, from: shared!.data(using: .utf8)!)
        if(data.agentState != nil){
          name = data.agentState!
        }
      }
    } catch {
      print(error)
    }
    switch name{
    case "after_call_work":
      let futuredate = Calendar.current.date(byAdding: .minute, value: 15, to: Date())
      return TimelineReloadPolicy.after(futuredate!)
    case "busy_call":
      let futuredate = Calendar.current.date(byAdding: .minute, value: 1, to: Date()) 
      return TimelineReloadPolicy.after(futuredate!)
    default:
      let futuredate = Calendar.current.date(byAdding: .hour, value: 4, to: Date())
      return TimelineReloadPolicy.after(futuredate!)
    }
  }
  
  func placeholder(in context: Context) -> SimpleEntry {
    SimpleEntry( date: Date(),
                 agentState: "",
                 title:"",
                 reason: ""
    )
  }
  
  func getSnapshot(in context: Context, completion: @escaping (SimpleEntry) -> ()) {
    let entry = constructEntry()
    completion(entry)
  }
  
  func getTimeline(in context: Context, completion: @escaping (Timeline<SimpleEntry>) -> ()) {
    let entry = constructEntry()
    let updatePolicy = constructUpdatePolicy()
    let timeline = Timeline(
      entries: [entry],
      policy: updatePolicy
    )
    completion(timeline)
  }
}


struct AgentStatus: Widget {
  let kind: String = "AgentStatus"
  
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: kind, provider: Provider()) { entry in
      AgentStatusEntryView(entry: entry)
    }
    .configurationDisplayName("Agent Status")
    .description("Displays your current status in brightpattern mobile.")
    .supportedFamilies([.systemSmall])
  }
}

@main
struct WidgetsBundle: WidgetBundle {
  var body: some Widget {
    AgentStatus()
  }
}

