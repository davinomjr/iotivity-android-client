package br.ufpe.cin.resources;

import android.util.Log;

import org.iotivity.base.EntityHandlerResult;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResourceRequest;
import org.iotivity.base.OcResourceResponse;
import org.iotivity.base.ResourceProperty;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import br.ufpe.cin.devices.BinarySwitch;

/**
 * Created by davinomjr on 8/17/17.
 */

public class BinarySwitchResource extends AbstractResource implements br.ufpe.cin.interfaces.Resource {

    private static final String TAG = BinarySwitchResource.class.getName();
    private final static String mResourceName = "Binary Switch";
    private BinarySwitch mBinarySwitch;
    private String mResourceType;
    private List<String> mInterfaces;

    public BinarySwitchResource(String uri, List<String> resourceTypes, List<String> resourceInterfaces, BinarySwitch binarySwitch) {
        super(uri, resourceTypes, resourceInterfaces, mResourceName);
        mResourceType = resourceTypes.get(0);
        mInterfaces = resourceInterfaces;
        this.mBinarySwitch = binarySwitch;
    }

    public void registerResource() throws OcException {
        if (mHandle == null) {
            mHandle = OcPlatform.registerResource(
                    mRepresentation.getUri(),
                    mResourceType,
                    mInterfaces.get(0),
                    this,
                    EnumSet.of(ResourceProperty.DISCOVERABLE, ResourceProperty.OBSERVABLE)
            );
            OcPlatform.bindInterfaceToResource(mHandle, mInterfaces.get(1));
            OcPlatform.bindInterfaceToResource(mHandle, mInterfaces.get(2));
        }
    }

    public void unregisterResource() throws OcException {
        if (mHandle != null) {
            OcPlatform.unregisterResource(mHandle);
        }
    }

    @Override
    protected EntityHandlerResult getResourceRepresentation(OcResourceRequest request) {
        EntityHandlerResult result = EntityHandlerResult.OK;

        OcResourceResponse response = new OcResourceResponse();
        response.setRequestHandle(request.getRequestHandle());
        response.setResourceHandle(request.getResourceHandle());

        Map<String, String> query = request.getQueryParameters();
        try {
            if (query.containsKey("if") && query.get("if").equals(OcPlatform.LINK_INTERFACE)) {
                OcRepresentation rep = new OcRepresentation();
                rep.addChild(mBinarySwitch.serialize());
                response.setResourceRepresentation(rep, OcPlatform.LINK_INTERFACE);
                response.setResponseResult(result);
                sendResponse(response);
            } else {
                result = EntityHandlerResult.FORBIDDEN;
            }
        } catch (Exception e) {
            result = EntityHandlerResult.ERROR;
        }
        return result;
    }

    @Override
    protected EntityHandlerResult setResourceRepresentation(OcResourceRequest request) {
        EntityHandlerResult result = EntityHandlerResult.OK;

        OcResourceResponse response = new OcResourceResponse();
        response.setRequestHandle(request.getRequestHandle());
        response.setResourceHandle(request.getResourceHandle());

        OcRepresentation rep = request.getResourceRepresentation();
        try {
            mBinarySwitch.setValue((boolean) rep.getValue("value"));
        } catch (OcException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        response.setResponseResult(result);
        sendResponse(response);
        return result;
    }

    @Override
    public EntityHandlerResult init() {
        mBinarySwitch.setValue(true);
        return EntityHandlerResult.OK;
    }
}
