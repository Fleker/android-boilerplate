## AboutAppDialogFragment
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

This starts a 
