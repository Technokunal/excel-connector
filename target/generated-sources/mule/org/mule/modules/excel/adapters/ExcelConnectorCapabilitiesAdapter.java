
package org.mule.modules.excel.adapters;

import javax.annotation.Generated;
import org.mule.api.devkit.capability.Capabilities;
import org.mule.api.devkit.capability.ModuleCapability;
import org.mule.modules.excel.ExcelConnector;


/**
 * A <code>ExcelConnectorCapabilitiesAdapter</code> is a wrapper around {@link ExcelConnector } that implements {@link org.mule.api.Capabilities} interface.
 * 
 */
@SuppressWarnings("all")
@Generated(value = "Mule DevKit Version 3.7.1", date = "2015-12-15T07:10:27-06:00", comments = "Build UNNAMED.2613.77421cc")
public class ExcelConnectorCapabilitiesAdapter
    extends ExcelConnector
    implements Capabilities
{


    /**
     * Returns true if this module implements such capability
     * 
     */
    public boolean isCapableOf(ModuleCapability capability) {
        if (capability == ModuleCapability.LIFECYCLE_CAPABLE) {
            return true;
        }
        return false;
    }

}
