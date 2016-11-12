package de.e621.rebane.components.listadapter;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import de.e621.rebane.a621.R;

public class TwoLinedViewHolder {
    TextView txtTitle, txtCreator;

    public TwoLinedViewHolder() { }

    public void populate(int position, View convertView, String line1, String line2) {
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


        txtTitle.setText(Html.fromHtml(line1));
        txtCreator.setText(Html.fromHtml(line2));
    }
}
