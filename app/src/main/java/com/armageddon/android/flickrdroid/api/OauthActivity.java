package com.armageddon.android.flickrdroid.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.oauth.HttpMethod;
import com.armageddon.android.flickrdroid.api.oauth.OAuthConfig;
import com.armageddon.android.flickrdroid.api.oauth.OAuthConfigBuilder;
import com.armageddon.android.flickrdroid.api.oauth.OAuthSignature;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.common.LogoIcon;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.model.Person;
import com.armageddon.android.flickrdroid.ui.activities.SearchActivity;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Authorizes the user at www.Flickr.com
 * OAuth 1.0
 */

public class OauthActivity extends AppCompatActivity {

    RequestResponse<Person> mResponse;
    public static final String CONSUMER_KEY = "c4b195350e32a26fbfabd8d4075ad20f";
    public static final String CONSUMER_SECRET = "10b115c6849811e6";
    public static final String OAUTH_SIGNATURE_METHOD = "HMAC-SHA1";
    public static final String OAUTH_VERSION = "1.0";
    private static final String OAUTH_CALLBACK = "appforflickr://callback";
    private static final String OAUTH_REQUEST_TOKEN_URL = "https://www.flickr.com/services/oauth/request_token?";
    private static final String OAUTH_ACCESS_TOKEN_URL = "https://www.flickr.com/services/oauth/access_token?";
    private static final String OAUTH_TOKEN_URL = "https://www.flickr.com/services/oauth/authorize?oauth_token=";

    public static String oauthToken;
    public static String oauthTokenSecret;
    public static String oauth_verifier;
    public static String userId;


    public static Intent newIntent (Context context) {
        Intent intent = new  Intent(context, OauthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    /**
     * If there is no data, then a request is sent to Flickr.com,
     * otherwise the received key from Flickr.com is processed.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.oauth_layout);


        if (getIntent().getData()!= null) {
            TextView textView = findViewById(R.id.redirect_message);
            textView.setText(getText(R.string.redirect_to_app_message));
            String str = getIntent().getData().toString();
            String[] array = str.split("=");
            oauth_verifier = array[2];
            QueryPreferences.setOauthVerifier(this,oauth_verifier);
            Log.i("Verifier", oauth_verifier);
            new Token().execute();
        } else {
            new Oauth().execute();
        }

    }

    /**
     * Step 1.
     * Send request to Flick.com and get RequestToken
     * "oauthToken", "oauthTokenSecret", "oauth_verifier".
     */

    class Oauth extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(getRequestToken())
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null || !s.contains("oauth_callback_confirmed=true")) {
                setErrorUi();
            } else {
                String[] responseArray = s.split("=");
                String[] token_array = responseArray[2].split("&");
                oauthToken = token_array[0];
                oauthTokenSecret = responseArray[3];

                Log.i("oauthToken", oauthToken);
                Log.i("oauthTokenSecret", oauthTokenSecret);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(OAUTH_TOKEN_URL + oauthToken));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }
    }

    /**
     * Generates signed URL for Step 1.
     */

    private String getRequestToken ()  {
        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                .build();

        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, OAUTH_REQUEST_TOKEN_URL)
                .addQueryParam("oauth_callback", OAUTH_CALLBACK)
                .create();

        String s = Uri.parse(OAUTH_REQUEST_TOKEN_URL)
                .buildUpon()
                .appendQueryParameter("oauth_callback", OAUTH_CALLBACK)
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                .appendQueryParameter("oauth_nonce", signature.getNonce())
                .appendQueryParameter("oauth_version", OAUTH_VERSION)
                .appendQueryParameter("oauth_signature", signature.getSignature())
                .build().toString();
        Log.i("signature", signature.getSignature());
        return s;
    }

    /**
     * Step 2.
     * Send another request to Flick.com and get AccessToken
     * "oauthToken", "oauthTokenSecret", "user_id".
     */

    class Token extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(getAccessToken())
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null || response.contains("oauth_problem")) {
                setErrorUi();
            } else {
                Log.i("AccessToken", response);
                String [] responseArray = response.split("=");
                String [] token_array = responseArray[2].split("&");
                oauthToken = token_array[0];

                String [] token_secret_array2 = responseArray[3].split("&");
                oauthTokenSecret = token_secret_array2[0];

                String [] token_user_id_array = responseArray[4].split("&");
                userId = token_user_id_array[0];
                userId = userId.replace("%40","@");

                QueryPreferences.setUserId(OauthActivity.this, userId);
                QueryPreferences.setOauthToken(OauthActivity.this, oauthToken);
                QueryPreferences.setOauthTokenSecret(OauthActivity.this, oauthTokenSecret);
                new FetchUsersData().execute();
            }
        }
    }


    /**
     * Generates signed URL for Step 2.
     */

    private String getAccessToken ()  {
        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                .setTokenKeys(oauthToken, oauthTokenSecret)
                .build();

        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, OAUTH_ACCESS_TOKEN_URL)
                .addQueryParam("oauth_verifier",oauth_verifier)
                .create();

        String s = Uri.parse(OAUTH_ACCESS_TOKEN_URL)
                .buildUpon()
                .appendQueryParameter("oauth_verifier", oauth_verifier)
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_token", oauthToken)
                .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                .appendQueryParameter("oauth_nonce", signature.getNonce())
                .appendQueryParameter("oauth_version", OAUTH_VERSION)
                .appendQueryParameter("oauth_signature", signature.getSignature())
                .build().toString();

        Log.i("AccessTokenResponce", s);
        return s;
    }

    /**
     * Step 3.
     * Parse response to get user_name, user_logo and back to SearchActivity with opened SlideBar
     */

    private class FetchUsersData extends AsyncTask<String, Void, RequestResponse<Person>> {

        @Override
        protected RequestResponse<Person> doInBackground(String... strings) {
            return new FlickrFetchr().getPersonInfo(userId);
        }

        @Override
        protected void onPostExecute(RequestResponse<Person> response) {
            mResponse = response;
            if (mResponse.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && mResponse.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                Person person = mResponse.getItems().get(0);
                String name = person.getRealname();
                if (name == null) {
                    name = person.getUsername();
                }
                QueryPreferences.setUserName(OauthActivity.this, name);
                QueryPreferences.setUserIconUrl(OauthActivity.this,
                        person.getIconUrl(LogoIcon.huge_300px));

                Intent intent = SearchActivity.newIntent(OauthActivity.this,
                        null, SearchActivity.SHOW_MODE, true);
                startActivity(intent);
                finish();
            } else {
                setErrorUi();
            }
        }
    }

    /**
     * Step 3.
     * Shows an error window if something went wrong
     */

    private void setErrorUi() {
        TextView textView = findViewById(R.id.text_title_1);
        TextView textView1 = findViewById(R.id.text_title_2);
        textView.setVisibility(View.GONE);
        textView1.setVisibility(View.GONE);
        TextView message = findViewById(R.id.redirect_message);
        message.setText(getString(R.string.internet_connection_error));
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        Button button = findViewById(R.id.back_button);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> finish());
    }
}