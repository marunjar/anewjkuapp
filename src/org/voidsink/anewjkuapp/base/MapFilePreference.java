package org.voidsink.anewjkuapp.base;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.R;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Environment;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

public class MapFilePreference extends ListPreference {

	public MapFilePreference(Context ctxt) {
		this(ctxt, null);
	}

	public MapFilePreference(Context ctxt, AttributeSet attrs) {
		super(ctxt, attrs);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		List<String> entries = new ArrayList<String>();
		List<String> entryValues = new ArrayList<String>();

		CollectMapFiles(entries, entryValues);

		setEntries(entries.toArray(new CharSequence[] {}));
		setEntryValues(entryValues.toArray(new CharSequence[] {}));

		int index = Math
				.max(findIndexOfValue(getSharedPreferences().getString(
						getKey(), "")), 0);

		setValueIndex(index);

		super.onPrepareDialogBuilder(builder);
	}

	private void CollectMapFiles(List<String> entries, List<String> entryValues) {
		entries.add("no .map file");
		entryValues.add("");

		ProgressDialog progressDialog = ProgressDialog.show(getContext(),
				getContext().getString(R.string.progress_title), getContext()
						.getString(R.string.progress_load_map_files), true);

		File root = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		IterateDir(root, entries, entryValues);

		progressDialog.dismiss();
	}

	private void IterateDir(File f, List<String> entries,
			List<String> entryValues) {

		File[] files = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						|| (pathname.isFile() && pathname.toString().endsWith(
								".map"));
			}
		});
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					IterateDir(file, entries, entryValues);
				} else {
					entries.add(file.getPath());
					entryValues.add(file.getPath());
				}
			}
		}
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			setSummary(getSummary());
		}
		super.onDialogClosed(positiveResult);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getString(index));
	}

	@Override
	public CharSequence getSummary() {
		return getValue();
	}
}