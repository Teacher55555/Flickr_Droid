package com.armageddon.android.flickrdroid.common;

/**
 * Helps to create user logo in different sizes
 */

public interface LogoIcon {
   String mini_48px = "_d";    // 48 x 48
   String small_60px = "_s";    // 60 x 60
   String normal_100px = "_m";    // 100 x 100
   String big_150px = "_l";    // 150 x 150
   String huge_300px = "_r";    // 300 x 300

//188864276@N07
    // Request Example
    //https://farm66.staticflickr.com/65535/buddyicons/188349986@N07_m.jpg

    default String getUrl (String iconFarm, String iconServer, String nsid, String size) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://farm");
        sb.append(iconFarm);
        sb.append(".staticflickr.com/");
        sb.append(iconServer);
        sb.append("/buddyicons/");
        sb.append(nsid);
        sb.append(size);
        sb.append(".jpg");
        return sb.toString();
    }


}
