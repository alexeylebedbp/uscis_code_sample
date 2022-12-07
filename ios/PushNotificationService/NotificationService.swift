//
//  NotificationService.swift
//  PushNotificationService
//
//  Created by Anthony Lin on 8/28/20.
//  Copyright © 2020 Brightpattern. All rights reserved.
//

import UserNotifications
import AudioToolbox
#if canImport(WidgetKit)
import WidgetKit
#endif

class NotificationService: UNNotificationServiceExtension {

    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?
  
    func getWaitTime(time: Int) -> String {
      let seconds = time / 1000
      let formatter = DateComponentsFormatter()
      
      var prefix = ""
      
      if (seconds < 1) {
        return "0:00"
      } else if (seconds < 60) {
        prefix = "0:"
        formatter.allowedUnits = [.second]
      } else if (seconds < 60 * 60) {
        formatter.allowedUnits = [.minute, .second]
      } else {
        formatter.allowedUnits = [.hour, .minute, .second]
      }
      formatter.zeroFormattingBehavior = .pad
      
      return prefix + (formatter.string(from: TimeInterval(seconds)) ?? "")
    }

    func getCustomerName(firstName: String?, lastName: String?) -> String {
      var name = ""
      if let checkFirstName = firstName {
        if let checkLastName = lastName {
          name = "\(checkFirstName) \(checkLastName)"
        } else {
          name = checkFirstName
        }
      } else if let checkLastName = lastName {
        name = checkLastName
      } else {
        return "Anonymous"
      }
      if !name.trimmingCharacters(in: .whitespaces).isEmpty {
        return name
      } else {
        return "Anonymous"
      }
    }

    func getNameFromParties(parties: [NSDictionary], displayNameFormat: String?) -> String {
        var name = ""
        for party in parties {
          let partyType = party["type"] as! String
          var isExternalParty = false
          if partyType == "external" {
            isExternalParty = true
          }
          if partyType == "conference_peer" {
              let peerType = party["peer_type"] as! String
              if peerType == "external" {
                isExternalParty = true
              }
          }
          if isExternalParty == true {
            let firstName = party["first_name"] as! String?
            let lastName = party["last_name"] as! String?
            if let checkFormat = displayNameFormat {
              name = formatDisplayName(firstName: firstName, lastName: lastName, displayNameFormat: checkFormat)
            } else {
              name = getCustomerName(firstName: firstName, lastName: lastName)
            }
            return name
          }
        }
        return name
    }
  
    func formatDisplayName(firstName: String?, lastName: String?, displayNameFormat: String) -> String {
        var name = ""
        if let checkFirstName = firstName {
          if let checkLastName = lastName {
            if (displayNameFormat == "1") {
              name = "\(checkLastName) \(checkFirstName)"
            } else if (displayNameFormat == "2") {
              name = "\(checkLastName), \(checkFirstName)"
            } else {
              name = "\(checkFirstName) \(checkLastName)"
            }
          } else {
            name = checkFirstName
          }
        } else if let checkLastName = lastName {
          name = checkLastName
        } else {
          return "Anonymous"
        }
        if !name.trimmingCharacters(in: .whitespaces).isEmpty {
          return name
        } else {
          return "Anonymous"
        }
    }
  
    func getFormattedNameFromTitle(title: String, displayNameFormat: String?) -> String {
      var name = ""
      let nameArray = title.components(separatedBy: " ")
      if (nameArray.count >= 2) {
        if let checkFormat = displayNameFormat {
          name = formatDisplayName(firstName: nameArray[0], lastName: nameArray[nameArray.count - 1], displayNameFormat: checkFormat)
        } else {
          name = getCustomerName(firstName: title, lastName: nil)
        }
      } else {
        name = getCustomerName(firstName: title, lastName: nil)
      }
      return name
    }
  
