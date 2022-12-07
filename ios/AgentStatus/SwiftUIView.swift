//
//  SwiftUIView.swift
//  AgentStatusExtension
//
//  Created by Maxim Vovenko on 7/13/21.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

import SwiftUI
import WidgetKit

func selectImage(name: String) -> Image{
  switch name {
  case "not_ready":
    return Image("NotReady")
  case "ready":
    return Image("Ready")
  case "busy":
    return Image("Busy")
  case "after_call_work":
    return Image("ACW")
  case "logged_out":
    return Image("LoggedOut")
  case "supervising":
    return Image("Supervising")
  case "busy_call":
    return Image("BusyCall")
  case "busy_chat":
    return Image("BusyChat")
  case "busy_email":
    return Image("BusyEmail")
  case "busy_preview":
    return Image("BusyPreview")
  case "incoming_call":
    return Image("IncomingCall")
  case "incoming_chat":
    return Image("IncomingChat")
  default:
    return Image("UnkownState")
  }
  
}

func isEqualNotReady(input: String) -> Bool{
  let formatted = input
    .lowercased()
    .replacingOccurrences(of: "_", with: "")
    .replacingOccurrences(of: " ", with: "")
  return formatted == "notready"
}

func formatTextForDisplay(name: String) -> String{
  let output=name
    .capitalized
    .replacingOccurrences(of: "_", with: " ")
  return output
}

func determinTitleToDisplay(agentState: String, title: String) -> String{
  if(title.rangeOfCharacter(from: CharacterSet.alphanumerics) == nil){
    return agentState
  }
  return title
}

func isIncomingInteraction(agentState: String)->Bool{
 return ("incoming_call" == agentState || "incoming_chat" == agentState)
}

func determinTimerColor(ags: String) -> Color {
  if(("incoming_call" == ags || "incoming_chat" == ags)){
    return Color(red: 0, green: 0.0, blue: 0.0, opacity: 0)
  }
  return Color("ColorSecondary")
}

struct AgentStatusEntryView : View {
  var entry: Provider.Entry
  
  var body: some View {
    ui1(entry:entry)
  }
}

struct SwiftUIView_Previews: PreviewProvider {
  static var previews: some View {
    AgentStatusEntryView(entry: SimpleEntry(date: Date(),
                                            agentState: "not_ready",
                                            title:"ALPHA CENTURY",
                                            reason:"hullabalu"))
      .previewContext(WidgetPreviewContext(family: .systemSmall))
    
  }
}

//Option 1 for UI (White Ui)
func ui1(entry: Provider.Entry) -> some View{
  if(entry.reason.rangeOfCharacter(from: CharacterSet.alphanumerics) == nil ||
      isEqualNotReady(input: entry.reason)){
    var body: some View {
      ZStack(){
        Color("ColorWidgetBackground")
        VStack(alignment: .leading){
          selectImage(name: entry.agentState)
            .resizable(resizingMode: .stretch)
            .aspectRatio(contentMode: .fit)
            .shadow(color: Color("ColorShadow"),
                    radius: /*@START_MENU_TOKEN@*/5/*@END_MENU_TOKEN@*/, x: /*@START_MENU_TOKEN@*/0.0/*@END_MENU_TOKEN@*/,
                    y: /*@START_MENU_TOKEN@*/5.0/*@END_MENU_TOKEN@*/)
          VStack(alignment: .leading){
            Text(formatTextForDisplay(name: determinTitleToDisplay(agentState: entry.agentState, title: entry.title)))
              .font(.body)
              .fontWeight(.semibold)
              .foregroundColor(Color("ColorPrimary"))
              .lineLimit(1)
            Text("\(entry.date, style: .timer)")
              .fontWeight(.light)
              .font(.footnote)
              .foregroundColor(determinTimerColor(ags: entry.agentState))
              .lineLimit(1)
            Text("")
              .font(.footnote)
              .fontWeight(.semibold)
              .foregroundColor(Color.white)
              .lineLimit(1)
          }
          //Spacer()
        }.padding(.all)
      }
    }
    return AnyView(body)
  }
  var body: some View {
    ZStack(){
      Color("ColorWidgetBackground")
      VStack(alignment: .leading){
        selectImage(name: entry.agentState)
          .resizable(resizingMode: .stretch)
          .aspectRatio(contentMode: .fit)
          .shadow(color: Color("ColorShadow"), radius: /*@START_MENU_TOKEN@*/5/*@END_MENU_TOKEN@*/, x: /*@START_MENU_TOKEN@*/0.0/*@END_MENU_TOKEN@*/, y: /*@START_MENU_TOKEN@*/5.0/*@END_MENU_TOKEN@*/)
        
        VStack(alignment: .leading){
          Text(formatTextForDisplay(name: determinTitleToDisplay(agentState: entry.agentState, title: entry.title)))
            .font(.body)
            .fontWeight(.semibold)
            .foregroundColor(Color("ColorPrimary"))
            .lineLimit(1)
          Text(formatTextForDisplay(name: entry.reason))
            .font(.footnote)
            .fontWeight(.semibold)
            .foregroundColor(Color("ColorPrimary"))
            .lineLimit(1)
          Text("\(entry.date, style: .timer)")
            .fontWeight(.light)
            .font(.footnote)
            .foregroundColor(determinTimerColor(ags: entry.agentState))
            .lineLimit(1)
        }
      }.padding(.all)
    }
  }
  return AnyView(body)
}
