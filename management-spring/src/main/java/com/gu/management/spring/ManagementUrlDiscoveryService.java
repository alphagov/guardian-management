/*
 * Copyright 2010 Guardian News and Media
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.gu.management.spring;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ManagementUrlDiscoveryService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ManagementUrlDiscoveryService.class);

	private Set<String> urls;
    private List<AbstractUrlHandlerMapping> handlerMappings;

    @Autowired
    public void setHandlerMappings(List<AbstractUrlHandlerMapping> handlerMappings) {
        this.handlerMappings = ImmutableList.copyOf(handlerMappings);
    }

	private Set<String> getAllRequestableUrls(AbstractUrlHandlerMapping urlHandlerMappings) {
		Set<String> urls = Sets.newTreeSet();       

		Map<String, Object> urlMap = urlHandlerMappings.getHandlerMap();
		for (Entry<String, Object> urlMapping : urlMap.entrySet()) {
			String url = urlMapping.getKey();
			Object controller = urlMapping.getValue();

			if (!url.contains("*")) {
                LOGGER.debug("");
                LOGGER.debug(String.format("Adding %s provided by %s without further inspection because it's not a wildcard url",
                        url, controller));
				urls.add(url);

			} else {

                LOGGER.debug(String.format("Inspecting for urls under %s provided by %s...", url, controller));
				urls.addAll(getUrlsForController(url, controller));
                LOGGER.debug(String.format("Inspected for urls under %s provided by %s", url, controller));

			}
		}
		return urls;
	}

	private Collection<String> getUrlsForController(String baseUrl, Object controller) {
		List<String> urlsForController = Lists.newArrayList();

		if (controller instanceof MultiActionController) {
			MultiActionController multiController = (MultiActionController) controller;
			MethodNameResolver methodNameResolver = multiController.getMethodNameResolver();

            // look for an explicit mapping first
			if (methodNameResolver instanceof PropertiesMethodNameResolver) {
				Set<Object> keySet = ((PropertiesMethodNameResolver) methodNameResolver).getMappings().keySet();
				for (Object key : keySet) {
                    LOGGER.debug(String.format("Adding %s due to explict mapping in MultiActionController", key));
					urlsForController.add(((String) key));
				}
				return urlsForController;
			}

            // now look for auto mappings
    		for (Method method : controller.getClass().getDeclaredMethods()) {
                if (isRequestableMethod(method)) {
                    String methodPath = replaceDoubleStarWithValue(method.getName(), baseUrl);
                    LOGGER.debug(String.format("Adding %s due to implicit mapping in MultiActionController", methodPath));
                    urlsForController.add(methodPath);
                }
            }

            return urlsForController;
		}

        for (Method method : controller.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                for (String url : method.getAnnotation(RequestMapping.class).value()) {
                    LOGGER.debug(String.format("Adding %s due to @RequestMapping attribute", url));
                    urlsForController.add(url);
                }
            }
        }

        if (controller instanceof Controller && urlsForController.isEmpty()) {
            String methodPath = replaceDoubleStarWithValue("", baseUrl);
            LOGGER.debug(String.format("Adding %s because it maps to a simple controller and we haven't found anything else", methodPath));
            urlsForController.add(methodPath);
        }

		return urlsForController;
	}

    private String replaceDoubleStarWithValue(String valueToReplaceWildCard, String baseUrl) {
        String methodPath = baseUrl.replace("**", valueToReplaceWildCard);
        if (methodPath.endsWith("/")) {
            methodPath = methodPath.substring(0, methodPath.length() - 1);
        }
        return methodPath;
    }

    private boolean isRequestableMethod(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();

		boolean isPublic = Modifier.isPublic(method.getModifiers());
		boolean isProtected = Modifier.isProtected(method.getModifiers());
		boolean returnsModelAndView = method.getReturnType().equals(ModelAndView.class);
		boolean takesRequestAndResponse = parameterTypes.length >= 2 && HttpServletRequest.class.isAssignableFrom(parameterTypes[0]) &&
				HttpServletResponse.class.isAssignableFrom(parameterTypes[1]);

		return (isPublic || isProtected) && (returnsModelAndView || takesRequestAndResponse);
	}

	public Collection<String> getManagementUrls() {
		if (urls == null) {
            LOGGER.debug("Inspecting to find management urls...");
            Set<String> urlSet = Sets.newTreeSet();
            for (AbstractUrlHandlerMapping handlerMapping : handlerMappings) {
                LOGGER.debug(String.format("Processing %s...", handlerMapping));
                urlSet.addAll(getAllRequestableUrls(handlerMapping));
                LOGGER.debug(String.format("Processed %s", handlerMapping));
            }
            urls = ImmutableSet.copyOf(urlSet);
            LOGGER.debug("Inspection of management urls complete");
		}
		return urls;
	}
}
