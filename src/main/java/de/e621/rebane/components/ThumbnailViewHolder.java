package de.e621.rebane.components;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;

import de.e621.rebane.a621.R;
import de.e621.rebane.activities.PostShowActivity;
import de.e621.rebane.xmlreader.XMLNode;

public class ThumbnailViewHolder {
    WebImageView previewImage;
    TextView txtLeft, txtMid, txtRight;
    ArrayAdapter<?> parentAdapter;

    String imageQuality;
    public ThumbnailViewHolder(String quality, ArrayAdapter parent) { imageQuality = quality; parentAdapter = parent; }

    public void populate(int position, View convertView, final XMLNode data) {
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
                view.getContext().startActivity(intent);
            }
        });


        Context context = convertView.getContext();
        boolean isBlacklisted = Boolean.valueOf(data.getAttribute("Blacklisted"));
        String tmp = data.getFirstChildText("file_ext");
        if (isBlacklisted)
            previewImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.thumb_blocked));
        else if (tmp.equals("swf"))
            previewImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.thumb_flash));
        else if (tmp.equals("webm"))
            previewImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.thumb_webm));
        else {
            previewImage.setPlaceholderImage(context.getResources().getDrawable(R.mipmap.thumb_loading));
            previewImage.setAdapter(parentAdapter);
            previewImage.setImageUrl(data.getFirstChildText("md5")+"th", data.getFirstChildText(imageQuality), false);
        }

        boolean isParent = Boolean.parseBoolean(data.getFirstChildText("has_children"));
        boolean isChild = !data.getChildrenByTagName("parent_id")[0].attributes().contains("nil");
        if (isChild)
            convertView.setBackground(context.getResources().getDrawable(R.drawable.thumb_bchild));
        else if (isParent)
            convertView.setBackground(context.getResources().getDrawable(R.drawable.thumb_bparent));
        else
            convertView.setBackground(context.getResources().getDrawable(R.drawable.thumb_bnormal));

        int value = Integer.parseInt(data.getFirstChildText("score"));
        if (value < 0) txtLeft.setTextColor(context.getResources().getColor(R.color.preview_red));
        else if (value > 0) txtLeft.setTextColor(context.getResources().getColor(R.color.preview_green));
        else txtLeft.setTextColor(context.getResources().getColor(R.color.text_neutral));
        txtLeft.setText("▲" + value);

        value = Integer.parseInt(data.getFirstChildText("fav_count"));
        txtMid.setText("♥" + value);

        String rating = data.getFirstChildText("rating").toUpperCase();
        if (rating.equals("E")) txtRight.setTextColor(context.getResources().getColor(R.color.preview_red));
        else if (rating.equals("S")) txtRight.setTextColor(context.getResources().getColor(R.color.preview_green));
        else txtRight.setTextColor(context.getResources().getColor(R.color.preview_yellow));
        txtRight.setText(rating);
    }
}
