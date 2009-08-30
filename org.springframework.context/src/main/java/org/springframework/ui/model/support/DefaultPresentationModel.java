/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ui.model.support;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.ui.format.Formatter;
import org.springframework.ui.format.FormatterRegistry;
import org.springframework.ui.format.support.GenericFormatterRegistry;
import org.springframework.ui.model.FieldModel;
import org.springframework.ui.model.FieldNotFoundException;
import org.springframework.ui.model.PresentationModel;
import org.springframework.ui.model.config.FieldModelConfiguration;
import org.springframework.util.Assert;

import com.sun.jna.TypeConverter;

/**
 * A default PresentationModel implementation suitable for use in most environments.
 * @author Keith Donald
 * @since 3.0
 * @see #setFormatterRegistry(FormatterRegistry)
 * @see #setMessageSource(MessageSource)
 * @see #setTypeConverter(TypeConverter)
 * @see #field(String)
 */
public class DefaultPresentationModel implements PresentationModel {

	private Object domainModel;

	private Map<String, PropertyFieldModelRule> fieldModelRules;

	private FormatterRegistry formatterRegistry;

	private ConversionService conversionService;

	private MessageSource messageSource;

	/**
	 * Creates a new presentation model for the domain model.
	 * @param domainModel the domain model object
	 */
	public DefaultPresentationModel(Object domainModel) {
		Assert.notNull(domainModel, "The domain model to bind to is required");
		this.domainModel = domainModel;
		fieldModelRules = new HashMap<String, PropertyFieldModelRule>();
		formatterRegistry = new GenericFormatterRegistry();
		conversionService = new DefaultConversionService();
	}

	/**
	 * Configures the registry of Formatters to query when no explicit Formatter has been registered for a field.
	 * Allows Formatters to be applied by property type and by property annotation.
	 * @param registry the formatter registry
	 */
	public void setFormatterRegistry(FormatterRegistry formatterRegistry) {
		Assert.notNull(formatterRegistry, "The FormatterRegistry is required");
		this.formatterRegistry = formatterRegistry;
	}

	/**
	 * Configure the MessageSource that resolves localized UI alert messages.
	 * @param messageSource the message source
	 */
	public void setMessageSource(MessageSource messageSource) {
		Assert.notNull(messageSource, "The MessageSource is required");
		this.messageSource = messageSource;
	}

	/**
	 * Configure the ConversionService that converts values as required by the binding system.
	 * For a {@link FieldModel#applySubmittedValue(Object) applySubmittedValue call}, the ConversionService will be asked to perform a conversion if the value parsed by the field's Formatter is not assignable to the target value type.
	 * For a {@link FieldModel#getRenderValue() getRenderValue call}, the ConversionService will be asked to perform a conversion if the value type does not match the type T required by the field's Formatter.
	 * For a {@link FieldModel#getMapValue(Object) getMapValue call} the ConversionService will be asked to convert the Map key to the type required if there is no keyFormatter registered for the field.
	 * @param conversionService the type converter used by the binding system
	 */
	public void setConversionService(ConversionService conversionService) {
		Assert.notNull(conversionService, "The ConversionService is required");
		this.conversionService = conversionService;
	}

	/**
	 * Add a FieldModel configuration at the path specified.
	 * @param fieldPath the domain object property path in format &lt;prop&gt;[.nestedProp]
	 * @return a builder for the {@link FieldModel} configuration
	 */
	public FieldModelConfiguration field(String fieldPath) {
		FieldPath path = new FieldPath(fieldPath);
		PropertyFieldModelRule rule = getRule(path.getFirstElement().getValue());
		for (FieldPathElement element : path.getNestedElements()) {
			rule = rule.getNestedRule(element.getValue());
		}
		return rule;
	}

	/**
	 * The domain-layer model this presentation model coordinates with.
	 */
	public Object getDomainModel() {
		return domainModel;
	}

	// implementing PresentationModel

	public FieldModel getFieldModel(String fieldName) {
		FieldPath path = new FieldPath(fieldName);
		FieldModel field = getRule(path.getFirstElement().getValue()).getFieldModel(domainModel);
		for (FieldPathElement element : path.getNestedElements()) {
			if (element.isIndex()) {
				if (field.isMap()) {
					field = field.getMapValue(element.getValue());
				} else if (field.isList()) {
					field = field.getListElement(element.getIntValue());
				} else {
					throw new IllegalArgumentException("Attempted to index a field that is not a List, Array, or a Map");
				}
			} else {
				field = field.getNested(element.getValue());
			}
		}
		return field;
	}

	public void validate() {

	}

	public boolean hasErrors() {
		return false;
	}

	public void commit() {

	}

	// internal helpers

