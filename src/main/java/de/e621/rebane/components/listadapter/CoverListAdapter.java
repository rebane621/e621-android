package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itwookie.XMLreader.XMLNode;

import java.util.List;

import de.e621.rebane.a621.R;

public class CoverListAdapter extends XMLListAdapter {

    public int getLastPage(int pagesize) {
        return Integer.MAX_VALUE;
    } //can't be retrieved
    String listType;

    public CoverListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList, String quality, String type) {
        super(context, textViewResourceId, rowDataList);
        listType = type;
        svNumPosts = Integer.MAX_VALUE; //can't be computed
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        CoverViewHolder holder = new CoverViewHolder();

        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.cover_layout, parent, false);

        holder.populate(position, convertView, list.get(position), listType);

        return convertView;
    }
}
