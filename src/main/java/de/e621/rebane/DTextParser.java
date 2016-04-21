package de.e621.rebane;

import java.util.HashMap;
import java.util.Map;

/**
 * Finish this class ... sometime... plz?
 * Could we maybe get a API call to receive the HTML result? Would that be easier for android Spanned stuff?
 */
public class DTextParser {
    public static String parse(String text) {
        String html = text;

        Map<String,String> bbMap = new HashMap<String , String>();

        //BBCode
        bbMap.put("(\r\n|\r|\n|\n\r)", "<br>");
        bbMap.put("\\[b\\](.+?)\\[/b\\]", "<b>$1</b>");
        bbMap.put("\\[i\\](.+?)\\[/i\\]", "<i>$1</i>");
        bbMap.put("\\[u\\](.+?)\\[/u\\]", "<u>$1</u>");
        bbMap.put("\\[s\\](.+?)\\[/s\\]", "<span style='text-decoration:line-through;'>$1</span>");
        bbMap.put("\\[o\\](.+?)\\[/o\\]", "<span style='text-decoration:overline;'>$1</span>");
        bbMap.put("\\[sup\\](.+?)\\[/sup\\]", "<sup>$1</sup>");
        bbMap.put("\\[sub\\](.+?)\\[/sub\\]", "<sub>$1</sub>");
        bbMap.put("\\[quote\\](.+?)\\[/quote\\]", "<pre>$1</pre>");
        bbMap.put("\\[color=(.+?)\\](.+?)\\[/color\\]", "<font color=\"$1;\">$2</span>");

        //enclosing stuff
        //bbMap.put("`(.+?)`", "<span style=\"background:#64ffffff;\">$1</span>"); //inline code .... really sloppy
        bbMap.put("\"(.+?)\":/([^\\s-]+)", "<a href=\"https://e621.net/$2\">$1</a>");    //local links, that stay on the page
        bbMap.put("\"(.+?)\":([^\\s-]+)", "<a href=\"$2\">$1</a>");
        bbMap.put("\\[\\[(.+?)\\]\\]", "<a href=\"https://e621.net/wiki/show?title=$1\">$1</a>");
        bbMap.put("\\[\\[(.+?)|(.+?)\\]\\]", "<a href=\"https://e621.net/wiki/show?title=$1\">$2</a>");
        bbMap.put("\\{\\{(.+?)\\}\\}", "<a href=\"https://e621.net/post/index?tags=$1\">$1</a>");
        bbMap.put("post #([0-9]+?)", "<a href=\"https://e621.net/post/show/$1\">post #$1</a>");
        bbMap.put("thumb #([0-9]+?)", "<a href=\"https://e621.net/post/show/$1\">thumb #$1</a>");

        for (Map.Entry entry: bbMap.entrySet()) {
            html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }

        return html;
    }
}