  func informWidget(request: UNNotificationRequest){
    if #available(iOS 14, *) {
      NSLog("Began Updating Widget from NotificationService")
      if let aps = request.content.userInfo["aps"] as? NSDictionary{
        if let data = aps["data"] as? NSDictionary{
          if let acd_state = data["acd_state"] as? NSString{
            let defaults = UserDefaults(suiteName: "group.com.brightpattern.mobile")
            var name = ""
            do {
              let shared = defaults?.string(forKey: "widgetData")
              if (shared != nil) {
                let data = try JSONDecoder().decode(Shared.self, from: shared!.data(using: .utf8)!)
                name = data.agentState
              }
            }catch {
              print(error)
            }
            if acd_state.compare(name) != .orderedSame{
              var json = ""
              if let reason = data["reason"] as? NSString {
                json = String(format: "{\"agentState\":\"%@\",\"reason\":\"%@\"}", acd_state, reason)
              } else {
                json = String(format: "{\"agentState\":\"%@\"}", acd_state)
              }
              defaults?.setValue(json, forKey: "widgetData")
              WidgetCenter.shared.reloadAllTimelines()
              NSLog("Updated Widget from NotificationService")
            }
          }
        }
      }
    }
  }
  struct Shared: Codable {
    var agentState: String
  }
  
    func formatNumber(num: String) -> String {
      if num.count == 10 {
          return num.replacingOccurrences(of: "(\\d{3})(\\d{3})(\\d+)", with: "($1) $2-$3", options: .regularExpression, range: nil)
      }
      else if num.count == 11 {
          return num.replacingOccurrences(of: "(\\d{1})(\\d{3})(\\d{3})(\\d+)", with: "$1 ($2) $3-$4", options: .regularExpression, range: nil)
      }
      else {
          return num
      }
    }

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        self.contentHandler = contentHandler
        bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)
      
        if let bestAttemptContent = bestAttemptContent {
          // Modify the notification content here...
          
          let userDefaults = UserDefaults(suiteName: "group.com.brightpattern.mobile")
          let displayNameFormat = userDefaults?.string(forKey: "displayNameFormat")
          let category = bestAttemptContent.categoryIdentifier
          
          // Handle notification appearing at top of screen then in the center of locked screen after an inbound call
          // Happens because the notification is shown right before VoIP notification is closed, causing it to appear at the top briefly
          var shouldDelay = false

          if (category == "INITIAL_MSG") {
            if let aps = request.content.userInfo["aps"] as? NSDictionary {
              if aps["parties"] != nil {
                if let parties = aps["parties"] as? [NSDictionary] {
                  let name = getNameFromParties(parties: parties, displayNameFormat: displayNameFormat)
                  bestAttemptContent.title = name
                }
              }
              
              let serviceName = bestAttemptContent.body
              if (serviceName != nil && !serviceName.isEmpty) {
                bestAttemptContent.body = serviceName
              }
              
              var waitTime = 0
              if let waitTimeString = aps["total_queue_time"] as? String {
                waitTime = Int(waitTimeString) ?? 0
              }
              
              if #available(iOSApplicationExtension 15.0, *) {
                bestAttemptContent.interruptionLevel = UNNotificationInterruptionLevel.timeSensitive
              }
              
              let imageName = "message"
              let imageURL = Bundle.main.url(forResource: imageName, withExtension: "png")
              if(imageURL != nil){
                if let attachment = try? UNNotificationAttachment.init(identifier: imageName+".png", url: imageURL!){
                  bestAttemptContent.attachments=[attachment];
                }
              }
              
              bestAttemptContent.sound = UNNotificationSound.init(named: UNNotificationSoundName(rawValue: "initial_msg_notification.wav"))
              
              bestAttemptContent.subtitle = bestAttemptContent.subtitle + ". Wait time: " + getWaitTime(time: waitTime)
            }
          } else if (category == "SESSION_MSG") {
            if let aps = request.content.userInfo["aps"] as? NSDictionary {
              if let partyType = aps["party_type"] as? String {
                if (partyType == "external") {
                  let name = getFormattedNameFromTitle(title: bestAttemptContent.title, displayNameFormat: displayNameFormat)
                  bestAttemptContent.title = name
                } else if (partyType == "scenario") {
                  bestAttemptContent.title = "Scenario"
                } else if let peerType = aps["peer_type"] as? String {
                  if (peerType == "external") {
                    let name = getFormattedNameFromTitle(title: bestAttemptContent.title, displayNameFormat: displayNameFormat)
                    bestAttemptContent.title = name
                  }
                }
              }
            }
          } else if (category == "INTERNAL_MSG") {
            let name = getFormattedNameFromTitle(title: bestAttemptContent.title, displayNameFormat: displayNameFormat)
            bestAttemptContent.title = name
          } else if (category == "AGENT_CHANGE_STATE") {
            if let aps = request.content.userInfo["aps"] as? NSDictionary {
              if let data = aps["data"] as? NSDictionary{
                if let acd_state = data["acd_state"] as? String{
                  var imageName = ""
                  switch acd_state {
                  case "not_ready":
                    imageName="away"
                  case "after_call_work":
                    shouldDelay = true
                    imageName="acw"
                  default:
                    imageName=""
                  }
                  if(imageName != ""){
                    let imageURL = Bundle.main.url(forResource: imageName, withExtension: "png")
                    if(imageURL != nil){
                      if let attachment = try? UNNotificationAttachment.init(identifier: imageName+".png", url: imageURL!){
                        bestAttemptContent.attachments=[attachment];
                      }
                    }
                  }
                }
              }
            }
          } else if (category == "MISSED_INTERACTION") {
            var name = ""
            if let aps = request.content.userInfo["aps"] as? NSDictionary {
              if let data = aps["data"] as? NSDictionary {
                var firstName = ""
                var lastName = ""
                if let first = data["first_name"] as? String {
                  firstName = first
                }
                if let last = data["last_name"] as? String {
                  lastName = last
                }
                // if both first and last name are empty, use phone number or Anonymous
                if firstName.trimmingCharacters(in: .whitespaces).isEmpty && lastName.trimmingCharacters(in: .whitespaces).isEmpty{
                    if let number = data["phonenumber"] as? String{
                      name = formatNumber(num: number)
                    }
                    if name.trimmingCharacters(in: .whitespaces).isEmpty {
                      name = "Anonymous"
                    }
                } else {
                  if let checkFormat = displayNameFormat {
                    name = formatDisplayName(firstName: firstName, lastName: lastName, displayNameFormat: checkFormat)
                  } else {
                    name = getCustomerName(firstName: firstName, lastName: lastName)
                  }
                }
                var interactionType = "interaction"
                if let mediaType = data["media_type"] as? String {
                  if mediaType == "voice" {
                    shouldDelay = true
                    interactionType = "call"
                  } else if mediaType == "chat" {
                    interactionType = "chat"
                  }
                }
                var date = Date()
                if let timestamp = data["timestamp"] as? Double {
                  date = Date(timeIntervalSince1970: timestamp)
                }
                let formatter = DateFormatter()
                formatter.dateFormat = "MMMM d' at 'h:mm a"
                let template = "You missed a %@ from %@ on %@."
                let datetime = formatter.string(from: date)
                bestAttemptContent.body = String(format: template, interactionType, name, datetime)
              }
            }
          } else if (category == "INBOUND_CALL_EXTERNAL" || category == "ADDITIONAL_INBOUND_CALL_EXTERNAL") {
              var name = ""
              if let aps = request.content.userInfo["aps"] as? NSDictionary {
                if let data = aps["data"] as? NSDictionary {
                  var firstName = ""
                  var lastName = ""
                  if let first = data["caller_first_name"] as? String {
                    firstName = first
                  }
                  if let last = data["caller_last_name"] as? String {
                    lastName = last
                  }
                  // if both first and last name and 'name' are empty, use phone number or Anonymous
                  if firstName.trimmingCharacters(in: .whitespaces).isEmpty && lastName.trimmingCharacters(in: .whitespaces).isEmpty{
                    if let unidentified_name = data["name"] as? String {
                      name = unidentified_name
                    }
                    
                    if name.trimmingCharacters(in: .whitespaces).isEmpty {
                      if let number = data["number"] as? String{
                        name = formatNumber(num: number)
                      }
                    }
                    
                    if name.trimmingCharacters(in: .whitespaces).isEmpty {
                      name = "Anonymous"
                    }
                  } else {
                    if let checkFormat = displayNameFormat {
                      name = formatDisplayName(firstName: firstName, lastName: lastName, displayNameFormat: checkFormat)
                    } else {
                      name = getCustomerName(firstName: firstName, lastName: lastName)
                    }
                }
                  let template = "Inbound call from %@"
                  bestAttemptContent.title = String(format: template, name)
              }
           }
        }

        if (bestAttemptContent.categoryIdentifier == "AGENT_CHANGE_STATE") {
          informWidget(request: request)
        }

        if (category == "SESSION_MSG" || category == "INTERNAL_MSG") {
          bestAttemptContent.body = bestAttemptContent.body.replacingOccurrences(of: "<br>", with: "\n")
          bestAttemptContent.body = bestAttemptContent.body.replacingOccurrences(of: "&amp;", with: "&")
          bestAttemptContent.body = bestAttemptContent.body.replacingOccurrences(of: "&lt;", with: "<")
          bestAttemptContent.body = bestAttemptContent.body.replacingOccurrences(of: "&gt;", with: ">")
          bestAttemptContent.body = bestAttemptContent.body.replacingOccurrences(of: "&nbsp;", with: " ")
        }
          
          if (shouldDelay) {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
              contentHandler(bestAttemptContent)
            }
          } else {
            contentHandler(bestAttemptContent)
          }
      }
    }
    
    override func serviceExtensionTimeWillExpire() {
        // Called just before the extension will be terminated by the system.
        // Use this as an opportunity to deliver your "best attempt" at modified content, otherwise the original push payload will be used.
        if let contentHandler = contentHandler, let bestAttemptContent =  bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }
}
