# AboutAppDialogFragment
This is a dialog that pops up giving the user some key information pertaining to the current app. It displays the version
name, a few bits of copyright info, and allows links to be set. This information is displayed in a webview inside of a dialog.

### Dependencies
<a href="https://github.com/afollestad/material-dialogs">`compile 'com.afollestad:material-dialogs:0.7.6.0'`</a>

### Customization
The exact dialog that is placed in the dialog can be set by modifying the class in its `onCreateDialog` method.

`String data = VERSION +"\<br\>Developed by Felker Tech - 2015\<br\>" + ...`

### Displaying
This dialog can be displayed by a method call in an activity on a specific action, like choosing the "About" option in the overflow menu.

    DialogFragment newFragment = new AboutAppDialogFragment();
    newFragment.show(getFragmentManager(), "missiles");
    
### TODO
* Migrate to a native layout to prevent load lagging
* Migrate text to a string to make it easier to change with different parameters

# ApplicationSettings
This activity includes a fragment which can be used to show preferences, giving the developer and user a consistent behavior and interface for configuring the app.

### Dependencies

* <a href="https://github.com/afollestad/material-dialogs">`compile 'com.afollestad:material-dialogs:0.7.6.0'`</a>
* <a href="https://github.com/jenzz/Android-MaterialPreference">`compile 'com.jenzz:materialpreference:1.3''`</a>
* SettingsManager

The app is designed to work with both Material Dialogs and Material Preferences to give an interface that looks consistent across all devices. There are several layouts that are included (layout, layout-720, layout-960) for a pleasant interface on larger screen sizes.

### Usage
This uses the normal xml method for adding settings, but integrates with these libraries using a few helper methods.

* `void bindSummary(int preference_key, int preference_type)` - Opens up an applicable dialog and updates the preference summary with the result. Works with `EDIT_TEXT_PREF` and `LIST_PREF`.
* `void enablePreference(int preference_key, int bool_id)` - Checks a boolean shared preference and enables or disables a preference based on the result
* `void bindAbout(int preference_key)` - Sets the summary of the given preference to be the version name and code. Optionally you can tap this preference a few times to enable some sort of developer or debug mode, which can be tied to enabling and disabling preferences.

# AppUtils
This class is a conglomeration of methods that I've found useful.

### Methods
* `boolean isWearEnabled(Context)` - Determines whether Android Wear is enabled on that device
* `boolean isTV(Context)` - Determines whether this device is a TV

### Usage
Both of these methods are static, so you can call them directly:

    if(AppUtils.isTV(getApplicationContext)) {
      Log.d(TAG, "This is a TV!");
    else
      Log.d(TAG, "This isn't a TV!");
      
# ConnectionUtils
This class gives a few helper methods to using the DataMessaging APIs on Android Wear, so I don't need to keep writing the same code.

### NodeManager
First, you want to create a new NodeManager (usage: `new ConnectionUtils.NodeManager(GoogleApiClient, String CAPABILITY, String PATH)`

* CAPABILITY - The capability of the node that you want to message (will expand on this in the future)
* PATH - The URI that you want to send something to

This starts a service that will run for the duration of your app that scans for nodes fulfilling the given capability and then allows you to send messages to it with `sendMessage(String)`.

You'll need to override `public void onMessageReceived(MessageEvent messageEvent)` either in your activity, or in a separate class extending `WearableListenerService` like so:

    <service android:name=".Services.MessageListener">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.BIND_LISTENER" />
            </intent-filter>
        </service>
        
There are other static methods.
* `sendLaunchCommand(GoogleApiClient)` sends a message to every node telling it to launch an activity. This does need to be tied to the listener serivce like so:


     if (messageEvent.getPath().equals(LAUNCHER_PATH)) {
        Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent p = PendingIntent.getActivity(this, 0, startIntent, 0);
        try {
            p.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
     }
    
* `sendData(GoogleApiClient, String name, String type, Object value)` sends a data update to all nodes. This can be done by here or using the SettingsManager class.
    * name - Variable name to use on both sides for logistics
    * type - Object type (`boolean` or `int`)
    * value - The value you want to send

# SettingsManager
The SettingsManager is a class that acts as an interface to SharedPreferences making it easy to execute a variety of commands to modify user data.

### Constructor
This can be initiated in a few ways.
`new SettingsManager(Context)`
`new SettingsManager(Activity)`

### Getters
There are a few different methods you can use for querying data.
`getString(resId/String)`
`getBoolean(resId/String)`
`getInt(resId/String)`
`getLong(resId/String)`


Each of these methods can take either a string directly, or a resource id to a string. They'll get the object in SharedPreferences and return it in the specified type (if that type is applicable).

There are also setters for each of these types. Calling `setString(resId/String, String)` sets the content of the key to the specified value.

### Wearable Integration
The SettingsManager also makes it easy to sync data to a Wearable so both devices are up-to-date.
`setSyncableSettingsManager(GoogleApiClient, null)` - You'll need to specify `null` as the second parameter as it is meant to be used for an unused interface.

Once you link the `GoogleApiClient` to the `SettingsManager`, you are able to use new methods to send and receive data.

* `pushData()` - Sends all of the SharedPreference data to the other devices
* `pullData(DataEventBuffer)` - You can easily call this method when you override the `public void onDataChanged(DataEventBuffer dataEvents)`. Provide that argument in the SettingsManager (no need to add the GoogleApiClient), and each datum will be stored in the SharedPreferences of the other devices.

As these methods use the DataMap API, it'll work across one phone and all your wearables seamlessly.

#### Note
When using this class on a Wearable, make sure you use the Wearable version of the class and not the phone version. They are slightly different but with small API changes.
