<img src="docs/logo.svg" width="280">

<img src="https://maven-badges.herokuapp.com/maven-central/com.mparticle/smartype-generator/badge.svg">


## Overview

Smartype is a powerful code generation tool that allows you to get the most out of the time you spend organizing and defining the structure of your data.

It aims to completely eliminate an entire class of data quality bugs 🐛 by generating statically-typed API clients based on the popular [JSON schema](https://json-schema.org/) standard.

🏁 Smartype is built to translate any JSON schema into type-safe models  
🏁 It gives you auto-complete for your data model in your IDE of choice  
🏁 It's open source and extensible via a plugin system   
🏁 And it's built for all and with ❤️ by [mParticle](https://www.mparticle.com)

### Example

The following JSON schema describes a coffee order with a few required parameters:
- item: An string value with a predefined set of allowed values
- quantity: A numeric value indicating how many coffees were ordered
- milk: A boolean value indicating if you want milk in your coffee

Smartype does the following with this:
- Consumes the JSON schema and generates Kotlin `data` classes
- Uses [Kotlin Multiplatform](https://kotlinlang.org/docs/reference/multiplatform.html) to translate that Kotlin code into other languages and generate consumable binaries
- Surfaces an API to send and receive these "messages", which can be consumed by any analytics provider or your own system

<img src="docs/example.jpg" width="1000">

## Supported Environments

Smartype supports the following language environments today:

1. Any JVM environment, including Kotlin and Java for Android and server-side contexts
2. iOS Swift and Objective-C
3. Web browsers and React Native via TypeScript and JavaScript

## mParticle Customers

Smartype is designed to be used by anyone, but support today is primarily for mParticle's Events API and SDKs by way of the mParticle Smartype receiver.

[Navigate to the mParticle docs](https://docs.mparticle.com/developers/smartype) for more specific docs related to mParticle Data Plans and SDKs.

## Workflow

Smartype is shipped as a CLI tool, and so a the typical workflow would be:

1. Assemble your JSON schema into a file for consumption by Smartype or download the mParticle Data Plan from the Web UI or using the mParticle CLI tool.
2. Run Smartype to generate your libraries.
3. Incorporate and use those libraries in any number of environments

## Using the mParticle CLI Tool to Download the Data Plan

Rather than manually creating a JSON file, mParticle provides these files ready for use by Smartype. There are multiple ways to retrieve them, but for automation purposes using the mParticle CLI tool is the best option. 

We provide a ready-to-use Github Actions workflow file here, which can be adapted to other CI systems: https://github.com/mParticle/mparticle-workflows/blob/main/.github/workflows/data-plan-fetch.yml

Individual developers can also manually download the Data Plan JSON by using the mParticle CLI directly from the command line.

### Install the mParticle CLI tool

1. Install Node using your preferred method for your platform.
2. Install the mParticle CLI tool: `npm install -g @mparticle/cli`.

### Download the Data Plan JSON

Once the mParticle CLI has been installed, the Data Plan JSON can be downloaded using a single command.

First you will need the following pieces of information:
- Data Plan ID
- Data Plan Version
- Workspace ID
- Client ID (mParticle access token)
- Client Secret (mParticle access token)

You can create and manage your mParticle access tokens for Data Planning with the [API Credentials interface](https://docs.mparticle.com/developers/credential-management).

```bash
# Here we'll use environment variables, but you can simply pass the values directly to the mp command if you'd prefer
export $DATA_PLAN_ID=...
export $DATA_PLAN_VERSION=...
export $WORKSPACE_ID=...
export $CLIENT_ID=...
export $CLIENT_SECRET=...
# $OUT_FILE can be any file name you like
export $OUT_FILE=${DATA_PLAN_ID}_${DATA_PLAN_VERSION}.json

# Pull down the JSON file
mp planning:data-plan-versions:fetch --dataPlanId=$DATA_PLAN_ID --versionNumber=$DATA_PLAN_VERSION --workspaceId=$WORKSPACE_ID --clientId=$CLIENT_ID --clientSecret=$CLIENT_SECRET --outFile=$OUT_FILE
```

## Smartype CLI Usage

Smartype is deployed as an executable jar CLI, and you can download the latest release from the [Github releases](https://github.com/mparticle/smartype/releases) page.

The CLI provides two key commands:

- `init`: Initialize a configuration file that's used by Smartype to generate code.
- `generate`: Generates strongly-type libraries based on your data model

### Smartype `init`

Smartype `init`  will ask you a series of questions and then create a Smartype configuration file.

```bash
java -jar smartype.jar init
```

### Smartype `generate`

Smartype `generate` will read your configuration file and output binaries that are ready for consumption in an application.

```bash
java -jar smartype.jar generate
```

If this is your first call to 'generate', you may want to go grab a cup of coffee while it downloads dependencies. (It will be much faster the second time!)

### Integrating Generated Code

To use the objects created by Smartype, you'll want to add the generated code to your projects. You will also want to initialize Smartype when the app starts up, and register any receivers that you would like to get notified for each message that gets logged.

The following code snippets use the mParticle receiver as an example, but receivers can be created to wrap any interface to which you want to send data, including for your own inhouse processing.

You can also (optionally) add yourself as a receiver, and then implement a `receive` function to get a copy of all JSON messages that are sent. See the [example projects](https://github.com/mParticle/smartype/tree/main/examples) for details of how this is done per platform.

#### iOS

Smartype `generate` will create an "fat" dynamic framework that you can include directly with your projects.

- To use Smartype on iOS, start by adding `smartype.xcframework` to your Xcode project
- Next, import and initialize Smartype prior to use, and register any receivers
- The `SmartypeApi` object will surface a series of methods which each represent the top-level items in your schema
- Pass the fully constructed objects into your `SmartypeApi` instance for all receivers 

```swift    
import Smartype

...
let api = SmartypeApi()
api.addReceiver(receiver: MParticleReceiver())
api.addReceiver(receiver: self)

let chooseCustomAttributes = ChooseItemDataCustomAttributes
    .init(quantity: 5,
          milk: true,
          item: .cortado
)
let itemData = ChooseItemData.init(customAttributes: chooseCustomAttributes)
let chooseItem = api.chooseItem(data: itemData)
api.send(message: chooseItem)
```

#### Android

Smartype `generate` will create an `aar` file that you can include directly with your projects.

To use Smartype on Android, start by adding the generated `smartype.aar` to your project and any 3rd-party receivers that you plan on using:

```kotlin
dependencies {
    implementation "com.mparticle:smartype-api:1.2.4"
    implementation "com.mparticle:smartype-mparticle:1.2.4"
    implementation fileTree(dir: 'libs', include: ['**/*.aar'])
}
```

The Smartype API dependencies are deployed as a multiplatform library leveraging [Gradle Module metadata](https://docs.gradle.org/current/userguide/publishing_gradle_module_metadata.html), and in order
for Android projects to resolve the right dependency, you may need to add the following to ensure debug builds use the "release" artifact.

```kotlin
buildTypes {
    debug {
        matchingFallbacks = ["release"]
    }
}
```

- Import and initialize Smartype prior to use, and register your receivers
- The `SmartypeApi` object will surface a series of methods which each represent the top-level items in your schema
- Pass the fully constructed objects into your `SmartypeApi` instance for all receivers 

```kotlin
val api = SmartypeApi()
api.addReceiver(MParticleReceiver())
api.addReceiver(this)
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
```

#### Web

Smartype supports both the mParticle Web SDK as well as the mParticle React Native plugin.

Smartype `generate` will create a set of `.js` and `.d.ts` files that you can include directly with your projects. Our [example](https://github.com/mParticle/smartype/blob/main/examples/webExample/src/index.js) uses webpack to concatenate and minify the source files.

To use Smartype with the Web SDK or with React Native, start by adding the generated `smartype-dist` directory to your project and any 3rd-party receivers that you plan on using, then include the relevant files in your typescript or javascript sources:

```js
import * as smartype from "../smartype-dist/smartype.js"
```

- Import and initialize Smartype prior to use, and register your receivers
- The `SmartypeApi` object will surface a series of methods which each represent the top-level items in your schema
- Pass the fully constructed objects into your `SmartypeApi` instance for all receivers 

```js
import * as smartype from "../smartype-dist/smartype.js"

var api = new smartype.SmartypeApi()
api.addReceiver(smartype.mParticleReceiver())

var message = smartype.chooseItem(
      new smartype.ChooseItemData(
        new smartype.ChooseItemDataCustomAttributes(
          1, true, new smartype.ChooseItemDataCustomAttributesItem().CORTADO()
        )
      )
    )

//the message object will now be sent to all receivers
api.send(message)
```


##### React Native

In order to enable React Native:
- [Add mParticle's React Native plugin](https://docs.mparticle.com/developers/sdk/react-native/getting-started/) to your React project if you haven't already
- Inject the mParticle React Native plugin into your `mParticleReceiver`:

```js
import MParticle from 'react-native-mparticle'

...

var api = new smartype.SmartypeApi()
var receiver = smartype.mParticleReceiver()
receiver.react = MParticle
api.addReceiver(receiver)
```

You will also want to exclude the generated `.smartype` directory from your React Project by configuring your `metro.config.js` file:

```js
const exclusionList = require('metro-config/src/defaults/exclusionList');

... 

module.exports = {
  resolver: {
      blockList: exclusionList([/\.smartype\/.*/])
    },
};
```



### Example Projects

[Check out the examples here](https://github.com/mParticle/smartype/tree/master/examples)

### Contributing
At mParticle, we are proud of our code and like to keep things open source. If you'd like to contribute, simply fork this repo, push any code changes to your fork, and submit a Pull Request against the `master` branch.

### License
Apache 2.0
