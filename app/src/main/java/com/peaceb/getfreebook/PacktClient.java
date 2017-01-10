package com.peaceb.getfreebook;


import android.util.Log;
import android.util.Pair;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

class PacktClient {
    private static final String TAG = "PacktClient";

    private final int TIMEOUT = 30 * 1000;

    public static final String FREE_BOOK_URL = "https://www.packtpub.com/packt/offers/free-learning";
    public static final String LOGIN_URL = "https://www.packtpub.com";
    public static final String MAIN_URL = "https://www.packtpub.com";
    public static final String MY_EBOOK_URL = "https://www.packtpub.com/account/my-ebooks";

    private final String COOKIE_KEY_SESSION = "SESS_live";

    private Map<String, String> _cookies = null;

    private static final String LOG_FILE_NAME = "packt.log";
    private String _logFilePath = null;

    public PacktClient(String logDir) {
        _logFilePath = logDir + "/" + LOG_FILE_NAME;
    }


    private boolean isLogin() {
        if (_cookies == null || !_cookies.containsKey(COOKIE_KEY_SESSION)) {
            return false;
        }

        return true;
    }

    private boolean setSessionCookie(Connection con) {
        if (_cookies != null && _cookies.containsKey(COOKIE_KEY_SESSION)) {
            con.cookie(COOKIE_KEY_SESSION, _cookies.get(COOKIE_KEY_SESSION));
            return true;
        }

        return false;
    }

    public boolean login(String email, String pass) {
        if (email.trim().length() == 0 || pass.trim().length() == 0) {
            return false;
        }

        boolean loginOk = false;

        try {
            Connection con = Jsoup.connect(LOGIN_URL);
            con.timeout(TIMEOUT);
            con.data("email", email);
            con.data("password", pass);
            con.data("op", "Login");
            con.data("form_id", "packt_user_login_form");

            Document doc = con.post();
            if (con.response().statusCode() >= 400) {
                return false;
            }

            if (doc.html().contains("\"uid\"")) {
                if (!doc.html().contains("\"uid\":0")) {
                    loginOk = true;
                }
            }

            if (loginOk) {
                // store cookies for session data
                _cookies = con.response().cookies();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return loginOk;
    }

    public Pair<String, String> checkNewBook() {
        String title = "";
        String claimUrl = "";

        try {
            Connection con = Jsoup.connect(FREE_BOOK_URL);
            con.timeout(TIMEOUT);
            setSessionCookie(con);

            Document doc = con.get();
            if (con.response().statusCode() >= 400) {
                return null;
            }

            // extract book title
            Elements els = doc.select("div.dotd-title");
            if (els.size() > 0) {
                title = els.get(0).text().trim();
                els.clear();
            }
            else {
                return null;
            }

            // extract book claim url
            els = doc.select("a[href^=/freelearning-claim/]");
            if (els.size() > 0) {
                claimUrl = MAIN_URL + els.get(0).attr("href");
                els.clear();
            }
            else {
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new Pair(title, claimUrl);
    }

    public boolean claimNewBook(String url) {
        boolean ret = false;

        try {
            Connection con = Jsoup.connect(url);
            con.timeout(TIMEOUT);

            if (!setSessionCookie(con)) {
                return false;
            }

            con.get();
            if (con.response().statusCode() >= 400) {
                return false;
            }

            ret = true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return ret;
    }

    public boolean checkMyBook(String title) {
        if (!isLogin()) {
            return false;
        }

        try {
            Connection con = Jsoup.connect(MY_EBOOK_URL);
            con.timeout(TIMEOUT);
            if (!setSessionCookie(con)) {
                return false;
            }

            Document doc = con.get();
            if (con.response().statusCode() >= 400) {
                return false;
            }

            Elements els = doc.select("div.title");
            for (Element el : els) {
                if (el.text().replace("[eBook]", "").trim().equals(title)) {
                    Log.i(TAG, "title found:" + title);
                    return true;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }
}

