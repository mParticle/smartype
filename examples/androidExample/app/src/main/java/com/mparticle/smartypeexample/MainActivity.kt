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
import kotlinx.serialization.json.JsonObject

class MainActivity : AppCompatActivity(), MessageReceiver {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initialize a SmartypeApi instance, passing any receivers
        //In this example, pass mParticle and yourself
        val options = MParticleOptions.builder(this)
            .credentials("a0a834d8e83d5c40916d6e4745eed72b", "ZMvnFWPszmQr27LTTuCMhumnW8P5TWsfVQVqrl2RBLyNIdDjdPjVksYSXUGVemfB")
            .dataplan(api.dataPlanId, api.dataPlanVersion)
            .build()

        MParticle.start(options)

        //create a strongly typed message object


        val api = SmartypeApi(listOf(MParticleReceiver(), this))
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

    override fun receive(message: JsonObject) {
        //receive the serialized object and send to other SDKs/consumers
    }
}
