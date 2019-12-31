/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2019 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.worker;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.Poi;
import org.voidsink.anewjkuapp.PoiContentContract;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BaseWorker;
import org.voidsink.anewjkuapp.notification.PoiNotification;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.voidsink.anewjkuapp.utils.Consts;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

public class ImportPoiWorker extends BaseWorker {

    private static final Logger logger = LoggerFactory.getLogger(ImportPoiWorker.class);
    private final File mFile;
    private final boolean mIsDefault;

    public ImportPoiWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        String filename = workerParams.getInputData().getString(Consts.ARG_FILENAME);
        if (filename != null) {
            this.mFile = new File(context.getFilesDir(), filename);
        } else {
            this.mFile = null;
        }
        this.mIsDefault = workerParams.getInputData().getBoolean(Consts.ARG_IS_DEFAULT, false);
    }

    @NonNull
    @Override
    public Result doWork() {
        return importPoi();
    }

    private Result importPoi() {
        if (mFile == null || !mFile.exists() || !mFile.canRead()) {
            return getFailure();
        }

        final ContentResolver mResolver = getApplicationContext().getContentResolver();
        if (mResolver == null) {
            return getFailure();
        }

        final ContentProviderClient mProvider = mResolver.acquireContentProviderClient(PoiContentContract.CONTENT_URI);

        if (mProvider == null) {
            return getFailure();
        }

        PoiNotification mNotification = new PoiNotification(getApplicationContext());

        try {
            Map<String, Poi> poiMap = new HashMap<>();

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
                        String name = names.item(0).getTextContent();

                        Poi poi = new Poi(name, lat, lon);
                        if (!poiMap.containsKey(poi.getName())) {
                            logger.debug("poi found: {}", poi.getName());

                            poiMap.put(poi.getName(), poi);
                            poi.parse(wpt);

                        }
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException
                    | XPathExpressionException e) {
                poiMap.clear();
                Analytics.sendException(getApplicationContext(), e, true);
            }

            if (!poiMap.isEmpty()) {
                logger.info("got {} pois", poiMap.size());

                ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                Uri poiUri = PoiContentContract.Poi.CONTENT_URI;

                try (Cursor c = mProvider.query(poiUri, PoiContentContract.Poi.DB.PROJECTION, null, null,
                        null)) {

                    if (c != null) {
                        logger.debug("Found {} local entries. Computing merge solution...", c.getCount());

                        int poiId;
                        String poiName;
                        boolean poiIsDefault;

                        // TODO
                        while (c.moveToNext()) {
                            poiId = c.getInt(PoiContentContract.Poi.DB.COL_ID);
                            poiName = c.getString(PoiContentContract.Poi.DB.COL_NAME);
                            poiIsDefault = KusssDatabaseHelper.toBool(c
                                    .getInt(PoiContentContract.Poi.DB.COL_IS_DEFAULT));

                            Poi poi = poiMap.get(poiName);
                            if (poi != null) {
                                poiMap.remove(poiName);

                                if (mIsDefault || !poiIsDefault) {
                                    // Check to see if the entry needs to be updated
                                    Uri existingUri = poiUri.buildUpon()
                                            .appendPath(Integer.toString(poiId))
                                            .build();
                                    logger.debug("Scheduling update: {} ({})", poiName, existingUri);

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
                                }
                            } else {
                                if (poiIsDefault && mIsDefault) {
                                    // Entry doesn't exist.
                                    Uri deleteUri = poiUri.buildUpon()
                                            .appendPath(Integer.toString(poiId))
                                            .build();
                                    logger.debug("Scheduling delete: {} ({})", poiName, deleteUri);

                                    batch.add(ContentProviderOperation.newDelete(
                                            deleteUri)
                                            // PoiContentContract
                                            // .asEventSyncAdapter(
                                            // deleteUri,
                                            // mAccount.name,
                                            // mAccount.type))
                                            .build());
                                }
                            }
                        }
                        for (Poi poi : poiMap.values()) {
                            batch.add(ContentProviderOperation
                                    .newInsert(poiUri)
                                    // PoiContentContract
                                    // .asEventSyncAdapter(poiUri,
                                    // mAccount.name,
                                    // mAccount.type))
                                    .withValues(poi.getContentValues(mIsDefault))
                                    .build());
                            logger.debug("Scheduling insert: {}", poi.getName());
                        }

                        if (batch.size() > 0) {
                            // mSyncNotification.update("LVAs werden gespeichert");

                            logger.debug("Applying batch update");
                            mProvider.applyBatch(batch);
                            logger.debug("Notify resolver");
                            getApplicationContext().getContentResolver().notifyChange(
                                    PoiContentContract.Poi.CONTENT_URI, null, // No
                                    // local
                                    // observer
                                    false); // IMPORTANT: Do not sync to network
                        } else {
                            logger.warn("No batch operations found! Do nothing");
                        }
                    } else {
                        logger.warn("selection failed");
                    }
                }
            }

            mNotification.show();

            return getSuccess();
        } catch (Exception e) {
            Analytics.sendException(getApplicationContext(), e, true);

            return getRetry();
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mProvider.close();
            } else {
                mProvider.release();
            }
        }
    }
}
