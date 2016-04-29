package parks.kinsa.com.utility_classes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import parks.kinsa.com.interfaces.NotifyLocationObtained;

/**
 * Created by oleg on 4/28/16.
 */
public class LocationTracker implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    Location mLastLocation;
    LocationSettingsRequest.Builder mBuilder;
    Pair<Double, Double> mYouAreHere; //our current location
    NotifyLocationObtained mCallback;
    LocationRequest mLocationRequest;

    public LocationTracker(Context ctx, NotifyLocationObtained callback) {
        mContext = ctx;
        mCallback = callback;
    }

    public void start() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            if (mGoogleApiClient != null)
                mGoogleApiClient.connect();
            else {
                Log.i("KinsaTest", "Failure connecting to GoogleApiClient");
                return;
            }
        }
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mBuilder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        }
    }

    public void stop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            stopLocationUpdates();
        }
    }

    /**
     * a very naive implementation of this functionality. This is a test, after all.
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //In production code, we would ask the user for permission to do this and override the permissions depending on user response.
            return;
        }
        startLocationUpdates(); //updates can be started only after initialization
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mYouAreHere = new Pair<>(mLastLocation.getLatitude(), mLastLocation.getLongitude()); //package it up.
            mCallback.onSuccess(mYouAreHere); //notify client that we got a location
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mYouAreHere = new Pair<>(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mCallback.onSuccess(mYouAreHere);
    }

    void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Starts the periodic update of location data.
     * Again this is a naive implementation for purposes of bravity. In production you would target API version 23, which would require more testing and a request to the
     * user to grant permission to access the user's location.
     */
    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
}
