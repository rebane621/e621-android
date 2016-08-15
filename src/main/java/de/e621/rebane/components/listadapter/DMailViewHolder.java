package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.util.logging.Logger;

import de.e621.rebane.HTMLformat;
import de.e621.rebane.LoginManager;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.activities.DMailShowActivity;
import de.e621.rebane.xmlreader.XMLNode;

public class DMailViewHolder {
    TextView txtTitle, txtCreator;

    public DMailViewHolder(int mailID) { this.mailID=mailID; }

    int mailID;

    public void populate(int position, final View convertView, final XMLNode data, LoginManager lm) {
        txtTitle = (TextView) convertView.findViewById(R.id.lblTitle);
        txtCreator = (TextView) convertView.findViewById(R.id.lblName);
        final int myID = lm.getUserid();

        txtTitle.setTag(position);
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Entry " + position + " was pressed", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(view.getContext(), DMailShowActivity.class);
                intent.putExtra(DMailShowActivity.DMAILMESSAGEID, myID);
                intent.putExtra(DMailShowActivity.DMAILMESSAGENODE, data);
                view.getContext().startActivity(intent);
            }
        });
        Context cont = convertView.getContext();

        Integer from = Integer.parseInt(data.getFirstChildText("from-id")),
                to = Integer.parseInt(data.getFirstChildText("to-id"));
        Integer dir = ((lm.getUserid() == null || from.equals(to)) ? 0 : (lm.getUserid().compareTo(to)!=0 ? 1 : -1));
        int colorcode = (dir < 0 ? cont.getResources().getColor(R.color.preview_green) :
                (dir > 0 ? cont.getResources().getColor(R.color.preview_red) :
                        cont.getResources().getColor(R.color.text_neutral)));
        Logger.getLogger("a621").info(String.format("f%d - t%d - u%d > %d", from, to, lm.getUserid(), dir));

        txtTitle.setText(Html.fromHtml(HTMLformat.colored((Boolean.parseBoolean(data.getFirstChildText("has-seen"))?"":"* ")+data.getFirstChildText("title"), colorcode)));
        txtCreator.setText(Html.fromHtml(from + " > " + to + " - " + MiscStatics.readableTime(data.getFirstChildText("created-at"))));
    }
}
