package org.voidsink.anewjkuapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;

import org.voidsink.anewjkuapp.fragment.MapFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;

import edu.emory.mathcs.backport.java.util.Collections;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

public class AppUtils {

	private static final String DEFAULT_POI_FILE_NAME = "JKU.gpx";
	private static final String TAG = AppUtils.class.getSimpleName();

	public static void doOnNewVersion(Context context) {
		int mLastVersion = PreferenceWrapper.getLastVersion(context);
		int mCurrentVersion = PreferenceWrapper.getCurrentVersion(context);

		if (mLastVersion != mCurrentVersion
				|| mLastVersion == PreferenceWrapper.PREF_LAST_VERSION_NONE) {
			boolean errorOccured = false;

			if (!initPreferences(context)) {
				errorOccured = true;
			}
			if (!importDefaultPois(context)) {
				errorOccured = true;
			}
			if (!copyDefaultMap(context)) {
				errorOccured = true;
			}
			if (!errorOccured) {
				PreferenceWrapper.setLastVersion(context, mCurrentVersion);
			}
		}
	}

	private static boolean initPreferences(Context context) {
		try {
			PreferenceManager.setDefaultValues(context, R.xml.preference_app,
					true);
			PreferenceManager.setDefaultValues(context,
					R.xml.preference_dashclock_extension_mensa, true);
			PreferenceManager.setDefaultValues(context, R.xml.preference_kusss,
					true);
		} catch (Exception e) {
			Log.e(TAG, "initPreferences", e);
			return false;
		}
		return true;
	}

	private static boolean copyDefaultMap(Context context) {
		try {
			// write file to sd for mapsforge
			OutputStream mapFileWriter = new BufferedOutputStream(
					context.openFileOutput(MapFragment.MAP_FILE_NAME,
							Context.MODE_PRIVATE));
			InputStream assetData = new BufferedInputStream(context.getAssets()
					.open(MapFragment.MAP_FILE_NAME));

			byte[] buffer = new byte[1024];
			int len = assetData.read(buffer);
			while (len != -1) {
				mapFileWriter.write(buffer, 0, len);
				len = assetData.read(buffer);
			}
			mapFileWriter.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "copyDefaultMap", e);
			return false;
		} catch (IOException e) {
			Log.e(TAG, "copyDefaultMap", e);
			return false;
		}
		return true;
	}

	private static boolean importDefaultPois(Context context) {
		// import JKU Pois
		try {
			// write file to sd for import
			OutputStream mapFileWriter = new BufferedOutputStream(
					context.openFileOutput(DEFAULT_POI_FILE_NAME,
							Context.MODE_PRIVATE));
			InputStream assetData = new BufferedInputStream(context.getAssets()
					.open(DEFAULT_POI_FILE_NAME));

			byte[] buffer = new byte[1024];
			int len = assetData.read(buffer);
			while (len != -1) {
				mapFileWriter.write(buffer, 0, len);
				len = assetData.read(buffer);
			}
			mapFileWriter.close();

			// import file
			new ImportPoiTask(context, new File(context.getFilesDir(),
					DEFAULT_POI_FILE_NAME), true).execute();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "importDefaultPois", e);
			return false;
		} catch (IOException e) {
			Log.e(TAG, "importDefaultPois", e);
			return false;
		}
		return true;
	}

	public static double getECTS(List<LvaWithGrade> lvas) {
		double sum = 0;
		for (LvaWithGrade lva : lvas) {
			sum += lva.getLva().getECTS();
		}
		return sum;
	}

	public static void sortLVAs(List<Lva> lvas) {
		// Collections.sort(lvas, new Comparator<Lva>() {
		//
		// @Override
		// public int compare(Lva lhs, Lva rhs) {
		// int value = lhs.getTitle().compareTo(rhs.getTitle());
		// if (value == 0) {
		// value = lhs.getTerm().compareTo(rhs.getTerm());
		// }
		// return value;
		// }
		// });
	}

	public static void sortLVAsWithGrade(List<LvaWithGrade> lvas) {
		Collections.sort(lvas, new Comparator<LvaWithGrade>() {

			@Override
			public int compare(LvaWithGrade lhs, LvaWithGrade rhs) {
				int value = lhs.getLva().getTitle()
						.compareTo(rhs.getLva().getTitle());
				if (value == 0) {
					value = lhs.getLva().getTerm()
							.compareTo(rhs.getLva().getTerm());
				}
				return value;
			}
		});

	}

	public static void sortGrades(List<ExamGrade> grades) {
		Collections.sort(grades, new Comparator<ExamGrade>() {

			@Override
			public int compare(ExamGrade lhs, ExamGrade rhs) {
				int value = lhs.getGradeType().compareTo(rhs.getGradeType());
				if (value == 0) {
					value = lhs.getDate().compareTo(rhs.getDate());
				}
				if (value == 0) {
					value = lhs.getTerm().compareTo(rhs.getTerm());
				}
				if (value == 0) {
					value = lhs.getTitle().compareTo(rhs.getTitle());
				}
				return value;
			}
		});
	}

	private static void removeDuplicates(LvaWithGrade lva,
			List<LvaWithGrade> lvas) {
		int i = 0;
		while (i < lvas.size()) {
			LvaWithGrade lva2 = lvas.get(i);

			if (lva != lva2
					&& lva.getLva().getCode().equals(lva2.getLva().getCode())) {
				Log.i("removeDuplicates", "remove " + lva2.getLva().getCode()
						+ " " + lva2.getLva().getTitle());
				lvas.remove(i);
			} else {
				i++;
			}
		}
	}

	public static void removeDuplicates(List<LvaWithGrade> mDoneLvas,
			List<LvaWithGrade> mOpenLvas, List<LvaWithGrade> mFailedLvas) {
		int i = 0;
		while (i < mDoneLvas.size()) {
			removeDuplicates(mDoneLvas.get(i), mDoneLvas);
			removeDuplicates(mDoneLvas.get(i), mOpenLvas);
			removeDuplicates(mDoneLvas.get(i), mFailedLvas);
			i++;
		}
		i = 0;
		while (i < mOpenLvas.size()) {
			removeDuplicates(mOpenLvas.get(i), mDoneLvas);
			removeDuplicates(mOpenLvas.get(i), mOpenLvas);
			removeDuplicates(mOpenLvas.get(i), mFailedLvas);
			i++;
		}
		i = 0;
		while (i < mFailedLvas.size()) {
			removeDuplicates(mFailedLvas.get(i), mDoneLvas);
			removeDuplicates(mFailedLvas.get(i), mOpenLvas);
			removeDuplicates(mFailedLvas.get(i), mFailedLvas);
			i++;
		}
	}

	public static double getAvgGrade(List<LvaWithGrade> lvas) {
		double sum = 0;
		int count = 0;
		for (LvaWithGrade lva : lvas) {
			ExamGrade grade = lva.getGrade();
			if (grade != null) {
				sum += grade.getGrade().getValue();
				count++;
			}
		}

		if (count == 0) {
			return 0;
		} else {
			return sum / count;
		}
	}

}
