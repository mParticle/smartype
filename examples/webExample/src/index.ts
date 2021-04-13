import mParticle from "@mparticle/web-sdk"

import * as smartype from "../smartype-dist/web/smartype.js"

// create namespace references for easier access


class MyApp {
  begin() {

    var api = new smartype.SmartypeApi()

    api.addReceiver(smartype.mParticleReceiver())
    api.addReceiver(this)

    var mParticleApiKey = "REPLACE ME WITH WEB KEY"
    const mParticleConfig = {
      isDevelopmentMode: true,
      identityCallback: function () {
        console.log("identity callback complete")
      },
      dataPlan: {
        planId: api.dataPlanId,
        planVersion: api.dataPlanVersion,
      },
    }
    mParticle.init(mParticleApiKey, mParticleConfig)

    // create a strongly typed message object
    var message = api.chooseItem(
      new smartype.ChooseItemData(
        new smartype.ChooseItemDataCustomAttributes(
          5.0, true, new smartype.ChooseItemDataCustomAttributesItem().CORTADO()
        )
      )
    )

    // the message object will now be sent to all receivers
    api.send(message)

  }

  receive(message: smartype.com.mparticle.smartype.api.Message) {
    // receive the serialized object and send to other SDKs/consumers  
    console.log(message)
  }
}

var app = new MyApp()
app.begin()
