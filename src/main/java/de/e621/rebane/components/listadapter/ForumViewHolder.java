package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.activities.PostShowActivity;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class ForumViewHolder {
    TextView txtTitle, txtCreator;

    public ForumViewHolder() { }

    public void populate(int position, View convertView, final XMLNode data) {
        txtTitle = (TextView) convertView.findViewById(R.id.lblTitle);
        txtCreator = (TextView) convertView.findViewById(R.id.lblName);

        txtTitle.setTag(position);
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Entry " + position + " was pressed", Toast.LENGTH_LONG).show();
                /*/Intent intent = new Intent(view.getContext(), PostShowActivity.class);
                intent.putExtra(PostShowActivity.EXTRAPOSTDATA, data);
                view.getContext().startActivity(intent);/*/
            }
        });


        txtTitle.setText(data.getFirstChildText("title"));
        txtCreator.setText(data.getFirstChildText("creator"));
    }
}
