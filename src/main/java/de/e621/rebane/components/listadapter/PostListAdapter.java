package de.e621.rebane.components.listadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.e621.rebane.a621.R;
import de.e621.rebane.activities.PostsActivity;
import de.e621.rebane.xmlreader.XMLNode;

public class PostListAdapter extends XMLListAdapter {
    private String quality;
    private int pageoffset;
    private String escapedQuery;

    public PostListAdapter(PostsActivity context, int textViewResourceId, List<XMLNode> rowDataList, String quality) {
        super(context.getApplicationContext(), textViewResourceId, rowDataList);
        this.quality = quality;
        this.pageoffset = (context.page-1)*context.pagesize;
        this.escapedQuery = context.query;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        PostListViewHolder holder = new PostListViewHolder(quality, this);

        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.preview_layout, parent, false);

        holder.populate(position, convertView, list.get(position), pageoffset, escapedQuery);

        return convertView;
    }
}
