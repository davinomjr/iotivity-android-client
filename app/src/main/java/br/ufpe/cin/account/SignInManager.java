package br.ufpe.cin.account;

import android.util.Log;

import org.iotivity.base.OcAccountManager;
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcRepresentation;

import java.util.List;

/**
 * Created by davinomjr on 8/17/17.
 */

public class SignInManager {

    private static final String TAG = SignInManager.class.getName();
    private String mAccessToken;
    private String mUid;
    private OcAccountManager mManager;

    public SignInManager(OcAccountManager manager, String accessToken, String uid) {
        this.mManager = manager;
        this.mUid = uid;
        this.mAccessToken = accessToken;

    }

    public void signIn(final SignInListener listener) throws OcException {
        this.mManager.signIn(this.mUid, this.mAccessToken, new OcAccountManager.OnPostListener() {
            @Override
            public void onPostCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
                try {
                    listener.onCompleted();
                }catch (Exception e) {
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

    public interface SignInListener {
        void onCompleted();
        void onFailed(Throwable throwable);
    }
}