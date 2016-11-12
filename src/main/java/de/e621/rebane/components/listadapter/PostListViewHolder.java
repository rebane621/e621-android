package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.itwookie.XMLreader.XMLNode;

import de.e621.rebane.a621.R;
import de.e621.rebane.activities.PostShowActivity;
import de.e621.rebane.components.WebImageView;

public class PostListViewHolder {
    WebImageView previewImage;
    TextView txtLeft, txtMid, txtRight;
    ArrayAdapter<?> parentAdapter;

    String imageQuality;
    public PostListViewHolder(String quality, ArrayAdapter parent) { imageQuality = quality; parentAdapter = parent; }

    public void populate(final int position, View convertView, final XMLNode data, final int pageoffset, final String query) {
        previewImage = (WebImageView) convertView.findViewById(R.id.previewImage);
        txtLeft = (TextView) convertView.findViewById(R.id.txtLeft);
        txtMid = (TextView) convertView.findViewById(R.id.txtMid);
        txtRight = (TextView) convertView.findViewById(R.id.txtRight);

        previewImage.setTag(position);
        previewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Entry " + position + " was pressed", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(view.getContext(), PostShowActivity.class);
                intent.putExtra(PostShowActivity.EXTRAPOSTDATA, data);
                intent.putExtra(PostShowActivity.EXTRASEARCHQUERY, query);
                intent.putExtra(PostShowActivity.EXTRASEARCHOFFSET, pageoffset+position+1); //counting natural (+1)
                view.getContext().startActivity(intent);
            }
        });
        previewImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View view) {
                Toast.makeText(view.getContext().getApplicationContext(), "TODO: Open popup and ask the user for action", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        Context context = convertView.getContext();
        boolean isBlacklisted = Boolean.valueOf(data.getAttribute("Blacklisted").orElse(""));
        String tmp = data.getFirstChildContent("file_ext").orElse("");
        if (isBlacklisted)
            previewImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.thumb_blocked));
        else if (tmp.equals("swf"))
            previewImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.thumb_flash));
        else if (tmp.equals("webm"))
            previewImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.thumb_webm));
        else {
            previewImage.setPlaceholderImage(context.getResources().getDrawable(R.mipmap.thumb_loading));
            previewImage.setAdapter(parentAdapter);
            previewImage.setImageUrl(data.getFirstChildContent("md5").orElse("")+"th", data.getFirstChildContent(imageQuality).orElse(""), false);
        }

        boolean isParent = Boolean.parseBoolean(data.getFirstChildContent("has_children").orElse(""));
        boolean isChild = !data.getChildrenByTagName("parent_id").get(0).attributes().contains("nil");
        if ("pending".equals(data.getFirstChildContent("status").orElse("")))
            convertView.setBackground(context.getResources().getDrawable(R.drawable.thumb_bpending));   //blue
        else if (isChild)
            convertView.setBackground(context.getResources().getDrawable(R.drawable.thumb_bchild));     //yellow
        else if (isParent)
            convertView.setBackground(context.getResources().getDrawable(R.drawable.thumb_bparent));    //green
        else
            convertView.setBackground(context.getResources().getDrawable(R.drawable.thumb_bnormal));    //none

        int value = Integer.parseInt(data.getFirstChildContent("score").orElse(""));
        if (value < 0) txtLeft.setTextColor(context.getResources().getColor(R.color.preview_red));
        else if (value > 0) txtLeft.setTextColor(context.getResources().getColor(R.color.preview_green));
        else txtLeft.setTextColor(context.getResources().getColor(R.color.text_neutral));
        txtLeft.setText("▲" + value);

        value = Integer.parseInt(data.getFirstChildContent("fav_count").orElse(""));
        txtMid.setText("♥" + value);

        String rating = data.getFirstChildContent("rating").orElse("").toUpperCase();
        if (rating.equals("E")) txtRight.setTextColor(context.getResources().getColor(R.color.preview_red));
        else if (rating.equals("S")) txtRight.setTextColor(context.getResources().getColor(R.color.preview_green));
        else txtRight.setTextColor(context.getResources().getColor(R.color.preview_yellow));
        txtRight.setText(rating);
    }
}
