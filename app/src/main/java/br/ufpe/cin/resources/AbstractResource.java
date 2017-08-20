package br.ufpe.cin.resources;

import android.util.Log;

import org.iotivity.base.EntityHandlerResult;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResourceHandle;
import org.iotivity.base.OcResourceRequest;
import org.iotivity.base.OcResourceResponse;
import org.iotivity.base.RequestHandlerFlag;
import org.iotivity.base.RequestType;

import java.util.EnumSet;
import java.util.List;

import br.ufpe.cin.devices.BinarySwitch;

/**
 * Created by davinomjr on 8/17/17.
 */

public abstract class AbstractResource implements OcPlatform.EntityHandler {

    private static final String TAG = AbstractResource.class.getName();
    protected OcResourceHandle mHandle;
    private String mResourceName;
    protected OcRepresentation mRepresentation;

    protected abstract EntityHandlerResult init();
    protected abstract EntityHandlerResult getResourceRepresentation(OcResourceRequest request);
    protected abstract EntityHandlerResult setResourceRepresentation(OcResourceRequest request);


    public AbstractResource(String uri, List<String> resourceTypes, List<String> resourceInterfaces, String resourceName) {
        this.mResourceName = resourceName;
        mRepresentation = new OcRepresentation();
        mRepresentation.setResourceTypes(resourceTypes);
        mRepresentation.setResourceTypes(resourceInterfaces);
        mRepresentation.setUri(uri);
    }

    @Override
    public EntityHandlerResult handleEntity(OcResourceRequest ocResourceRequest) {
        Log.i(TAG, String.format("Server %s entity handler", mResourceName));
        EntityHandlerResult ehResult = EntityHandlerResult.ERROR;
        if (ocResourceRequest == null) {
            return ehResult;
        }

        EnumSet<RequestHandlerFlag> requestFlags = ocResourceRequest.getRequestHandlerFlagSet();
        if (requestFlags.contains(RequestHandlerFlag.INIT)) {
            Log.i(TAG, "Initializing resources...");
            ehResult = this.init();
        }

        if (requestFlags.contains(RequestHandlerFlag.REQUEST)) {
            ehResult = handleRequest(ocResourceRequest);
        }

        return ehResult;
    }


    protected void sendResponse(OcResourceResponse response) {
        try {
            OcPlatform.sendResponse(response);
        } catch (OcException e) {
            Log.i(TAG, String.format("Error sending response: %s", e.toString()));
            e.printStackTrace();
        }
    }


    protected EntityHandlerResult handleRequest(OcResourceRequest ocResourceRequest) {
        EntityHandlerResult ehResult = EntityHandlerResult.ERROR;
        RequestType requestType = ocResourceRequest.getRequestType();
        Log.i(TAG, String.format("Got request: %s",requestType));
        switch (requestType) {
            case GET:
                ehResult = this.getResourceRepresentation(ocResourceRequest);
                break;
            case PUT:
            case POST:
                ehResult = this.setResourceRepresentation(ocResourceRequest);
                break;
            case DELETE:
                break;
        }
        return ehResult;
    }

    public OcResourceHandle getHandle() {
        return mHandle;
    }
}