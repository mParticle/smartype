package com.mparticle.smartypeexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mparticle.MParticle
import com.mparticle.MParticleOptions
import com.mparticle.smartype.ChooseItemData
import com.mparticle.smartype.ChooseItemDataCustomAttributes
import com.mparticle.smartype.ChooseItemDataCustomAttributesItem
import com.mparticle.smartype.SmartypeApi
import com.mparticle.smartype.api.MessageReceiver
import com.mparticle.smartype.api.receivers.mparticle.MParticleReceiver

class MainActivity : AppCompatActivity(), MessageReceiver {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initialize a SmartypeApi instance, adding any receivers
        //In this example, add mParticle and yourself
        val api = SmartypeApi()
        api.addReceiver(MParticleReceiver())
        api.addReceiver(this)

        val options = MParticleOptions.builder(this)
            .credentials("REPLACE WITH APP KEY", "REPLACE WITH APP SECRET")
            .dataplan(api.dataPlanId, api.dataPlanVersion)
            .build()

        MParticle.start(options)

        //create a strongly typed message object


        val message = api.chooseItem(
            ChooseItemData(
                ChooseItemDataCustomAttributes(
                    quantity = 5.0,
                    milk = true,
                    item = ChooseItemDataCustomAttributesItem.CORTADO
                )
            )
        )

        //the message object will now be sent to all receivers
        api.send(message)
    }

    override fun receive(message: String) {
        //receive the serialized object and send to other SDKs/consumers
    }
}
