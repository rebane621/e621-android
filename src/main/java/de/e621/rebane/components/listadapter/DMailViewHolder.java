package de.e621.rebane.components.listadapter;

import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import de.e621.rebane.a621.R;
import de.e621.rebane.activities.DMailShowActivity;

public class DMailViewHolder {
    TextView txtTitle, txtCreator;

    public DMailViewHolder(int mailID) { this.mailID=mailID; }

    int mailID;

    public void populate(int position, View convertView, String line1, String line2) {
        txtTitle = (TextView) convertView.findViewById(R.id.lblTitle);
        txtCreator = (TextView) convertView.findViewById(R.id.lblName);

        txtTitle.setTag(position);
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Entry " + position + " was pressed", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(view.getContext(), DMailShowActivity.class);
                intent.putExtra(DMailShowActivity.DMAILMESSAGEID, mailID);
                view.getContext().startActivity(intent);
            }
        });


        txtTitle.setText(Html.fromHtml(line1));
        txtCreator.setText(Html.fromHtml(line2));
    }
}
