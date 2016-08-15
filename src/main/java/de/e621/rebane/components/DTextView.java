package de.e621.rebane.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import de.e621.rebane.DTextParser;
import de.e621.rebane.MiscStatics;

public class DTextView extends TextView {

    public DTextView(Context context) {
        super(context);
    }

    public DTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDText(String raw) {
        String html = DTextParser.parse(raw); //returns HTML
        MiscStatics.setTextViewHTML(this, html);
        //setText(Html.fromHtml(html));
    }


}
