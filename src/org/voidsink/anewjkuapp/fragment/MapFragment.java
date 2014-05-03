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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.layer.MyLocationOverlay;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class MapFragment extends BaseFragment {
	MyLocationOverlay myLocationOverlay;

	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	private static final String ARG_ITEM_ID = "item_id";
	private static final String MAP_FILE_NAME = "campus.map";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private MapView mapView;
	private TileCache tileCache;

	private MapViewPosition mapViewPosition;

	private LayerManager mLayerManager;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MapFragment() {
		super();
	}
	
	@Override
	public void onPause() {
		myLocationOverlay.disableMyLocation();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.myLocationOverlay.enableMyLocation(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.map, menu);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_map, container,
				false);
		try {
			// write file to sd for mapsforge
			OutputStream mapFileWriter = new BufferedOutputStream(getActivity()
					.openFileOutput(MAP_FILE_NAME, Context.MODE_PRIVATE));
			InputStream assetData = new BufferedInputStream(getActivity()
					.getAssets().open(MAP_FILE_NAME));

			byte[] buffer = new byte[1024];
			int len = assetData.read(buffer);
			while (len != -1) {
				mapFileWriter.write(buffer, 0, len);
				len = assetData.read(buffer);
			}
			mapFileWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.mapView = (MapView) rootView.findViewById(R.id.mapView);
		this.mapView.setClickable(true);
		this.mapView.getFpsCounter().setVisible(true);
		this.mapView.getMapScaleBar().setVisible(true);

		this.mLayerManager = this.mapView.getLayerManager();
		Layers layers = mLayerManager.getLayers();

		this.mapViewPosition = this.mapView.getModel().mapViewPosition;

		initializePosition(this.mapViewPosition);

		this.tileCache = AndroidUtil.createTileCache(this.getActivity(),
				"fragments",
				this.mapView.getModel().displayModel.getTileSize(), 1.0f, 1.5);
		layers.add(createTileRendererLayer(this.tileCache, mapViewPosition,
				getMapFile(), InternalRenderTheme.OSMARENDER, false));

		// a marker to show at the position
		Drawable drawable = getResources().getDrawable(R.drawable.marker_red);
		Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);

		// create the overlay and tell it to follow the location
		this.myLocationOverlay = new MyLocationOverlay(this.getActivity(),
				this.mapViewPosition, bitmap);
		this.myLocationOverlay.setSnapToLocationEnabled(true);
		this.mLayerManager.getLayers().add(this.myLocationOverlay);

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
		mvp.setZoomLevelMax((byte) 19);
		mvp.setZoomLevelMin((byte) 15);// full campus fits to screen
		return mvp;
	}

	protected MapPosition getInitialPosition() {
		MapDatabase mapDatabase = new MapDatabase();
		final FileOpenResult result = mapDatabase.openFile(getMapFile());
		if (result.isSuccess()) {
			final MapFileInfo mapFileInfo = mapDatabase.getMapFileInfo();
			if (mapFileInfo != null && mapFileInfo.startPosition != null) {
				return new MapPosition(mapFileInfo.startPosition,
						(byte) mapFileInfo.startZoomLevel);
			} else {
				// return new
				// MapPosition(mapFileInfo.boundingBox.getCenterPoint(), (byte)
				// 12);
				return new MapPosition(new LatLong(48.33706, 14.31960),
						(byte) 17); // Insel im Uniteich
			}
		}
		throw new IllegalArgumentException("Invalid Map File " + MAP_FILE_NAME);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (this.mapView != null) {
			this.mapView.destroy();
		}
		if (this.tileCache != null) {
			this.tileCache.destroy();
		}
		org.mapsforge.map.android.graphics.AndroidResourceBitmap
				.clearResourceBitmaps();

	}

	protected File getMapFile() {
		return new File(getActivity().getFilesDir(), MAP_FILE_NAME);
	}
}
