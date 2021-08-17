# cordova-in-app-update
This pluging enabels [In app update](https://developer.android.com/guide/playcore/in-app-updates) For cordova.

## Supports
* Flexible update
* Immidiate update

# Install Plugin

```console
cordova plugin add @lucario/cordova-in-app-update
```

# Examples

## Check Available Update Information

If you want to prompt user with a prompt about new version information before initiating the update, you can use `window.plugins.InAppUpdate.check` to retrive the new app version information.

```console
window.plugins.InAppUpdate.check(success, error);
```

## Initiate Update Flow

### Flexible updates
Flexible updates provide background download and installation with graceful state monitoring. This UX flow is appropriate when it's acceptable for the user to use the app while downloading the update. For example, you might want to encourage users to try a new feature that's not critical to the core functionality of your app.

```console
window.plugins.InAppUpdate.update(success, error, { updateType: "FLEXIBLE" });
```

### Immediate updates
Immediate updates are fullscreen UX flows that require the user to update and restart the app in order to continue using it. This UX flow is best for cases where an update is critical to the core functionality of your app. After a user accepts an immediate update, Google Play handles the update installation and app restart.

```console
window.plugins.InAppUpdate.update(success, error, { updateType: "IMMEDIATE" });
```

## Complete Flexible Update

Flexible updates provide background download. Once flexible update completes the download in background, completion of upgrade can be initiated by calling `window.plugins.InAppUpdate.completeFlexibleUpdate`.

```console
window.plugins.InAppUpdate.update((data) => {
    if(data.installStatus == 2)
        window.plugins.InAppUpdate.completeFlexibleUpdate(success, error);
}, (error) => {}, { updateType: "FLEXIBLE" });
```
