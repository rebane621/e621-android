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

public class PostListAdapter extends ArrayAdapter<XMLNode> {
    private List<XMLNode> list = null;
    private String quality;
    public int svNumPosts=0;
    public int getLastPage(int pagesize) {
        int max = (int)Math.ceil((double)svNumPosts/(double)pagesize);
        return max;
    }

    public PostListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList, String quality) {
        super(context, textViewResourceId, rowDataList);
        this.list = new ArrayList<XMLNode>();
        this.list.addAll(rowDataList);
        this.quality = quality;
    }

    public int getResultCount() { return (list==null ? 0 : list.size()); }
    public XMLNode getResult(int i) { return (list==null ? null : list.get(i)); }


    public View getView(final int position, View convertView, ViewGroup parent) {
        ThumbnailViewHolder holder = new ThumbnailViewHolder(quality);

        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.preview_layout, parent, false);

        holder.populate(position, convertView, list.get(position));

        //return the row view.
        return convertView;
    }
}
