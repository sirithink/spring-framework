/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.support;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.URIEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.PlaceholderResolvingStringValueResolver;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;
import org.springframework.util.SystemPropertiesPropertyResolver;

/**
 * PropertyEditorRegistrar implementation that populates a given
 * {@link org.springframework.beans.PropertyEditorRegistry}
 * (typically a {@link org.springframework.beans.BeanWrapper} used for bean
 * creation within an {@link org.springframework.context.ApplicationContext})
 * with resource editors. Used by
 * {@link org.springframework.context.support.AbstractApplicationContext}.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ResourceEditorRegistrar implements PropertyEditorRegistrar {

	private final ResourceLoader resourceLoader;
	private final StringValueResolver placeholderResolver;


	/**
	 * Create a new ResourceEditorRegistrar for the given ResourceLoader
	 * @param resourceLoader the ResourceLoader (or ResourcePatternResolver)
	 * to create editors for (usually an ApplicationContext)
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.context.ApplicationContext
	 */
	public ResourceEditorRegistrar(ResourceLoader resourceLoader) {
		this(resourceLoader, new PlaceholderResolvingStringValueResolver(new PropertyPlaceholderHelper(true), new SystemPropertiesPropertyResolver()));
	}

	/**
	 * Create a new ResourceEditorRegistrar for the given ResourceLoader
	 * @param resourceLoader the ResourceLoader (or ResourcePatternResolver)
	 * to create editors for (usually an ApplicationContext)
	 * @param placeholderResolver a StringValueResolver used to resolve 
	 * placeholders in resource paths
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.context.ApplicationContext
	 */
	public ResourceEditorRegistrar(ResourceLoader resourceLoader, StringValueResolver placeholderResolver) {
		this.resourceLoader = resourceLoader;
		this.placeholderResolver = placeholderResolver;
	}


	/**
	 * Populate the given bean factory with the following resource editors:
	 * ResourceEditor, InputStreamEditor, FileEditor, URLEditor, ClassEditor, URIEditor.
	 * <p>In case of a {@link org.springframework.core.io.support.ResourcePatternResolver},
	 * a ResourceArrayPropertyEditor will be registered as well.
	 * @see org.springframework.core.io.ResourceEditor
	 * @see org.springframework.beans.propertyeditors.InputStreamEditor
	 * @see org.springframework.beans.propertyeditors.FileEditor
	 * @see org.springframework.beans.propertyeditors.URLEditor
	 * @see org.springframework.beans.propertyeditors.ClassEditor
	 * @see org.springframework.beans.propertyeditors.URIEditor
	 * @see org.springframework.core.io.support.ResourceArrayPropertyEditor
	 */
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader, this.placeholderResolver);
		registry.registerCustomEditor(Resource.class, baseEditor);
		registry.registerCustomEditor(InputStream.class, new InputStreamEditor(baseEditor));
		registry.registerCustomEditor(File.class, new FileEditor(baseEditor));
		registry.registerCustomEditor(URL.class, new URLEditor(baseEditor));

		ClassLoader classLoader = this.resourceLoader.getClassLoader();
		registry.registerCustomEditor(Class.class, new ClassEditor(classLoader));
		registry.registerCustomEditor(URI.class, new URIEditor(classLoader));

		if (this.resourceLoader instanceof ResourcePatternResolver) {
			registry.registerCustomEditor(Resource[].class,
					new ResourceArrayPropertyEditor((ResourcePatternResolver) this.resourceLoader, this.placeholderResolver));
		}
	}

}
