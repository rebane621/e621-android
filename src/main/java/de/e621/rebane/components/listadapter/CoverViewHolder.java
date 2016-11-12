package de.e621.rebane.components.listadapter;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.itwookie.XMLreader.XMLNode;

import de.e621.rebane.a621.R;
import de.e621.rebane.activities.CoverShowActivity;

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
        txtName.setText(type.equals("set")?data.getFirstChildContent("name").orElse(""):data.getAttribute("name").orElse(""));
        txtDesc.setText((type.equals("set")?data.getFirstChildContent("post-count").orElse(""):data.getAttribute("post_count").orElse("")) + " posts, by user " + (type.equals("set")?data.getFirstChildContent("user-id").orElse(""):data.getAttribute("user_id").orElse("")));
    }
}
