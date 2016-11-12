package de.e621.rebane;

import android.text.Html;
import android.text.Spanned;

public class HTMLformat {

    public static String colorToString(int color) {
        return String.format("#%02X%02X%02X", (color>>>16)&255, (color>>>8)&255, color&255);
    }

    public static String colored(String append, int color) {
        return "<font color=\"" + colorToString(color) + "\">" + append + "</font>";
    }
    public static String bold(String append) {
        return "<b>" + append + "</b>";
    }
    public static String italic(String append) {
        return "<i>" + append + "</i>";
    }
    public static String underline(String append) {
        return "<u>" + append + "</u>";
    }
    public static String small(String append) {
        return "<small>" + append + "</small>";
    }
    public static String link(String url, String text) {
        return "<a href=\"" + url + "\">" + text + "</b>";
    }
    public static String colored(String left, String append, int color) {
        return left + colored(append, color);
    }
    public static String bold(String left, String append) {
        return left + bold(append);
    }
    public static String italic(String left, String append) {
        return left + italic(append);
    }
    public static String underline(String left, String append) {
        return left + underline(append);
    }
    public static String small(String left, String append) {
        return left + small(append);
    }
    public static String link(String left, String url, String text) {
        return left + link(url, text);
    }
    public static Spanned forTextView(String text) {
        return Html.fromHtml(text);
    }
}
