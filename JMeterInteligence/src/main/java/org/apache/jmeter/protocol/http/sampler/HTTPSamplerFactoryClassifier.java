/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.protocol.http.sampler;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * Factory to return the appropriate HTTPSampler for use with classes that need
 * an HTTPSampler; also creates the implementations for use with
 * HTTPSamplerProxy.
 * 
 */
public final class HTTPSamplerFactoryClassifier {

	// N.B. These values are used in jmeter.properties (jmeter.httpsampler) - do
	// not change
	// They can alse be used as the implementation name
	/** Use the the default Java HTTP implementation */
	public static final String HTTP_SAMPLER_JAVA = "HTTPSampler"; //$NON-NLS-1$

	/** Use Apache HTTPClient HTTP implementation */
	public static final String HTTP_SAMPLER_APACHE = "HTTPSampler2"; //$NON-NLS-1$

	// + JMX implementation attribute values (also displayed in GUI) - do not
	// change
	public static final String IMPL_HTTP_CLIENT4 = "HttpClient4"; // $NON-NLS-1$

	public static final String IMPL_HTTP_CLIENT3_1 = "HttpClient3.1"; // $NON-NLS-1$

	public static final String IMPL_JAVA = "Java"; // $NON-NLS-1$

	public static final String IMPL_JAVA_CLASSIFIER = "JavaClassifier"; // $NON-NLS-1$
	// - JMX

	public static final String DEFAULT_CLASSNAME = "org.apache.jmeter.protocol.http.sampler.HTTPJavaImplClassifier"; //$NON-NLS-1$

	private HTTPSamplerFactoryClassifier() {
		// Not intended to be instantiated
	}

	/**
	 * Create a new instance of the default sampler
	 * 
	 * @return instance of default sampler
	 */
	public static HTTPSamplerBaseClassifier newInstance() {
		return newInstance(DEFAULT_CLASSNAME);
	}

	/**
	 * Create a new instance of the required sampler type
	 * 
	 * @param alias
	 *            HTTP_SAMPLER or HTTP_SAMPLER_APACHE or IMPL_HTTP_CLIENT3_1 or
	 *            IMPL_HTTP_CLIENT4
	 * @return the appropriate sampler
	 * @throws UnsupportedOperationException
	 *             if alias is not recognised
	 */
	public static HTTPSamplerBaseClassifier newInstance(String alias) {
		return new HTTPSamplerProxyClassifier(DEFAULT_CLASSNAME);

	}

	public static String[] getImplementations() {
		return new String[] { DEFAULT_CLASSNAME };
	}

	public static HTTPAbstractImplClassifier getImplementation(String impl,
			HTTPSamplerBaseClassifier base) {
		return new HTTPJavaImplClassifier(base);
	}

}
