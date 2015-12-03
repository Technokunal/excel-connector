
package org.mule.modules.excel.adapters;

import javax.annotation.Generated;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.devkit.ProcessAdapter;
import org.mule.api.devkit.ProcessTemplate;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.routing.filter.Filter;
import org.mule.modules.excel.ExcelConnector;
import org.mule.security.oauth.callback.ProcessCallback;


/**
 * A <code>ExcelConnectorProcessAdapter</code> is a wrapper around {@link ExcelConnector } that enables custom processing strategies.
 * 
 */
@SuppressWarnings("all")
@Generated(value = "Mule DevKit Version 3.7.1", date = "2015-12-02T05:50:09-08:00", comments = "Build UNNAMED.2613.77421cc")
public class ExcelConnectorProcessAdapter
    extends ExcelConnectorLifecycleInjectionAdapter
    implements ProcessAdapter<ExcelConnectorCapabilitiesAdapter>
{


    public<P >ProcessTemplate<P, ExcelConnectorCapabilitiesAdapter> getProcessTemplate() {
        final ExcelConnectorCapabilitiesAdapter object = this;
        return new ProcessTemplate<P,ExcelConnectorCapabilitiesAdapter>() {


            @Override
            public P execute(ProcessCallback<P, ExcelConnectorCapabilitiesAdapter> processCallback, MessageProcessor messageProcessor, MuleEvent event)
                throws Exception
            {
                return processCallback.process(object);
            }

            @Override
            public P execute(ProcessCallback<P, ExcelConnectorCapabilitiesAdapter> processCallback, Filter filter, MuleMessage message)
                throws Exception
            {
                return processCallback.process(object);
            }

        }
        ;
    }

}
