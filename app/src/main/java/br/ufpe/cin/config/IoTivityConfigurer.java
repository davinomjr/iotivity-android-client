package br.ufpe.cin.config;

import android.content.Context;
import android.util.Log;

import org.iotivity.base.ModeType;
import org.iotivity.base.OcAccountManager;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;

import java.util.EnumSet;

import br.ufpe.cin.account.SignInManager;
import br.ufpe.cin.account.SignUpManager;
import br.ufpe.cin.util.Provider;

/**
 * Created by davinomjr on 8/17/17.
 */

public class IoTivityConfigurer implements SignUpManager.SignUpListener, SignInManager.SignInListener {

    private static final String TAG = IoTivityConfigurer.class.getName();
    private String mHost;
    private OcAccountManager mAccountManager;
    private OnSignInCompleted mListener;

    public IoTivityConfigurer(String host, OnSignInCompleted listener) {
        this.mHost = host;
        this.mListener = listener;
    }

    public void configurePlatform(Context context) {
        Log.i(TAG, "Configuring...");

        PlatformConfig config = new PlatformConfig(
                context,
                ServiceType.IN_PROC,
                ModeType.CLIENT_SERVER,
                "0.0.0.0",
                0,
                QualityOfService.LOW
        );

        OcPlatform.Configure(config);
    }

    public void signIn(String accessToken) throws OcException {
        mAccountManager = OcPlatform
                .constructAccountManagerObject(
                        this.mHost,
                        EnumSet.of(OcConnectivityType.CT_ADAPTER_TCP)
                );

        SignUpManager manager = new SignUpManager(mAccountManager, SignUpManager.Provider.GITHUB, accessToken);

        Log.i(TAG, "Signing up...");
        manager.signUp(this);
    }

    @Override
    public void onCompleted() {
        Log.i(TAG, "Signed up!");
        mListener.onSignedIn();
    }

    @Override
    public void onCompleted(String accessToken, String uid) {
        SignInManager manager = new SignInManager(mAccountManager, accessToken, uid);
        try {
            Log.i(TAG, "Signing in...");
            manager.signIn(this);
        } catch (Exception e) {
            onFailed(e);
        }
    }

    @Override
    public void onFailed(Throwable throwable) {
        Log.e(TAG, throwable.getMessage());
        throwable.printStackTrace();
        System.exit(1);
    }

    public interface OnSignInCompleted {
        void onSignedIn();
    }
}
