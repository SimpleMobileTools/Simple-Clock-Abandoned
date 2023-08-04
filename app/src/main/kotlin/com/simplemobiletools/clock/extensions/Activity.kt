package com.simplemobiletools.clock.extensions

import com.simplemobiletools.clock.BuildConfig
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.PermissionRequiredDialog
import com.simplemobiletools.commons.extensions.canUseFullScreenIntent
import com.simplemobiletools.commons.extensions.openFullScreenIntentSettings

fun BaseSimpleActivity.handleFullScreenNotificationsPermission(
    fullScreenNotificationsDeniedCallback: () -> Unit = {},
    notificationsCallback: (granted: Boolean) -> Unit,
) {
    handleNotificationPermission { granted ->
        if (granted) {
            if (canUseFullScreenIntent()) {
                notificationsCallback(true)
            } else {
                PermissionRequiredDialog(
                    activity = this,
                    textId = com.simplemobiletools.commons.R.string.allow_full_screen_notifications_reminders,
                    positiveActionCallback = {
                        openFullScreenIntentSettings(BuildConfig.APPLICATION_ID)
                    },
                    negativeActionCallback = {
                        fullScreenNotificationsDeniedCallback()
                    }
                )
            }
        } else {
            notificationsCallback(false)
        }
    }
}