/*
 * Copyright 2010-2011 the original author or authors.
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

package org.springframework.cache.annotation;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.cache.interceptor.AbstractFallbackCacheOperationSource;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.util.Assert;

/**
 * 
 * Implementation of the {@link org.springframework.cache.interceptor.CacheOperationSource}
 * interface for working with caching metadata in JDK 1.5+ annotation format.
 *
 * <p>This class reads Spring's JDK 1.5+ {@link Cacheable} and {@link CacheEvict} 
 * annotations and exposes corresponding caching operation definition to Spring's cache infrastructure.
 * This class may also serve as base class for a custom CacheOperationSource.
 *
 * @author Costin Leau
 */
@SuppressWarnings("serial")
public class AnnotationCacheOperationSource extends AbstractFallbackCacheOperationSource implements
		Serializable {

	private final boolean publicMethodsOnly;

	private final Set<CacheAnnotationParser> annotationParsers;

	/**
	 * Create a default AnnotationCacheOperationSource, supporting
	 * public methods that carry the <code>Cacheable</code> and <code>CacheEvict</code>
	 * annotations.
	 */
	public AnnotationCacheOperationSource() {
		this(true);
	}

	/**
	 * Create a custom AnnotationCacheOperationSource, supporting
	 * public methods that carry the <code>Cacheable</code> and 
	 * <code>CacheEvict</code> annotations.
	 *  
	 * @param publicMethodsOnly whether to support only annotated public methods
	 * typically for use with proxy-based AOP), or protected/private methods as well
	 * (typically used with AspectJ class weaving)
	 */
	public AnnotationCacheOperationSource(boolean publicMethodsOnly) {
		this.publicMethodsOnly = publicMethodsOnly;
		this.annotationParsers = new LinkedHashSet<CacheAnnotationParser>(1);
		this.annotationParsers.add(new SpringCachingAnnotationParser());
	}

	/**
	 * Create a custom AnnotationCacheOperationSource.
	 * @param annotationParsers the CacheAnnotationParser to use
	 */
	public AnnotationCacheOperationSource(CacheAnnotationParser... annotationParsers) {
		this.publicMethodsOnly = true;
		Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
		Set<CacheAnnotationParser> parsers = new LinkedHashSet<CacheAnnotationParser>(annotationParsers.length);
		Collections.addAll(parsers, annotationParsers);
		this.annotationParsers = parsers;
	}

	@Override
	protected CacheOperation findCacheOperation(Class<?> clazz) {
		return determineCacheOperation(clazz);
	}

	@Override
	protected CacheOperation findCacheOperation(Method method) {
		return determineCacheOperation(method);
	}

	/**
	 * Determine the cache operation definition for the given method or class.
	 * <p>This implementation delegates to configured
	 * {@link CacheAnnotationParser CacheAnnotationParsers}
	 * for parsing known annotations into Spring's metadata attribute class.
	 * Returns <code>null</code> if it's not cacheable.
	 * <p>Can be overridden to support custom annotations that carry caching metadata.
	 * @param ae the annotated method or class
	 * @return CacheOperation the configured caching operation,
	 * or <code>null</code> if none was found
	 */
	protected CacheOperation determineCacheOperation(AnnotatedElement ae) {
		for (CacheAnnotationParser annotationParser : this.annotationParsers) {
			CacheOperation attr = annotationParser.parseCacheAnnotation(ae);
			if (attr != null) {
				return attr;
			}
		}
		return null;
	}

	/**
	 * By default, only public methods can be made cacheable.
	 */
	@Override
	protected boolean allowPublicMethodsOnly() {
		return this.publicMethodsOnly;
	}
}