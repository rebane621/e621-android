package de.e621.rebane;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLUtils;

public class FilterManager {

    String[] Blacklist = new String[0];
    Context context;
    String Filter = "";
    static boolean isRunning = false;

    private static Pattern numericFilter = null;

    public FilterManager(Context context, String... blacklists) {
        if (numericFilter == null) numericFilter = Pattern.compile("([\\d]+[.]{2}[\\d]+)|([<>=]{0,2}[\\d]+)");

        Blacklist = blacklists;
        this.context = context;
    }

    public List<XMLNode> proxyBlacklist(List<XMLNode> toCheck) {
        Logger.getLogger("a621").info("Blacklist: ");
        for (String bl : Blacklist) Logger.getLogger("a621").info(bl);
        for (XMLNode node : toCheck) {
            isBlacklisted(node);
        }
        return toCheck;
    }

    public boolean isBlacklisted(XMLNode check) {
        boolean listed = false;

        String tgs = check.getChildrenByTagName("tags")[0].getInnerText();
        if ( !(tgs == null || tgs.isEmpty()) ) {
            for (String bl : Blacklist) {
                //hastags && not filter applies
                if (tgs != null && !tgs.trim().isEmpty() && filterApplies(check, bl)) {
                    listed = true;
                    break;
                }
            }
        }

        check.setAttribute("Blacklisted", (listed?"true":"false"));
        //Logger.getLogger("a621").info("Set blacklisted: " + check.toString());
        return listed;
    }

    static boolean filterApplies(XMLNode node, String filter) {	//filter may contain spaces
        String[] tags = filter.split(" ");
        boolean result = true;
        for (String tag : tags) {
            if (!compareTags(node, tag)) { result = false; break; }
        }
        return result;
    }

    private static boolean compareTags(XMLNode node, String tag) {	//singel tag - NO SPCAES
        boolean fail = false;
        if (tag.startsWith("-") && tag.length()>1) { fail = true; tag = tag.substring(1); }
        if (tag.indexOf(':')>0) {	//seach for metatag
            String k = tag.substring(0,tag.indexOf(':'));
            String v = tag.substring(tag.indexOf(':')+1);
            String nv;
            switch (k) {
                case "rating":
                    nv = node.getChildrenByTagName("rating")[0].getInnerText();
                    if (nv == null) return false;
                    if (v.charAt(0) != nv.charAt(0)) return fail;
                    break;
                /*/case "order":
                    boolean asc=true;
                    if (v.endsWith("_desc")) {
                        asc = false;
                        v = v.substring(0, v.lastIndexOf('_'));
                    } else if (v.endsWith("_asc")) {
                        v = v.substring(0, v.lastIndexOf('_'));
                    }
                    sorting = new XMLComparator(v, !asc);
                    break;/*/
                default:
                    nv = node.getChildrenByTagName(k)[0].getInnerText();

                    if (numericFilter.matcher(v).matches()) {
                        return numCompare(v,nv) == !fail;
                    }
                    if (nv != null) { if (XMLUtils.isStringArray(nv)) {
                        return XMLUtils.isInList(nv,v) == !fail;
                    } else {
                        return nv.equals(v) == !fail;
                    } } else {	//if the post does not contain this tag at all, but it is searched for it failed
                        return fail;
                    }
            }
        } else {
            String ntags = node.getChildrenByTagName("tags")[0].getInnerText();
            if (ntags != null && !ntags.isEmpty()) {
                String[] ntag = ntags.split(" ");
                boolean found = false;
                for (String nt : ntag) {
                    if (nt.equals(tag)) { found = true; break; }
                }
                if (!found) return fail;
            }
        }
        return !fail;
    }

    private static boolean numCompare(String c, String n) {
        try {
            int v = Integer.valueOf(n);

            if (c.startsWith(">=")) return (v >= Integer.valueOf(c.substring(2)));
            else if (c.startsWith(">")) return (v > Integer.valueOf(c.substring(1)));
            else if (c.startsWith("<=")) return (v <= Integer.valueOf(c.substring(2)));
            else if (c.startsWith("<")) return (v < Integer.valueOf(c.substring(1)));
            else if (c.contains("..")) {
                int Start = 0, End = 0;
                if (c.startsWith("..")) End = Integer.valueOf(c.substring(2));
                else if (c.endsWith("..")) Start = Integer.valueOf(c.substring(0, c.length()-2));
                else {
                    Start = Integer.valueOf(c.substring(0, c.indexOf("..")));
                    End = Integer.valueOf(c.substring(c.indexOf("..")+2));
                }
                return (v >= Start && v <= End);
            } else {
                return (Integer.valueOf(c) == v);
            }
        } catch (NumberFormatException e) { return false; }
    }
}
