package de.e621.rebane.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import de.e621.rebane.a621.R;
import de.e621.rebane.xmlreader.XMLNode;

public class ForumListAdapter extends XMLListAdapter {

    public int getLastPage(int pagesize) {
        return 100;
    }

    public ForumListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList) {
        super(context, textViewResourceId, rowDataList);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ForumViewHolder holder = new ForumViewHolder();

        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.forumtopic_layout, parent, false);

        holder.populate(position, convertView, list.get(position));

        return convertView;
    }
}
