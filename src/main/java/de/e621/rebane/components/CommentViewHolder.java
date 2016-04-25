package de.e621.rebane.components;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class CommentViewHolder {
    private static Map<String, AuthorData> authorData = new HashMap<String, AuthorData>();
    private static List<String> requestRunningFor = new LinkedList<String>();
    public static void resetAuthorData() {
        authorData = new HashMap<String, AuthorData>();
        requestRunningFor = new LinkedList<String>();
    }
    class AuthorData {
        final private String name;
        final private int uid;
        private int iid;    //avatar
        private int rank;
        public AuthorData (String name, int userID, int avatar, int rank) {
            this.name = name;
            uid=userID;
            iid= avatar;
            this.rank=rank;
        }
        public String getName() { return name; }
        public int getUid() { return uid; }
        public int getIid() { return iid; }
        public void setIid(int avatar) { iid=avatar; }
        public int getLevel() { return rank; }
        public void setLevel(int level) { rank=level; }
        public String getRankString() { return MiscStatics.getRankString(rank); }
    }
    Context context;
    public CommentViewHolder(Context context) {
        this.context = context;
    }

    WebImageView avatar;
    TextView txtNick, txtRank, txtInfo, txtScore;
    DTextView txtComment;
    int ptag;

    public void populate(int position, View element, final XMLNode data) {
        avatar = (WebImageView) element.findViewById(R.id.imgAvatar);
        txtNick = (TextView) element.findViewById(R.id.lblName);
        txtRank = (TextView) element.findViewById(R.id.lblRank);
        txtInfo = (TextView) element.findViewById(R.id.lblStats1);
        txtScore = (TextView) element.findViewById(R.id.txtScore);
        txtComment = (DTextView) element.findViewById(R.id.txtComment);
        ptag = position;
        avatar.setTag(ptag);
        final View elem = element;

        final String UserID = data.getFirstChildText("creator_id");
        AuthorData author = (authorData.containsKey(UserID) ? authorData.get(UserID) : null);
        if (author == null && !requestRunningFor.contains(UserID) && MiscStatics.canRequest(context)) {
            author = new AuthorData(data.getFirstChildText("creator"), Integer.valueOf(UserID), 0, -100);
            authorData.put(UserID, author);
            requestRunningFor.add(UserID);
            (new XMLTask(context) {
                @Override protected void onPostExecute(XMLNode result) {
                    XMLNode workwith = result.children().get(0);
                    String id = workwith.getAttribute("id");
                    AuthorData res = authorData.get(id);//new AuthorData(workwith.getAttribute("name"), Integer.valueOf(id), 0, Integer.valueOf(workwith.getAttribute("level")));
                    res.setLevel(Integer.valueOf(workwith.getAttribute("level")));
                    try {
                        fillAuthorData(res);
                        authorData.put(id, res);
                    } catch (Exception e) {e.printStackTrace();} // in case activity gets closed
                }
            }).execute("https://e621.net/user/index.xml?id=" + UserID);
        }

        txtNick.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Toast.makeText(view.getContext(), "User ID: " + UserID + "\nTODO: make user activity", Toast.LENGTH_LONG).show();
            }
        });
        fillAuthorData(author);

        String info = MiscStatics.readableTime(data.getFirstChildText("created_at")) + "\n#" + data.getFirstChildText("id");
        txtInfo.setText(info);

        int score = Integer.valueOf(data.getFirstChildText("score"));
        txtScore.setText(data.getFirstChildText("score"));
        if (score > 0)
            txtScore.setTextColor(element.getResources().getColor(R.color.preview_green));
        else if (score < 0)
            txtScore.setTextColor(element.getResources().getColor(R.color.preview_red));
        else txtScore.setTextColor(element.getResources().getColor(R.color.text_neutral));

        txtComment.setDText(data.getFirstChildText("body"));
    }

    public void fillAuthorData(AuthorData author) {
        try {
            avatar.setImageDrawable(context.getResources().getDrawable(R.mipmap.thumb_loading));
            avatar.setBackground(context.getResources().getDrawable(R.drawable.thumb_bdeleted));
        /*/// There is currently NO way of getting a users avatar and I will NOT request the HTML
            setPlaceholder(Loading)
            setImageURL(iid, url, false)
        //*///
            txtNick.setText(author.getName());
            txtRank.setText(author.getLevel() == -100 ? "..." : author.getRankString());
        } catch (Exception e) {}    //in case the element was not yet loaded
    }
}