	private PropertyFieldModelRule getRule(String fieldName) {
		PropertyFieldModelRule rule = fieldModelRules.get(fieldName);
		if (rule == null) {
			rule = new PropertyFieldModelRule(fieldName, domainModel.getClass());
			fieldModelRules.put(fieldName, rule);
		}
		return rule;
	}

	@SuppressWarnings("unchecked")
	class PropertyFieldModelRule implements FieldModelConfiguration, FieldModelContext {

		private Class<?> domainModelClass;

		private PropertyDescriptor property;

		private Formatter formatter;

		private Formatter keyFormatter;

		private Formatter elementFormatter;

		private Condition editableCondition = Condition.ALWAYS_TRUE;

		private Condition enabledCondition = Condition.ALWAYS_TRUE;

		private Condition visibleCondition = Condition.ALWAYS_TRUE;

		private Map<String, PropertyFieldModelRule> nestedFieldModelRules;

		private FieldModel fieldModel;

		private Map<Integer, FieldModel> listElements;

		private Map<Object, FieldModel> mapValues;

		public PropertyFieldModelRule(String property, Class domainModelClass) {
			this.domainModelClass = domainModelClass;
			this.property = findPropertyDescriptor(property);
		}

		// implementing FieldModelContext

		public MessageSource getMessageSource() {
			return messageSource;
		}

		public Locale getLocale() {
			return LocaleContextHolder.getLocale();
		}

		public Condition getEnabledCondition() {
			return enabledCondition;
		}

		public Condition getEditableCondition() {
			return editableCondition;
		}

		public Condition getVisibleCondition() {
			return visibleCondition;
		}

		public Formatter<?> getFormatter() {
			if (formatter != null) {
				return formatter;
			} else {
				TypeDescriptor type = new TypeDescriptor(new MethodParameter(property.getReadMethod(), -1));
				return getFormatter(type);
			}
		}

		public Formatter<?> getKeyFormatter() {
			if (keyFormatter != null) {
				return keyFormatter;
			} else {
				return getFormatter(TypeDescriptor.valueOf(getKeyType()));
			}
		}

		public Formatter<?> getElementFormatter() {
			if (elementFormatter != null) {
				return formatter;
			} else {
				return getFormatter(TypeDescriptor.valueOf(getElementType()));
			}
		}

		public ConversionService getConversionService() {
			return conversionService;
		}

		public FieldModel getNested(String fieldName) {
			createValueIfNecessary();
			return getNestedRule(fieldName, fieldModel.getValueType()).getFieldModel(fieldModel.getValue());
		}

		public FieldModel getListElement(int index) {
			if (listElements == null) {
				listElements = new HashMap<Integer, FieldModel>();
			}
			growListIfNecessary(index);
			FieldModel field = listElements.get(index);
			if (field == null) {
				FieldModelContext context = new ListElementContext(index, this);
				ValueModel valueModel = new ListElementValueModel(index, getElementType(), fieldModel.getValue());
				field = new DefaultFieldModel(valueModel, context);
				listElements.put(index, field);
			}
			return field;
		}

		public FieldModel getMapValue(Object key) {
			if (key instanceof String) {
				try {
					key = keyFormatter != null ? keyFormatter.parse((String) key, getLocale()) : key;
				} catch (ParseException e) {
					throw new IllegalArgumentException("Unable to parse map key '" + key + "'", e);
				}
			}
			key = conversionService.convert(key, getKeyType());
			if (mapValues == null) {
				mapValues = new HashMap<Object, FieldModel>();
			}
			createMapValueIfNecessary();
			FieldModel field = mapValues.get(key);
			if (field == null) {
				FieldModelContext context = new MapValueContext(key, this);
				ValueModel valueModel = new MapValueValueModel(key, getElementType(), (Map) fieldModel.getValue(),
						context);
				field = new DefaultFieldModel(valueModel, context);
				mapValues.put(key, field);
			}
			return field;
		}

		public String getLabel() {
			return property.getName();
		}

		// implementing FieldModelConfiguration

		public FieldModelConfiguration formatWith(Formatter<?> formatter) {
			this.formatter = formatter;
			return this;
		}

		public FieldModelConfiguration formatElementsWith(Formatter<?> formatter) {
			if (!List.class.isAssignableFrom(domainModelClass) || domainModelClass.isArray()) {
				throw new IllegalStateException("Field is not a List or an Array; cannot set a element formatter");
			}
			elementFormatter = formatter;
			return this;
		}

		public FieldModelConfiguration formatKeysWith(Formatter<?> formatter) {
			if (!Map.class.isAssignableFrom(domainModelClass)) {
				throw new IllegalStateException("Field is not a Map; cannot set a key formatter");
			}
			keyFormatter = formatter;
			return this;
		}

