package fr.efrei.tp6;

import java.util.ArrayList;
import java.util.List;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public final class MainActivity
    extends AppCompatActivity
{

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final int PERMISSION_REQUEST_CODE = 1000;

  private static final double EFREI_LATITUDE = 48.79383644544335;

  private static final double EFREI_LONGITUDE = 2.3691616846547396;

  private final List<Location> userLocations = new ArrayList<>();

  private FusedLocationProviderClient fusedLocationProviderClient;

  private RecyclerView recyclerView;

  private LocationCallback locationCallback = new LocationCallback()
  {
    @Override
    public void onLocationResult(LocationResult locationResult)
    {
      Log.d(MainActivity.TAG, "New position found");

      final Location location = locationResult.getLastLocation();

      updateList(location);
      checkEfreiPosition(location);
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability)
    {
      Log.d(MainActivity.TAG, locationAvailability.isLocationAvailable() + "?");
      super.onLocationAvailability(locationAvailability);
    }
  };

  @SuppressLint("MissingPermission")
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults)
  {
    if (requestCode == MainActivity.PERMISSION_REQUEST_CODE)
    {
      Log.d(MainActivity.TAG, "Checking runtime permission result");

      if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
      {
        Log.d(MainActivity.TAG, "Runtime permission has been granted");
        trackLocation();
      }
      else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]) == false)
      {
        Log.d(MainActivity.TAG, "Runtime permission has been disabled for ever");
        displayAlertDialog();
      }
      else
      {
        Log.d(MainActivity.TAG, "Runtime permission has been disabled");
        trackPermissionCheck();
      }
    }

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    recyclerView = findViewById(R.id.recyclerView);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    trackPermissionCheck();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }

  private void checkEfreiPosition(Location location)
  {
    final float[] result = new float[3];
    Location.distanceBetween(location.getLatitude(), location.getLongitude(), MainActivity.EFREI_LATITUDE, MainActivity.EFREI_LONGITUDE, result);

    if (result[0] <= 100)
    {
      displayNotification();
    }
  }

  private void displayNotification()
  {
    final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
    final String notificationChannelId = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ? "MyChannel" : null;

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
    {
      final NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, "My Channel", NotificationManager.IMPORTANCE_HIGH);
      notificationManagerCompat.createNotificationChannel(notificationChannel);
    }

    final Intent intent = new Intent(MainActivity.this, MainActivity.class);
    final PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this, notificationChannelId);
    notificationBuilder.setContentTitle(getString(R.string.notification_title));
    notificationBuilder.setContentText(getString(R.string.notification_message));
    notificationBuilder.setSmallIcon(R.drawable.ic_baseline_local_convenience_store_24);
    notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
    notificationBuilder.setAutoCancel(true);
    notificationBuilder.setChannelId(notificationChannelId);
    notificationBuilder.setContentIntent(pendingIntent);

    notificationManagerCompat.notify(1, notificationBuilder.build());
  }

  private void updateList(Location location)
  {
    userLocations.add(location);
    recyclerView.setAdapter(new LocationAdapter(userLocations));
  }

  private void displayAlertDialog()
  {
    final Builder builder = new Builder(this);
    builder.setTitle(android.R.string.dialog_alert_title);
    builder.setMessage(R.string.permissions);
    builder.setPositiveButton(android.R.string.ok, null);

    builder.show();
  }

  private void trackPermissionCheck()
  {
    Log.d(MainActivity.TAG, "Checking for permission");

    if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    {
      Log.d(MainActivity.TAG, "Permission granted");
      trackLocation();
    }
    else
    {
      Log.d(MainActivity.TAG, "Permission not granted");
      ActivityCompat.requestPermissions(this, new String[] { permission.ACCESS_COARSE_LOCATION }, MainActivity.PERMISSION_REQUEST_CODE);
    }
  }

  @RequiresPermission(permission.ACCESS_COARSE_LOCATION)
  private void trackLocation()
  {
    final LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(10_000);
    locationRequest.setFastestInterval(5_000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    Log.d(MainActivity.TAG, "Request location updates");
    fusedLocationProviderClient.flushLocations();
    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
  }

}