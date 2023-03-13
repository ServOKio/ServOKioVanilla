package net.servokio.vanilla.preferences;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import android.widget.TextView;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import java.util.Objects;

public class CustomFontListPreference extends ListPreference {

    public CustomFontListPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if(!TextUtils.isEmpty(getValue()) && !Objects.equals(getValue(), "")){
            final TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
            summaryView.setTypeface(Typeface.createFromFile(getValue()));
        }
    }
}
