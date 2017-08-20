package br.ufpe.cin.interfaces;

import org.iotivity.base.OcException;
import org.iotivity.base.OcResourceHandle;

/**
 * Created by davinomjr on 8/17/17.
 */

public interface Resource {
    void registerResource() throws OcException;
    void unregisterResource() throws OcException, OcException;
    OcResourceHandle getHandle();
}
