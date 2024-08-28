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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.jboss.as.camel.integration.service.CamelContextProxyFactory;
import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class CamelContextClassfileTransformerTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class CamelContextClassfileTransformerTestCase {

    @Mock
    CamelContextServiceMetaInfo metaInfoHolder;
    @Mock
    CamelContextService camelService;

    @Mock
    CamelContextClassfileTransformer classTransformer;

    CamelContextClassfileTransformer realClassTransformer;

    @Before
    public void setupTestCases() {
        realClassTransformer = new CamelContextClassfileTransformer(metaInfoHolder);
    }

    @Test
    public void injectCamelContextProxyTest() {
        final String fieldName = "ctx";
        final String deploymentName = "test.war";
        final String expected = "this.ctx=" + CamelContextProxyFactory.class.getCanonicalName() + ".createProxy(\"test.war\");\n";
        final String actual = realClassTransformer.createConstructorInjectionUsingProxy(fieldName, deploymentName);
        Assert.assertEquals(expected, actual);
        Assert.assertNotNull(realClassTransformer.getMetaInfoHolder());
    }

    @Test
    public void transformTest_MatchesClass() {

        final ClassForEnhancment tic = setupTransformMocks();

        Mockito.when(classTransformer.getMetaInfoHolder()).thenReturn(metaInfoHolder);
        Mockito.when(classTransformer.loadClasssByteCode(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName(), "ctx", "test.war"))
                .thenReturn(
                        "Test".getBytes());
        Mockito.when(
                classTransformer.transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                        .getClass().getProtectionDomain(), null)).thenCallRealMethod();

        classTransformer.transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                .getClass().getProtectionDomain(), null);
        Mockito.verify(classTransformer).loadClasssByteCode(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName(), "ctx", "test.war");
    }

    @Test
    public void transformTest_DoesNotMatchClass() {
        final ClassForEnhancment tic = setupTransformMocks();

        Mockito.when(classTransformer.getMetaInfoHolder()).thenReturn(metaInfoHolder);
        Mockito.when(classTransformer.loadClasssByteCode(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName(), "ctx", "test.war"))
                .thenReturn(
                        "Test".getBytes());
        Mockito.when(
                classTransformer.transform(tic.getClass().getClassLoader(), this.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                        .getClass().getProtectionDomain(), null)).thenCallRealMethod();
        classTransformer.transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                .getClass().getProtectionDomain(), null);
        Mockito.verify(classTransformer, Mockito.times(0)).loadClasssByteCode(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName(),
                "ctx",
                "test.war");
    }

    @Test
    public void tranformTest_FieldInfoSetEmpty() {

        final ClassForEnhancment tic = new ClassForEnhancment();
        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();

        Mockito.when(metaInfoHolder.getInjectionPoints()).thenReturn(fieldInfoSet);
        Mockito.when(
                classTransformer.transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                        .getClass().getProtectionDomain(), null)).thenCallRealMethod();
        Mockito.when(classTransformer.getMetaInfoHolder()).thenReturn(metaInfoHolder);
        final byte[] result = classTransformer.transform(tic.getClass().getClassLoader(), tic.getClass().getCanonicalName().replace('.', '/'),
                tic.getClass(), tic.getClass().getProtectionDomain(), null);

        Assert.assertNull(result);
    }

    @Test
    public void test_TransformByteCode() {

        final ClassForEnhancment tic = new ClassForEnhancment();
        final DeploymentUnit du = Mockito.mock(DeploymentUnit.class);
        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();
        final FieldInfo fieldInfo = createFiledInfo(fieldInfoSet);

        Mockito.when(metaInfoHolder.getInjectionPoints()).thenReturn(fieldInfoSet);
        Mockito.when(metaInfoHolder.getDu()).thenReturn(du);
        Mockito.when(du.getParent()).thenReturn(null);
        Mockito.when(du.getName()).thenReturn("test.war");

        Mockito.when(classTransformer.getMetaInfoHolder()).thenReturn(metaInfoHolder);
        Mockito.when(
                classTransformer.transform(tic.getClass().getClassLoader(), this.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                        .getClass().getProtectionDomain(), null)).thenCallRealMethod();

        final ClassLoader loader = tic.getClass().getClassLoader();
        final String dotClassName = tic.getClass().getCanonicalName();
        final String fieldName = fieldInfo.name();
        final String deploymentName = DeploymentUtils.getTopDeploymentUnit(du).getName();

        Mockito.when(classTransformer.createConstructorInjectionUsingProxy(fieldName, deploymentName)).thenCallRealMethod();

        Mockito.when(classTransformer.loadClasssByteCode(loader, dotClassName, fieldName, deploymentName)).thenCallRealMethod();
        final byte[] result = classTransformer.loadClasssByteCode(loader, dotClassName, fieldName, deploymentName);

        Assert.assertNotNull(result);
        Mockito.verify(classTransformer, Mockito.times(1)).createConstructorInjectionUsingProxy(fieldName, deploymentName);
    }

    @Test
    public void test_TransformByteCodeException() {

        final ClassForEnhancment tic = new ClassForEnhancment();
        final DeploymentUnit du = Mockito.mock(DeploymentUnit.class);

        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();
        final FieldInfo fieldInfo = createFiledInfo(fieldInfoSet);

        Mockito.when(metaInfoHolder.getInjectionPoints()).thenReturn(fieldInfoSet);
        Mockito.when(metaInfoHolder.getDu()).thenReturn(du);
        Mockito.when(du.getParent()).thenReturn(null);
        Mockito.when(du.getName()).thenReturn("test.war");
        Mockito.when(
                classTransformer.transform(tic.getClass().getClassLoader(), this.getClass().getCanonicalName().replace('.', '/'), tic.getClass(), tic
                        .getClass().getProtectionDomain(), null)).thenCallRealMethod();
        Mockito.when(classTransformer.getMetaInfoHolder()).thenReturn(metaInfoHolder);

        final ClassLoader loader = tic.getClass().getClassLoader();
        final String dotClassName = tic.getClass().getCanonicalName();
        final String fieldName = fieldInfo.name();
        final String deploymentName = DeploymentUtils.getTopDeploymentUnit(du).getName();
        Mockito.when(classTransformer.loadClasssByteCode(loader, dotClassName, fieldName, deploymentName)).thenCallRealMethod();
        final byte[] result = classTransformer.loadClasssByteCode(loader, dotClassName, fieldName, deploymentName);

        Assert.assertNull(result);
        Mockito.verify(classTransformer, Mockito.times(1)).createConstructorInjectionUsingProxy(fieldName, deploymentName);
    }

    private FieldInfo createFiledInfo(final Set<FieldInfo> fieldInfoSet) {
        final DotName dotName = DotName.createSimple(ClassForEnhancment.class.getCanonicalName());
        final Map<DotName, List<AnnotationInstance>> annotations = new HashMap<DotName, List<AnnotationInstance>>();
        final ClassInfo classInfo = ClassInfo.create(dotName, null, (short) 0, null, annotations);
        final DotName dotNameForField = DotName.createSimple(CamelContext.class.getCanonicalName());
        final Type type = Type.create(dotNameForField, Kind.CLASS);
        final FieldInfo fieldInfo = FieldInfo.create(classInfo, "ctx", type, (short) 0);

        fieldInfoSet.add(fieldInfo);
        return fieldInfo;
    }

    private ClassForEnhancment setupTransformMocks() {
        final ClassForEnhancment tic = new ClassForEnhancment();
        final DeploymentUnit du = Mockito.mock(DeploymentUnit.class);
        final DotName dotName = DotName.createSimple(ClassForEnhancment.class.getCanonicalName());
        final Map<DotName, List<AnnotationInstance>> annotations = new HashMap<DotName, List<AnnotationInstance>>();
        final ClassInfo classInfo = ClassInfo.create(dotName, null, (short) 0, null, annotations);
        final DotName dotNameForField = DotName.createSimple(CamelContext.class.getCanonicalName());
        final Type type = Type.create(dotNameForField, Kind.CLASS);
        final FieldInfo fieldInfo = FieldInfo.create(classInfo, "ctx", type, (short) 0);
        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();
        fieldInfoSet.add(fieldInfo);

        Mockito.when(metaInfoHolder.getInjectionPoints()).thenReturn(fieldInfoSet);
        Mockito.when(metaInfoHolder.getDu()).thenReturn(du);
        Mockito.when(du.getParent()).thenReturn(null);
        Mockito.when(du.getName()).thenReturn("test.war");

        return tic;
    }
}
