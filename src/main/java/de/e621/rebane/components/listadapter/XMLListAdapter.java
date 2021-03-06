package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.itwookie.XMLreader.XMLNode;

import java.util.ArrayList;
import java.util.List;

public abstract class XMLListAdapter extends ArrayAdapter<XMLNode> {
    List<XMLNode> list = null;
    public int svNumPosts=0;
    public int getLastPage(int pagesize) {
        int max = (int)Math.ceil((double)svNumPosts/(double)pagesize);
        return max;
    }

    public XMLListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList) {
        super(context, textViewResourceId, rowDataList);
        this.list = new ArrayList<XMLNode>();
        this.list.addAll(rowDataList);
    }

    public int getResultCount() { return (list==null ? 0 : list.size()); }
    public XMLNode getResult(int i) { return (list==null ? null : list.get(i)); }

    public abstract View getView(final int position, View convertView, ViewGroup parent);
}
