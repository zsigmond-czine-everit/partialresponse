/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.web.partialresponse.tests.servlet;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletContext;

import org.everit.osgi.webresource.util.CommonContextWebResourceURIGenerator;

/**
 * Helper class to generate {@link org.everit.osgi.webresource.WebResource} URI based on {@link Map}
 * interface. The key should be formed in the following way: "library:resourceName:versionRange".
 */
public class WebResourceURIMap implements Map<String, String> {

  private final CommonContextWebResourceURIGenerator uriGenerator;

  public WebResourceURIMap(final ServletContext servletContext) {
    this.uriGenerator = new CommonContextWebResourceURIGenerator(servletContext);
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsKey(final Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsValue(final Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String get(final Object key) {
    String complexWebResourceId = String.valueOf(key);
    String[] webResourceIdParts = complexWebResourceId.split(":");

    String lib = webResourceIdParts[0];

    if (webResourceIdParts.length < 2) {
      throw new IllegalArgumentException(
          "WebResource id must contain at least the lib and resource name"
              + " separated with double points: " + key);
    }

    String file = webResourceIdParts[1];

    Optional<String> versionRange = Optional.empty();
    if (webResourceIdParts.length > 2) {
      versionRange = Optional.of(webResourceIdParts[2]);
    }
    Optional<String> generateURI = uriGenerator.generateURI(lib, file, versionRange);
    return generateURI
        .orElseThrow(() -> new NoSuchElementException("WebResource not found: " + key));
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String put(final String key, final String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(final Map<? extends String, ? extends String> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String remove(final Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<String> values() {
    throw new UnsupportedOperationException();
  }

}
