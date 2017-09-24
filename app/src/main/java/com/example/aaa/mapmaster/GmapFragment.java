package com.example.aaa.mapmaster;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.AppCompatButton;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.aaa.mapmaster.R.id.teke_shot;

/**
 * Created by Данил on 22.09.2017.
 */

public class GmapFragment  extends Fragment implements OnMapReadyCallback {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragments_gmaps, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public class Gmap extends FragmentActivity implements OnMapReadyCallback {
        private GoogleMap mMap;
        private LocationManager mLocationManager;
        static final int REQUEST_TAKE_PHOTO = 1;

        private List<ImagedMarker> markers = new ArrayList<>();

        String mCurrentPhotoPath;

        




        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.fragments_gmaps);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //TODO: нужно что-то тут делать
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA
                }, 0);
            }
        }

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(0, 0);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            this.updateImagesOnMap();

            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 10, mLocationListener);
        }

        private final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(l));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        public void takeAShot(View button) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                        }

                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(this,
                                    "com.example.android.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                        }
                    }

                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            this.updateImagesOnMap();
        }

        private void updateImagesOnMap() {
//        TODO: сделать обновление иконок
            File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File[] files = directory.listFiles();
            Boolean finded = false;

            List<ImagedMarker> tMarkers = new ArrayList<>();
            tMarkers.addAll(markers);

            for (File file : files) {
                for (ImagedMarker marker : markers) {
                    if (marker.isSame(file)) {
                        finded = true;
                        break;
                    }
                }

                if (!finded) {
                    ImagedMarker marker = new ImagedMarker(file);
                    tMarkers.add(marker);
                }
            }

            markers = tMarkers;
            mMap.clear();
            for (ImagedMarker marker : markers) {
                mMap.addMarker(marker.getMarker());
            }
        }

        private File createImageFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }

        public class ImagedMarker {
            File mImage = null;

            Float Latitude, Longitude;

            Bitmap bmp = null;

            public ImagedMarker(File image) {
                mImage = image;
            }

            public MarkerOptions getMarker() {
                if (bmp == null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(mImage.getAbsolutePath(), options);

                    bmp = Bitmap.createScaledBitmap(bitmap, 80, 80, false);

                    try {
                        ExifInterface exif = new ExifInterface(mImage.getAbsolutePath());
                        String LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                        String LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        String LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                        if ((LATITUDE != null)
                                && (LATITUDE_REF != null)
                                && (LONGITUDE != null)
                                && (LONGITUDE_REF != null)) {

                            if (LATITUDE_REF.equals("N")) {
                                Latitude = convertToDegree(LATITUDE);
                            } else {
                                Latitude = 0 - convertToDegree(LATITUDE);
                            }

                            if (LONGITUDE_REF.equals("E")) {
                                Longitude = convertToDegree(LONGITUDE);
                            } else {
                                Longitude = 0 - convertToDegree(LONGITUDE);
                            }

                        }

                    } catch (Exception e) {

                    }
                }
                if (Longitude == null || Latitude == null) {
                    Longitude = new Float(0);
                    Latitude = new Float(0);
                }

                //TODO: get lat lon from Image
                return new MarkerOptions().position(new LatLng(Latitude, Longitude)).icon(BitmapDescriptorFactory.fromBitmap(bmp));//.anchor(0.5f,0.5f);
            }

            private Float convertToDegree(String stringDMS) {
                Float result = null;
                String[] DMS = stringDMS.split(",", 3);

                String[] stringD = DMS[0].split("/", 2);
                Double D0 = new Double(stringD[0]);
                Double D1 = new Double(stringD[1]);
                Double FloatD = D0 / D1;

                String[] stringM = DMS[1].split("/", 2);
                Double M0 = new Double(stringM[0]);
                Double M1 = new Double(stringM[1]);
                Double FloatM = M0 / M1;

                String[] stringS = DMS[2].split("/", 2);
                Double S0 = new Double(stringS[0]);
                Double S1 = new Double(stringS[1]);
                Double FloatS = S0 / S1;

                result = new Float(FloatD + (FloatM / 60) + (FloatS / 3600));

                return result;


            }

            ;

            public boolean isSame(File checkImage) {
                return mImage.getName().equals(checkImage.getName());
            }
        }



    }

}