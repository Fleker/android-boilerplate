package com.felkertech.n.weatherdelta.micro;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by N on 5/29/2015.
 */
public class ConnectionUtils {

    /*
        NODE MANAGER
        These methods allow you to easily create listeners to handle finding nodes of a given capability
        Each module also requires a `values/wear.xml` which specifies the capabilities of said module.
        Specify which capability you're seeking for the given node and you're set.
        Below is an example of a capability file

        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="CAPABILITY_MOBILE">MOBILE</string>
            <string-array name="android_wear_capabilities">
                <item>@string/CAPABILITY_MOBILE</item>
            </string-array>
        </resources>

     */
    public static class NodeManager {
        private String transcriptionNodeId = null;
        private GoogleApiClient mGoogleApiClient;
        private String CAPABILITY;
        private String PATH;
        public NodeManager(final GoogleApiClient mGoogleApiClient, String CAPABILITY, String PATH) {
            this.mGoogleApiClient = mGoogleApiClient;
            this.CAPABILITY = CAPABILITY;
            this.PATH = PATH;
            setupNode();
        }
        private void setupNode() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CapabilityApi.GetCapabilityResult result =
                            Wearable.CapabilityApi.getCapability(
                                    mGoogleApiClient, CAPABILITY,
                                    CapabilityApi.FILTER_REACHABLE).await();
                    updateTranscriptionCapability(result.getCapability());

                    CapabilityApi.CapabilityListener capabilityListener =
                            new CapabilityApi.CapabilityListener() {
                                @Override
                                public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                                    updateTranscriptionCapability(capabilityInfo);
                                }
                            };

                    Wearable.CapabilityApi.addCapabilityListener(
                            mGoogleApiClient,
                            capabilityListener,
                            CAPABILITY);
                }
            }).start();
        }
        private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
            Set<Node> connectedNodes = capabilityInfo.getNodes();

            transcriptionNodeId = pickBestNodeId(connectedNodes);
        }
        private String pickBestNodeId(Set<Node> nodes) {
            String bestNodeId = null;
            // Find a nearby node or pick one arbitrarily
            for (Node node : nodes) {
                if (node.isNearby()) {
                    return node.getId();
                }
                bestNodeId = node.getId();
            }
            return bestNodeId;
        }
        public void sendMessage(String msg) {
            byte[] voiceData = msg.getBytes();
            if (transcriptionNodeId != null) {
                Wearable.MessageApi.sendMessage(mGoogleApiClient, transcriptionNodeId,
                        PATH, voiceData).setResultCallback(
                        new ResultCallback() {
                            @Override
                            public void onResult(Result result) {
                                MessageApi.SendMessageResult sendMessageResult = (MessageApi.SendMessageResult) result;
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    // Failed to send message
                                }
                            }
                        }
                );
            } else {
                // Unable to retrieve node with transcription capability
            }
        }
        private Collection<String> getNodes() {
            HashSet<String> results = new HashSet<String>();
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {
                results.add(node.getId());
            }
            return results;
        }
    }

    public static void sendLaunchCommand(final GoogleApiClient mGoogleApiClient) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for(Node node : nodes.getNodes()) {
                    Log.i("test", "pinging to: " + node.getDisplayName());
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/messageapi/launcher", "Hello World".getBytes()).await();
                    if(!result.getStatus().isSuccess()){
                        Log.e("test", "error");
                    } else {
                        Log.i("test", "success!! sent to: " + node.getDisplayName());
                    }
                }
            }
        }).start();
    }
    public static void sendData(GoogleApiClient mGoogleApiClient, String name, String type, Object value) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/dataitem/data");
        if(type.equals("boolean"))
            dataMap.getDataMap().putBoolean(name, (Boolean) value);
        if(type.equals("int"))
            dataMap.getDataMap().putInt(name, (Integer) value);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, request);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d("TAG", "onResult: " + dataItemResult.getStatus().toString());
            }
        });
    }
}
