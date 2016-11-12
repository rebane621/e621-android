package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.itwookie.XMLreader.XMLNode;

import java.util.ArrayList;
import java.util.List;

import de.e621.rebane.FilterManager;
import de.e621.rebane.a621.R;

public class CommentListAdapter extends ArrayAdapter<XMLNode> {
    private List<XMLNode> list = null;
    public int svNumPosts=0;
    public boolean canPaginateNext(int page, int pagesize) { return ((page-1)*pagesize + list.size() < svNumPosts); }
    public boolean canPaginateLast(int page) { return page > 1; }
    FilterManager blacklist;
    String baseURL;
    boolean postLoad;
    public CommentListAdapter(Context context, int textViewResourceId, List<XMLNode> rowDataList, String baseURL, FilterManager avatarFilter, boolean postLoadUserdata) {
        super(context, textViewResourceId, rowDataList);
        this.list = new ArrayList<XMLNode>();
        this.list.addAll(rowDataList);
        CommentViewHolder.resetAuthorData();
        blacklist = avatarFilter;
        this.baseURL = baseURL;
        postLoad = postLoadUserdata;
    }

    public int getResultCount() { return (list==null ? 0 : list.size()); }
    public XMLNode getResult(int i) { return (list==null ? null : list.get(i)); }

    public View getView(final int position, View convertView, ViewGroup parent) {

        CommentViewHolder holder = new CommentViewHolder(getContext(), baseURL, blacklist, postLoad, this);
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        convertView = inflator.inflate(R.layout.comment_layout, parent, false);

        holder.populate(position, convertView, list.get(position));

        //return the row view.
        return convertView;
    }
}
