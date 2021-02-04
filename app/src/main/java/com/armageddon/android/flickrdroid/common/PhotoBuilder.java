package com.armageddon.android.flickrdroid.common;

/**
 * Helps to create photo in different sizes
 */

public interface PhotoBuilder {

    String s_75x75 = "_s";
    String q_150x150 = "_q";
    String t_100 = "_t";
    String m_240 = "_m";
    String n_320 = "_n";
    String mDefault = "";
    String z_640 = "_z";
    String c_800 = "_c";
    String b_1024 = "_l";
    String h_1600 = "_h";
    String k_2048 = "_k";
    String x_large_3k_3072 = "_k";
    String x_large_3k_4096 = "_k";
    String x_large_3k_5120 = "_k";

    String o_original ="_o";



    // Request Example
    // https://farm66.staticflickr.com/65535/50231907996_c8ee111098_z.jpg

    // Request Example 2
    // https://live.staticflickr.com/65535/50231907996_68b9429f6e_k.jpg",

    default String getUrl (String farm, String server, String photo_id, String secret, String size) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://farm");
        sb.append(farm);
        sb.append(".staticflickr.com/");
        sb.append(server);
        sb.append("/");
        sb.append(photo_id);
        sb.append("_");
        sb.append(secret);
        sb.append(size);
        sb.append(".jpg");
        return sb.toString();
    }

//    default String getUrl (String farm, String server, String photo_id, String secret, String size) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("https://live.staticflickr.com/");
//        sb.append(farm);
//        sb.append("/");
//        sb.append(photo_id);
//        sb.append("_");
//        sb.append(secret);
//        sb.append(size);
//        sb.append(".jpg");
//        return sb.toString();
//    }

}
