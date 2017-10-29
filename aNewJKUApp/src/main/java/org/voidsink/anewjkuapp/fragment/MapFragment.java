/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2017 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidSupportUtil;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.labels.LabelLayer;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.voidsink.anewjkuapp.ImportPoiTask;
import org.voidsink.anewjkuapp.LocationOverlay;
import org.voidsink.anewjkuapp.Poi;
import org.voidsink.anewjkuapp.PoiAdapter;
import org.voidsink.anewjkuapp.PoiContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.anewjkuapp.utils.MapUtils;

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

    private static final String KEY_GOAL_LATITUDE = "GOAL_LATITUDE";
    private static final String KEY_GOAL_LONGITUDE = "GOAL_LONGITUDE";
    private static final String KEY_GOAL_NAME = "GOAL_NAME";

    private static final byte PERMISSIONS_REQUEST_READ_STORAGE = 122;

    // Map Layer
    private MyMarker goalLocation = null;
    private Marker goalLocationOverlay;
    private LocationOverlay mMyLocationOverlay;

    /**
     * The dummy content this fragment is presenting.
     */
    private MapView mapView;

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


    static class MyMarker {
        private final LatLong mLatLon;
        private final String mName;

        MyMarker(double lat, double lon, String name) {
            this.mLatLon = new LatLong(lat, lon);
            this.mName = name;
        }

        LatLong getLatLon() {
            return mLatLon;
        }

        public String getName() {
            return mName;
        }
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
        query = query.trim();
        Log.i(TAG, "query: " + query);

        List<Poi> pois = new ArrayList<>();

        ContentResolver cr = getContext().getContentResolver();
        Uri searchUri = PoiContentContract.CONTENT_URI.buildUpon()
                .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
                .appendPath(query).build();
        Cursor c = cr.query(searchUri, ImportPoiTask.POI_PROJECTION, null,
                null, null);
        if (c != null) {
            while (c.moveToNext()) {
                Poi p = new Poi(c);
                if (!isExactLocation || p.getName().equalsIgnoreCase(query)) {
                    pois.add(p);
                }
            }
            c.close();
        }

        if (pois.size() == 0) {
            Toast.makeText(getContext(), getContext().getString(R.string.map_place_not_found, query), Toast.LENGTH_LONG)
                    .show();
        } else if (pois.size() == 1) {
            finishSearch(pois.get(0));
        } else {
            AlertDialog.Builder poiSelector = new AlertDialog.Builder(getContext());

            poiSelector.setTitle(R.string.map_select_location);

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

        if (uri.getScheme() == null && uri.toString().contains(PoiContentContract.AUTHORITY)) {
            uri = Uri.parse(String.format("content://%1$s", uri));
        }

        // jump to point with given Uri
        ContentResolver cr = getActivity().getContentResolver();

        Cursor c = cr
                .query(uri, ImportPoiTask.POI_PROJECTION, null, null, null);
        if (c != null) {
            if (c.moveToNext()) {
                String name = c.getString(ImportPoiTask.COLUMN_POI_NAME);
                double lon = c.getDouble(ImportPoiTask.COLUMN_POI_LON);
                double lat = c.getDouble(ImportPoiTask.COLUMN_POI_LAT);

                setNewGoal(new MyMarker(lat, lon, name));
            }
            c.close();
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

    private void setNewGoal(MyMarker marker) {
        if (goalLocationOverlay != null) {
            if (marker != null) {
                this.goalLocation = marker;
                if (!marker.getName().isEmpty()) {
                    // generate Bubble image
                    TextView bubbleView = new TextView(this.getContext());
                    MapUtils.setBackground(bubbleView, ContextCompat.getDrawable(getContext(), R.drawable.balloon_overlay));
                    bubbleView.setGravity(Gravity.CENTER);
                    bubbleView.setMaxEms(20);
                    bubbleView.setTextSize(15);
                    bubbleView.setTextColor(Color.BLACK);
                    bubbleView.setText(marker.getName());
                    Bitmap bubble = MapUtils.viewToBitmap(getContext(), bubbleView);
                    bubble.incrementRefCount();

                    // set new goal
                    this.goalLocationOverlay.setLatLong(marker.getLatLon());
                    this.goalLocationOverlay.setBitmap(bubble);
                    this.goalLocationOverlay.setHorizontalOffset(0);
                    this.goalLocationOverlay.setVerticalOffset(-bubble.getHeight() / 2);
                } else {
                    Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_goal_position);
                    Bitmap b = AndroidGraphicFactory.convertToBitmap(d);

                    // set new goal
                    this.goalLocationOverlay.setLatLong(marker.getLatLon());
                    this.goalLocationOverlay.setBitmap(b);
                    this.goalLocationOverlay.setHorizontalOffset(0);
                    this.goalLocationOverlay.setVerticalOffset(0);
                }

                if (this.mMyLocationOverlay != null) {
                    this.mMyLocationOverlay.setSnapToLocationEnabled(false);

                    if (this.mMyLocationOverlay.getLastLocation() != null) {

                        LatLong mLocation = LocationOverlay
                                .locationToLatLong(this.mMyLocationOverlay
                                        .getLastLocation());

                        // lat long difference for bounding box
                        double mDLat = Math.abs((mLocation.latitude - marker.getLatLon().latitude));
                        double mDLong = Math.abs((mLocation.longitude - marker.getLatLon().longitude));

                        // trunc distance
                        double distance = mLocation.distance(marker.getLatLon());
                        if (distance > 0.0088) {
                            mDLat = (mDLat * 0.0088 / distance);
                            mDLong = (mDLong * 0.0088 / distance);
                        }

                        // zoom to bounds
                        BoundingBox bb = new BoundingBox(
                                Math.max(MercatorProjection.LATITUDE_MIN, marker.getLatLon().latitude - mDLat),
                                Math.max(-180, marker.getLatLon().longitude - mDLong),
                                Math.min(MercatorProjection.LATITUDE_MAX, marker.getLatLon().latitude + mDLat),
                                Math.min(180, marker.getLatLon().longitude + mDLong));
                        Dimension dimension = this.mapView.getModel().mapViewDimension
                                .getDimension();
                        // zoom to bounding box, center at goalPosition
                        this.mapView.getModel().mapViewPosition
                                .setMapPosition(new MapPosition(marker.getLatLon(), LatLongUtils
                                        .zoomForBounds(dimension, bb, this.mapView
                                                .getModel().displayModel.getTileSize())));
                    } else {
                        this.mapView.setCenter(marker.getLatLon());
                    }
                } else {
                    this.mapView.setCenter(marker.getLatLon());
                }
            } else {
                this.goalLocationOverlay.setLatLong(null);
                this.mMyLocationOverlay.setSnapToLocationEnabled(true);
            }
            this.goalLocationOverlay.requestRedraw();
            this.mMyLocationOverlay.requestRedraw();
        }
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
        mSearchView = (SearchView) searchItem.getActionView();
        setupSearchView(mSearchView);

        if (mMyLocationOverlay != null) {
            MenuItem snapToLocationItem = menu
                    .findItem(R.id.action_snap_to_location);
            mMyLocationOverlay.setSnapToLocationItem(snapToLocationItem);
        }
    }

    private void setupSearchView(SearchView searchView) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getActivity().getComponentName()));

            searchView.setOnQueryTextListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container,
                false);

        this.mapView = rootView.findViewById(R.id.mapView);
        this.mapView.setClickable(true);
        //this.mapView.getFpsCounter().setVisible(true);
        this.mapView.getMapScaleBar().setVisible(true);
        this.mapView.setBuiltInZoomControls(true);
        this.mapView.setZoomLevelMin(MIN_ZOOM_LEVEL);
        this.mapView.setZoomLevelMax(MAX_ZOOM_LEVEL);
        this.mapView.getMapZoomControls().setZoomInResource(R.drawable.zoom_control_in);
        this.mapView.getMapZoomControls().setZoomOutResource(R.drawable.zoom_control_out);
        this.mapView.getMapZoomControls().setMarginHorizontal(getContext().getResources().getDimensionPixelSize(R.dimen.map_zoom_control_margin_horizontal));
        this.mapView.getMapZoomControls().setMarginVertical(getContext().getResources().getDimensionPixelSize(R.dimen.map_zoom_control_margin_vertical));


        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createLayers();

        restoreMarker(savedInstanceState);
    }

    private void restoreMarker(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_GOAL_LATITUDE) && savedInstanceState.containsKey(KEY_GOAL_LONGITUDE) && savedInstanceState.containsKey(KEY_GOAL_NAME)) {
                setNewGoal(new MyMarker(savedInstanceState.getDouble(KEY_GOAL_LATITUDE), savedInstanceState.getDouble(KEY_GOAL_LONGITUDE), savedInstanceState.getString(KEY_GOAL_NAME)));
            }
        }
    }

    private void createLayers() {
        if (AndroidSupportUtil.runtimePermissionRequiredForReadExternalStorage(this.getActivity(), getMapFileDirectory())) {
            // note that this the Fragment method, not compat lib
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_STORAGE);
        } else {
            TileCache tileCache = AndroidUtil.createTileCache(getContext(),
                    "mapFragment",
                    this.mapView.getModel().displayModel.getTileSize(),
                    1.0f,
                    this.mapView.getModel().frameBufferModel.getOverdrawFactor());

            final Layers layers = this.mapView.getLayerManager().getLayers();

            MapViewPosition mapViewPosition = this.mapView.getModel().mapViewPosition;
            initializePosition(mapViewPosition);

            TileRendererLayer tileRendererLayer = createTileRendererLayer(tileCache, mapViewPosition,
                    getMapFile(), getRenderTheme());
            layers.add(tileRendererLayer);

            LabelLayer labelLayer = new LabelLayer(AndroidGraphicFactory.INSTANCE, tileRendererLayer.getLabelStore());
            mapView.getLayerManager().getLayers().add(labelLayer);

            // overlay with a marker to show the goal position
            this.goalLocationOverlay = new Marker(null, null, 0, 0);
            layers.add(this.goalLocationOverlay);

            // overlay with a marker to show the actual position
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_own_position);
            Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);

            this.mMyLocationOverlay = new LocationOverlay(getActivity(), mapViewPosition, bitmap);
            this.mMyLocationOverlay.setSnapToLocationEnabled(false);
            layers.add(this.mMyLocationOverlay);
        }
    }

    private XmlRenderTheme getRenderTheme() {
        try {
            return InternalRenderTheme.DEFAULT;
        } catch (Exception e) {
            Analytics.sendException(getContext(), e, false);
        }

        return InternalRenderTheme.OSMARENDER;
    }

    @Override
    public void onDestroyView() {
        removeLayers();

        super.onDestroyView();
    }

    private void removeLayers() {
        this.mMyLocationOverlay = null;
        this.goalLocationOverlay = null;

        this.mapView.destroyAll();
        this.mapView = null;

        AndroidGraphicFactory.clearResourceMemoryCache();
    }

    private TileRendererLayer createTileRendererLayer(TileCache tileCache,
                                                      MapViewPosition mapViewPosition, File mapFile,
                                                      XmlRenderTheme renderTheme) {
        MapDataStore mapDataStore = new MapFile(mapFile);

        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(tileCache,
                mapViewPosition, mapDataStore, renderTheme, false, false, true);
        tileRendererLayer.setTextScale(1.5f);

        return tileRendererLayer;
    }

    private MapViewPosition initializePosition(MapViewPosition mvp) {
        LatLong center = mvp.getCenter();

        if (center.equals(new LatLong(0, 0))) {
            mvp.setMapPosition(this.getInitialPosition());
        }
        return mvp;
    }

    private MapPosition getInitialPosition() {
        File mapFile = getMapFile();
        MapDataStore mapDataStore = new MapFile(mapFile);

        final LatLong uniteich = new LatLong(48.33706, 14.31960);
        final byte zoomLevel = (byte) Math.max(Math.min(DEFAULT_ZOOM_LEVEL /*mapDataStore.startZoomLevel()*/, MAX_ZOOM_LEVEL), MIN_ZOOM_LEVEL);

        if (mapDataStore.boundingBox().contains(uniteich)) {
            // Insel im Uniteich
            return new MapPosition(uniteich, zoomLevel);
        } else if (mapDataStore.startPosition() != null &&
                mapDataStore.boundingBox().contains(mapDataStore.startPosition())) {
            // given start position, zoom in range
            return new MapPosition(mapDataStore.startPosition(), zoomLevel);
        } else {
            // center of the map
            return new MapPosition(
                    mapDataStore.boundingBox().getCenterPoint(),
                    zoomLevel);
        }
    }

    private File getMapFile() {
        File mapFile = PreferenceWrapper.getMapFile(getContext());
        if (mapFile == null || !mapFile.exists() || !mapFile.canRead()) {
            mapFile = new File(getActivity().getFilesDir(), MAP_FILE_NAME);
            Log.i(TAG, "use internal map: " + mapFile.toString());
        } else {
            Log.i(TAG, "use external map: " + mapFile.toString());
        }
        return mapFile;
    }

    private File getMapFileDirectory() {
        File mapFile = PreferenceWrapper.getMapFile(getContext());
        if (mapFile == null || !mapFile.exists() || !mapFile.canRead()) {
            return getActivity().getFilesDir();
        } else {
            return mapFile.getParentFile();
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Log.i(TAG, newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        return false;
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_MAP;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (goalLocation != null) {
            outState.putDouble(KEY_GOAL_LATITUDE, goalLocation.getLatLon().latitude);
            outState.putDouble(KEY_GOAL_LONGITUDE, goalLocation.getLatLon().longitude);
            outState.putString(KEY_GOAL_NAME, goalLocation.getName());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // permission is not granted, the app should do something meaningful here.
                    return;
                }
                createLayers();
                return;
            }
            case LocationOverlay.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                mMyLocationOverlay.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
