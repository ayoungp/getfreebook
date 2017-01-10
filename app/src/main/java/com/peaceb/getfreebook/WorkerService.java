package com.peaceb.getfreebook;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;


public class WorkerService extends IntentService {
    private static final String TAG = "WorkerService";

    public WorkerService() {
        super("Get Free Book Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean notify = Setting.getBoolean(this, Setting.KEY_USE_NOTIFY_NEWBOOK);
        boolean autoClaim = Setting.getBoolean(this, Setting.KEY_USE_AUTO_CLAIM);
        boolean useLog = Setting.getBoolean(this, Setting.KEY_USE_LOG);
        String email = Setting.get(this, Setting.KEY_EMAIL);
        String pass = Setting.decrypt(this, Setting.get(this, Setting.KEY_PASS));
        String lastBookTitle = Setting.get(this, Setting.KEY_LAST_BOOK_TITLE);

        boolean loginOk = false;

        PacktClient client = new PacktClient(getFilesDir().getAbsolutePath());
        FileLog.init(this);
        FileLog.setEnable(useLog);

        Pair<String, String> bookInfo = client.checkNewBook();
        if (bookInfo == null) {
            FileLog.e("failed to get free book info");
            return;
        }

        String bookTitle = bookInfo.first;
        String claimUrl = bookInfo.second;

        FileLog.i("book title:" + bookTitle + " url:" + claimUrl);

        if (lastBookTitle.equals(bookTitle)) {
            // this book is checked already
            FileLog.i("old book");
            return;
        } else {
            // new book found!
            FileLog.i("new book!");
            Setting.set(this, Setting.KEY_LAST_BOOK_TITLE, bookTitle);
        }

        if (autoClaim) {
            boolean isAutoClaimOk = false;

            loginOk = client.login(email, pass);
            FileLog.i("login result: " + loginOk);
            if (loginOk) {
                if (!client.claimNewBook(claimUrl)) {
                    FileLog.e("failed to claim");
                }

                if (client.checkMyBook(bookTitle)) {
                    isAutoClaimOk = true;
                }
                else {
                    FileLog.e("cannot found from my ebooks");
                }
            }

            if (isAutoClaimOk) {
                showNotification(getString(R.string.book_claimed), bookTitle);
                FileLog.i("new book claim done");
            } else {
                String title = getString(R.string.error_claim_book);
                if (!loginOk) {
                    title += " (Login)";
                }
                showNotification(title, bookTitle);
            }
        } else {
            showNotification(getString(R.string.new_free_book), bookTitle);
        }
    }

    private void showNotification(String title, String message) {
        Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PacktClient.FREE_BOOK_URL));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_noti)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent);

        Notification noti = builder.build();

        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        if (Setting.getBoolean(this, Setting.KEY_USE_VIBRATE)) {
            noti.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (Setting.getBoolean(this, Setting.KEY_USE_SOUND)) {
            noti.defaults |= Notification.DEFAULT_SOUND;
        }

        NotificationManager nm =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(0, noti);
    }
}

