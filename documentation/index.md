# Developer Documentation

## Architecture Overview
The app is primarily composed of two distinct components: the UI code and the Connectivity Service, which contains the mood player and handles all networking. Additionally there is database, which is accessible to both the UI and Connectivity service through the LampShade provider.

### User Interface
The UI code is built with standard Android components such as Activities, Fragments, Views, etc. Common UI elements live in the root directory, and major features live in their own directories. Most of the pages in the app are Fragments hosted in MainActivity.
    
### Connectivity Service
Connectivity Service is an Android Foreground Service which manages the device manager and mood player. The device manager instantiates and manages the device-specific classes for each supported manufacture. Each device type has it's own subdirectory, but implements standard interfaces such as NetBulb and NetConnection. The code for connecting to the Philips Hue currently uses the Hue web API and the LIFX code uses LIFX's (now deprecated) Android SDK.
