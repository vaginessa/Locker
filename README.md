Locker
======

Android app for enforcing maximum failed unlock attempts. When the user specified number of attempts have been exceeded the device will wipe and reset to factory default. The app uses the Android Device Administration API to implement this functionality.

Description
=======
While Android does not offer the option to restrict the number of device unlock attempts directly, there is a device administration API that allows such a policy to be enforced through third party applications. This is exactly what Locker does.

Locker activates and enforces the security policy that will trigger a full system wipe when the specified number of unlock attempts have been exhausted. Depending on what lock screen application you are using the user will be presented with warning messages when the number of remaining attempts is 4 or less. 
