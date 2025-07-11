/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 16 May 2024 09:56:21 +0100
 * @copyright  Copyright (c) 2024 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2024 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Thu, 16 May 2024 09:38:17 +0100
 */
package com.streamwide.smartms.altbeacon.beacon;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import androidx.annotation.NonNull;

/**
 * An interface for an Android <code>Activity</code> or <code>Service</code>
 * that wants to interact with beacons.  The interface is used in conjunction
 * with <code>BeaconManager</code> and provides a callback when the <code>BeaconService</code>
 * is ready to use.  Until this callback is made, ranging and monitoring of beacons is not
 * possible.
 * <p>
 * In the example below, an Activity implements the <code>BeaconConsumer</code> interface, binds
 * to the service, then when it gets the callback saying the service is ready, it starts ranging.
 *
 * <pre><code>
 *  public class RangingActivity extends Activity implements BeaconConsumer {
 *      protected static final String TAG = "RangingActivity";
 *      private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
 *      {@literal @}Override
 *      protected void onCreate(Bundle savedInstanceState) {
 *          super.onCreate(savedInstanceState);
 *          setContentView(R.layout.activity_ranging);
 *          beaconManager.bind(this);
 *      }
 *
 *      {@literal @}Override
 *      protected void onDestroy() {
 *          super.onDestroy();
 *          beaconManager.unbind(this);
 *      }
 *
 *      {@literal @}Override
 *      public void onBeaconServiceConnect() {
 *          beaconManager.setRangeNotifier(new RangeNotifier() {
 *            {@literal @}Override
 *            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
 *                 if (beacons.size() > 0) {
 *                      Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
 *                 }
 *            }
 *          });
 *
 *          try {
 *              beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
 *          } catch (RemoteException e) {
 *              e.printStackTrace();
 *          }
 *      }
 *  }
 *  </code></pre>
 *
 * @author David G. Young
 * @see BeaconManager
 */
public interface BeaconConsumer {

    /**
     * Called when the beacon service is running and ready to accept your commands through the BeaconManager
     */
    public void onBeaconServiceConnect();

    /**
     * Called by the BeaconManager to get the context of your Service or Activity.  This method is implemented by Service or Activity.
     * You generally should not override it.
     *
     * @return the application context of your service or activity
     */
    @NonNull
    public Context getApplicationContext();

    /**
     * Called by the BeaconManager to unbind your BeaconConsumer to the  BeaconService.  This method is implemented by Service or Activity, and
     * You generally should not override it.
     *
     * @return the application context of your service or activity
     */
    public void unbindService(@NonNull ServiceConnection connection);

    /**
     * Called by the BeaconManager to bind your BeaconConsumer to the  BeaconService.  This method is implemented by Service or Activity, and
     * You generally should not override it.
     *
     * @return the application context of your service or activity
     */
    public boolean bindService(@NonNull Intent intent, @NonNull ServiceConnection connection, int mode);
}
