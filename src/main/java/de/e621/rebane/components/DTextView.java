package de.e621.rebane.components;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import de.e621.rebane.DTextParser;

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
        setText(Html.fromHtml(html));
    }


}
