/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.integration.service;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.NamingException;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.util.jndi.JndiContext;
import org.jboss.as.camel.integration.deployment.CamelContributionBeanMetaInfo;
import org.jboss.as.camel.integration.deployment.CamelContributionMetaInfoHolder;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.camel.exception.CamelEngineException;
import org.jboss.camel.tx.TransactionPolicy;
import org.jboss.msc.service.StartException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * Camel context service Encapsulates ability to create, star/stop and bind beans into either shared or per-deployment camel context.
 */
public class CamelContextService {

    /**
     * Suffix for deployment names, final string is deployment_name + this suffix.
     */
    public static final String CAMEL_CONTEXT_NAME_SUFFIX = "-camel-context";

    /**
     * INFO level log message for deploy.
     */
    private static final String DEPLOYED_HANDLER = "Deployed handler: {} with shareContextEnabled: {}";

    /**
     * INFO level log message for undeploy.
     */
    private static final String UNDEPLOYED_HANDLER = "Undeployed handler: {} with shareContextEnabled: {}";

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextService.class);

    final Map<String, DeploymentMetaInfo> instantiatedContexts;
    final Map<String, CamelContributionMetaInfoHolder> boundBeans;
    final JtaTransactionManager jtaTransactionManager;
    final boolean shareContextEnabled;

    private DeploymentMetaInfo sharedContext;

    /**
     * Full argument constructor.
     *
     * @param shareContextEnabled
     *            Boolean value that controls if context will be shared or created for each deployment.
     * @param ctxName
     *            Name of the shared context - (configured in subsystem xml, or toplevel deployment name, if context is created per deployment)
     * @throws StartException
     *             in case we can't execute all operations.
     */
    public CamelContextService(final boolean shareContextEnabled, final String ctxName) throws StartException {
        this.shareContextEnabled = shareContextEnabled;
        instantiatedContexts = new ConcurrentHashMap<String, DeploymentMetaInfo>();
        boundBeans = new ConcurrentHashMap<String, CamelContributionMetaInfoHolder>();
        jtaTransactionManager = new JtaTransactionManager(TransactionUtil.getTransactionManager());
        if (shareContextEnabled) {
            final JndiContext jndiCtx = createJndiContext();
            sharedContext = createCamelContext(ctxName, jndiCtx);
        } else {
            sharedContext = null;
        }
    }

    /**
     * Bind all scanned beans to camel context.
     *
     * @param metaInfoHolder
     *            Meta information on all found beans.
     * @param beanManager
     *            BeanManager.
     * @throws NamingException
     *             if a bean cannot be bound.
     * @throws CamelEngineException
     *             if the context does not exist.
     */
    @SuppressWarnings({ "PMD.UseProperClassLoader" })
    public void bindBeans(final CamelContributionMetaInfoHolder metaInfoHolder, final BeanManager beanManager) throws NamingException,
            CamelEngineException {
        final String deploymentName = metaInfoHolder.getDeploymentUnit().getName();
        for (final CamelContributionBeanMetaInfo beanMeta : metaInfoHolder.getCamelBeanMetaInfo()) {
            final ClassLoader classLoader = metaInfoHolder.getDeploymentUnit().getAttachment(Attachments.MODULE).getClassLoader();
            final String classDotName = beanMeta.getClassInfo().name().toString();
            final String beanHandlerId = beanMeta.getHandlerId();
            LOGGER.debug("Binding bean with id: {} with shareContextEnabled: {}", beanHandlerId, shareContextEnabled);
            if (shareContextEnabled) {
                LOGGER.trace("Using shared camel context");
                // global camel context is used
                final Processor proxy = CamelBeanCdiProxyFactory.createProxy(classDotName, classLoader, beanManager);
                sharedContext.getRegistry().bind(beanHandlerId, proxy);
                LOGGER.info(DEPLOYED_HANDLER, beanHandlerId, shareContextEnabled);
            } else {
                // private context is being used:
                boolean found = false;
                final String contextName = beanMeta.getContextName();
                final Collection<DeploymentMetaInfo> deployments = instantiatedContexts.values();
                for (final DeploymentMetaInfo dmi : deployments) {
                    if (contextName.equals(((DefaultCamelContext) dmi.getCamelContext()).getName())) {
                        final Processor proxy = CamelBeanCdiProxyFactory.createProxy(classDotName, classLoader, beanManager);
                        dmi.getRegistry().bind(beanHandlerId, proxy);
                        LOGGER.info(DEPLOYED_HANDLER, beanHandlerId, shareContextEnabled);
                        found = true;
                    }
                }
                if (!found) {
                    boundBeans.put(deploymentName, metaInfoHolder);
                    LOGGER.error(
                            "Unable to do the binding for bean: {} to non-existing camel context with name {}. Context with that name does not exist",
                            beanHandlerId, contextName);
                    throw new CamelEngineException("Unable to do the binding for bean " + beanHandlerId + " to non-existing camel context with name "
                            + contextName + ". Context with that name does not exist");
                }
            }
        }
        boundBeans.put(deploymentName, metaInfoHolder);
    }

    /**
     * Unbind all bound beans, happens as part of undeploy.
     *
     * @param deploymentName
     *            Name of the deployment unit who's beans were are unbinding.
     * @throws NamingException
     *             if a bean cannot be unbound.
     */
    public void unbindBeans(final String deploymentName) throws NamingException {
        final CamelContributionMetaInfoHolder metaHolder = boundBeans.get(deploymentName);
        LOGGER.trace("Unbinding, CamelContributionMetaInfoHolder is : {}", metaHolder);
        if (metaHolder != null) {
            final Set<CamelContributionBeanMetaInfo> internalBoundBeans = metaHolder.getCamelBeanMetaInfo();
            final int numberOfBeansToUnbind = internalBoundBeans.size();
            LOGGER.trace("Number of beans to unbind: {}", numberOfBeansToUnbind);
            for (final CamelContributionBeanMetaInfo beanMetaInfo : internalBoundBeans) {
                final String beanHandlerId = beanMetaInfo.getHandlerId();
                LOGGER.debug("Unbinding bean with id: {} with shareContextEnabled: {}", beanHandlerId, shareContextEnabled);
                if (shareContextEnabled) {
                    LOGGER.trace("Unbinding bean with id: {} from Camel Context with shareContextEnabled: {}", beanHandlerId, shareContextEnabled);
                    sharedContext.getRegistry().unbind(beanHandlerId);
                    LOGGER.warn(UNDEPLOYED_HANDLER, beanHandlerId, shareContextEnabled);
                } else {
                    final String contextName = beanMetaInfo.getContextName();
                    final Collection<DeploymentMetaInfo> deployments = instantiatedContexts.values();
                    for (final DeploymentMetaInfo dmi : deployments) {
                        if (contextName.equals(((DefaultCamelContext) dmi.getCamelContext()).getName())) {
                            dmi.getRegistry().unbind(beanHandlerId);
                            LOGGER.warn(UNDEPLOYED_HANDLER, beanHandlerId, shareContextEnabled);
                        }
                    }
                }
            }
            LOGGER.trace("Clearing everything under key: {}", deploymentName);
            final CamelContributionMetaInfoHolder result = this.boundBeans.remove(deploymentName);
            if (LOGGER.isTraceEnabled()) {
                if (result != null) {
                    LOGGER.trace("All content under key: {} was successfully removed.", deploymentName);
                } else {
                    LOGGER.trace("Key: {} was not found in the map, and was not removed.", deploymentName);
                }
            }
        }
    }

    /**
     * Create camel context for this deployment name.
     *
     * @param deploymentName
     *            Name of the deployment (top level deployment is used as a key)
     * @return Instance of DeploymentMetaInfo that holds reference to created context and jndi registry associated with that context
     * @throws StartException
     *             in case something went wrong.
     */
    public DeploymentMetaInfo createCamelContextForDeployment(final String deploymentName) throws StartException {
        if (shareContextEnabled) {
            return sharedContext;
        } else {
            final JndiContext jndiContext = createJndiContext();
            return createCamelContext(deploymentName + CAMEL_CONTEXT_NAME_SUFFIX, jndiContext);
        }
    }

    /**
     * Start camel context.
     *
     * @param dmi
     *            DeploymentMetaInfo who's camel context we are about to start.
     * @throws CamelEngineException
     *             in case something went wrong.
     */
    public void startCamelContext(final DeploymentMetaInfo dmi) throws CamelEngineException {
        if (!shareContextEnabled) {
            try {
                dmi.getCamelContext().start();
            } catch (final Exception e) {
                throw new CamelEngineException(e);
            }
        }
    }

    /**
     * Stop camel context.
     *
     * @param dmi
     *            DeploymentMetaInfo who's camel context we are about to stop.
     * @throws CamelEngineException
     *             in case something went wrong.
     */
    public void stopCamelContext(final DeploymentMetaInfo dmi) throws CamelEngineException {
        if (!shareContextEnabled) {
            try {
                dmi.getCamelContext().stop();
            } catch (final Exception e) {
                throw new CamelEngineException(e);
            }
        }
    }

    /**
     * Register this DeploymentMetaInfo with key.
     *
     * @param dmi
     *            DeploymentMetaInfo to register.
     * @param key
     *            String key, name of the top level deployment will be used as key
     */
    public void registerCamelContext(final DeploymentMetaInfo dmi, final String key) {
        if (!shareContextEnabled) {
            instantiatedContexts.put(key, dmi);
        }
    }

    /**
     * Remove association between this DeploymentMetaInfo and key.
     *
     * @param key
     *            Top level deployment name will be used as a key.
     */
    public void deregisterCamelContext(final String key) {
        if (!shareContextEnabled) {
            instantiatedContexts.remove(key);
        }
    }

    /**
     * Return camel context for given deployment.
     *
     * @param deployment
     *            Deployment name, here top level deployment name is used as a key
     * @return DeploymentMetaInfo holding reference to requesting camel context
     */
    public DeploymentMetaInfo getCamelContextForDeployment(final String deployment) {
        if (shareContextEnabled) {
            return sharedContext;
        } else {
            final DeploymentMetaInfo dmi = instantiatedContexts.get(deployment);
            if (dmi == null) {
                throw new java.lang.IllegalStateException("Deployment " + deployment + " did not register any camel contexts");
            }
            return dmi;
        }
    }

    /**
     * Stop and deregister all contexts, and free all resources.
     */
    public void destroy() {
        final Set<Entry<String, DeploymentMetaInfo>> entrySet = instantiatedContexts.entrySet();
        for (final Entry<String, DeploymentMetaInfo> entry : entrySet) {
            if (entry.getValue() != null) {
                tryToStopContext(entry.getValue());
            }
        }
        if (sharedContext != null) {
            tryToStopContext(sharedContext);
        }
        instantiatedContexts.clear();
    }

    public DeploymentMetaInfo getSharedContext() {
        return sharedContext;
    }

    void setSharedContext(final DeploymentMetaInfo sharedContext) {
        this.sharedContext = sharedContext;
    }

    /**
     * Try to stop camel context for this deployment meta info.
     *
     * @param meta
     *            DeploymentMetaInfo holding reference to camel context we are trying to stop.
     */
    private void tryToStopContext(final DeploymentMetaInfo meta) {
        try {
            if (meta != null && meta.getCamelContext() != null) {
                tryToStopContext(meta.getCamelContext(), meta.getRegistry());
                meta.setRegistry(null);
                meta.setCamelContext(null);
            }
        } catch (final Exception e) {
            LOGGER.error("Error trying to stop context:", e);
        }
    }

    private void tryToStopContext(final CamelContext ctx, final JndiContext jndi) {
        try {
            if (ctx != null) {
                LOGGER.info("Stopping CamelContext {}...", ctx.getName());
                ctx.stop();
                if (jndi != null) {
                    jndi.close();
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Error trying to stop camel context:", e);
        }
    }

    /**
     * Create camel context.
     *
     * @param name
     *            Name to be used for this context.
     * @param jndiContext
     *            JndiContext instanct that will be used by this camel context.
     * @return Constructed and started camel context
     * @throws StartException
     *             in case something goes wrong.
     */
    private DeploymentMetaInfo createCamelContext(final String name, final JndiContext jndiContext) throws StartException {
        LOGGER.debug("Initializing camel context: {}", name);
        final DefaultCamelContext ctx = new DefaultCamelContext(jndiContext);
        DeploymentMetaInfo dmi = null;
        try {
            ctx.setName(name);
            bindTransactionPolicies(jndiContext, new String[] { TransactionPolicy.PROPAGATION_REQUIRED, TransactionPolicy.PROPAGATION_REQUIRES_NEW });
            bindJmsComponent(ctx);
            ctx.start();
            dmi = new DeploymentMetaInfo(ctx, jndiContext);
            return dmi;
        } catch (final Exception e) {
            LOGGER.error("Error while creating Camel Context:", e);
            tryToStopContext(dmi);
            throw new StartException(e.getMessage(), e.getCause());
        }
    }

    private SpringTransactionPolicy createTransactionPolicy(final String name) {
        final SpringTransactionPolicy policy = new SpringTransactionPolicy();
        policy.setPropagationBehaviorName(name);
        policy.setTransactionManager(jtaTransactionManager);
        return policy;
    }

    /**
     * Utility method to create jndi context.
     *
     * @return Instance of jndi context
     * @throws StartException
     *             in case something went wrong.
     */
    private JndiContext createJndiContext() throws StartException {
        try {
            final JndiContext jndiCtx = new JndiContext();
            return jndiCtx;
        } catch (final Exception ne) {
            LOGGER.error("Unable to bind transaction policies for camel, camel transaction support will not work.", ne);
            throw new StartException(ne);
        }
    }

    private void bindTransactionPolicies(final JndiContext jndi, final String... policies) throws NamingException {
        for (final String policyName : policies) {
            jndi.bind(policyName, createTransactionPolicy(policyName));
        }
    }

    private void bindJmsComponent(final CamelContext context) throws NamingException {
        context.addComponent(JmsUtil.JMS_COMPONENT_NAME, JmsComponent.jmsComponent(JmsUtil.getConnectionFactory()));
    }
}
