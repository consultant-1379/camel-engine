/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.integration.deployment;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Set;

import org.jboss.as.camel.integration.service.CamelContextProxyFactory;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.LoaderClassPath;
/**
 * Class file transformer that will enhance all annotated classes and inject camel context proxy.
 */
public class CamelContextClassfileTransformer implements ClassFileTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextClassfileTransformer.class);

    final CamelContextServiceMetaInfo metaInfoHolder;

    /**
     * Full arg constructor.
     *
     * @param metaInfoHolder
     *            Meta info holder.
     */
    public CamelContextClassfileTransformer(final CamelContextServiceMetaInfo metaInfoHolder) {
        this.metaInfoHolder = metaInfoHolder;
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {
        final String dotClassName = className.replace('/', '.');
        final Set<FieldInfo> fieldInfoSet = getMetaInfoHolder().getInjectionPoints();
        for (final FieldInfo fieldInfo : fieldInfoSet) {
            final ClassInfo declaringClass = fieldInfo.declaringClass();
            if (dotClassName.equals(declaringClass.name().toString())) {
                final byte[] ret = loadClasssByteCode(loader, dotClassName, fieldInfo.name(),
                        DeploymentUtils.getTopDeploymentUnit(getMetaInfoHolder().getDu()).getName());
                return ret;
            }
        }
        return null;
    }

    /**
     * Transform the bytecode of the class and insert initialization code.
     *
     * @param loader
     *            Classloader which we will use to load classes.
     * @param dotClassName
     *            Fully qualified class name in dot format.
     * @param fieldName
     *            Name of the field that we will initialize.
     * @param deploymentName
     *            Deployment name.
     * @return Transformed bytecode
     */
    public byte[] loadClasssByteCode(final ClassLoader loader, final String dotClassName, final String fieldName, final String deploymentName) {
        final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        byte[] result = null;
        LOGGER.trace("Thread context classloader is: {}", tcl);
        try {
            LOGGER.trace("Setting context class loader to: {}", loader);
            Thread.currentThread().setContextClassLoader(loader);
            final ClassPool classPool = ClassPool.getDefault();
            LOGGER.trace("ClassPool will use classloader: {}", classPool.getClassLoader());
            classPool.appendClassPath(new LoaderClassPath(loader));
            final CtClass ctClass = classPool.get(dotClassName);
            if (ctClass.isFrozen()) {
                ctClass.defrost();
            }
            final CtConstructor[] ctConstrArray = ctClass.getConstructors();
            final String ctorEnhancment = createConstructorInjectionUsingProxy(fieldName, deploymentName);
            LOGGER.trace("[{}] will be inserted into every ctor as last line", ctorEnhancment);
            for (final CtConstructor ctConstr : ctConstrArray) {
                ctConstr.insertAfter(ctorEnhancment);
            }
            result = ctClass.toBytecode();
            LOGGER.trace("Transformed class: {}", dotClassName);
            ctClass.detach();
            return result;
        } catch (final Exception e) {
            e.printStackTrace();
            return result;
        } finally {
            LOGGER.trace("Setting back context classloader to: {}", tcl);
            Thread.currentThread().setContextClassLoader(tcl);
        }
    }

    /**
     * Create field initializer for the constructors being enhanced.
     *
     * @param fieldName
     *            Name of the field where injection will happen.
     * @param deploymentName
     *            Deployment name (top level deployment, ie if this is war inside ear, this will be name.ear)
     * @return String line to be injected into constructors
     */
    protected String createConstructorInjectionUsingProxy(final String fieldName, final String deploymentName) {
        final StringBuilder stringBuilder = new StringBuilder("this.");
        stringBuilder.append(fieldName);
        stringBuilder.append("=");
        stringBuilder.append(CamelContextProxyFactory.class.getCanonicalName());
        stringBuilder.append(".createProxy(\"");
        stringBuilder.append(deploymentName);
        stringBuilder.append("\");\n");
        return stringBuilder.toString();
    }

    /**
     * Getter method, use this getter only for testing purposes.
     *
     * @return the metaInfoHolder
     */
    public CamelContextServiceMetaInfo getMetaInfoHolder() {
        return metaInfoHolder;
    }
}
