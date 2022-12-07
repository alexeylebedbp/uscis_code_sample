//
//  LottieAnimation.swift
//  mob
//
//  Created by Rostislav on 29.03.2022.
//  Copyright © 2021 Brightpattern. All rights reserved.
//

import Foundation
import Lottie

@objc(LottieAnimation)
class LottieAnimation: NSObject {

    @objc
    func addTo (_ splashView:UIView) {
      let statusBarFrame = UIApplication.shared.statusBarFrame
      let screenSizeRect = UIScreen.main.bounds
      
      let animationView = AnimationView(name: "splash_animation")
      
      animationView.frame = CGRect(
        x: screenSizeRect.size.width * 0.1,
        y: screenSizeRect.size.height / 2 - animationView.frame.size.height / 2 - statusBarFrame.size.height / 2,
        width: screenSizeRect.size.width * 0.8,
        height: animationView.frame.size.height
      )
      
      animationView.loopMode = .loop
      animationView.contentMode = .scaleAspectFit
      animationView.play()

      splashView.addSubview(animationView)
      
      let textView = UILabel.init(frame:CGRect(
        x: 0,
        y: screenSizeRect.size.height / 2 + animationView.frame.size.height / 2 - statusBarFrame.size.height / 2,
        width: screenSizeRect.size.width,
        height: 32
      ))
      
      textView.textColor = UIColor.init(
        red: 136/255,
        green: 136/255,
        blue: 136/255,
        alpha: 1
      )
      
      textView.font = UIFont.init(name: "Trebuchet MS", size: 18)
      textView.textAlignment = .center
      textView.tag = 1
      
      splashView.addSubview(textView)
    }
}
