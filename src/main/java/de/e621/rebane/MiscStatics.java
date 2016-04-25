package de.e621.rebane;

import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.e621.rebane.components.WebImageView;

/**
 * Created by Michael on 17.04.2016.
 */
public class MiscStatics {

    private static int maxRequests = 30;
    private static List<Long> requestLimiter = new LinkedList<Long>();
    private static long limiterlasttoast=0; //to prevent toast spamming
    public static boolean canRequest(Context context) {
        List<Long> remove = new ArrayList<Long>();
        for (Long l : requestLimiter)
            if (System.currentTimeMillis()-l > 60000)
                remove.add(l);
        requestLimiter.removeAll(remove);

        if (requestLimiter.size() < maxRequests) {
            requestLimiter.add(System.currentTimeMillis());
            return true;
        }
        long now = System.currentTimeMillis();
        if (now-limiterlasttoast > 3000) {
            Toast.makeText(context, "Too many requests!\nPlease don't stress the Server this much", Toast.LENGTH_SHORT).show();
            limiterlasttoast = now;
        }
        return false;
    }
    public static void addRequestTO(long time) {
        requestLimiter.add(time);
    }
    public static List<Long> getRequestHistory() {
        return requestLimiter;
    }

    public static String getRankString(int level) {
        int[] lvl = new int[] {0, 10, 20, 30, 33, 34, 35, 40, 50};
        String[] name = new String[] {"Unactivated", "Blocked", "Member", "Privileged", "Contributor", "Test Janitor", "Mod", "Admin"};
        for (int i=0; i< lvl.length; i++)
            if (lvl[i] == level) return name[i];
        return "???";
    }

    public static String readableTime(String text) {
        long time;
        if (text.contains("T")) {
            String ab[] = text.split("T");
            SimpleDateFormat dateReader = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeReader = new SimpleDateFormat("HH:mm:ssZZZZZ");
            try {
                time = System.currentTimeMillis() - (dateReader.parse(ab[0]).getTime() + timeReader.parse(ab[1]).getTime());
            } catch (Exception e) {
                time = 0;
            }
        } else {
            SimpleDateFormat timeReader = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                time = System.currentTimeMillis() - timeReader.parse(text).getTime();
            } catch (Exception e) {
                time = 0;
            }
        }
        time /= 1000;   //in seconds
        if (time < 60)  return "Just now";
        time /=60;      //first calc to minutes
        String[] units = new String[]{" minute",    " hour",    " day",     " week",    " month",   " year",    "decade"};
        double[] conv = new double[] {  60.0,         24.0,       7.0,        4.2857,     12,         10};
        int i = 0;
        while (time > conv[i] && i < conv.length) { time /= conv[i]; i++; }
        String res = time + units[i] + (time > 1 ? "s ago" : " ago");
        return res;
    }

    public static void clearMem(Context context) {
        WebImageView.cleanUp(context);
    }
    public static void clearMem(Context context, int reqMiB, double reqPercent) {
        WebImageView.cleanUp(context, reqMiB, reqPercent);
    }
    // used mem = total mem - free mem
    // total free mem (because the heap can expand and free mem is related to the current heap (total mem), not the max heap):
    // max heap - used mem = max heap - (total mem - free mem)
    public static long freeRamB() {
        Runtime rt = Runtime.getRuntime();
        return rt.maxMemory()-(rt.totalMemory()-rt.freeMemory());
    }
    public static float freeRamPerc() {
        Runtime rt = Runtime.getRuntime();
        return ((float)(rt.maxMemory()-(rt.totalMemory()-rt.freeMemory()))/rt.maxMemory())*100;
    }
}
