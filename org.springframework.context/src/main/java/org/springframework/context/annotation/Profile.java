/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.context.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.type.AnnotationMetadata;

/**
 * TODO SPR-7508: document
 * 
 * Components not @Profile-annotated will always be registered
 * ConfigurableEnvironment.setActiveProfiles(String...) sets which profiles are active
 * 'spring.profile.active' sets which profiles are active (typically as a -D system property)
   servlet context/init param)
 * ConfigurableEnvironment.setDefaultProfiles(String...) or 'spring.profile.default' property specifies one
   or more default profiles, e.g., 'default'
 * if 'default' is specified as a default profile, @Profile({"xyz,default}) means that beans will be
   registered if 'xyz' is active or if no profile is active
 *
 * @author Chris Beams
 * @since 3.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
	ANNOTATION_TYPE, // @Profile may be used as a meta-annotation
	TYPE             // In conjunction with @Component and its derivatives
})
public @interface Profile {

	/**
	 * The set profiles for which, if active, this component should be registered.
	 */
	String[] value();


	static class Helper {
		/**
		 * @return whether the given metadata includes Profile information, whether directly or
		 * through meta-annotation
		 */
		static boolean isProfileAnnotationPresent(AnnotationMetadata metadata) {
			return metadata.isAnnotated(Profile.class.getName());
		}

		static String[] getCandidateProfiles(AnnotationMetadata metadata) {
			return (String[])metadata.getAnnotationAttributes(Profile.class.getName()).get("value");
		}
	}
}
