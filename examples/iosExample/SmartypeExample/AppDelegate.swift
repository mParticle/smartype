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
class AppDelegate: UIResponder, UIApplicationDelegate {



    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        
        let options = MParticleOptions.init(
            key: "REPLACE WITH KEY",
            secret: "REPLACE WITH SECRET")
        options.dataPlanId = api.dataPlanId
        options.dataPlanVersion = api.dataPlanVersion as NSNumber
        options.logLevel = MPILogLevel.verbose
        MParticle.sharedInstance().start(with: options)
        
        let api = SmartypeApi(receivers: [MParticleReceiver(), self])
        
        let customAttributes = EmailBouncesDataCustomAttributes.init(
            campaignName: "a campaign name",
            campaignId: 5,
            subject: "a subject")
        let data = EmailBouncesData.init(customAttributes: customAttributes)
        let emailBounces = api.emailBounces(data: data)
        
        api.send(message: emailBounces)
        
        let screenView = api.home(data: HomeData())
        
        api.send(message: screenView)
        
        let chooseCustomAttributes = ChooseItemDataCustomAttributes
            .init(quantity: 5,
                  milk: true,
                  item: .cortado
        )
        let itemData = ChooseItemData.init(customAttributes: chooseCustomAttributes)
        
        let chooseItem = api.chooseItem(data: itemData)
        api.send(message: chooseItem)
        
        return true
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a confi guration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }


}

