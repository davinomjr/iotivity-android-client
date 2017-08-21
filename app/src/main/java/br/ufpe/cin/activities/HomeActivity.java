package br.ufpe.cin.activities;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import org.iotivity.base.ObserveType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufpe.cin.R;
import br.ufpe.cin.adapters.ResourceListAdapter;
import br.ufpe.cin.config.Constants;
import br.ufpe.cin.config.IoTivityConfigurer;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = HomeActivity.class.getName();
    private String mHost = Constants.DEFAULT_CLOUD_HOST + "192.168.25.126:5683";
    private final String requestTokenUrl = "https://github.com/login?return_to=%2Flogin%2Foauth%2Fauthorize%3Fclient_id%3Dea9c18f540323b0213d0%26redirect_uri%3Dhttp%253A%252F%252Fwww.example.com%252Foauth_callback%252F";
    private OcResource mResource;
    private IoTivityConfigurer mConfigurer;
    private Button mConnectBtn, mFindResourcesBtn;
    private String mToken;
    private ResourceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<String> mResourcesFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewById(R.id.connect_btn).setOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mResourcesFound = new ArrayList<>();
    }

    public void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void startController(){
        try {
            startLoading();
            findViewById(R.id.connect_btn).setVisibility(View.GONE);
            mConfigurer = new IoTivityConfigurer(mHost, () -> {
                stopLoading();
                showSnackBar("Connected!");

                runOnUiThread(() -> {
                    findViewById(R.id.find_resources_btn).setVisibility(View.VISIBLE);
                    findViewById(R.id.find_resources_btn).setOnClickListener(this);
                });
            });

            mConfigurer.configurePlatform(this.getApplicationContext());
            mConfigurer.signIn(mToken);
        }
        catch(Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void startLoading(){
        runOnUiThread(() -> {
            ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
            progress.setVisibility(View.VISIBLE);
        });
    }

    private void stopLoading(){
        runOnUiThread(() -> {
            ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
            progress.setVisibility(View.INVISIBLE);
        });
    }

    private void findResources()  {
        Log.i(TAG, "Finding resources");
        startLoading();
        try {
            OcPlatform.findResource(mHost,
                    "/oic/res",
                    EnumSet.of(OcConnectivityType.CT_ADAPTER_TCP,
                            OcConnectivityType.CT_IP_USE_V4),
                    new ResourceListener());
        }
        catch(OcException ex){
            ex.printStackTrace();
        }
    }


    private void setupRecyclerview(View recycView, ArrayList<String> values) {
        mRecyclerView  = (RecyclerView) recycView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ResourceListAdapter(this, values);
        mRecyclerView.setAdapter(mAdapter);
    }



    private void getAuthorizationToken(){
        WebView webView = new WebView(getApplicationContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, url);
                if(url.contains("/?code=")){ // got redirect
                    mToken = url.substring(url.indexOf("=") + 1, url.length());
                    Log.i(TAG, String.format("Got token: %s", mToken));
                    setContentView(R.layout.activity_home);
                    stopLoading();
                    startController();
                }

                super.onPageFinished(view, url);
            }


        });

        webView.loadUrl(requestTokenUrl);
        setContentView(webView);
    }

    private void connectProvider(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.auth_dialog_message)
                .setTitle(R.string.auth_dialog_title)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    startLoading();
                    getAuthorizationToken();
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                });

        builder.create().show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.connect_btn:
                connectProvider();
                break;
            case R.id.find_resources_btn:
                findResources();
                break;
        }
    }


    private class ResourceListener implements OcPlatform.OnResourceFoundListener{
        @Override
        public void onResourceFound(OcResource ocResource) {
            List<String> resourceTypes = ocResource.getResourceTypes();
            Log.i(TAG, "Device found: " + ocResource.getUri());
            Log.i(TAG, "Server ID: " + ocResource.getServerId());
            String searchQuery = OcPlatform.WELL_KNOWN_QUERY + "?di=";
            for (String type : resourceTypes) {
                Log.i(TAG, "Found: " + type);
                if (type.equals("oic.d.bswitch")) {
                    String query = searchQuery + ocResource.getServerId();
                    try {
                        OcPlatform.findResource(mHost, query,
                                EnumSet.of(OcConnectivityType.CT_ADAPTER_TCP,
                                        OcConnectivityType.CT_IP_USE_V4),
                                new SwitchFinder());

                        OcPlatform.subscribeDevicePresence(mHost,
                                Collections.singletonList(ocResource.getServerId()),
                                EnumSet.of(OcConnectivityType.CT_ADAPTER_TCP, OcConnectivityType.CT_IP_USE_V4),
                                new OcResource.OnObserveListener() {
                                    @Override
                                    public void onObserveCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation, int i) {
                                        Map<String, Object> values = ocRepresentation.getValues();
                                        Log.i(TAG, "Observe result: ");
                                        for (String key : values.keySet()) {
                                            Object value = values.get(key);
                                            Log.i(TAG, String.format("Key: %s, Value: %s", key, value));
                                            if (value instanceof OcRepresentation) {
                                                values = ((OcRepresentation)value).getValues();
                                                Log.i(TAG, "Key: " + key);
                                                values.forEach((k, v) -> Log.i(TAG, String.format("Key: %s, Value: %s", k, v)));
                                            }
                                        }
                                    }

                                    @Override
                                    public void onObserveFailed(Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                });

                    } catch (OcException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }

        @Override
        public void onFindResourceFailed(Throwable throwable, String s) {
            Log.e(TAG, s);
        }

    }

    private class SwitchFinder implements OcPlatform.OnResourceFoundListener, OcResource.OnGetListener {
        @Override public void onResourceFound(OcResource ocResource) {List<String> resourceTypes = ocResource.getResourceTypes();String resourceUri = ocResource.getUri();Log.i(TAG, String.format("Binary Switch resources found: %s", resourceUri));
            mResourcesFound.add(resourceUri);
            for (String type : resourceTypes) {if (type.equals("x.org.iotivity.bs")) {
                Log.i(TAG, String.format("Found Binary Switch", type));
                Map<String, String> query = new HashMap<>();
                query.put("if", OcPlatform.LINK_INTERFACE);
                try {
                    ocResource.get(query, this);
                } catch (OcException e) {
                    e.printStackTrace();
                }
            }}}
        @Override public void onFindResourceFailed(Throwable throwable, String s) {throwable.printStackTrace();System.exit(1);}
        @Override public void onGetCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {Log.i(TAG, "Connected!");
            runOnUiThread(() -> {showSnackBar("Resources found!");stopLoading();RecyclerView recyclerView = (RecyclerView)findViewById(R.id.rv);setupRecyclerview(recyclerView, mResourcesFound);});
            String resourceUri = ocRepresentation.getUri();try {mResource = OcPlatform.constructResourceObject(mHost, resourceUri,
                    EnumSet.of(OcConnectivityType.CT_ADAPTER_TCP, OcConnectivityType.CT_IP_USE_V4),
                    true,
                    Collections.singletonList("x.org.iotivity.bs"),
                    Collections.singletonList(OcPlatform.DEFAULT_INTERFACE));
                mResource.observe(ObserveType.OBSERVE, new HashMap<>(), new SwitchObserver());} catch (Exception e) {e.printStackTrace();System.exit(1);}}
        @Override public void onGetFailed(Throwable throwable) {throwable.printStackTrace();System.exit(1);}}



    private class SwitchObserver implements OcResource.OnObserveListener {

        @Override
        public void onObserveCompleted(List<OcHeaderOption> list,
                                       OcRepresentation ocRepresentation,
                                       int i) {
            try {
                Log.i(TAG, String.format("Switch state changed: %s", ocRepresentation.getValue("value")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onObserveFailed(Throwable throwable) {

        }
    }
}
