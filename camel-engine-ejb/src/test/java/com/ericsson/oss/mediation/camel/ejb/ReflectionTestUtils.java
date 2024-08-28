/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.camel.ejb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * The Class ReflectionTestUtils.
 */
public final class ReflectionTestUtils {

    /**
     * Instantiates a new reflection test utils.
     */
    private ReflectionTestUtils() {}

    /**
     * Find non primitive field.
     *
     * @param classToSearch
     *            the class to search.
     * @param fieldType
     *            the field type.
     * @return the field
     * @throws SecurityException
     *             the security exception.
     */
    public static Field findNonPrimitiveField(final Class<?> classToSearch, final Class<?> fieldType) throws SecurityException {
        for (final Field field : getFields(classToSearch, fieldType)) {
            field.setAccessible(true);
            final Class<?> clazz = field.getType();
            if (clazz.getName().equals(fieldType.getName())) {
                return field;
            }
        }

        throw new IllegalArgumentException("The field of type " + fieldType + "was not found");
    }

    /**
     * Gets the fields.
     *
     * @param classToSearch
     *            the class to search.
     * @param fieldType
     *            the field type.
     * @return the fields
     * @throws SecurityException
     *             the security exception.
     */
    public static Field[] getFields(final Class<?> classToSearch, final Class<?> fieldType) throws SecurityException {
        if (fieldType == null) {
            throw new IllegalArgumentException("The fieldType Class must not be null");
        }

        final Field[] fields = classToSearch.getDeclaredFields();
        if (fields.length == 0) {
            throw new IllegalArgumentException("The Class " + classToSearch.getName() + "must contain fields");
        }
        return fields;
    }

    /**
     * Sets the non primitive field.
     *
     * @param classToSearch
     *            the class to search.
     * @param fieldType
     *            the field type.
     * @param classInstance
     *            the class instance.
     * @param value
     *            the value.
     * @throws SecurityException
     *             the security exception.
     * @throws IllegalAccessException
     *             the illegal access exception.
     */
    public static void setNonPrimitiveField(final Class<?> classToSearch, final Class<?> fieldType, final Object classInstance, final Object value)
            throws SecurityException, IllegalAccessException {

        final Field field = findNonPrimitiveField(classToSearch, fieldType);
        field.set(classInstance, value);
    }

    /**
     * Sets the primitive field.
     *
     * @param classToSearch
     *            the class to search.
     * @param fieldType
     *            the field type.
     * @param fieldName
     *            the field name.
     * @param classInstance
     *            the class instance.
     * @param value
     *            the value.
     * @throws SecurityException
     *             the security exception.
     * @throws IllegalAccessException
     *             the illegal access exception.
     */
    public static void setPrimitiveField(final Class<?> classToSearch, final Class<?> fieldType, final String fieldName, final Object classInstance,
            final Object value) throws SecurityException, IllegalAccessException {

        for (final Field field : getFields(classToSearch, fieldType)) {
            field.setAccessible(true);
            if (field.getName().equalsIgnoreCase(fieldName)) {
                field.set(classInstance, value);
                return;
            }
        }
        throw new IllegalArgumentException("The field name " + fieldName + "was not found");
    }

    /**
     * Sets the final non primitive field.
     *
     * @param classToSearch
     *            the class to search.
     * @param fieldType
     *            the field type.
     * @param classInstance
     *            the class instance.
     * @param value
     *            the value.
     * @throws Exception
     *             the exception.
     */
    public static void setFinalNonPrimitiveField(final Class<?> classToSearch, final Class<?> fieldType, final Object classInstance,
            final Object value) throws Exception {

        final Field field = findNonPrimitiveField(classToSearch, fieldType);
        setFinalStatic(field, value);
    }

    /**
     * Sets the final static.
     *
     * @param field
     *            the field.
     * @param newValue
     *            the new value.
     * @throws Exception
     *             the exception.
     */
    public static void setFinalStatic(final Field field, final Object newValue) throws Exception {
        field.setAccessible(true);
        // remove final modifier from field
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
