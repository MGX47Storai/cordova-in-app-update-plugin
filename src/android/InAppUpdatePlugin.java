package com.lucario.cordova.plugin;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
// import android.support.design.widget.Snackbar;
import android.R;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import static java.lang.System.out;

public class InAppUpdatePlugin extends CordovaPlugin {
    public int REQUEST_CODE = 7;
    private static String IN_APP_UPDATE_TYPE = "FLEXIBLE";
    private static Integer DAYS_FOR_FLEXIBLE_UPDATE = 0;
    private static Integer DAYS_FOR_IMMEDIATE_UPDATE = 0;
    private static Integer HIGH_PRIORITY_UPDATE = 3;
    private static Integer MEDIUM_PRIORITY_UPDATE = 1;
    private static AppUpdateManager appUpdateManager;
    private static InstallStateUpdatedListener listener;
    private static CallbackContext cordovaCallbackContext;
    private static InstallState installState;
    private static final String LOG_TAG = "InAppUpdate";

    @Override
    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
            super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) {
            // Verify that the user sent a "show" action
            if (action.equals("check")) {
                final Context context = this.cordova.getContext();
                appUpdateManager = AppUpdateManagerFactory.create(context);

                final Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
                appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                            constructAppUpdateInfo(appUpdateInfo));
                    callbackContext.sendPluginResult(pluginResult);
                });

            } else if (action.equals("update")) {
                try {
                    final JSONObject argument = args.getJSONObject(0);
                    IN_APP_UPDATE_TYPE = argument.getString("updateType");
                } catch (final JSONException e) {
                    e.printStackTrace();
                }

                try {
                    appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                                && IN_APP_UPDATE_TYPE.equals("IMMEDIATE")) {
                            checkForUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo);
                        } else {
                            // FLEXIBLE
                            cordovaCallbackContext = callbackContext;
                            checkForUpdate(AppUpdateType.FLEXIBLE, appUpdateInfo);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }   else if (action.equals("completeFlexibleUpdate")) {
                    completeFlexibleUpdate();
            }   else {
                callbackContext.error("\"" + action + "\" is not a recognized action.");
                return false;
            }
        return true;
    }

    public void onStateUpdate(final InstallState installState) {
        this.installState = installState;
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, constructInstallStateInfo(installState));
        cordovaCallbackContext.sendPluginResult(pluginResult);
    };

    public void checkForUpdate(final int updateType, final AppUpdateInfo appUpdateInfo) {
        if (updateType == 0) {
            listener = state -> {
                onStateUpdate(state);
            };
            appUpdateManager.registerListener(listener);

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, constructAppUpdateInfo(appUpdateInfo));
            cordovaCallbackContext.sendPluginResult(pluginResult);

            appUpdateManager.startUpdateFlow(appUpdateInfo, cordova.getActivity(),
                    AppUpdateOptions.defaultOptions(updateType));
        } else {
            try {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, updateType, cordova.getActivity(),
                        REQUEST_CODE);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void completeFlexibleUpdate() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if(installState != null && installState.installStatus() == InstallStatus.DOWNLOADED)
                appUpdateManager.completeUpdate();
        });
    }

    private void notifyFlexibleUpdateDownloaded(AppUpdateInfo appUpdateInfo) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                constructAppUpdateInfo(appUpdateInfo));
        if(cordovaCallbackContext != null)
            cordovaCallbackContext.sendPluginResult(pluginResult);
    }

    private JSONObject constructInstallStateInfo(final InstallState installState) {
        JSONObject result = new JSONObject();
        try {
            result.put("updateType", IN_APP_UPDATE_TYPE);
            result.put("installStatus", installState.installStatus());
            result.put("bytesDownloaded", installState.bytesDownloaded());
            result.put("installErrorCode", installState.installErrorCode());
            result.put("packageName", installState.packageName());
            result.put("totalBytesToDownload", installState.totalBytesToDownload());
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JSONObject constructAppUpdateInfo(AppUpdateInfo appUpdateInfo) {
        JSONObject result = new JSONObject();
        try {
            result.put("updateType", IN_APP_UPDATE_TYPE);
            result.put("installStatus", appUpdateInfo.installStatus());
            result.put("availableVersionCode", appUpdateInfo.availableVersionCode());
            result.put("bytesDownloaded", appUpdateInfo.bytesDownloaded());
            result.put("totalBytesToDownload", appUpdateInfo.totalBytesToDownload());
            result.put("clientVersionStalenessDays", appUpdateInfo.clientVersionStalenessDays());
            result.put("packageName", appUpdateInfo.packageName());
            result.put("totalBytesToDownload", appUpdateInfo.totalBytesToDownload());
            result.put("updateAvailability", appUpdateInfo.updateAvailability());
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onResume(final boolean multitasking) {
        super.onResume(multitasking);
        appUpdateManager
        .getAppUpdateInfo()
        .addOnSuccessListener(appUpdateInfo -> {
            if (IN_APP_UPDATE_TYPE.equals("FLEXIBLE")) {
                notifyFlexibleUpdateDownloaded(appUpdateInfo);
            }
            if (appUpdateInfo.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an in-app update is already running, resume the update.
                try {
                    checkForUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}