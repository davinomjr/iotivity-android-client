package br.ufpe.cin.account;

import android.util.Log;

import org.iotivity.base.OcAccountManager;
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcRepresentation;

import java.util.List;

import br.ufpe.cin.util.Provider;

/**
 * Created by davinomjr on 8/17/17.
 */

public class SignUpManager {

    private static final String TAG = SignUpManager.class.getName();
    private String mAccessToken;
    private String mProvider;
    private OcAccountManager mManager;

    public SignUpManager(OcAccountManager manager, Provider provider, String accessToken) {
        this.mManager = manager;
        this.mProvider = provider.toString().toLowerCase();
        this.mAccessToken = accessToken;

    }

    public void signUp(SignUpListener listener) throws OcException {
        this.mManager.signUp(mProvider, this.mAccessToken, new OcAccountManager.OnPostListener() {
            @Override
            public void onPostCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
                try {
                    Log.i(TAG, "signUp post completed");
                    String accessToken = ocRepresentation.getValue("accesstoken");
                    String uid = ocRepresentation.getValue("uid");
                    listener.onCompleted(accessToken, uid);
                }catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    listener.onFailed(e);
                }
            }

            @Override
            public void onPostFailed(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
                listener.onFailed(throwable);
            }
        });
    }

    public interface SignUpListener {
        void onCompleted(String accessToken, String uid);
        void onFailed(Throwable throwable);
    }


    public enum Provider {
        GITHUB,
        GOOGLE
    }
}

