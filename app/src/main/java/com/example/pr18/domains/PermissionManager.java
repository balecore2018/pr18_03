package com.example.pr18.domains;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionManager {

    public static void PermissionNotification(Context context, Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (CheckPermission(context) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1
            );

        }

    }

    public static int CheckPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return PackageManager.PERMISSION_GRANTED;
        }

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS);

    }

}
