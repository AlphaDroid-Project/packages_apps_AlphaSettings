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
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

public class ResetCategoryPreference extends PreferenceCategory {

    private View.OnClickListener mOnResetClickListener;

    public ResetCategoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_category_reset);
    }

    public void setOnResetClickListener(View.OnClickListener listener) {
        mOnResetClickListener = listener;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        View resetButton = holder.findViewById(R.id.reset_button);
        if (resetButton != null) {
            resetButton.setOnClickListener(mOnResetClickListener);
            resetButton.setVisibility(mOnResetClickListener != null ? View.VISIBLE : View.GONE);
        }
    }
}
