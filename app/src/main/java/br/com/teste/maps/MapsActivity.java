package br.com.teste.maps;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.List;

import br.com.teste.entities.Driver;
import br.com.teste.entities.MarkersMap;
import br.com.teste.servieces.Service;

public class MapsActivity extends FragmentActivity implements ClusterManager.OnClusterClickListener<MarkersMap>,
                                                                ClusterManager.OnClusterInfoWindowClickListener<MarkersMap>,
                                                                ClusterManager.OnClusterItemClickListener<MarkersMap>,
                                                                ClusterManager.OnClusterItemInfoWindowClickListener<MarkersMap>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private Future<List<Driver>> future;
    public List<Driver> driverList = new ArrayList<>();
    private double longitude;
    public double latitude;
    private ImageButton imageGps;
    private ClusterManager<MarkersMap> mClusterManager;
    private Handler UI_HANDLER;
    private boolean center = false;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        imageGps = (ImageButton) findViewById(R.id.image_gps);
        imageGps.setOnClickListener(onclickImageGPS());
        buildGoogleApiClient();
        setUpMapIfNeeded();

        UI_HANDLER = new Handler();
        UI_HANDLER.postDelayed(UI_UPDTAE_RUNNABLE, 30000);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private View.OnClickListener onclickImageGPS() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                center = true;
                getDrivers();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {

        if (mMap == null) {

            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    getDrivers();
                }
            });
        }
    }

    private void getDrivers() {
        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        double bottom = visibleRegion.latLngBounds.southwest.latitude;
        double left = visibleRegion.latLngBounds.southwest.longitude;
        double top = visibleRegion.latLngBounds.northeast.latitude;
        double right = visibleRegion.latLngBounds.northeast.longitude;

        if (center) {
            latitude = visibleRegion.latLngBounds.getCenter().latitude;
            longitude = visibleRegion.latLngBounds.getCenter().longitude;
        }

        LatLng sw = new LatLng(bottom, left);
        LatLng ne = new LatLng(top, right);
        future = Service.getlistDrivers(callback, sw, ne, MapsActivity.this);
    }

    private class MarkersMapRenderer extends DefaultClusterRenderer<MarkersMap> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public MarkersMapRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(MarkersMap markersMap, MarkerOptions markerOptions) {

            mImageView.setImageResource(markersMap.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(markersMap.title);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MarkersMap> markersMapCluster, MarkerOptions markerOptions) {

            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, markersMapCluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (MarkersMap p : markersMapCluster.getItems()) {
                if (profilePhotos.size() == 4) break;
                Drawable drawable = getDrawable(p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(markersMapCluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }
    }

    private void setUpClusterer() {

        mClusterManager = new ClusterManager<MarkersMap>(this, mMap);
        mClusterManager.setRenderer(new MarkersMapRenderer());

        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        addItems();
    }

    private void addItems() {
        if (!center) {
            mClusterManager.addItem(new MarkersMap(new LatLng(latitude, longitude), R.drawable.ic_social_person, "Onde estou"));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < driverList.size(); i++) {
            double offset = i / 60d;
            Driver driver = driverList.get(i);

            latitude = driver.getLatitude() + offset;
            longitude = driver.getLongitude() + offset;

            LatLng position = new LatLng(driver.getLatitude(), driver.getLongitude());
            builder.include(position);

            MarkersMap offsetItem = new MarkersMap(position, R.drawable.ic_pin_taxi, String.valueOf(driver.getDriverId()));
            mClusterManager.addItem(offsetItem);
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 30);
        mMap.moveCamera(cameraUpdate);
    }

    private FutureCallback<List<Driver>> callback = new FutureCallback<List<Driver>>() {
        @Override
        public void onCompleted(Exception e, List<Driver> result) {
           if (e == null) {
                if (result.size() > 0) {
                    driverList = result;
                    setUpClusterer();
                }
            } else {
                e.printStackTrace();
            }
        }
    };

    Runnable UI_UPDTAE_RUNNABLE = new Runnable() {

        @Override
        public void run() {
            mMap.clear();
            getDrivers();
            UI_HANDLER.postDelayed(UI_UPDTAE_RUNNABLE, 50000);
        }
    };

    @Override
    public boolean onClusterClick(Cluster<MarkersMap> cluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MarkersMap> cluster) {

    }

    @Override
    public boolean onClusterItemClick(MarkersMap markersMap) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(MarkersMap markersMap) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }
}
