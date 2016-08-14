package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.media.CamcorderProfile;
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
    } //can't be computed

    public DMailListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList) {
        super(context, textViewResourceId, rowDataList);
        svNumPosts = Integer.MAX_VALUE; //can't be computed
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

        holder.populate(position, convertView, tmp, lm);

        return convertView;
    }
}
