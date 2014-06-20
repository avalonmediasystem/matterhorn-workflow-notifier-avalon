/*
 * This file has been modified from the original distribution. 
 * Modifications Copyright 2013 The Trustees of Indiana University and Northwestern University.
 */

/**
 *  Copyright 2009, 2010 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
package org.avalonmediasystem;

import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.serviceregistry.api.ServiceRegistryException;
import org.opencastproject.workflow.api.WorkflowService;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvalonWorkflowNotifier {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(AvalonWorkflowNotifier.class);

  /** The workflow service */
  private WorkflowService workflowService;

  /** The opencast service registry */
  private ServiceRegistry serviceRegistry;

  /** The listener that pings Avalon when an operation changed **/
  private AvalonWorkflowListener listener;

  /** The configuration key for the Avalon pingback URL **/
  private static final String urlConfigKey = "org.avalonmediasystem.avalon.url";

  protected String hostname;

  public AvalonWorkflowNotifier() {
  }

  /**
   * OSGI callback for activating this component
   * 
   * @param cc
   *          the osgi component context
   */
  protected void activate(ComponentContext cc) {
    logger.info("AvalonWorkflowNotifier started.");

    String avalonUrl = null;
    // Get the configured avalon server URL
    if (cc != null) {
      avalonUrl = StringUtils.trimToNull(cc.getBundleContext().getProperty(urlConfigKey));
      if (avalonUrl == null)
        logger.warn("Avalon pingback url was not set (" + urlConfigKey + ")");
      else
        logger.info("Avalon pingback url is {}", avalonUrl);

      // Find this host's url
      hostname = StringUtils.trimToNull(cc.getBundleContext().getProperty("org.opencastproject.server.url"));
    }

    listener = new AvalonWorkflowListener(avalonUrl);
    workflowService.addWorkflowListener(listener);
    try {
      serviceRegistry.registerService(AvalonWorkflowNotifier.class.getName(), hostname, "/", false);
    } catch (ServiceRegistryException e) {
      logger.warn("AvalonWorkflowNotifier not registered with service registry.", e);
    }
  }

  /**
   * Callback from OSGi on service deactivation.
   */
  public void deactivate() {
    workflowService.removeWorkflowListener(listener);
    try {
      serviceRegistry.unRegisterService(AvalonWorkflowNotifier.class.getName(), hostname);
    } catch (ServiceRegistryException e) {
      logger.warn("AvalonWorkflowNotifier not registered with service registry.", e);
    }
    logger.info("AvalonWorkflowNotifier stopped.");
  }

  /**
   * Sets the service registry
   * 
   * @param serviceRegistry
   *          the serviceRegistry to set
   */
  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  public void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  protected ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

}
