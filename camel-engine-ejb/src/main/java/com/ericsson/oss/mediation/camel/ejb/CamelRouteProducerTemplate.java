/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.camel.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.jboss.camel.annotations.CamelContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton responsible for producing route exchanges.
 */
@Singleton
@Startup
public class CamelRouteProducerTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelRouteProducerTemplate.class);
    @CamelContextService
    private CamelContext camelContext;

    private ProducerTemplate template;

    /**
     * Post construct method which will create and start template.
     */
    @PostConstruct
    public void postConstruct() {
        template = camelContext.createProducerTemplate();
        try {
            template.start();
        } catch (final Exception e) {
            LOGGER.error("Error starting camel producer template:", e);
        }
    }

    /**
     * Pre-destroy method which will stop the producer template.
     */
    @PreDestroy
    public void preDestroy() {
        try {
            template.stop();
        } catch (final Exception e) {

            LOGGER.error("Error stoping camel producer template:", e);
        }
    }

    /**
     * Getter method for producer template.
     *
     * @return the template
     */
    public ProducerTemplate getTemplate() {
        return template;
    }

    /**
     * Setter method for producer template.
     *
     * @param template
     *            the template to set.
     */
    public void setTemplate(final ProducerTemplate template) {
        this.template = template;
    }
}
