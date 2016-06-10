package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.logging.Logger;

import de.e621.rebane.HTMLformat;
import de.e621.rebane.LoginManager;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;
import de.e621.rebane.xmlreader.XMLNode;

public class DMailListAdapter extends XMLListAdapter {

    public int getLastPage(int pagesize) {
        return 100;
    }

    public DMailListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList) {
        super(context, textViewResourceId, rowDataList);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        DMailViewHolder holder = new DMailViewHolder(Integer.parseInt(list.get(position).getFirstChildText("id")));

        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.twolined_listentry, parent, false);

        SQLiteDB db = new SQLiteDB(getContext().getApplicationContext());
        db.open();
        LoginManager lm = LoginManager.getInstance(getContext().getApplicationContext(), db);
        XMLNode tmp = list.get(position);
        //Logger.getLogger("a621").info(tmp.toString());

        Integer from = Integer.parseInt(tmp.getFirstChildText("from-id")),
            to = Integer.parseInt(tmp.getFirstChildText("to-id"));
        Integer dir = (lm.getUserid() == null ? 0 : (lm.getUserid().compareTo(to)!=0 ? 1 : -1));
        int colorcode = (dir < 0 ? getContext().getResources().getColor(R.color.preview_green) :
                            (dir > 0 ? getContext().getResources().getColor(R.color.preview_red) :
                            getContext().getResources().getColor(R.color.text_neutral)));
        Logger.getLogger("a621").info(String.format("f%d - t%d - u%d > %d", from, to, lm.getUserid(), dir));
        holder.populate(position, convertView,
                HTMLformat.colored((Boolean.parseBoolean(tmp.getFirstChildText("has-seen"))?"":"* ")+tmp.getFirstChildText("title"), colorcode),
                from + " > " + to + " - " + MiscStatics.readableTime(tmp.getFirstChildText("created-at")));

        return convertView;
    }
}
