package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.common.AppPreferences.setIsProUser
import com.armageddon.android.flickrdroid.common.AppPreferences.setOauthToken
import com.armageddon.android.flickrdroid.common.AppPreferences.setOauthTokenSecret
import com.armageddon.android.flickrdroid.common.AppPreferences.setOauthVerifier
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserIconFarm
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserIconServer
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserIconUrl
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserId
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserName
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserRealName
import com.armageddon.android.flickrdroid.databinding.ActivityOauthBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.network.api.FlickrFetchr
import com.armageddon.android.flickrdroid.network.api.KEY
import com.armageddon.android.flickrdroid.network.api.SECRET
import com.armageddon.android.flickrdroid.network.oauth.HttpMethod
import com.armageddon.android.flickrdroid.network.oauth.OAuthConfigBuilder
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_OK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InterruptedIOException
import java.net.UnknownHostException


private const val CONSUMER_KEY = KEY
private const val CONSUMER_SECRET = SECRET
private const val OAUTH_SIGNATURE_METHOD = "HMAC-SHA1"
private const val OAUTH_VERSION = "1.0"
private const val OAUTH_CALLBACK = "appforflickr://callback"
private const val OAUTH_REQUEST_TOKEN_URL = "https://www.flickr.com/services/oauth/request_token?"
private const val OAUTH_ACCESS_TOKEN_URL = "https://www.flickr.com/services/oauth/access_token?"
private const val OAUTH_TOKEN_URL = "https://www.flickr.com/services/oauth/authorize?oauth_token="

class OauthActivity : AppCompatActivity() {
    private lateinit var binding : ActivityOauthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityUtils.onActivityCreateSetTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityOauthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.data != null) {
            binding.redirectMessage.text = getText(R.string.redirect_to_app_message)
            val str = intent.data.toString()
            val array = str.split("=").toTypedArray()
            oauth_verifier = array[2]
            setOauthVerifier(this, oauth_verifier)
            Log.e("Verifier", oauth_verifier)
            lifecycleScope.launch(Dispatchers.IO) { token() }
        } else {
            lifecycleScope.launch(Dispatchers.IO) { oauth() }

        }
    }


    private fun oauth() {
        fun getRequestToken(): String {
            val oauthConfig = OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET).build()
            val signature = oauthConfig.buildSignature(HttpMethod.GET, OAUTH_REQUEST_TOKEN_URL)
                .addQueryParam("oauth_callback", OAUTH_CALLBACK)
                .create()
            val s = Uri.parse(OAUTH_REQUEST_TOKEN_URL)
                .buildUpon()
                .appendQueryParameter("oauth_callback", OAUTH_CALLBACK)
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                .appendQueryParameter("oauth_timestamp", signature.timestamp)
                .appendQueryParameter("oauth_nonce", signature.nonce)
                .appendQueryParameter("oauth_version", OAUTH_VERSION)
                .appendQueryParameter("oauth_signature", signature.signature)
                .build().toString()
            Log.e("signature", signature.signature)
            return s
        }

        val client = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .url(getRequestToken())
            .method("GET", null)
            .build()
        try {
            val otvet = client.newCall(request).execute()
            val response = otvet.body?.string()
            if (response == null || !response.contains("oauth_callback_confirmed=true")) {
                lifecycleScope.launch(Dispatchers.Main) { setErrorUi() }

            } else {
                val responseArray: Array<String> = response.split("=").toTypedArray()
                val token_array = responseArray[2].split("&").toTypedArray()
                oauthToken = token_array[0]
                oauthTokenSecret = responseArray[3]
                Log.e("oauthToken", oauthToken)
                Log.e("oauthTokenSecret", oauthTokenSecret)
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(OAUTH_TOKEN_URL + oauthToken)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun token() {
        fun getAccessToken(): String {
            val oauthConfig = OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(oauthToken, oauthTokenSecret)
                    .build()
            val signature = oauthConfig.buildSignature(HttpMethod.GET, OAUTH_ACCESS_TOKEN_URL)
                    .addQueryParam("oauth_verifier", oauth_verifier)
                    .create()
            val s = Uri.parse(OAUTH_ACCESS_TOKEN_URL)
                .buildUpon()
                .appendQueryParameter("oauth_verifier", oauth_verifier)
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_token", oauthToken)
                .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                .appendQueryParameter("oauth_timestamp", signature.timestamp)
                .appendQueryParameter("oauth_nonce", signature.nonce)
                .appendQueryParameter("oauth_version", OAUTH_VERSION)
                .appendQueryParameter("oauth_signature", signature.signature)
                .build().toString()
            Log.e("AccessTokenResponce", s)
            return s
        }

        val client = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .url(getAccessToken())
            .method("GET", null)
            .build()
        try {
            val a = "oauth_problem"
            val response = client.newCall(request).execute().body?.string()
            if (response == null || response.contains("oauth_problem")) {
                lifecycleScope.launch(Dispatchers.Main) { setErrorUi() }
            } else {
                Log.e("AccessToken", response)
                val responseArray = response.split("=").toTypedArray()
                val token_array = responseArray[2].split("&").toTypedArray()
                oauthToken = token_array[0]
                val token_secret_array2 = responseArray[3].split("&").toTypedArray()
                oauthTokenSecret = token_secret_array2[0]
                val token_user_id_array = responseArray[4].split("&").toTypedArray()
                userId = token_user_id_array[0].replace("%40", "@")
                setUserId(this@OauthActivity, userId)
                setOauthToken(this@OauthActivity, oauthToken)
                setOauthTokenSecret(this@OauthActivity, oauthTokenSecret)
                lifecycleScope.launch(Dispatchers.IO) { fetchUserData() }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchUserData() {
        val flickrFetchr = FlickrFetchr()
        val response : FlickrResponse<Person> = try {
            flickrFetchr.fetchPerson(userId)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }

        if (response.stat == RESPONSE_DATA_OK) {
            val person = response.data!!
            setUserName(this, person.userName)
            setUserRealName(this, person.realName)
            setUserIconServer(this, person.iconServer)
            setUserIconFarm(this, person.iconFarm)
            setIsProUser(this, person.proUser)
            setUserIconUrl(
                this@OauthActivity,
                person.getIconUrl(LogoIcon.Icon.ICON_HUGE_300PX.prefix)
            )
            startActivity(FollowingsActivity.newIntent(this, true))
        }



    }

    private fun setErrorUi() {
        binding.apply {
            textTitle1.visibility = View.GONE
            textTitle2.visibility = View.GONE
            progressBar.visibility = View.GONE
            redirectMessage.text = getString(R.string.internet_connection_error)
            backButton.apply {
                visibility = View.VISIBLE
                setOnClickListener { finish() }
            }
        }
    }

    companion object {
        fun newIntent(context: Context) : Intent {
            val intent = Intent(context, OauthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            return intent
        }
        private var oauthToken: String = ""
        private var oauthTokenSecret: String = ""
        private var oauth_verifier: String = ""
        private var userId: String = ""
    }
}