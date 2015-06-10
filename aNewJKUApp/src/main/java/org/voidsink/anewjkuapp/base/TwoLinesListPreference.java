/*******************************************************************************
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/

package org.voidsink.anewjkuapp.base;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import org.voidsink.anewjkuapp.R;

public class TwoLinesListPreference extends ListPreference {

    private CharSequence[] mEntriesSubtitles;

    private int mClickedDialogEntryIndex;

    public TwoLinesListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);

        if (mEntriesSubtitles == null) {
            throw new IllegalStateException(
                    "ListPreference requires an subtitle array.");
        }

        mEntriesSubtitles = getEntriesSubtitles();
        mClickedDialogEntryIndex = getValueIndex();


        String[] mEntriesString = (String[]) getEntries();

        // adapter
        ListAdapter adapter = new ArrayAdapter<String>(
                getContext(), R.layout.custom_simple_list_item_2_single_choice, mEntriesString) {

            ViewHolder holder;

            class ViewHolder {
                TextView title;
                TextView subTitle;
                public RadioButton radio;
            }

            @SuppressLint("InflateParams")
            public View getView(int position, View convertView, ViewGroup parent) {
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.custom_simple_list_item_2_single_choice, null);

                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(android.R.id.text1);
                    holder.subTitle = (TextView) convertView.findViewById(android.R.id.text2);
                    holder.radio = (RadioButton) convertView.findViewById(R.id.radio);

                    convertView.setTag(holder);
                } else {
                    // view already defined, retrieve view holder
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.title.setText(getEntries()[position]);
                holder.subTitle.setText(mEntriesSubtitles[position]);
                if (holder.radio != null) {
                    holder.radio.setChecked(position == mClickedDialogEntryIndex);
                }

                return convertView;
            }
        };

        builder.setSingleChoiceItems(adapter, mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClickedDialogEntryIndex = which;
                /*
                 * Clicking on an item simulates the positive button click, and
				 * dismisses the dialog.
				 */
                TwoLinesListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            }
        });

        /*
         * The typical interaction for list-based dialogs is to have
         * click-on-an-item dismiss the dialog instead of the user having to
         * press 'Ok'.
         */
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        CharSequence[] mEntryValues = getEntryValues();

        if (positiveResult && mClickedDialogEntryIndex >= 0 && mEntryValues != null) {
            String value = mEntryValues[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    private int getValueIndex() {
        return findIndexOfValue(getValue());
    }

    public CharSequence[] getEntriesSubtitles() {
        return mEntriesSubtitles;
    }

    public void setEntriesSubtitles(CharSequence[] mEntriesSubtitles) {
        this.mEntriesSubtitles = mEntriesSubtitles;
    }
}
