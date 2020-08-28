import mParticle from "@mparticle/web-sdk"
import * as kotlin from "../smartype-dist/kotlin.js"
import * as smartype from "../smartype-dist/smartype-smartype.js"
import * as smartypeMparticle from "../smartype-dist/smartype-smartype-mparticle.js"

// alias to window and create namespace references for easier access
window.smartype = smartype
window.smartypeMparticle = smartypeMparticle
window.kotlin = kotlin
var api = smartype.com.mparticle.smartype
var receivers = smartypeMparticle.com.mparticle.smartype.api.receivers

class MyApp {
  begin() {
    
    // Initialize Smartype, adding any receivers
    // in this example, add mParticle and yourself
    var smartypeApi = new api.SmartypeApi()
    smartypeApi.addReceiver(new receivers.mparticle.MParticleReceiver())
    smartypeApi.addReceiver(this)

    var mParticleApiKey = "REPLACE WITH API KEY"
    const mParticleConfig = {
      isDevelopmentMode: true,
      identityCallback: function (result) {
        console.log("identity callback complete")
      },
      dataPlan: {
        planId: smartypeApi.dataPlanId,
        planVersion: smartypeApi.dataPlanVersion,
      },
    }
    mParticle.init(mParticleApiKey, mParticleConfig)

    // create a strongly typed message object


    var message = smartypeApi.chooseItem(
      new api.ChooseItemData(
        new api.ChooseItemDataCustomAttributes(
          5.0, true, new api.ChooseItemDataCustomAttributesItem().CORTADO()
        )
      )
    )

    // the message object will now be sent to all receivers
    smartypeApi.send(message)

  }

  receive(message) {
    // receive the serialized object and send to other SDKs/consumers  
  }
}

var app = new MyApp()
app.begin()
