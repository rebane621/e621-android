package de.e621.rebane;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

/**
 * Created by Michael on 17.04.2016.
 */
public class MiscStatics {

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
                Logger.getLogger("a621").info(System.currentTimeMillis() + " < " + dateReader.parse(ab[0]).getTime() + " T " + timeReader.parse(ab[1]).getTime() + " > " + time);
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
        Logger.getLogger("a621").info(text + " > " + res);
        return res;
    }

    public static String parseDText(String raw) {
        return raw;
    }
}
