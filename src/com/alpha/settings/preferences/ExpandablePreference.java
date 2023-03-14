/*
 * Copyright (C) 2016-2020 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.preferences;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

public class ExpandablePreference extends Preference {

    private boolean mExpanded;
    private String mCollapsedSummary;
    private String mExpandedSummary;

    public ExpandablePreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes, String collapsedSummary, String expandedSummary) {
        this(context, attrs, defStyleAttr, defStyleRes);
        mCollapsedSummary = collapsedSummary;
        mExpandedSummary = expandedSummary;
    }

    public ExpandablePreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ExpandablePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExpandablePreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                androidx.preference.R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
    }

    public ExpandablePreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        View mChevronView = (ImageView) holder.findViewById(R.id.dot_chevron);
        View mPreferenceCardView = (LinearLayout) holder.findViewById(R.id.layout_card_preference);

        if (mPreferenceCardView != null) {
            mPreferenceCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mExpanded) {
                        collapse(mChevronView);
                    }
                    else {
                        expand(mChevronView);
                    }
                    mExpanded = !mExpanded;
                    notifyChanged();
                }
            });
        }
    }

    private void expand(View chevron) {
        if (chevron != null) {
             chevron.setScaleX(-1); // mirror chevron icon
        }
        if (mExpandedSummary != null) {
            setSummary(mExpandedSummary);
        }
    }

    private void collapse(View chevron) {
        if (chevron != null) {
            chevron.setScaleX(1);
        }
        if (mCollapsedSummary != null) {
            setSummary(mCollapsedSummary);
        }
    }

    protected String getCollapsedSummary() {
        return mCollapsedSummary;
    }

    protected String getExpandedSummary() {
        return mExpandedSummary;
    }

    protected void setCollapsedSummary(String summary) {
        mCollapsedSummary = summary;
    }

    protected void setExpandedSummary(String summary) {
        mExpandedSummary = summary;
    }
}
