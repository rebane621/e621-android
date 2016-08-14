package de.e621.rebane.components.listadapter;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import de.e621.rebane.a621.R;
import de.e621.rebane.activities.CoverShowActivity;
import de.e621.rebane.xmlreader.XMLNode;

public class CoverViewHolder {
    TextView txtName, txtDesc;

    public CoverViewHolder() { }

    public void populate(int position, View convertView, final XMLNode data, final String type) {
        txtName = (TextView) convertView.findViewById(R.id.txtName);
        txtDesc = (TextView) convertView.findViewById(R.id.txtDesc);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Entry " + position + " was pressed", Toast.LENGTH_LONG).show();
                Intent intent;

                intent = new Intent(view.getContext(), CoverShowActivity.class);
                intent.putExtra(CoverShowActivity.EXTRADISPLAYDATA, data);
                intent.putExtra(CoverShowActivity.EXTRADISPLAYTYPE, type);

                view.getContext().startActivity(intent);
            }
        });

        txtName.setTag(position);
        txtName.setText(type.equals("set")?data.getFirstChildText("name"):data.getAttribute("name"));
        txtDesc.setText((type.equals("set")?data.getFirstChildText("post-count"):data.getAttribute("post_count")) + " posts, by user " + (type.equals("set")?data.getFirstChildText("user-id"):data.getAttribute("user_id")));
    }
}
