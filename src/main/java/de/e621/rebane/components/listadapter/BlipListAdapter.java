package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.e621.rebane.FilterManager;
import de.e621.rebane.a621.R;
import de.e621.rebane.xmlreader.XMLNode;

public class BlipListAdapter extends XMLListAdapter {
    FilterManager blacklist;
    String baseURL;
    boolean postLoad;
    public int getLastPage(int pagesize) {
        return 100;
    } //can't be retrieved

    public BlipListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList, String baseURL, FilterManager avatarFilter, boolean postLoadUserdata) {
        super(context, textViewResourceId, rowDataList);
        CommentViewHolder.resetAuthorData();
        blacklist = avatarFilter;
        this.baseURL = baseURL;
        postLoad = postLoadUserdata;
        svNumPosts=Integer.MAX_VALUE;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        BlipViewHolder holder = new BlipViewHolder(getContext(), baseURL, blacklist, postLoad, this);
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.blip_layout, parent, false);

        holder.populate(position, convertView, list.get(position));

        //return the row view.
        return convertView;
    }
}
