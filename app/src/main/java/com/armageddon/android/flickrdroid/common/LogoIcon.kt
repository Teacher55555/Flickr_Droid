package com.armageddon.android.flickrdroid.common

import java.lang.StringBuilder

//const val ICON_MINI_48PX = "_d" // 48 x 48
//const val ICON_SMALL_60PX = "_s" // 60 x 60
//const val ICON_NORMAL_100PX = "_m" // 100 x 100
//const val ICON_BIG_150PX = "_l" // 150 x 150
//const val ICON_HUGE_300PX = "_r" // 300 x 300

//const val PHOTO_URL_Z_640 = "_z.jpg" //640 * 480
//const val PHOTO_URL_L_1024= "_b.jpg" //1024 * 768
//const val PHOTO_GROUP= "_l.jpg" //1024 * 768

//188864276@N07
// Request Example
//https://farm66.staticflickr.com/65535/buddyicons/188349986@N07_m.jpg
//https://live.staticflickr.com/{server-id}/{id}_{secret}.jpg
//https://live.staticflickr.com/65535/51832157745_7e47f12de8_b.jpg

interface LogoIcon {

    enum class Photo (
        val size: String,
        val prefix: String,
        val flickrLabel: String
    ) {
        UNKNOWN("?","_f.jpg","unknown"),
        SQUARE_75("75","_s.jpg","Square"),
        SQUARE_150("150","_q.jpg","Large Square"),
        Thumbnail("100","_t.jpg","Thumbnail"),
        SMALL_240("240","_m.jpg","Small"),
        SMALL_320("320","_n.jpg","Small 320"),
        SMALL_400("400","_w.jpg","Small 400"),
        MEDIUM_500("500",".jpg","Medium"),
        MEDIUM_640("640","_z.jpg","Medium 640"),
        MEDIUM_800("800","_c.jpg","Medium 800"),
        LARGE_1024("1024","_b.jpg","Large"),
        LARGE_1600("1600","_h.jpg","Large 1600"),
        LARGE_2048("2048","_k.jpg","Large 2048"),
        X_LARGE_3K("3072","_3k.jpg","X-Large 3K"),
        X_LARGE_4K("4096","_4k.jpg","X-Large 4K"),
        X_LARGE_5K("5120","_5k.jpg","X-Large 5K"),
        X_LARGE_6K("6144","_6k.jpg","X-Large 6K"),
        ORIGINAL("","_o.jpg","Original"),
        MAX("","max","Max");

        companion object {
            fun getLabel (label: String) = values().first { it.flickrLabel == label }
        }
    }

    enum class Icon (val prefix: String) {
        ICON_MINI_48PX("_d.jpg"), // 48 x 48
        ICON_SMALL_60PX("_s.jpg"), // 60 x 60
        ICON_NORMAL_100PX("_m.jpg"), // 100 x 100
        ICON_BIG_150PX("_l.jpg"), // 150 x 150
        ICON_HUGE_300PX("_r.jpg") // 300 x 300
    }

    fun getIconUrl(iconFarm: String, iconServer: String, nsid: String, size: String): String {
        val sb = StringBuilder()
        sb.append("https://farm")
        sb.append(iconFarm)
        sb.append(".staticflickr.com/")
        sb.append(iconServer)
        sb.append("/buddyicons/")
        sb.append(nsid)
        sb.append(size)
        return sb.toString()
    }

    fun getPhotoUrl(serverId: String, photoId: String, photoSecret: String, size: String): String {
        return "https://live.staticflickr.com/$serverId/${photoId}_${photoSecret}${size}"
    }
}