Cyclo
=====

Cyclo Location Recording Service


What is?
--------

Cyclo is a SDK contains a service which does record current location.

Any app can use CycloService for recording location by simply using CycloConnector class.

CycloService is a Messenger Service, so you don't need to configure complexly.

How to?
-------

1. Build this project by Android Studio.
1. Install app.(Dashboard)
1. Copy CycloConnector.java file to your project.
1. Use it in your activity. (See DashboardActivity.java)
1. You can control the service with CycloConnector.
1. You can receive locations through Broadcast Receiver.
1. You can also receive status and locations through CycloConnector.StatusListener class


Synopsis
--------

```java

  CycloConnector gpsConn;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);
    
    gpsConn = new CycloConnector(this, new CycloConnector.StatusListener() {
      @Override
      public void onReceiveStatus(Bundle bundle) {
      
        // bundle comes from Messenger(a kind of Handler).
        
        int state = bundle.getInt("state", CycloConnector.STATE_STOPPED);
        Log.d(TAG, "state : " + state);
        switch (state) {
          case CycloConnector.STATE_STOPPED:
              break;
          case CycloConnector.STATE_STARTED:
              break;
          case CycloConnector.STATE_PAUSED:
              break;
        }
      }
    
      @Override
      public void onReceiveUpdate(Bundle bundle) {
      
        // bundle comes from BroadcastReceiver created programmatically.
        
        // Extendable various messages
        String type = bundle.getString("type");
        
        // Just focus at Location!!
        Location location = bundle.getParcelable("location");
        if (location != null) {
            // do something with Location here.
        }
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    gpsConn.bindService();
  }

  @Override
  protected void onStop() {
    super.onStop();
    gpsConn.unbindService();
  }
  
  public void onClickStart(View view){
    gpsConn.start();
    // Even there are more methods to control service - stop(), pause(), resume() and updateProfile().
  }
  
```

Recording in background
-----------------------

If you want to receive Locations when your app is background, 

1. CREATE CycloConnector in your `LOCAL SERVICE` as foreground.
1. CREATE explicit `BroadcastReceiver` which is descripted in AndroidManifest.xml, 
  and use CycloConnector(context, statusListener, broadcastAction) constructor.
  It is required to use same `ACTION` string both like below.

```xml
<receiver
    android:name="YOUR_RECEIVER">
    <intent-filter>
        <action android:name="YOUR_BROADCAST_ACTION" />
    </intent-filter>
</receiver>
```

```java
gpsConn = new CycloConnector(this, statusListener, "YOUR_BROADCAST_ACTION");
```

Then, CycloService will send broadcast with `YOUR_BROADCAST_ACTION` action containing Locations.
But BroadcastReceiver has a big problem that it has not enough time to process something.
_I don't recommend it_.

Profile
-------

...I will be back...

Controlling CycloService
------------------------

...I will be back...

About control-lock for owned app
--------------------------------

...I will be back...


To do
-----

* Enhance gathering location without huge bettery drain.
* Calculate statistics for speed, distance and duration.
* Detect break-time.

I need your help.
