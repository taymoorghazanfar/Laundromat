package com.laundromat.delivery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.laundromat.delivery.R;
import com.laundromat.delivery.utils.Constants;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PickLocationActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String FORM_VIEW_INDICATOR = "FormToFill";
    public static final String LOCATION_NAME = "LocationName";
    public static final String LOCATION_LAT_LONG = "LocationLatLong";
    private static final int REQUEST_PERMISSION_LOCATION = 991;

    TextView autoCompleteTextView;
    TextView currentAddress;
    Button selectLocation;
    ImageView backButton;

    private GoogleMap gMap;
    private GoogleApiClient googleApiClient;
    private int formToFill;

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        autoCompleteTextView = findViewById(R.id.locationPicker_autoCompleteText);
        currentAddress = findViewById(R.id.locationPicker_currentAddress);
        selectLocation = findViewById(R.id.locationPicker_destinationButton);
        backButton = findViewById(R.id.back_btn);
        Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
        Places.createClient(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
        }

        setupGoogleApiClient();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.locationPicker_maps);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        Intent intent = getIntent();
        formToFill = intent.getIntExtra(FORM_VIEW_INDICATOR, -1);

        selectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectLocation();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupAutocompleteTextView();
            }
        });

    }

    private void selectLocation() {
        LatLng selectedLocation = gMap.getCameraPosition().target;
        String selectedAddress = currentAddress.getText().toString();

        Intent intent = new Intent();
        intent.putExtra(FORM_VIEW_INDICATOR, formToFill);
        intent.putExtra(LOCATION_NAME, selectedAddress);
        intent.putExtra(LOCATION_LAT_LONG, selectedLocation);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void setupGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void setupAutocompleteTextView() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, 1);
    }


    private void updateLastLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION);
            return;
        }

        gMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

        Location lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);

        if (lastKnownLocation != null) {

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 15f)
            );

            gMap.animateCamera(CameraUpdateFactory.zoomTo(15f));
        } else {

            //This is what you need:
            locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        setupMapOnCameraChange();
        updateLastLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLastLocation();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                currentAddress.setText(place.getAddress());
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(latLng.latitude, latLng.longitude), 15f)
                    );
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("TAG", Objects.requireNonNull(status.getStatusMessage()));
            }
        }
    }

    private void setupMapOnCameraChange() {
        gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng center = gMap.getCameraPosition().target;
                fillAddress(currentAddress, center);
            }
        });
    }

    private void fillAddress(final TextView textView, final LatLng latLng) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geocoder = new Geocoder(PickLocationActivity.this, Locale.getDefault());
                    final List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    PickLocationActivity.this.runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            if (!addresses.isEmpty()) {
                                addresses.size();
                                String address = addresses.get(0).getAddressLine(0);
                                textView.setText(address);
                            } else {
                                textView.setText("Not Available");
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateLastLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 15f)
        );

        gMap.animateCamera(CameraUpdateFactory.zoomTo(15f));
    }
}
