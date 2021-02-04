package com.armageddon.android.flickrdroid.model;

import com.armageddon.android.flickrdroid.common.LogoIcon;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Element of List of groups when do "search groups".
 */

public class GroupSearchItem implements LogoIcon {
    public static final int PRIVACY_PRIVATE = 0;
    public static final int PRIVACY_INVITE = 2;
    public static final int PRIVACY_PUBLIC = 3;
    public static final int PRIVACY_NOT_INITIALIZED = 5;

    private NumberFormat format = DecimalFormat.getInstance(Locale.US);

    private String nsid;
    private String name;
    private int eighteenplus;
    private String iconserver;
    private String iconfarm;
    private String members;
    private String pool_count;
    private String topic_count;
    private int is_member;

    private int privacy = PRIVACY_NOT_INITIALIZED;

    public String getNsid() {
        return nsid;
    }

    public String getName() { return name; }

    public boolean isMember () {
        return is_member != 0;
    }

    public int getEighteenplus() {
        return eighteenplus;
    }

    public String getIconserver() {
        return iconserver;
    }

    public String getIconfarm() {
        return iconfarm;
    }

    public String getMembers() {
       return format.format(Integer.parseInt(members));
    }

    public String getPool_count() {
        return format.format(Integer.parseInt(pool_count));
    }

    public String getTopic_count() {
        if (topic_count == null) {
            return null;
        }
        return format.format(Integer.parseInt(topic_count));
    }

    public int getPrivacy() {
        return privacy;
    }

    public String getIcon () {
        if (Integer.parseInt(iconserver) == 0) {
            return null;
        }
       return getUrl(iconfarm, iconserver, nsid, LogoIcon.big_150px);
   }


}
