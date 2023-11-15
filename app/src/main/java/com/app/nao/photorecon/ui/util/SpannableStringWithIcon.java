package com.app.nao.photorecon.ui.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

public class SpannableStringWithIcon {
    static public CharSequence getSpannableStringWithIcon(Context context, String text,@DrawableRes int drawableID) {
        SpannableString spannableString = new SpannableString("  " + text); // スペースを追加してアイコンとテキストを区切る
        // アイコンを挿入
        Drawable icon = ContextCompat.getDrawable(context, drawableID);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        ImageSpan imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_BASELINE);
        spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }
}
