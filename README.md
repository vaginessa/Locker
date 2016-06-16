# Locker

Android app for enforcing maximum failed unlock attempts. When the user specified number of attempts have been exceeded the device will wipe and reset to factory default. The app uses the Android Device Administration API to implement this functionality.

## Description
While Android does not offer the option to restrict the number of device unlock attempts directly, there is a device administration API that allows such a policy to be enforced through third party applications. This is exactly what Locker does.

Locker activates and enforces the security policy that will trigger a full system wipe when the specified number of unlock attempts have been exhausted. Depending on what lock screen application you are using the user will be presented with warning messages when the number of remaining attempts is 4 or less. This behaviour can be overridden by choosing to hide the lock screen warning from within the app. In this case, failed unlock attempts are handled directly by Locker.

Be aware - Failed unlock attempts are tracked even across reboots.

Locker does not currently support fingerprint unlocking. This seems to be a limitation with the Device Administration API. 

## How does it work?
Locker uses the [Device Administration API](http://developer.android.com/guide/topics/admin/device-admin.html) to implement the appropriate security policies. By using the `setMaximumFailedPasswordsForWipe()` policy the device can be set to automatically wipe after the specified number of failed unlock attempts. This includes a warning indicator on your lock screen that shows up when you have few remaining unlock attempts. If you want a more stealthly implementation you can choose to hide the lock screen warning all together. In this case, Locker is notified on a failed unlock attempts and checks the number of failed attempts. if it exceeds the specified limit it calls [wipeData()](http://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#wipeData%28int%29). Note that Android keeps track of failed unlock attempts even across reboots, so rebooting your device does not reset the failed unlock counter.


### wipeData()

When Locker triggers the wipe your device is reset to factory default and user data is deleted as the /data partition is formatted. Beaware that data is not overwritten so you should ensure device encryption is enabled in order to prevent data recovery after the wipe resets your device.

## How to Contribute

### Translations 
Translations are always welcome though translation maintaners are even more welcome. Feel free to fork the repository and create a pull request.

### Donations
If you're feeling generous, some BTC's to 19tgyZsPQU2VeXWVYGha4kqMa2e5BgXaab is always appreciated to help support future development. 
