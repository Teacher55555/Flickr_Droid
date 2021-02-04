package com.armageddon.android.flickrdroid.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import com.armageddon.android.flickrdroid.common.QueryPreferences;


/**
 * Launcher (Main) Activity.
 * Only for the first time launch logo animation (LogoActivity.class) will be started.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!QueryPreferences.isIntroShown(this)) {
            QueryPreferences.setIntroShown(this, true);
            startActivity(LogoActivity.newIntent(this, true));
        } else {
            startActivity(SearchActivity.newIntent(this,
                    null,
                    SearchActivity.SHOW_MODE,
                    false));
        }
        finish();
    }

}




