package org.voidsink.anewjkuapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.voidsink.anewjkuapp.fragment.MapFragment;

import android.content.Context;

public class AppUtils {

	private static final String DEFAULT_POI_FILE_NAME = "JKU.gpx";

	public static void doOnNewVersion(Context context) {
		int mLastVersion = PreferenceWrapper.getLastVersion(context);
		int mCurrentVersion = PreferenceWrapper.getCurrentVersion(context);
		
		if (mLastVersion != mCurrentVersion) {
			boolean errorOccured = false;
			
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
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
