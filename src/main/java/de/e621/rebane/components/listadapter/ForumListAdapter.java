package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itwookie.XMLreader.XMLNode;

import java.util.List;

import de.e621.rebane.a621.R;

public class ForumListAdapter extends XMLListAdapter {

    public int getLastPage(int pagesize) {
        return 100;
    } //can't be retrieved

    public ForumListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList) {
        super(context, textViewResourceId, rowDataList);
        svNumPosts = Integer.MAX_VALUE; //can't be computed
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ForumViewHolder holder = new ForumViewHolder();

        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.twolined_listentry, parent, false);

        holder.populate(position, convertView, list.get(position));

        return convertView;
    }
}
