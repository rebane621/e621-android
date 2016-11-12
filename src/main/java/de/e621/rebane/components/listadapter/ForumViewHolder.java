package de.e621.rebane.components.listadapter;

import android.view.View;
import android.widget.TextView;

import com.itwookie.XMLreader.XMLNode;

import de.e621.rebane.a621.R;

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


        txtTitle.setText(data.getFirstChildContent("title").orElse(""));
        txtCreator.setText(data.getFirstChildContent("creator").orElse(""));
    }
}
