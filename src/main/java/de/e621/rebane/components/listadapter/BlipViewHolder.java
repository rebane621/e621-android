package de.e621.rebane.components.listadapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.itwookie.XMLreader.XMLNode;
import com.itwookie.XMLreader.XMLTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.e621.rebane.FilterManager;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.activities.PostShowActivity;
import de.e621.rebane.components.DTextView;
import de.e621.rebane.components.WebImageView;

/** blips do not have as detailed information, so I duped the commentViewHoler to match */
public class BlipViewHolder {
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
        private String ImageURL = null;    //to reduce queries
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
        public String getImageURL() { return ImageURL; }
        public void setImageURL(String url) { ImageURL = url; }
        public int getLevel() { return rank; }
        public void setLevel(int level) { rank=level; }
        public String getRankString() { return MiscStatics.getRankString(rank); }
    }
    Context context;
    String baseURL;
    FilterManager fm;   //to check blacklisted avatars
    boolean fancyComments;
    ArrayAdapter parentAdapter;
    public BlipViewHolder(Context context, String baseURL, FilterManager blacklist, boolean postLoad, ArrayAdapter parent) {
        this.context = context; this.baseURL = baseURL; fm = blacklist; fancyComments = postLoad; parentAdapter = parent;
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
        
        final String UserID = data.getFirstChildContent("user_id").orElse(""); //blips are working with "user" instead of "creator"
        AuthorData author = (authorData.containsKey(UserID) ? authorData.get(UserID) : null);
        if (author == null && !requestRunningFor.contains(UserID) && MiscStatics.canRequest(context)) {
            author = new AuthorData(data.getFirstChildContent("user").orElse(""), Integer.valueOf(UserID), 0, -100);
            authorData.put(UserID, author);
            if (fancyComments) {
                if (!MiscStatics.canRequest(context)) return;
                requestRunningFor.add(UserID);
                (new XMLTask() {
                    @Override protected void onPostExecute(XMLNode result) {
                        if (result == null || !"users".equals(result.getType()) || result.getChildCount()<1) {
                            Logger.getLogger("a621").info("Got Problems with UserInformation " + (result==null?"=NUL": result.getChildCount()+" "+result.getType()+"@\n"+result.toString()));
                            return;
                        }
                        XMLNode workwith = result.getChildren().get(0);
                        String id = workwith.getAttribute("id").orElse("");
                        AuthorData res = authorData.get(id);//new AuthorData(workwith.getAttribute("name"), Integer.valueOf(id), 0, Integer.valueOf(workwith.getAttribute("level")));
                        res.setLevel(Integer.valueOf(workwith.getAttribute("level").orElse("")));
                        if (workwith.attributes().contains("avatar_id") && !workwith.getAttribute("avatar_id").isPresent())
                            res.setIid(Integer.valueOf(workwith.getAttribute("avatar_id").orElse("")));
                        try {
                            fillAuthorData(res);
                            authorData.put(id, res);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } // in case activity gets closed
                    }
                }).execute(baseURL + "user/index.xml?id=" + UserID);
            }
        }

        txtNick.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Toast.makeText(view.getContext(), "User ID: " + UserID + "\nTODO: make user activity", Toast.LENGTH_LONG).show();
            }
        });
        fillAuthorData(author);
        if (fancyComments) {
            avatar.setAdapter(parentAdapter);
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    int iid = authorData.get(UserID).getIid();
                    Toast.makeText(view.getContext(), "Show Post: " + iid, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(view.getContext(), PostShowActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PostShowActivity.EXTRAPOSTID, iid);
                    view.getContext().startActivity(intent);
                }
            });
        } else {
            avatar.setVisibility(View.GONE);
            txtRank.setVisibility(View.GONE);
        }

        //created and score functions not available for blips

        txtComment.setDText(data.getFirstChildContent("body").orElse(""));

    }

    public void fillAuthorData(AuthorData author) {
        try {
            //avatar.setImageDrawable(context.getResources().getDrawable(R.mipmap.thumb_loading));
            //avatar.setBackground(context.getResources().getDrawable(R.drawable.thumb_bdeleted));

            //get the avatar thumbnail url
            String url = author.getImageURL();
            final String aid = String.valueOf(author.getUid());
            if (author.getIid() > 0) {
                if (url == null) {
                    if (MiscStatics.canRequest(context)) {
                        avatar.setPlaceholderImage(context.getResources().getDrawable(R.mipmap.thumb_loading));
                        author.setImageURL("");//only load once
                        new XMLTask() {
                            @Override protected void onPostExecute(XMLNode result) {
                                AuthorData putto = authorData.get(aid);
                                if (result == null) {
                                    putto.setImageURL(null);    // ready for retry
                                } else if (fm.isBlacklisted(result)) {
                                    putto.setImageURL("blacklisted");
                                    avatar.setPlaceholderImage(context.getResources().getDrawable(R.mipmap.thumb_blocked));
                                    avatar.postInvalidate();
                                } else {
                                    putto.setImageURL(result.getFirstChildContent("preview_url").orElse(""));
                                    avatar.setImageUrl("avatar" + aid, putto.getImageURL(), false);
                                    avatar.postInvalidate();
                                }
                                authorData.put(aid, putto);
                            }
                        }.execute(baseURL + "post/show.xml?id=" + author.getIid());
                    }
                } else {
                    if (url == "blacklisted") {
                        avatar.setPlaceholderImage(context.getResources().getDrawable(R.mipmap.thumb_blocked));
                    } else if (!url.isEmpty()) {
                        avatar.setImageUrl("avatar" + author.getIid(), url, false);
                    }
                }
            } else {
                avatar.setPlaceholderImage(context.getResources().getDrawable(R.mipmap.thumb_loading));
            }

            txtNick.setText(author.getName());
            txtRank.setText(author.getLevel() == -100 ? "..." : author.getRankString());
        } catch (Exception e) {}    //in case the element was not yet loaded
    }
}
