/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2013-2014 Ludwig M Brinckmann
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.voidsink.anewjkuapp.fragment;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.graphics.AndroidResourceBitmap;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.voidsink.anewjkuapp.ImportPoiTask;
import org.voidsink.anewjkuapp.LocationOverlay;
import org.voidsink.anewjkuapp.utils.MapUtils;
import org.voidsink.anewjkuapp.Poi;
import org.voidsink.anewjkuapp.PoiAdapter;
import org.voidsink.anewjkuapp.PoiContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends BaseFragment implements
        SearchView.OnQueryTextListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String MAP_FILE_NAME = "campus.map";
    private static final String TAG = MapFragment.class.getSimpleName();
    private static final byte MAX_ZOOM_LEVEL = 19;
    private static final byte MIN_ZOOM_LEVEL = 14; // full campus fits to screen at zoom level 15
    private static final byte DEFAULT_ZOOM_LEVEL = 17;

    LocationOverlay mMyLocationOverlay;
    Marker goalLocationOverlay;
    /**
     * The dummy content this fragment is presenting.
     */
    private MapView mapView;
    private TileCache tileCache;

    private MapViewPosition mapViewPosition;

    private SearchView mSearchView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MapFragment() {
        super();
    }

    @Override
    public void onPause() {
        mMyLocationOverlay.disableMyLocation();
        super.onPause();
    }

    @Override
    public void handlePendingIntent(Intent intent) {
        super.handlePendingIntent(intent);
        if (intent != null) {
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                if (intent.getData() != null) {
                    finishSearch(intent.getData());
                } else {
                    String query = intent.getStringExtra(SearchManager.QUERY);
                    boolean isExactLocation = intent.getBooleanExtra(MainActivity.ARG_EXACT_LOCATION, false);
                    doSearch(query, isExactLocation);
                }
            } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                if (intent.getData() != null) {
                    finishSearch(intent.getData());
                } else {
                    String query = intent.getStringExtra(SearchManager.QUERY);
                    boolean isExactLocation = intent.getBooleanExtra(MainActivity.ARG_EXACT_LOCATION, false);
                    doSearch(query, isExactLocation);
                }
            }
        }
    }

    private void doSearch(String query, boolean isExactLocation) {
        Log.i(TAG, "query: " + query);

        List<Poi> pois = new ArrayList<>();

        ContentResolver cr = getContext().getContentResolver();
        Uri searchUri = PoiContentContract.CONTENT_URI.buildUpon()
                .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
                .appendEncodedPath(query).build();
        Cursor c = cr.query(searchUri, ImportPoiTask.POI_PROJECTION, null,
                null, null);
        while (c.moveToNext()) {
            Poi p = new Poi(c);
            if (!isExactLocation || p.getName().equalsIgnoreCase(query)) {
                pois.add(p);
            }
        }
        c.close();

        if (pois.size() == 0) {
            Toast.makeText(getContext(), String.format(getContext().getString(R.string.map_place_not_found), query), Toast.LENGTH_LONG)
                    .show();
        } else if (pois.size() == 1) {
            finishSearch(pois.get(0));
        } else {
            AlertDialog.Builder poiSelector = new AlertDialog.Builder(
                    new ContextThemeWrapper(getContext(), R.style.AppTheme));

            poiSelector.setIcon(R.drawable.ic_launcher);

            final PoiAdapter arrayAdapter = new PoiAdapter(getContext());
            arrayAdapter.addAll(pois);

            poiSelector.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            poiSelector.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishSearch(arrayAdapter.getItem(which));
                        }
                    });
            poiSelector.show();
        }
    }

    private void finishSearch(Poi poi) {
        if (poi != null) {
            finishSearch(PoiContentContract.Poi.CONTENT_URI.buildUpon()
                    .appendEncodedPath(Integer.toString(poi.getId())).build());
        }
    }

    private void finishSearch(Uri uri) {
        Log.i(TAG, "finish search: " + uri.toString());

        // jump to point with given Uri
        ContentResolver cr = getActivity().getContentResolver();

        Cursor c = cr
                .query(uri, ImportPoiTask.POI_PROJECTION, null, null, null);
        if (c.moveToNext()) {
            String name = c.getString(ImportPoiTask.COLUMN_POI_NAME);
            double lon = c.getDouble(ImportPoiTask.COLUMN_POI_LON);
            double lat = c.getDouble(ImportPoiTask.COLUMN_POI_LAT);

            setNewGoal(new LatLong(lat, lon), name);
        }
        if (mSearchView != null) {
            mSearchView.setQuery("", false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mMyLocationOverlay.enableMyLocation(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setNewGoal(LatLong latLong, String name) {
        if (latLong != null) {
            this.mMyLocationOverlay.setSnapToLocationEnabled(false);

            if (!name.isEmpty()) {
                // generate Bubble image
                TextView bubbleView = new TextView(this.getContext());
                MapUtils.setBackground(bubbleView,
                        getResources().getDrawable(R.drawable.balloon_overlay));
                bubbleView.setGravity(Gravity.CENTER);
                bubbleView.setMaxEms(20);
                bubbleView.setTextSize(15);
                bubbleView.setTextColor(Color.BLACK);
                bubbleView.setText(name);
                Bitmap bubble = MapUtils.viewToBitmap(getContext(), bubbleView);
                bubble.incrementRefCount();

                // set new goal
                this.goalLocationOverlay.setLatLong(latLong);
                this.goalLocationOverlay.setBitmap(bubble);
                this.goalLocationOverlay.setHorizontalOffset(0);
                this.goalLocationOverlay.setVerticalOffset(-bubble.getHeight() / 2);
            } else {
                Drawable d = getResources().getDrawable(R.drawable.ic_marker_goal_position);
                Bitmap b = AndroidGraphicFactory.convertToBitmap(d);

                // set new goal
                this.goalLocationOverlay.setLatLong(latLong);
                this.goalLocationOverlay.setBitmap(b);
                this.goalLocationOverlay.setHorizontalOffset(0);
                this.goalLocationOverlay.setVerticalOffset(0);
            }

            if (this.mMyLocationOverlay.getLastLocation() != null) {
                LatLong mLocation = LocationOverlay
                        .locationToLatLong(this.mMyLocationOverlay
                                .getLastLocation());

                // zoom to bounds
                BoundingBox bb = new BoundingBox(Math.min(latLong.latitude,
                        mLocation.latitude), Math.min(latLong.longitude,
                        mLocation.longitude), Math.max(latLong.latitude,
                        mLocation.latitude), Math.max(latLong.longitude,
                        mLocation.longitude));
                Dimension dimension = this.mapView.getModel().mapViewDimension
                        .getDimension();
                this.mapView.getModel().mapViewPosition
                        .setMapPosition(new MapPosition(latLong, LatLongUtils
                                .zoomForBounds(dimension, bb, this.mapView
                                        .getModel().displayModel.getTileSize())));
            } else {
                this.mapViewPosition.setCenter(latLong);
            }
        } else {
            this.goalLocationOverlay.setLatLong(null);
            this.mMyLocationOverlay.setSnapToLocationEnabled(true);
        }
        this.goalLocationOverlay.requestRedraw();
        this.mMyLocationOverlay.requestRedraw();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_snap_to_location:
                item.setChecked(!item.isChecked());
                this.mMyLocationOverlay.setSnapToLocationEnabled(item.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_poi);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        setupSearchView(searchItem);

        if (mMyLocationOverlay != null) {
            MenuItem snapToLocationItem = menu
                    .findItem(R.id.action_snap_to_location);
            mMyLocationOverlay.setSnapToLocationItem(snapToLocationItem);
        }
    }

    private void setupSearchView(MenuItem searchItem) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getActivity().getComponentName()));

            mSearchView.setOnQueryTextListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container,
                false);

        this.mapView = (MapView) rootView.findViewById(R.id.mapView);
        this.mapView.setClickable(true);
        //this.mapView.getFpsCounter().setVisible(true);
        this.mapView.getMapScaleBar().setVisible(true);

        final LayerManager mLayerManager = this.mapView.getLayerManager();

        this.mapViewPosition = this.mapView.getModel().mapViewPosition;

        initializePosition(this.mapViewPosition);

        this.tileCache = AndroidUtil.createTileCache(this.getActivity(),
                "mapFragment",
                this.mapView.getModel().displayModel.getTileSize(), 1.0f, this.mapView.getModel().frameBufferModel.getOverdrawFactor());
        mLayerManager.getLayers().add(createTileRendererLayer(this.tileCache, mapViewPosition,
                getMapFile(), InternalRenderTheme.OSMARENDER, false));

        // overlay with a marker to show the goal position
        this.goalLocationOverlay = new Marker(null, null, 0, 0);
        mLayerManager.getLayers().add(this.goalLocationOverlay);

        // overlay with a marker to show the actual position
        //TODO: find a way to colorize and use R.drawable.ic_marker_position instead of R.drawable.ic_marker_own_position
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_marker_own_position);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);

        this.mMyLocationOverlay = new LocationOverlay(this.getActivity(),
                this.mapViewPosition, bitmap);
        this.mMyLocationOverlay.setSnapToLocationEnabled(false);
        mLayerManager.getLayers().add(this.mMyLocationOverlay);

        return rootView;
    }

    private TileRendererLayer createTileRendererLayer(TileCache tileCache,
                                                      MapViewPosition mapViewPosition, File mapFile,
                                                      XmlRenderTheme renderTheme, boolean hasAlpha) {
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache,
                mapViewPosition, hasAlpha, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setMapFile(mapFile);
        tileRendererLayer.setXmlRenderTheme(renderTheme);
        tileRendererLayer.setTextScale(1.5f);

        return tileRendererLayer;
    }

    protected MapViewPosition initializePosition(MapViewPosition mvp) {
        LatLong center = mvp.getCenter();

        if (center.equals(new LatLong(0, 0))) {
            mvp.setMapPosition(this.getInitialPosition());
        }
        mvp.setZoomLevelMax(MAX_ZOOM_LEVEL);
        mvp.setZoomLevelMin(MIN_ZOOM_LEVEL);
        return mvp;
    }

    protected MapPosition getInitialPosition() {
        File mapFile = getMapFile();
        MapDatabase mapDatabase = new MapDatabase();
        final FileOpenResult result = mapDatabase.openFile(mapFile);
        if (result.isSuccess()) {
            LatLong uniteich = new LatLong(48.33706, 14.31960);
            final MapFileInfo mapFileInfo = mapDatabase.getMapFileInfo();
            if (mapFileInfo != null) {
                if (mapFileInfo.boundingBox.contains(uniteich)) {
                    // Insel im Uniteich
                    return new MapPosition(uniteich, DEFAULT_ZOOM_LEVEL);
                } else if (mapFileInfo.startPosition != null) {
                    // given start position, zoom in range
                    return new MapPosition(mapFileInfo.startPosition,
                            (byte) Math.max(
                                    Math.min(mapFileInfo.startZoomLevel,
                                            MAX_ZOOM_LEVEL), MIN_ZOOM_LEVEL));
                } else {
                    // center of the map
                    return new MapPosition(
                            mapFileInfo.boundingBox.getCenterPoint(),
                            DEFAULT_ZOOM_LEVEL);
                }
            } else {
                // Insel im Uniteich
                return new MapPosition(uniteich, DEFAULT_ZOOM_LEVEL);
            }
        }
        throw new IllegalArgumentException("Invalid Map File "
                + mapFile.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.tileCache != null) {
            this.tileCache.destroy();
        }
        if (this.mapView != null) {
            this.mapView.getModel().mapViewPosition.destroy();
            this.mapView.destroy();
        }

        AndroidResourceBitmap.clearResourceBitmaps();
    }

    protected File getMapFile() {
        File mapFile = PreferenceWrapper.getMapFile(getContext());
        if (mapFile == null || !mapFile.exists() || !mapFile.canRead()) {
            mapFile = new File(getActivity().getFilesDir(), MAP_FILE_NAME);
            Log.i(TAG, "use internal map: " + mapFile.toString());
        } else {
            Log.i(TAG, "use external map: " + mapFile.toString());
        }
        return mapFile;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Log.i(TAG, newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        // Toast.makeText(mContext, newText + " submitted", Toast.LENGTH_SHORT)
        // .show();
        return false;
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_MAP;
    }
}
