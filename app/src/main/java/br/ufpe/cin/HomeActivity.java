package br.ufpe.cin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.iotivity.base.ObserveType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufpe.cin.config.Constants;
import br.ufpe.cin.config.IoTivityConfigurer;
import br.ufpe.cin.resources.AbstractResource;

import static android.R.attr.button;

public class HomeActivity extends AppCompatActivity implements OcPlatform.OnResourceFoundListener, View.OnClickListener{

    private static final String TAG = HomeActivity.class.getName();
    private String mHost = Constants.DEFAULT_CLOUD_HOST + "192.168.25.126:5683";
    private OcResource mResource;
    private IoTivityConfigurer mConfigurer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        start();

        Button btn = (Button) findViewById(R.id.clickMe);
        btn.setOnClickListener(this);
    }


    private void start(){
        try {
            mConfigurer = new IoTivityConfigurer(mHost, () -> {
                    try {
                        Log.i(TAG, "Finding resources");
                        findResources();
                    }
                    catch(Exception e){
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
            });

            mConfigurer.configurePlatform(this.getApplicationContext());

        }
        catch(Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void findResources() throws OcException {
        OcPlatform.findResource(mHost,
                "/oic/res",
                EnumSet.of(OcConnectivityType.CT_ADAPTER_TCP,
                           OcConnectivityType.CT_IP_USE_V4),
                this);
    }


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
                                            values = ((OcRepresentation) value).getValues();
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

    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "clicou");
        try {
            mConfigurer.signIn("33f4c2e7d164158a35c0");
        } catch (OcException e) {
            e.printStackTrace();
        }
    }


    public class SwitchObserver implements OcResource.OnObserveListener {

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

        public class SwitchFinder implements OcPlatform.OnResourceFoundListener,
                OcResource.OnGetListener {

            @Override
            public void onResourceFound(OcResource ocResource) {
                List<String> resourceTypes = ocResource.getResourceTypes();
                Log.i(TAG, String.format("Binary Switch resources found: %s", ocResource.getUri()));
                for (String type : resourceTypes) {
                    if (type.equals("x.org.iotivity.bs")) {
                        Log.i(TAG, String.format("Found Binary Switch", type));
                        Map<String, String> query = new HashMap<>();
                        query.put("if", OcPlatform.LINK_INTERFACE);
                        try {
                            ocResource.get(query, this);
                        } catch (OcException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFindResourceFailed(Throwable throwable, String s) {
                throwable.printStackTrace();
                System.exit(1);
            }

            @Override
            public void onGetCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
                Log.i(TAG, "Connected!");
                String resourceUri = ocRepresentation.getUri();
                try {
                    mResource = OcPlatform.constructResourceObject(mHost, resourceUri,
                            EnumSet.of(OcConnectivityType.CT_ADAPTER_TCP, OcConnectivityType.CT_IP_USE_V4),
                            true,
                            Collections.singletonList("x.org.iotivity.bs"),
                            Collections.singletonList(OcPlatform.DEFAULT_INTERFACE));
                    mResource.observe(ObserveType.OBSERVE, new HashMap<>(),
                            new SwitchObserver());



                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            @Override
            public void onGetFailed(Throwable throwable) {
                throwable.printStackTrace();
                System.exit(1);
            }
        }
}
