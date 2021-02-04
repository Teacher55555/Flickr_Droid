package com.armageddon.android.flickrdroid.common;

import com.armageddon.android.flickrdroid.R;

/**
 * Category filters with photo.
 */

public enum EnumCategory {
    Sunset (R.drawable.sunset),
    Beach (R.drawable.beach),
    Water (R.drawable.water),
    Sky (R.drawable.sky),
    Flower (R.drawable.flower),
    Nature (R.drawable.nature),
    Blue (R.drawable.blue),
    Night (R.drawable.night),
    White (R.drawable.white),
    Tree (R.drawable.tree),
    Green (R.drawable.green),
    Flowers (R.drawable.flowers),
    Portrait (R.drawable.portrait),
    Art (R.drawable.art),
    Light (R.drawable.light),
    Snow (R.drawable.snow),
    Dog (R.drawable.dog),
    Sun (R.drawable.sun),
    Clouds (R.drawable.clouds),
    Cat (R.drawable.cat),
    Park (R.drawable.park),
    Winter (R.drawable.winter),
    Landscape (R.drawable.landscape),
    Street (R.drawable.street),
    Summer (R.drawable.summer),
    Sea (R.drawable.sea),
    City (R.drawable.city),
    Trees (R.drawable.trees),
    Yellow (R.drawable.yellow),
    Lake (R.drawable.lake),
    Christmas (R.drawable.christmas),
    People (R.drawable.people),
    Bridge (R.drawable.bridge),
    Family (R.drawable.family),
    Bird (R.drawable.bird),
    River (R.drawable.river),
    Pink (R.drawable.pink),
    House (R.drawable.house),
    Car (R.drawable.car),
    Food (R.drawable.food),
    Bw (R.drawable.bw),
    Old (R.drawable.old),
    Macro (R.drawable.macro),
    Music (R.drawable.music),
    New (R.drawable.newa),
    Moon (R.drawable.moon),
    Orange (R.drawable.orange),
    Gardens (R.drawable.gardens),
    BlackWhite (R.drawable.blackwhite);

    public final int drawableId;

    EnumCategory(int drawableId) {
        this.drawableId = drawableId;
    }

    public int getDrawble() {
        return drawableId;
    }
}

