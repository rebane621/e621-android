package de.e621.rebane;

import android.content.Context;
import android.widget.Toast;

import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

/**
 * Created by Michael on 05.05.2016.
 */
public abstract class ActionRequest extends XMLTask {

    private static final String[] APIr = {
            "post/vote.xml?id=${0}&score=1",
            "post/vote.xml?id=${0}&score=-1"
    };
    private static final boolean[] APIb = {  //this array flags requests as broken
            true,
            true
    };
    private static final boolean[] APIl = {  //this array flags requests to require login
            true,
            true
    };
    private static final String[] APIsr = {  //Contains Success messages
            "Voted +1",
            "Voted -1"
    };
    public static final int POST_VOTE_UP = 0;
    public static final int POST_VOTE_DOWN = 1;

    private static String login;
    private int requestID, xdata[];
    private Context context;
    /** leave loginAppendix null if no login is required */
    public ActionRequest(Context context, String loginAppendix, int RequestType, int... data) {
        super(context);
        this.context = context;
        login = loginAppendix;
        requestID = RequestType;
        xdata = data;
    }

    public boolean isBroken() {
        return APIb[requestID];
    }
    public boolean requiresLogin(int RequestType) {
        return APIl[RequestType];
    }

    public void on(String baseURL) {
        if (APIb[requestID]) {
            quickToast("This Action was flagged as broken");
        } else if (APIl[requestID] && (login == null || login.isEmpty())) {
            quickToast("You need to sign in");
        } else {
            String completeAppendix = APIr[requestID];
            for (int i = 0; i < xdata.length; i++) {
                completeAppendix.replaceAll("${" + i + "}", String.valueOf(xdata[i]));
            }
            if (APIl[requestID]) completeAppendix = completeAppendix + login;
            super.execute(baseURL + completeAppendix);
        }
    }

    @Override protected void onPostExecute(XMLNode result) {
        if (result.getType().equals("response")) {
            if (!Boolean.parseBoolean(result.getFirstChildText("success"))) {
                quickToast(result.getFirstChildText("reason"));
            } else {
                quickToast("Action Successfull!");
            }
        } else if (result.getType().equals("error")) {
            quickToast(result.getAttribute("type"));
        } else {
            quickToast(APIsr[requestID]);
        }
    }

    abstract public void onSuccess();

    public void quickToast(CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
