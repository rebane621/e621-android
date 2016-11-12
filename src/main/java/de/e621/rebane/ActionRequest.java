package de.e621.rebane;

import android.content.Context;
import android.widget.Toast;

import com.itwookie.XMLreader.XMLNode;
import com.itwookie.XMLreader.XMLTask;

import java.util.Map;
import java.util.logging.Logger;

public abstract class ActionRequest extends XMLTask {

    private static final String[] APIr = {
            "post/vote.xml?",
            "post/vote.xml?",
            "favorite/create.xml?",
            "favorite/destroy.xml?",
            "dmail/create.xml?"
    };
    private static final boolean[] APIb = {  //this array flags requests as broken
            false,
            false,
            false,
            false,
            false
    };
    private static final boolean[] APIl = {  //this array flags requests to require login
            true,
            true,
            true,
            true,
            true
    };
    public static final int POST_VOTE_UP = 0;
    public static final int POST_VOTE_DOWN = 1;
    public static final int POST_FAVOURITE = 2;
    public static final int POST_UNFAVOURITE = 3;
    public static final int DMAIL_CREATE = 4;

    private static String login;
    private int requestID;
    private Context context;
    /** leave loginAppendix null if no login is required */
    public ActionRequest(Context context, String loginAppendix, int RequestType, Map<String, String> data) {
        //super(context);
        this.context = context;
        login = loginAppendix;
        requestID = RequestType;
        setPostData(data);
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
            if (APIl[requestID]) completeAppendix = completeAppendix + login;

            super.execute(baseURL + completeAppendix);
        }
    }

    @Override protected void onPostExecute(XMLNode result) {
        if (result == null) {
            quickToast("Connection error");
            Logger.getLogger("a621").info("Action lost (No response)");
            return;
        }
        Logger.getLogger("a621").info(result.toString());
        if (result.getType().equals("response")) {
            if (!Boolean.parseBoolean(result.getFirstChildContent("success").orElse("false"))) {
                quickToast(result.getFirstChildContent("reason").orElse("UNKNOWN ERROR"));
            } else {
                String msg = result.getFirstChildContent("reason").orElse("UNKNOWN ERROR");
                quickToast(msg != null ? msg : "Action completed");
                onSuccess(result);
            }
        } else if (result.getType().equals("error")) {
            quickToast(result.getAttribute("type").orElse("UNKNOWN TYPE"));
        } else {
            //quickToast(APIsr[requestID]);
            onSuccess(result);
        }
    }

    abstract public void onSuccess(XMLNode result);

    public void quickToast(CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