		// package private helpers

		PropertyFieldModelRule getNestedRule(String propertyName) {
			return getNestedRule(propertyName, this.property.getPropertyType());
		}

		PropertyFieldModelRule getNestedRule(String propertyName, Class<?> domainModelClass) {
			if (nestedFieldModelRules == null) {
				nestedFieldModelRules = new HashMap<String, PropertyFieldModelRule>();
			}
			PropertyFieldModelRule rule = nestedFieldModelRules.get(propertyName);
			if (rule == null) {
				rule = new PropertyFieldModelRule(propertyName, domainModelClass);
				nestedFieldModelRules.put(propertyName, rule);
			}
			return rule;
		}

		// internal helpers

		private Formatter<?> getFormatter(TypeDescriptor type) {
			return formatterRegistry.getFormatter(type);
		}

		private Class<?> getElementType() {
			Class<?> propertyType = property.getPropertyType();
			if (Map.class.isAssignableFrom(propertyType)) {
				return GenericCollectionTypeResolver.getMapValueReturnType(property.getReadMethod());
			} else if (propertyType.isArray()) {
				return property.getPropertyType().getComponentType();
			} else {
				return GenericCollectionTypeResolver.getCollectionReturnType(property.getReadMethod());
			}
		}

		private Class<?> getKeyType() {
			return GenericCollectionTypeResolver.getMapKeyReturnType(property.getReadMethod());
		}

		FieldModel getFieldModel(Object domainObject) {
			if (fieldModel == null) {
				PropertyValueModel valueModel = new PropertyValueModel(property, domainObject);
				fieldModel = new DefaultFieldModel(valueModel, this);
			}
			return fieldModel;
		}

		private PropertyDescriptor findPropertyDescriptor(String property) {
			PropertyDescriptor[] propDescs = getBeanInfo(domainModelClass).getPropertyDescriptors();
			for (PropertyDescriptor propDesc : propDescs) {
				if (propDesc.getName().equals(property)) {
					return propDesc;
				}
			}
			throw new FieldNotFoundException(property);
		}

		private BeanInfo getBeanInfo(Class<?> clazz) {
			try {
				return Introspector.getBeanInfo(clazz);
			} catch (IntrospectionException e) {
				throw new IllegalStateException("Unable to introspect model type " + clazz);
			}
		}

		private void createValueIfNecessary() {
			Object value = fieldModel.getValue();
			if (value == null) {
				value = newValue(fieldModel.getValueType());
				fieldModel.applySubmittedValue(value);
				fieldModel.commit();
			}
		}

		private void createMapValueIfNecessary() {
			Object value = fieldModel.getValue();
			if (value == null) {
				value = newMapValue(fieldModel.getValueType());
				fieldModel.applySubmittedValue(value);
				fieldModel.commit();
			}
		}

		private void growListIfNecessary(int index) {
			Object list = fieldModel.getValue();
			if (list == null) {
				list = newListValue(fieldModel.getValueType());
				fieldModel.applySubmittedValue(list);
				fieldModel.commit();
				list = fieldModel.getValue();
			}
			if (list instanceof List) {
				List jdkList = (List) list;
				if (index >= jdkList.size()) {
					Class<?> elementType = getElementType();
					for (int i = jdkList.size(); i <= index; i++) {
						jdkList.add(newValue(elementType));
					}
				}
			} else if (list.getClass().isArray()) {
				int length = Array.getLength(list);
				if (index >= length) {
					Class<?> elementType = getElementType();	
					Object array = Array.newInstance(elementType, index);
					System.arraycopy(list, 0, array, 0, length);
					for (int i = length; i <= index; i++) {
						Array.set(array, i, newValue(elementType));
					}
				}
			}
		}

		private Map newMapValue(Class<?> type) {
			if (type.isInterface()) {
				return (Map) newValue(LinkedHashMap.class);
			} else {
				return (Map) newValue(type);
			}
		}

		private Object newListValue(Class<?> type) {
			if (List.class.isAssignableFrom(type) && type.isInterface()) {
				return newValue(ArrayList.class);
			} else {
				return newValue(type);
			}
		}

		private Object newValue(Class<?> type) {
			try {
				if (type.isArray()) {
					Class<?> elementType = getElementType();
					if (elementType.isArray()) {
						Object array = Array.newInstance(getElementType(), 1);
						Array.set(array, 0, Array.newInstance(elementType.getComponentType(), 1));
						return array;
					} else {
						return Array.newInstance(elementType, 1);
					}
				} else {
					return type.newInstance();
				}
			} catch (InstantiationException e) {
				throw new IllegalStateException("Could not instantiate element of type [" + type.getName() + "]", e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Could not instantiate element of type [" + type.getName() + "]", e);
			}
		}

	}

