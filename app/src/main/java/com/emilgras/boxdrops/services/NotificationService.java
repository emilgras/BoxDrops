package com.emilgras.boxdrops.services;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;

import com.emilgras.boxdrops.ActivityMain;
import com.emilgras.boxdrops.R;
import com.emilgras.boxdrops.beans.Drop;

import br.com.goncalves.pugnotification.notification.PugNotification;
import io.realm.Realm;
import io.realm.RealmResults;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class NotificationService extends IntentService {

    public static final String TAG = "MILO";
    
    public NotificationService() {
        super("NotificationService");
        Log.d(TAG, "NotificationService: ");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        Realm realm = null;
        try {

            realm = Realm.getDefaultInstance();
            RealmResults<Drop> results = realm.where(Drop.class).equalTo("complete", false).findAll(); // No need for async - we are already on a background thread

            for (Drop drop: results) {
                if (isNotificationNeeded(drop.getAdded(), drop.getWhen())) {

                    fireNotification(drop);

                }
            }

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void fireNotification(Drop drop) {
        String message = getString(R.string.notif_message) + "\"" + drop.getWhat() + "\"";
        PugNotification.with(this)
                .load()
                .title(R.string.notif_title)
                .message(message)
                .bigTextStyle(R.string.notif_long_message)
                .smallIcon(R.drawable.ic_drop)
                .largeIcon(R.drawable.ic_drop)
                .flags(Notification.DEFAULT_ALL)
                .autoCancel(true)
                .click(ActivityMain.class)
                .simple()
                .build();
    }

    private boolean isNotificationNeeded(long added, long when) {
        long now = System.currentTimeMillis();

        if (now > when) {
            // drop has expired
            return false;
        }
        else {
            // drop is in the future
            long difference90 = (long) (0.9 * (when - added)); // This will get the time when 90% of it has time is gone

            return (now > (added + difference90)) ? true : false;

        }

    }

}
