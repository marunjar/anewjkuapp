package org.voidsink.anewjkuapp.provider;

import org.voidsink.anewjkuapp.PoiContentContract;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class PoiContentProvider extends ContentProvider {

	private static final int CODE_POI = 1;
	private static final int CODE_POI_ID = 2;

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(PoiContentContract.AUTHORITY,
				PoiContentContract.Poi.PATH, CODE_POI);
		sUriMatcher.addURI(PoiContentContract.AUTHORITY,
				PoiContentContract.Poi.PATH + "/#", CODE_POI_ID);
	}

	private KusssDatabaseHelper mDbHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String whereIdClause = "";
		int rowsDeleted = -1;
		switch (sUriMatcher.match(uri)) {
		case CODE_POI:
			rowsDeleted = db.delete(PoiContentContract.Poi.TABLE_NAME,
					selection, selectionArgs);
			break;
		case CODE_POI_ID:
			whereIdClause = PoiContentContract.Poi.COL_ID + "="
					+ uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection))
				whereIdClause += " AND " + selection;
			rowsDeleted = db.delete(PoiContentContract.Poi.TABLE_NAME,
					whereIdClause, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		// Notifying the changes, if there are any
		if (rowsDeleted != -1)
			getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case CODE_POI:
			return PoiContentContract.CONTENT_TYPE_DIR + "/"
					+ PoiContentContract.Poi.PATH;
		case CODE_POI_ID:
			return PoiContentContract.CONTENT_TYPE_ITEM + "/"
					+ PoiContentContract.Poi.PATH;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		switch (sUriMatcher.match(uri)) {
		case CODE_POI: {
			long id = db
					.insert(PoiContentContract.Poi.TABLE_NAME, null, values);
			if (id != -1)
				getContext().getContentResolver().notifyChange(uri, null);
			return PoiContentContract.Poi.CONTENT_URI.buildUpon()
					.appendPath(String.valueOf(id)).build();
		}
		default: {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		}
	}

	@Override
	public boolean onCreate() {
		mDbHelper = new KusssDatabaseHelper(getContext());

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		/*
		 * Choose the table to query and a sort order based on the code returned
		 * for the incoming URI. Here, too, only the statements for table 3 are
		 * shown.
		 */
		switch (sUriMatcher.match(uri)) {
		case CODE_POI_ID:
			builder.appendWhere(PoiContentContract.Poi.COL_ID + "="
					+ uri.getLastPathSegment());
		case CODE_POI:
			if (TextUtils.isEmpty(sortOrder))
				sortOrder = PoiContentContract.Poi.COL_ID + " ASC";
			builder.setTables(PoiContentContract.Poi.TABLE_NAME);
			return builder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);
		default:
			throw new IllegalArgumentException("URI " + uri
					+ " is not supported.");
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String whereIdClause = "";

		switch (sUriMatcher.match(uri)) {
		case CODE_POI: {
			return db.update(PoiContentContract.Poi.TABLE_NAME, values,
					selection, selectionArgs);
		}
		case CODE_POI_ID: {
			whereIdClause = PoiContentContract.Poi.COL_ID + "="
					+ uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection))
				whereIdClause += " AND " + selection;
			return db.update(PoiContentContract.Poi.TABLE_NAME, values,
					whereIdClause, selectionArgs);
		}
		default:
			throw new IllegalArgumentException("URI " + uri
					+ " is not supported.");
		}
	}
}
