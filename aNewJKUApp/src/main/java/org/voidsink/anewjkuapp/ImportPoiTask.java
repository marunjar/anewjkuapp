package org.voidsink.anewjkuapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.voidsink.anewjkuapp.base.BaseAsyncTask;
import org.voidsink.anewjkuapp.notification.PoiNotification;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

public class ImportPoiTask extends BaseAsyncTask<Void, Void, Void> {
	
	private static final String TAG = ImportPoiTask.class.getSimpleName();

	private ContentProviderClient mProvider;
	private Context mContext;
	private File mFile;
	private boolean mIsDefault;

	public static final String[] POI_PROJECTION = new String[] {
			PoiContentContract.Poi.COL_ROWID, 
			PoiContentContract.Poi.COL_NAME,
			PoiContentContract.Poi.COL_LON, 
			PoiContentContract.Poi.COL_LAT,
			PoiContentContract.Poi.COL_DESCR,
			PoiContentContract.Poi.COL_IS_DEFAULT };

	public static final int COLUMN_POI_ID = 0;
	public static final int COLUMN_POI_NAME = 1;
	public static final int COLUMN_POI_LON = 2;
	public static final int COLUMN_POI_LAT = 3;
	public static final int COLUMN_POI_DESCR = 4;
	public static final int COLUMN_POI_IS_DEFAULT = 5;

	public ImportPoiTask(Context context, File file, boolean isDefault) {
		this.mProvider = context.getContentResolver()
				.acquireContentProviderClient(PoiContentContract.CONTENT_URI);
		this.mContext = context;
		this.mFile = file;
		this.mIsDefault = isDefault;
	}

	@Override
	protected Void doInBackground(Void... params) {

		Log.d(TAG, "start importing POIs");
		PoiNotification mNotification = new PoiNotification(mContext);
		try {
			Map<String, Poi> poiMap = new HashMap<String, Poi>();

			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				Document gpx = builder.parse(mFile);

				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression wpt_path = xpath
						.compile("//gpx/wpt[name][@lat][@lon]");
				// XPathExpression wpt_address =
				// xpath.compile("/*/extensions/gpxx:WaypointExtension/gpxx:Address");
				NodeList wpts = (NodeList) wpt_path.evaluate(gpx,
						XPathConstants.NODESET);
				for (int i = 0; i < wpts.getLength(); i++) {
					Element wpt = (Element) wpts.item(i);
					double lat = Double.parseDouble(wpt.getAttribute("lat"));
					double lon = Double.parseDouble(wpt.getAttribute("lon"));

					NodeList names = wpt.getElementsByTagName("name");
					if (names.getLength() == 1) {
						String name = ((Element) names.item(0))
								.getTextContent();

						Poi poi = new Poi(name, lat, lon);
						if (!poiMap.containsKey(poi.getName())) {
							Log.d(TAG, "poi found: " + poi.getName());

							poiMap.put(poi.getName(), poi);
							poi.parse(wpt);

						}
					}
				}
			} catch (ParserConfigurationException | SAXException | IOException
					| XPathExpressionException e) {
				poiMap.clear();
				Log.e(TAG, "parse failed", e);
				Analytics.sendException(mContext, e, true);
			}

			if (!poiMap.isEmpty()) {
				Log.i(TAG, String.format("got %s pois", poiMap.size()));

				ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

				Uri poiUri = PoiContentContract.Poi.CONTENT_URI;
				Cursor c = mProvider.query(poiUri, POI_PROJECTION, null, null,
						null);

				if (c != null) {
					Log.d(TAG, "Found " + c.getCount()
							+ " local entries. Computing merge solution...");

					int poiId;
					String poiName;
					boolean poiIsDefault;

					// TODO
					while (c.moveToNext()) {
						poiId = c.getInt(COLUMN_POI_ID);
						poiName = c.getString(COLUMN_POI_NAME);
						poiIsDefault = KusssDatabaseHelper.toBool(c
								.getInt(COLUMN_POI_IS_DEFAULT));

						Poi poi = poiMap.get(poiName);
						if (poi != null) {
							poiMap.remove(poiName);

							if (mIsDefault || !poiIsDefault) {
								// Check to see if the entry needs to be updated
								Uri existingUri = poiUri.buildUpon()
										.appendPath(Integer.toString(poiId))
										.build();
								Log.d(TAG, String.format("Scheduling update: %s (%s)", poiName, existingUri));

								batch.add(ContentProviderOperation
										.newUpdate(existingUri)
										// PoiContentContract
										// .asEventSyncAdapter(
										// existingUri,
										// mAccount.name,
										// mAccount.type))
										.withValue(
												PoiContentContract.Poi.COL_ROWID,
												Integer.toString(poiId))
										.withValues(
												poi.getContentValues(
														poiIsDefault,
														mIsDefault)).build());
								// mSyncResult.stats.numUpdates++;
							}
						} else {
							if (poiIsDefault && mIsDefault) {
								// Entry doesn't exist.
								Uri deleteUri = poiUri.buildUpon()
										.appendPath(Integer.toString(poiId))
										.build();
								Log.d(TAG, String.format("Scheduling delete: %s (%s)", poiName, deleteUri));

								batch.add(ContentProviderOperation.newDelete(
										deleteUri)
								// PoiContentContract
								// .asEventSyncAdapter(
								// deleteUri,
								// mAccount.name,
								// mAccount.type))
										.build());
								// mSyncResult.stats.numDeletes++;
							}
						}
					}
					c.close();

					for (Poi poi : poiMap.values()) {
						batch.add(ContentProviderOperation
								.newInsert(poiUri)
								// PoiContentContract
								// .asEventSyncAdapter(poiUri,
								// mAccount.name,
								// mAccount.type))
								.withValues(poi.getContentValues(mIsDefault))
								.build());
						Log.d(TAG, "Scheduling insert: " + poi.getName());
						// mSyncResult.stats.numInserts++;
					}

					if (batch.size() > 0) {
						// mSyncNotification.update("LVAs werden gespeichert");

						Log.d(TAG, "Applying batch update");
						mProvider.applyBatch(batch);
						Log.d(TAG, "Notify resolver");
						mContext.getContentResolver().notifyChange(
								PoiContentContract.Poi.CONTENT_URI, null, // No
																			// local
																			// observer
								false); // IMPORTANT: Do not sync to network
					} else {
						Log.w(TAG, "No batch operations found! Do nothing");
					}

				} else {
					Log.w(TAG, "selection failed");
				}
			}
		} catch (RemoteException | OperationApplicationException e) {
			Analytics.sendException(mContext, e, true);
		} finally {
			mNotification.show();
		}

		return null;
	}
}