	private static class ListElementContext implements FieldModelContext {

		private int index;

		private PropertyFieldModelRule listContext;

		final Map<String, FieldModel> nestedBindings = new HashMap<String, FieldModel>();

		private Map<Integer, FieldModel> listElements;

		public ListElementContext(int index, PropertyFieldModelRule listBindingContext) {
			this.index = index;
			this.listContext = listBindingContext;
		}

		public MessageSource getMessageSource() {
			return listContext.getMessageSource();
		}

		public Locale getLocale() {
			return listContext.getLocale();
		}

		public ConversionService getConversionService() {
			return listContext.getConversionService();
		}

		@SuppressWarnings("unchecked")
		public Formatter getFormatter() {
			return listContext.getElementFormatter();
		}

		@SuppressWarnings("unchecked")
		public Formatter getElementFormatter() {
			// TODO multi-dimensional support
			return null;
		}

		@SuppressWarnings("unchecked")
		public Formatter getKeyFormatter() {
			// TODO multi-dimensional support
			return null;
		}

		public Condition getEditableCondition() {
			return listContext.getEditableCondition();
		}

		public Condition getEnabledCondition() {
			return listContext.getEnabledCondition();
		}

		public Condition getVisibleCondition() {
			return listContext.getVisibleCondition();
		}

		public String getLabel() {
			return listContext.getLabel() + "[" + index + "]";
		}

		public FieldModel getNested(String property) {
			Object model = ((List<?>) listContext.fieldModel.getValue()).get(index);
			Class<?> elementType = listContext.getElementType();
			if (elementType == null) {
				elementType = model.getClass();
			}
			PropertyFieldModelRule rule = listContext.getNestedRule(property, elementType);
			FieldModel binding = nestedBindings.get(property);
			if (binding == null) {
				PropertyValueModel valueModel = new PropertyValueModel(rule.property, model);
				binding = new DefaultFieldModel(valueModel, rule);
				nestedBindings.put(property, binding);
			}
			return binding;
		}

		public FieldModel getListElement(int index) {
			// TODO multi-dimensional support		
			throw new IllegalArgumentException("Not yet supported");
		}

		public FieldModel getMapValue(Object key) {
			// TODO multi-dimensional support			
			throw new IllegalArgumentException("Not yet supported");
		}
	};

	private static class MapValueContext implements FieldModelContext {

		private Object key;

		private PropertyFieldModelRule mapContext;

		final Map<String, FieldModel> nestedBindings = new HashMap<String, FieldModel>();

		public MapValueContext(Object key, PropertyFieldModelRule mapContext) {
			this.key = key;
			this.mapContext = mapContext;
		}

		public MessageSource getMessageSource() {
			return mapContext.getMessageSource();
		}

		public Locale getLocale() {
			return mapContext.getLocale();
		}

		public ConversionService getConversionService() {
			return mapContext.getConversionService();
		}

		@SuppressWarnings("unchecked")
		public Formatter getFormatter() {
			return mapContext.getElementFormatter();
		}

		@SuppressWarnings("unchecked")
		public Formatter getElementFormatter() {
			// TODO multi-dimensional support
			return null;
		}

		@SuppressWarnings("unchecked")
		public Formatter getKeyFormatter() {
			// TODO multi-dimensional support
			return null;
		}

		public Condition getEditableCondition() {
			return mapContext.getEditableCondition();
		}

		public Condition getEnabledCondition() {
			return mapContext.getEnabledCondition();
		}

		public Condition getVisibleCondition() {
			return mapContext.getVisibleCondition();
		}

		@SuppressWarnings("unchecked")
		public FieldModel getNested(String property) {
			Object model = ((Map) mapContext.fieldModel.getValue()).get(key);
			Class<?> elementType = mapContext.getElementType();
			if (elementType == null) {
				elementType = model.getClass();
			}
			PropertyFieldModelRule rule = mapContext.getNestedRule(property, elementType);
			FieldModel binding = nestedBindings.get(property);
			if (binding == null) {
				PropertyValueModel valueModel = new PropertyValueModel(rule.property, model);
				binding = new DefaultFieldModel(valueModel, rule);
				nestedBindings.put(property, binding);
			}
			return binding;
		}

		public FieldModel getListElement(int index) {
			// TODO multi-dimensional support
			throw new IllegalArgumentException("Not yet supported");
		}

		public FieldModel getMapValue(Object key) {
			// TODO multi-dimensional support
			throw new IllegalArgumentException("Not yet supported");
		}

		public String getLabel() {
			return mapContext.getLabel() + "[" + key + "]";
		}

	};
}
