package de.e621.rebane.components;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.xmlreader.XMLNode;

public class CommentViewHolder {
    static Map<String, String> avatarMap = new HashMap<String, String>();   //associate a userID with a imageID (md5) to be used for the WebImageView

    WebImageView avatar;
    TextView txtNick, txtRank, txtInfo, txtScore;
    DTextView txtComment;

    public void populate(int position, View element, final XMLNode data) {
        avatar =        (WebImageView)  element.findViewById(R.id.imgAvatar);
        txtNick =       (TextView)      element.findViewById(R.id.lblName);
        txtRank =       (TextView)      element.findViewById(R.id.lblRank);
        txtInfo =       (TextView)      element.findViewById(R.id.lblStats1);
        txtScore =      (TextView)      element.findViewById(R.id.txtScore);
        txtComment =    (DTextView)     element.findViewById(R.id.txtComment);

        avatar.setTag(position);
        avatar.setImageDrawable(element.getResources().getDrawable(R.mipmap.thumb_loading));
        avatar.setBackground(element.getResources().getDrawable(R.drawable.thumb_bdeleted));

        txtNick.setText(data.getFirstChildText("creator"));
        final String UserID = data.getFirstChildText("creator_id");
        txtNick.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Toast.makeText(view.getContext(), "User ID: " + UserID, Toast.LENGTH_LONG).show();
            }
        });
        txtRank.setText("Bad API");

        String info = MiscStatics.readableTime(data.getFirstChildText("created_at")) + "\n#" + data.getFirstChildText("id");
        txtInfo.setText(info);

        int score = Integer.valueOf(data.getFirstChildText("score"));
        txtScore.setText(data.getFirstChildText("score"));
        if (score>0) txtScore.setTextColor(element.getResources().getColor(R.color.preview_green));
        else if (score<0) txtScore.setTextColor(element.getResources().getColor(R.color.preview_red));
        else txtScore.setTextColor(element.getResources().getColor(R.color.text_neutral));

        txtComment.setDText(data.getFirstChildText("body"));
    }
}
