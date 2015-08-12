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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.expression.ParserConfiguration;
import org.everit.expression.mvel.MvelExpressionCompiler;
import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.HTMLTemplateCompiler;
import org.everit.templating.text.TextTemplateCompiler;
import org.everit.web.partialresponse.core.PartialResponseBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * Servlet for test.
 */
@Component
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@Service(value = { Servlet.class, IndexServlet.class })
public class IndexServlet implements Servlet {

  private static final String DEFAULT_STRING = "default";

  private static final String UPDATE_STRING = "update_";

  private static final String VAR_DIV_TABLE_1_CELL_MSG = "div_table_1_cell_msg";

  private static final String VAR_DIV_TABLE_2_CELL_MSG = "div_table_2_cell_msg";

  private static final String VAR_NEW_CONTENT = "new_content";

  private static final String VAR_SAMPLE_MSG = "sample_msg";

  private static final String VAR_SUB_DIV_0_MSG = "sub_div_0_msg";

  private static final String VAR_SUB_DIV_1_MSG = "sub_div_1_msg";

  private static final String VAR_SUB_DIV_2_MSG = "sub_div_2_msg";

  private ClassLoader classLoader;

  private ServletConfig config;

  private CompiledTemplate pageTemplate;

  /**
   * Compiles the layout template and sets the classLoader member variable.
   */
  @Activate
  public void activate(final BundleContext bundleContext) {
    classLoader = bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader();
    pageTemplate = compileTemplate("META-INF/webcontent/" + getPageId() + ".html");
  }

  private void appendVars(final String prefix, final Map<String, Object> vars) {
    vars.put(VAR_SUB_DIV_0_MSG, prefix + "_sub_div_0_msg");
    vars.put(VAR_SUB_DIV_1_MSG, prefix + "_sub_div_1_msg");
    vars.put(VAR_DIV_TABLE_1_CELL_MSG, prefix + "_div_table_1_cell_msg");
    vars.put(VAR_DIV_TABLE_2_CELL_MSG, prefix + "_div_table_2_cell_msg");
    vars.put(VAR_SUB_DIV_2_MSG, prefix + "_sub_div_2_msg");
    vars.put(VAR_SAMPLE_MSG, prefix + "_sample_msg");
    vars.put(VAR_NEW_CONTENT, prefix + "_new_content");
  }

  /**
   * Compiles a template.
   *
   * @param templateName
   *          The url of the template resource.
   * @param classLoader
   *          The classLoader that is used to find the template resource.
   * @return The compiled template.
   */
  private CompiledTemplate compileTemplate(final String templateName) {
    MvelExpressionCompiler mvelExpressionCompiler = new MvelExpressionCompiler();

    Map<String, TemplateCompiler> inlineCompilers = new HashMap<>();
    inlineCompilers.put("text", new TextTemplateCompiler(mvelExpressionCompiler));

    HTMLTemplateCompiler htmlTemplateCompiler = new HTMLTemplateCompiler(mvelExpressionCompiler,
        inlineCompilers);
    ParserConfiguration parserConfiguration = new ParserConfiguration(classLoader);

    String template = readResourceContent(templateName);

    if (template == null) {
      return null;
    }

    CompiledTemplate result = htmlTemplateCompiler.compile(template, parserConfiguration);
    return result;
  }

