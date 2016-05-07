package de.e621.rebane.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import de.e621.rebane.a621.R;
import de.e621.rebane.activities.PostsActivity;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLReader;

public class DMailService extends IntentService {
    private static final long delay = 1000*30; //delay between checks
    public static final String DMAILLOGINEXTRA = "a621 DMail Login Extra";
    private static String login, userAgent;
    private static boolean running = false;
    public static boolean isRunning() { return running; }
    public static void stop() {
        Logger.getLogger("a621").info("DMail Service stopped!");
        running = false; //will prevent the service from sheduling itself
    }
    private static Timer timer = null;
    private static TimerTask ttask = null;

    public DMailService() {
        super("e621 DMail");
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        // Gets data from the incoming Intent
        String tmp = intent.getStringExtra(DMAILLOGINEXTRA);
        if (tmp != null) login = tmp;
        if (login == null || login.isEmpty()) {
            login = null;
            stopSelf();
            return START_NOT_STICKY;    //basically "quit" this service
        }
        userAgent = getResources().getString(R.string.requestUserAgent);
        running = true;

        if (timer == null || ttask == null) {
            timer = new Timer();
            ttask = new TimerTask() {
                @Override public void run() {
                    if (!running) {
                        //Logger.getLogger("a621").info("Canceling DMail schedule");
                        timer.cancel();
                        timer.purge();
                        ttask = null;
                        timer = null;
                        return;
                    }
                    //Logger.getLogger("a621").info("DMail Service resurrected.");
                    requestDMail();
                    //Logger.getLogger("a621").info("DMail Service hibernating...");
                }
            };
            //Logger.getLogger("a621").info("Scheduling DMail Service");
            timer.schedule(ttask, 0, delay);
        }

        return START_REDELIVER_INTENT;
    }

    public void requestDMail() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null) {
            int cnt = 0;
            StringBuilder cont = new StringBuilder();
            for (XMLNode msg : result.getChildren()) {
                if (!msg.getType().equals("dmail")) {
                    cnt = 0;
                    break;
                }
                //Logger.getLogger("a621").info ("DMail: " + msg.toString());
                Boolean seen = Boolean.parseBoolean(msg.getFirstChildText("has-seen"));
                if (!seen) {
                    cnt++;
                    cont.append(msg.getFirstChildText("title") + " \n");
                }
            }
            if (cnt > 0) {
                NotificationManager notif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                Intent dmail = new Intent(getApplicationContext(), PostsActivity.class);
                PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, dmail, 0);
                Notification notify = new Notification.Builder(this)
                        .setContentTitle("You have " + cnt + " new DMails")
                        .setContentText(cont.toString().trim())
                        .setSmallIcon(R.drawable.ic_notify_dmail)
                        .setContentIntent(pending)
                        .setAutoCancel(true)
                        .build();

                notif.notify(0, notify);
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
    }

    @Override public void onDestroy() {
        super.onDestroy();
        Logger.getLogger("a621").info("DMail Service stopped! (Destroyed)");
    }
}
