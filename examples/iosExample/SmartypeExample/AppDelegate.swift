//
//  AppDelegate.swift
//  SmartypeExample
//
//  Created by Sam Dozor on 3/24/20.
//  Copyright © 2020 mParticle. All rights reserved.
//

import UIKit
import Smartype
import mParticle_Apple_SDK
@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, MessageReceiver {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // Initialize a SmartypeApi instance, adding any receivers
        // In this example, add mParticle and yourself
        let api = SmartypeApi()
        api.addReceiver(receiver: MParticleReceiver())
        api.addReceiver(receiver: self)
        
        let options = MParticleOptions.init(
            key: "95fdeeb88524f04099929b8e0b18fad4",
            secret: "9_kufb6cWAdneh49BV4xtoWzluFTbQ26on_P-Pox2t_PprFDIahs8rJCqX-iEoFX")
        options.dataPlanId = api.dataPlanId
        options.dataPlanVersion = api.dataPlanVersion as NSNumber
        options.logLevel = MPILogLevel.verbose
        MParticle.sharedInstance().start(with: options)
        
        // create a strongly typed message object
        
        
        let message = api.chooseItem(data:
            ChooseItemData.init(customAttributes:
                ChooseItemDataCustomAttributes
                    .init(quantity: 5,
                          milk: true,
                          item: .cortado
                )
            )
        )
        
        // the message object will now be sent to all receivers
        api.send(message: message)
        
        return true
    }
    
    func receive(message: String) {
        // receive the serialized object and send to other SDKs/consumers
    }
    
    
    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }


}

