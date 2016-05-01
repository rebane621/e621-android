package de.e621.rebane.service;

import android.app.AlarmManager;
import android.app.Application;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.e621.rebane.a621.R;
import de.e621.rebane.activities.PostsActivity;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLReader;
import de.e621.rebane.xmlreader.XMLTask;

public class DMailService extends IntentService {
    public static final String DMAILLOGINEXTRA = "a621 DMail Login Extra";
    private static String login, userAgent;
    private static Context context = null;
    private static PendingIntent alarmIntent = null;
    private static boolean running = false;
    public static boolean isRunning() { return running; }
    public static void stop() {
        running = false; //will prevent the service from sheduling itself
        if (context == null) return;    //stop the schedule if running:
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.cancel(DMailService.getAlarmIntent());
    }

    public DMailService() {
        super("e621 DMail");
        context = getBaseContext();
        makeAlarmIntent(context);
    }

    public static void makeAlarmIntent(Context cont) {
        if (alarmIntent == null) {
            Intent restartIntent = new Intent(cont, DMailService.class);
            alarmIntent = PendingIntent.getService(cont, 0, restartIntent, 0);
        }
    }
    public static PendingIntent getAlarmIntent() {
        if (alarmIntent == null && context != null) {
            makeAlarmIntent(context);
        }
        return alarmIntent;
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String tmp = workIntent.getStringExtra(DMAILLOGINEXTRA);
        if (tmp != null) login = tmp;
        if (login == null || login.isEmpty()) {
            login = null;
            stopSelf();
            return;
        }
        userAgent = getResources().getString(R.string.requestUserAgent);

        running = true;
        //periodically check dmail from https://e621.net/dmail/index.xml? & login+
        XMLNode result = null;
        try {
            //Logger.getLogger("a621").info("Login query: " + login);
            HttpURLConnection urlc = (HttpURLConnection) new URL("https://e621.net/dmail/inbox.xml?" + login).openConnection();
            urlc.addRequestProperty("User-Agent", userAgent);
            urlc.setRequestMethod("GET");
            urlc.setChunkedStreamingMode(256);
            urlc.setConnectTimeout(15000);
            urlc.setReadTimeout(15000);
            urlc.setUseCaches(false);
            InputStream bis = urlc.getInputStream();
            Logger.getLogger("a621").info("Background - Connected with response code " + urlc.getResponseCode() + ": " + urlc.getResponseMessage());

            result = (new XMLReader()).parse(bis);
        } catch (Exception e) {e.printStackTrace();}
        if (result != null) {
            int cnt = 0;
            StringBuilder cont = new StringBuilder();
            for (XMLNode msg : result.getChildren()) {
                if (!msg.getType().equals("dmail")) { cnt = 0; break; }
                //Logger.getLogger("a621").info ("DMail: " + msg.toString());
                Boolean seen = Boolean.parseBoolean(msg.getFirstChildText("has-seen"));
                if (!seen) {
                    cnt++;
                    cont.append(msg.getFirstChildText("title") + "\n");
                }
            }
            if (cnt>0) {
                NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                Intent dmail = new Intent(getApplicationContext(), PostsActivity.class);
                PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, dmail, 0);
                Notification notify = new Notification.Builder(this)
                        .setContentTitle("You have " + cnt + " new DMails")
                        .setContentText(cont.toString().trim())
                        .setSmallIcon(R.drawable.ic_menu_dmail)
                        .setContentIntent(pending)
                        .setAutoCancel(true)
                        .build();

                notif.notify(0, notify);
            }
        }
        stopSelf();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (login != null && running) { //check again in 5 minutes
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000 * 60 * 1, getAlarmIntent());
        } else
            running = false;
    }
}
