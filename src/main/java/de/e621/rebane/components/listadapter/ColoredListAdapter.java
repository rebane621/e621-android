package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.e621.rebane.FilterManager;
import de.e621.rebane.a621.R;
import de.e621.rebane.xmlreader.XMLNode;

public class ColoredListAdapter extends ArrayAdapter<String> {
    private List<String> list = null;
    private List<Integer> color = null;

    public ColoredListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        list = new LinkedList<String>();
        color = new LinkedList<Integer>();
    }

    public void add(String s, int c) {
        super.add(s);
        list.add(s);
        color.add(c);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.singleline_listentry, parent, false);

        //holder.populate(position, convertView, list.get(position));
        TextView tv = (TextView) convertView;
        tv.setText(list.get(position));
        tv.setTextColor(color.get(position));

        //return the row view.
        return convertView;
    }
}
