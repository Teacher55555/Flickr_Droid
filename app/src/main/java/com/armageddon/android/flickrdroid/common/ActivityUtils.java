package com.armageddon.android.flickrdroid.common;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.Window;
import android.view.WindowManager;
import com.armageddon.android.flickrdroid.R;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Controls activity theme and status bar.
 */

public class ActivityUtils {
    private static int sTheme;
    public final static int THEME_DEFAULT = 0;
    public final static int THEME_WHITE = 1;
    public final static int THEME_DARK = 2;

    public static void hideStatusBar (AppCompatActivity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

    public static int getTheme() {
        return sTheme;
    }



    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity) {
        sTheme = QueryPreferences.getAppTheme(activity);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
                    && sTheme == THEME_DEFAULT) {
                int nightModeFlags =
                        activity.getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK;
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    sTheme = THEME_DARK;
                } else {
                    sTheme = THEME_WHITE;
                }
            }
        onActivityCreateSetTheme(activity, sTheme);
    }

    /** Manually set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme (Activity activity, int theme)
    {
        switch (theme)
        {
            default:
            case THEME_WHITE:
                activity.setTheme(R.style.AppTheme);
                break;
            case THEME_DARK:
                activity.setTheme(R.style.DarkMyTheme);
                break;
        }
    }
}
