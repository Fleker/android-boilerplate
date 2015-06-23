package com.felkertech.n.weatherdelta.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.felkertech.n.weatherdelta.R;

/**
 * Created by N on 9/18/2014.
 * Last modified 25/5/2015
 *   Move over to MaterialDialog library
 */
public class AboutAppDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String VERSION = "";
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            VERSION = "Version "+pInfo.versionName;
        } catch(Exception e) {

        }
        // Use the Builder class for convenient dialog construction
        String data = VERSION +"<br>" +
                "Developed by Felker Tech - 2015<br>" +
                "Follow the developer <a href='http://www.twitter.com/handnf'>@HandNF</a> or on <a href='https://plus.google.com/+NickFelker/'>Google+</a><br><br>" +
                "<a href='http://forecast.io'>Powered by Forecast</a><br>" +
                "<br>" +
                "Want to test out new versions? <a href='https://plus.google.com/communities/102039422936690861345'>Become a beta tester!</a>";
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.about_dialog, null);
        ((WebView) ll.findViewById(R.id.about_dialog_webview)).loadData(data, "text/html", null);
        ((WebView) ll.findViewById(R.id.about_dialog_webview)).setPadding(16,4,16,4);

        builder.customView(ll, false)
                .title("About " + getString(R.string.app_name));
        return builder.build();
    }
}