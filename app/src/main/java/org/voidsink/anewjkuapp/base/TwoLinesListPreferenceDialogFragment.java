/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import org.voidsink.anewjkuapp.R;

public class TwoLinesListPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    private int mClickedDialogEntryIndex;

    protected int getClickedDialogEntryIndex() {
        return mClickedDialogEntryIndex;
    }

    public static TwoLinesListPreferenceDialogFragment newInstance(String key) {
        TwoLinesListPreferenceDialogFragment fragment = new TwoLinesListPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    private TwoLinesListPreference getListPreference() {
        return (TwoLinesListPreference) this.getPreference();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        TwoLinesListPreference preference = this.getListPreference();
        if (preference.getEntries() != null && preference.getEntryValues() != null) {
            ListAdapter adapter = new ArrayAdapter<TwoLinesListPreferenceEntry>(
                    getContext(), R.layout.custom_simple_list_item_2_single_choice, preference.getPreferenceEntries()) {

                ViewHolder viewHolder;

                @NonNull
                @SuppressLint("InflateParams")
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    if (convertView == null) {
                        convertView = inflater.inflate(R.layout.custom_simple_list_item_2_single_choice, null);

                        viewHolder = new ViewHolder(convertView);
                        convertView.setTag(viewHolder);
                    } else {
                        // view already defined, retrieve view holder
                        viewHolder = (ViewHolder) convertView.getTag();
                    }

                    viewHolder.getTitle().setText(getItem(position).getTitle());
                    viewHolder.getSubTitle().setText(getItem(position).getSubTitle());
                    if (viewHolder.getRadio() != null) {
                        viewHolder.getRadio().setChecked(position == getClickedDialogEntryIndex());
                    }

                    return convertView;
                }
            };


            this.mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
            builder.setSingleChoiceItems(adapter, this.mClickedDialogEntryIndex, (dialog, which) -> {
                TwoLinesListPreferenceDialogFragment.this.mClickedDialogEntryIndex = which;
                TwoLinesListPreferenceDialogFragment.this.onClick(dialog, -1);
                dialog.dismiss();
            });
            builder.setPositiveButton(null, null);
        } else {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        TwoLinesListPreference preference = this.getListPreference();
        if (positiveResult && this.mClickedDialogEntryIndex >= 0 && preference.getEntryValues() != null) {
            String value = preference.getEntryValues()[this.mClickedDialogEntryIndex].toString();
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }

    }

    private static class ViewHolder {
        private final TextView title;
        private final TextView subTitle;
        private final RadioButton radio;

        ViewHolder(View convertView) {
            this.title = convertView.findViewById(android.R.id.text1);
            this.subTitle = convertView.findViewById(android.R.id.text2);
            this.radio = convertView.findViewById(R.id.radio);
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getSubTitle() {
            return subTitle;
        }

        public RadioButton getRadio() {
            return radio;
        }
    }
}

