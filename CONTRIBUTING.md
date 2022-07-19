Thanks for considering contributing to the Smartype project!

The following provides a brief overview of how the project is laid out.

### Project Structure

`smartype-generator`
- JVM project
- Is meant to copy `smartype-kmp-template` and generate a new project based on schema

`smartype-api`
- KMP library project
- Defines the common interface
   -  `SmartypeApi`: the class app's will send data into
   -  `SmartypeMessage`: a data point to be sent
   -  `SmartypeReceiver`: a receiver of `SmartypeMessage`

`smartype-kmp-template`
- KMP library project
- solely used to generate a project in a different directory

#### Receivers

- `smartype-mparticle`
   - KMP project
   - implements `SmartypeReceiver`
   - maps `SmartypeMessage`'s to mParticle SDKs
   - Smartype users will call `addListener` with this on their`SmartypeApi`

### Building

To build the `smartype-generator` Java application, first build it using `./gradlew publishToMavenLocal`, then the jar file will be located at `~/.m2/repository/com/mparticle/smartype-generator/X.X.X` where `X.X.X` is the version number.