package org.wso2.carbon.cloud.complimentary.users.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="validation.serviceComponent"" immediate="true"
 */
public class ValidationServiceComponent {

    private static final Log log = LogFactory.getLog(ValidationServiceComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param context OSGi component context.
     */
    protected void activate(ComponentContext context) {
        log.info("Activating validation service listener component");
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        log.info("Deactivating validation service listener component");
    }
}
