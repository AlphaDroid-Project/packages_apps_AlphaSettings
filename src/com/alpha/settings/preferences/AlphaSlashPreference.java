/*
 * Copyright (C) 2025 AlphaDroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alpha.settings.preferences;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

public class AlphaSlashPreference extends Preference {

    private int mSlashColor;
    private Drawable mSlashIcon;

    public AlphaSlashPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_alpha_slash);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlphaSlashPreference);
        mSlashColor = a.getColor(R.styleable.AlphaSlashPreference_slashColor, 0xFF1976D2);
        mSlashIcon = a.getDrawable(R.styleable.AlphaSlashPreference_slashIcon);
        a.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // 1. Strip the stock "Card" background enforced by Expressive theme
        holder.itemView.setBackground(null);
        holder.itemView.setBackgroundColor(0x00000000);

        // 2. Disable click on the row container, pass it to children
        // Note: We performClick() manually so Fragment navigation works
        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);

        // Configure Right Container (Icon + Color)
        View rightContainer = holder.findViewById(R.id.slash_right_container);
        if (rightContainer != null) {
            rightContainer.setBackgroundTintList(ColorStateList.valueOf(mSlashColor));
            rightContainer.setOnClickListener(v -> performClick());
        }

        // Configure Left Container (Text)
        View leftContainer = holder.findViewById(R.id.slash_left_container);
        if (leftContainer != null) {
            leftContainer.setOnClickListener(v -> performClick());
        }

        // Configure Icon
        ImageView iconView = (ImageView) holder.findViewById(R.id.slash_icon);
        if (iconView != null && mSlashIcon != null) {
            iconView.setImageDrawable(mSlashIcon);
        }
    }
}
