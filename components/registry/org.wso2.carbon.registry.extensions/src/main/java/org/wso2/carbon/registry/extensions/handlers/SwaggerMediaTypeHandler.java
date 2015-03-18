/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.RESTServiceUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.SwaggerProcessor;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * This class is an implementation of {@link org.wso2.carbon.registry.core.jdbc.handlers.Handler}. This class handles
 * the documents with the media type application/swagger+json.
 */
public class SwaggerMediaTypeHandler extends Handler {

	private String swaggerLocation;
	private String restServiceLocation;

	/**
	 * Extracts the common location for swagger docs from registry.xml entry
	 *
	 * @param locationConfiguration location configuration element
	 */
	public void setSwaggerLocationConfiguration(OMElement locationConfiguration) {
		Iterator confElements = locationConfiguration.getChildElements();
		while (confElements.hasNext()) {
			OMElement confElement = (OMElement)confElements.next();
			if (confElement.getQName().equals(new QName(CommonConstants.LOCATION_TAG))) {
				swaggerLocation = confElement.getText();
				if (!swaggerLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
					swaggerLocation = RegistryConstants.PATH_SEPARATOR + swaggerLocation;
				}
				if (!swaggerLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
					swaggerLocation = swaggerLocation + RegistryConstants.PATH_SEPARATOR;
				}
			}
		}
	}

	/**
	 * Extracts the common location for REST API artifacts from registry.xml entry.
	 *
	 * @param locationConfiguration location configuration element
	 */
	public void setRestServiceLocationConfiguration(OMElement locationConfiguration) {
		Iterator confElements = locationConfiguration.getChildElements();
		while (confElements.hasNext()) {
			OMElement confElement = (OMElement)confElements.next();
			if (confElement.getQName().equals(new QName(CommonConstants.LOCATION_TAG))) {
				restServiceLocation = confElement.getText();
				if (!restServiceLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
					restServiceLocation = RegistryConstants.PATH_SEPARATOR + restServiceLocation;
				}
				if (!restServiceLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
					restServiceLocation = restServiceLocation + RegistryConstants.PATH_SEPARATOR;
				}
			}
		}
		RESTServiceUtils.setCommonRestServiceLocation(restServiceLocation);
	}

	/**
	 * Processes the PUT action for swagger files.
	 *
	 * @param requestContext        information about the current request.
	 * @throws RegistryException    If fails due a handler specific error.
	 */
	@Override
	public void put(RequestContext requestContext) throws RegistryException {
		//Acquiring the update lock if available.
		if (!CommonUtil.isUpdateLockAvailable()) {
			return;
		}
		CommonUtil.acquireUpdateLock();

		InputStream inputStream;
		try {
			Resource resource = requestContext.getResource();

			if (resource == null) {
				throw new RegistryException("Resource does not exist.");
			}

			Object resourceContentObj = resource.getContent();

			if (resourceContentObj == null || !(resourceContentObj instanceof byte[])) {
				throw new RegistryException("Resource content is not valid.");
			}

			inputStream = new ByteArrayInputStream((byte[]) resourceContentObj);
			SwaggerProcessor processor = new SwaggerProcessor(requestContext);
			processor.processSwagger(inputStream, getChrootedLocation(requestContext.getRegistryContext()), null);
			requestContext.setProcessingComplete(true);
		} finally {
			//TODO: Close input stream.
			CommonUtil.releaseUpdateLock();
		}
	}

	/**
	 * Creates a resource in the given path by fetching the resource content from the given URL.
	 *
	 * @param requestContext        information about the current request.
	 * @throws RegistryException    If import fails due a handler specific error
	 */
	@Override
	public void importResource(RequestContext requestContext) throws RegistryException {
		//Acquiring the update lock if available.
		if (!CommonUtil.isUpdateLockAvailable()) {
			return;
		}
		CommonUtil.acquireUpdateLock();

		String sourceURL = null;
		InputStream inputStream = null;
		try {
			sourceURL = requestContext.getSourceURL();

			if (sourceURL == null) {
				throw new RegistryException("Swagger source url is null.");
			}


			if (sourceURL.toLowerCase().startsWith("file:")) {
				throw new RegistryException(
						"The source URL must not be point to a file in the server's local file system. ");
			}
			//Open a stream to the sourceURL
			inputStream = new URL(sourceURL).openStream();

			SwaggerProcessor processor = new SwaggerProcessor(requestContext);
			processor.processSwagger(inputStream, getChrootedLocation(requestContext.getRegistryContext()), sourceURL);
			requestContext.setProcessingComplete(true);
		} catch (IOException e) {
			throw new RegistryException("The URL " + sourceURL + " is incorrect.", e);
		} finally {
			//TODO: inputStream.close()
			CommonUtil.releaseUpdateLock();
		}
	}

	/**
	 * Returns the root location of the Swagger.
	 *
	 * @param registryContext   Registry context
	 * @return                  the root location of the Swagger.
	 */
	private String getChrootedLocation(RegistryContext registryContext) {
		return RegistryUtils
				.getAbsolutePath(registryContext, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + swaggerLocation);
	}

}
