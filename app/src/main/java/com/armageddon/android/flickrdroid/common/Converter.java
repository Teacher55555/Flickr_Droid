package com.armageddon.android.flickrdroid.common;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Converts pixels to equivalent dp unit, depending on device density.
 */

public interface Converter {
    default ViewGroup.LayoutParams pxToDp (View view, float density, int columns) {
        int pixels;
        switch (columns) {
            case 1 : pixels = (int) (350 * density + 0.5f); break;
            case 2 : pixels = (int) (190 * density + 0.5f); break;
            default: pixels = (int) (120 * density + 0.5f);
        }

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = pixels;
        return layoutParams;
    }

    default int dpToPx (float scale, int sizeInDp) {
        return (int) (sizeInDp * scale + 0.5f);
    }

    default int getAttrColor(Context context, int color) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(color, typedValue, true);
        return typedValue.data;
    }

}
