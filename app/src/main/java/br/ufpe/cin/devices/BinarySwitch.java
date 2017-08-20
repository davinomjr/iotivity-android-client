package br.ufpe.cin.devices;

import android.util.Log;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

import br.ufpe.cin.config.IoTivityConfigurer;

/**
 * Created by davinomjr on 8/17/17.
 */

public class BinarySwitch {

    private static final String TAG = BinarySwitch.class.getName();
    private boolean value = false;

    public void setValue(boolean value) {
        this.value = value;
        Log.i(TAG, String.format("Changing state: %s", this.value));
    }

    public void toggleBinarySwitch() {
        this.value = !value;
        Log.i(TAG, String.format("Changing state: %s", value));
    }

    public OcRepresentation serialize() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue("value", String.valueOf(value));
        return rep;
    }

}
