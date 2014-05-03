package org.voidsink.anewjkuapp.provider;

import org.voidsink.anewjkuapp.KusssContentContract;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class KusssContentProvider extends ContentProvider {

	private static final int CODE_LVA = 1;
	private static final int CODE_LVA_ID = 2;
	private static final int CODE_EXAM = 3;
	private static final int CODE_EXAM_ID = 4;
	private static final int CODE_GRADE = 5;
	private static final int CODE_GRADE_ID = 6;

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(KusssContentContract.AUTHORITY,
				KusssContentContract.Lva.PATH, CODE_LVA);
		sUriMatcher.addURI(KusssContentContract.AUTHORITY,
				KusssContentContract.Lva.PATH + "/#", CODE_LVA_ID);
		sUriMatcher.addURI(KusssContentContract.AUTHORITY,
				KusssContentContract.Exam.PATH, CODE_EXAM);
		sUriMatcher.addURI(KusssContentContract.AUTHORITY,
				KusssContentContract.Exam.PATH + "/#", CODE_EXAM_ID);
		sUriMatcher.addURI(KusssContentContract.AUTHORITY,
				KusssContentContract.Grade.PATH, CODE_GRADE);
		sUriMatcher.addURI(KusssContentContract.AUTHORITY,
				KusssContentContract.Grade.PATH + "/#", CODE_GRADE_ID);
	}

	private KusssDatabaseHelper mDbHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String whereIdClause = "";
		int rowsDeleted = -1;
		switch (sUriMatcher.match(uri)) {
		case CODE_LVA:
			rowsDeleted = db.delete(KusssContentContract.Lva.LVA_TABLE_NAME,
					selection, selectionArgs);
			break;
		case CODE_EXAM:
			rowsDeleted = db.delete(KusssContentContract.Exam.EXAM_TABLE_NAME,
					selection, selectionArgs);
			break;
		case CODE_GRADE:
			rowsDeleted = db.delete(
					KusssContentContract.Grade.GRADE_TABLE_NAME, selection,
					selectionArgs);
			break;
		case CODE_LVA_ID:
			whereIdClause = KusssContentContract.Lva.LVA_COL_ID + "="
					+ uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection))
				whereIdClause += " AND " + selection;
			rowsDeleted = db.delete(KusssContentContract.Lva.LVA_TABLE_NAME,
					whereIdClause, selectionArgs);
			break;
		case CODE_EXAM_ID:
			whereIdClause = KusssContentContract.Exam.EXAM_COL_ID + "="
					+ uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection))
				whereIdClause += " AND " + selection;
			rowsDeleted = db.delete(KusssContentContract.Exam.EXAM_TABLE_NAME,
					whereIdClause, selectionArgs);
			break;
		case CODE_GRADE_ID:
			whereIdClause = KusssContentContract.Grade.GRADE_COL_ID + "="
					+ uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection))
				whereIdClause += " AND " + selection;
			rowsDeleted = db.delete(
					KusssContentContract.Grade.GRADE_TABLE_NAME, whereIdClause,
					selectionArgs);
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
		case CODE_LVA:
			return KusssContentContract.CONTENT_TYPE_DIR + "/"
					+ KusssContentContract.Lva.PATH;
		case CODE_LVA_ID:
			return KusssContentContract.CONTENT_TYPE_ITEM + "/"
					+ KusssContentContract.Lva.PATH;
		case CODE_EXAM:
			return KusssContentContract.CONTENT_TYPE_DIR + "/"
					+ KusssContentContract.Exam.PATH;
		case CODE_EXAM_ID:
			return KusssContentContract.CONTENT_TYPE_ITEM + "/"
					+ KusssContentContract.Exam.PATH;
		case CODE_GRADE:
			return KusssContentContract.CONTENT_TYPE_DIR + "/"
					+ KusssContentContract.Grade.PATH;
		case CODE_GRADE_ID:
			return KusssContentContract.CONTENT_TYPE_ITEM + "/"
					+ KusssContentContract.Grade.PATH;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		switch (sUriMatcher.match(uri)) {
		case CODE_LVA: {
			long id = db.insert(KusssContentContract.Lva.LVA_TABLE_NAME, null,
					values);
			if (id != -1)
				getContext().getContentResolver().notifyChange(uri, null);
			return KusssContentContract.Lva.CONTENT_URI.buildUpon()
					.appendPath(String.valueOf(id)).build();
		}
		case CODE_EXAM: {
			long id = db.insert(KusssContentContract.Exam.EXAM_TABLE_NAME,
					null, values);
			if (id != -1)
				getContext().getContentResolver().notifyChange(uri, null);
			return KusssContentContract.Exam.CONTENT_URI.buildUpon()
					.appendPath(String.valueOf(id)).build();
		}
		case CODE_GRADE: {
			long id = db.insert(KusssContentContract.Grade.GRADE_TABLE_NAME,
					null, values);
			if (id != -1)
				getContext().getContentResolver().notifyChange(uri, null);
			return KusssContentContract.Grade.CONTENT_URI.buildUpon()
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
		case CODE_LVA_ID:
			builder.appendWhere(KusssContentContract.Lva.LVA_COL_ID + "="
					+ uri.getLastPathSegment());
		case CODE_LVA:
			if (TextUtils.isEmpty(sortOrder))
				sortOrder = KusssContentContract.Lva.LVA_COL_ID + " ASC";
			builder.setTables(KusssContentContract.Lva.LVA_TABLE_NAME);
			return builder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);
		case CODE_EXAM_ID:
			builder.appendWhere(KusssContentContract.Exam.EXAM_COL_ID + "="
					+ uri.getLastPathSegment());
		case CODE_EXAM:
			if (TextUtils.isEmpty(sortOrder))
				sortOrder = KusssContentContract.Exam.EXAM_COL_ID + " ASC";
			builder.setTables(KusssContentContract.Exam.EXAM_TABLE_NAME);
			return builder.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);
		case CODE_GRADE_ID:
			builder.appendWhere(KusssContentContract.Grade.GRADE_COL_ID + "="
					+ uri.getLastPathSegment());
		case CODE_GRADE:
			if (TextUtils.isEmpty(sortOrder))
				sortOrder = KusssContentContract.Grade.GRADE_COL_ID + " ASC";
			builder.setTables(KusssContentContract.Grade.GRADE_TABLE_NAME);
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
		case CODE_LVA: {
			return db.update(KusssContentContract.Lva.LVA_TABLE_NAME, values,
					selection, selectionArgs);
		}
		case CODE_EXAM: {
			return db.update(KusssContentContract.Exam.EXAM_TABLE_NAME, values,
					selection, selectionArgs);
		}
		case CODE_GRADE: {
			return db.update(KusssContentContract.Grade.GRADE_TABLE_NAME,
					values, selection, selectionArgs);
		}
		case CODE_LVA_ID: {
			whereIdClause = KusssContentContract.Lva.LVA_COL_ID + "="
					+ uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection))
				whereIdClause += " AND " + selection;
			return db.update(KusssContentContract.Lva.LVA_TABLE_NAME, values,
					whereIdClause, selectionArgs);
		}
		case CODE_EXAM_ID: {
			whereIdClause = KusssContentContract.Exam.EXAM_COL_ID + "="
					+ uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection))
				whereIdClause += " AND " + selection;
			return db.update(KusssContentContract.Exam.EXAM_TABLE_NAME, values,
					whereIdClause, selectionArgs);
		}
		case CODE_GRADE_ID: {
			whereIdClause = KusssContentContract.Grade.GRADE_COL_ID + "="
					+ uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection))
				whereIdClause += " AND " + selection;
			return db.update(KusssContentContract.Grade.GRADE_TABLE_NAME,
					values, whereIdClause, selectionArgs);
		}
		default:
			throw new IllegalArgumentException("URI " + uri
					+ " is not supported.");
		}
	}
}