  private Map<String, Object> createBasisVars(final HttpServletRequest request) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("resources", new WebResourceURIMap(request.getServletContext()));
    return vars;
  }

  @Override
  public void destroy() {
  }

  private void doAjax(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    String action = req.getParameter("action");
    if ("replace_by_id".equals(action)) {
      doReplaceById(req, resp);
    } else if ("reset_to_default".equals(action)) {
      doResetToDefault(req, resp);
    } else if ("replace".equals(action)) {
      doReplace(req, resp);
    } else if ("append".equals(action)) {
      doAppend(resp);
    } else if ("prepend".equals(action)) {
      doPrepend(resp);
    } else if ("complex".equals(action)) {
      doComplex(req, resp);
    }
  }

  private void doAppend(final HttpServletResponse resp) {
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.append("#new_content", "_append");

      prb.append("#main_div", "<div><div id=\"append_new_content\">append_new_content</div>"
          + "<div id=\"after_append\"></div></div>");

      prb.append("#after_append", "after_append");
    }
  }

  private void doComplex(final HttpServletRequest req, final HttpServletResponse resp) {
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      Map<String, Object> vars = createBasisVars(req);
      appendVars("replace_by_id", vars);

      prb.replaceById(writer -> pageTemplate.render(writer, vars, "main_div"));

      prb.append("#new_content", "_append");

      prb.prepend("#new_content", "prepend_");

      appendVars(UPDATE_STRING + "_SUB_DIV_1", vars);
      prb.replace("#sub_div_1", writer -> pageTemplate.render(writer, vars, "sub_div_1"));
    }
  }

  private void doPrepend(final HttpServletResponse resp) {
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.prepend("#new_content", "prepend_");

      prb.prepend("#main_div", "<div><div id=\"prepend_new_content\">prepend_new_content</div>"
          + "<div id=\"after_prepend\"></div></div>");

      prb.prepend("#after_prepend", "after_prepend");
    }
  }

  private void doReplace(final HttpServletRequest req, final HttpServletResponse resp) {
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.replace("#new_content",
          "<div id=\"new_content\">replace_new_content_with_hard_code_html</div>");

      Map<String, Object> vars = createBasisVars(req);
      appendVars("replace", vars);

      prb.replace("#main_div", writer -> pageTemplate.render(writer, vars, "main_div"));

      appendVars(UPDATE_STRING + "_DIV_TABLE_2", vars);
      prb.replace("#div_table_2", writer -> pageTemplate.render(writer, vars, "div_table_2"));

      appendVars(UPDATE_STRING + "_DIV_TABLE_1", vars);
      prb.replace("#div_table_1", writer -> pageTemplate.render(writer, vars, "div_table_1"));

      appendVars(UPDATE_STRING + "_SUB_DIV_0", vars);
      prb.replace("#sub_div_0", writer -> pageTemplate.render(writer, vars, "sub_div_0"));

      appendVars(UPDATE_STRING + "_SUB_DIV_1", vars);
      prb.replace("div:nth-child(3)",
          writer -> pageTemplate.render(writer, vars, "sub_div_1"));

    }
  }

  private void doReplaceById(final HttpServletRequest req, final HttpServletResponse resp) {
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.replaceById(
          "<div id=\"new_content\">replace_by_id_new_content_with_hard_code_html</div>");

      Map<String, Object> vars = createBasisVars(req);
      appendVars("replace_by_id", vars);

      prb.replaceById(writer -> pageTemplate.render(writer, vars, "main_div"));

      appendVars(UPDATE_STRING + "_DIV_TABLE_2", vars);
      prb.replaceById(writer -> pageTemplate.render(writer, vars, "div_table_2"));

      appendVars(UPDATE_STRING + "_DIV_TABLE_1", vars);
      prb.replaceById(writer -> pageTemplate.render(writer, vars, "div_table_1"));

      appendVars(UPDATE_STRING + "_SUB_DIV_0", vars);
      prb.replaceById(writer -> pageTemplate.render(writer, vars, "sub_div_0"));

      appendVars(UPDATE_STRING + "_SUB_DIV_1", vars);
      prb.replaceById(writer -> pageTemplate.render(writer, vars, "sub_div_1"));

    }
  }

  private void doResetToDefault(final HttpServletRequest req, final HttpServletResponse resp) {
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      Map<String, Object> vars = createBasisVars(req);
      appendVars(DEFAULT_STRING, vars);
      prb.replaceById(writer -> pageTemplate.render(writer, vars, "full_content"));
    }
  }

  private void doService(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    if (isAjaxRequest(request)) {
      doAjax(request, response);
      return;
    }

    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("text/html");
    Map<String, Object> vars = createBasisVars(request);
    appendVars(DEFAULT_STRING, vars);

    pageTemplate.render(response.getWriter(), vars, null);
  }

  private String getPageId() {
    return "index";
  }

  @Override
  public ServletConfig getServletConfig() {
    return config;
  }

  @Override
  public String getServletInfo() {
    return "";
  }

  @Override
  public void init(final ServletConfig pConfig) throws ServletException {
    config = pConfig;
  }

  private boolean isAjaxRequest(final HttpServletRequest request) {
    String ajaxHeader = request.getHeader("x-partialresponse-ajax");
    return !((ajaxHeader == null) || Boolean.FALSE.toString().equalsIgnoreCase(ajaxHeader));
  }

  /**
   * Reads the content of a resource into a String with UTF8 character encoding.
   *
   * @param resource
   *          The name of the resource.
   * @param classLoader
   *          The ClassLoader that sees the resource.
   * @return The content of the file.
   */
  private String readResourceContent(final String resource) {
    URL resourceURL = classLoader.getResource(resource);

    if (resourceURL == null) {
      return null;
    }

    try (InputStream is = resourceURL.openStream()) {
      final int bufferSize = 1024;
      byte[] buffer = new byte[bufferSize];
      int r = is.read(buffer);
      StringBuilder sb = new StringBuilder();
      while (r >= 0) {
        sb.append(new String(buffer, 0, r, StandardCharsets.UTF_8));
        r = is.read(buffer);
      }
      return sb.toString();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void service(final ServletRequest req, final ServletResponse res) throws ServletException,
      IOException {

    HttpServletRequest request;
    HttpServletResponse response;

    if (!((req instanceof HttpServletRequest) && (res instanceof HttpServletResponse))) {
      throw new ServletException("non-HTTP request or response");
    }

    request = (HttpServletRequest) req;
    response = (HttpServletResponse) res;

    doService(request, response);
  }
}
