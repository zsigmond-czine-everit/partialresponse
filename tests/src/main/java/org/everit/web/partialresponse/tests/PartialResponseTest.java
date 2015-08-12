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
package org.everit.web.partialresponse.tests;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.osgi.jetty.server.component.JettyServerConstants;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * Tests partial response.
 */
@Component
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({
    @StringAttribute(attributeId = "eosgi.testId", defaultValue = "PartialResponseTest"),
    @StringAttribute(attributeId = "eosgi.testEngine", defaultValue = "junit4") })
@Service
public class PartialResponseTest {

  /**
   * Data holder to expected texts.
   */
  private static class ExpectedTextMsgDTO {

    public String divTable1CellMsg;

    public String divTable2CellMsg;

    public String newContent;

    public String sampleMsg;

    public String subDiv0Msg;

    public String subDiv1Msg;

    public String subDiv2Msg;

    public ExpectedTextMsgDTO divTable1CellMsg(final String divTable1CellMsg) {
      this.divTable1CellMsg = divTable1CellMsg;
      return this;
    }

    public ExpectedTextMsgDTO divTable2CellMsg(final String divTable2CellMsg) {
      this.divTable2CellMsg = divTable2CellMsg;
      return this;
    }

    public ExpectedTextMsgDTO newContent(final String newContent) {
      this.newContent = newContent;
      return this;
    }

    public ExpectedTextMsgDTO sampleMsg(final String sampleMsg) {
      this.sampleMsg = sampleMsg;
      return this;
    }

    public ExpectedTextMsgDTO subDiv0Msg(final String subDiv0Msg) {
      this.subDiv0Msg = subDiv0Msg;
      return this;
    }

    public ExpectedTextMsgDTO subDiv1Msg(final String subDiv1Msg) {
      this.subDiv1Msg = subDiv1Msg;
      return this;
    }

    public ExpectedTextMsgDTO subDiv2Msg(final String subDiv2Msg) {
      this.subDiv2Msg = subDiv2Msg;
      return this;
    }

  }

  private static final int ONE_HUNDRED = 1000;

  private static final String WINDOW_NAME = "testWindow";

  private int jettyPort;

  private void assertPageTexts(final HtmlPage page, final ExpectedTextMsgDTO expectedTextMsgDTO) {
    Assert.assertNotNull(expectedTextMsgDTO);

    assertText(page, "sub_div_0_msg", expectedTextMsgDTO.subDiv0Msg);
    assertText(page, "sub_div_1_msg", expectedTextMsgDTO.subDiv1Msg);
    assertText(page, "div_table_1_cell_msg", expectedTextMsgDTO.divTable1CellMsg);
    assertText(page, "sub_div_2_msg", expectedTextMsgDTO.subDiv2Msg);
    assertText(page, "div_table_2_cell_msg", expectedTextMsgDTO.divTable2CellMsg);
    assertText(page, "sample_msg", expectedTextMsgDTO.sampleMsg);
    assertText(page, "new_content", expectedTextMsgDTO.newContent);
  }

  private void assertText(final HtmlPage page, final String elementId, final String expectedText) {
    String actualText = getTextContentById(page, elementId);
    Assert.assertEquals("[ elementId: " + elementId + "]", expectedText, actualText);
  }

  private <R> R cast(final Object object) {
    if (object == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    R result = (R) object;
    return result;
  }

  private ExpectedTextMsgDTO createDefaultExpectedTextMsgDTO() {
    return new ExpectedTextMsgDTO()
        .subDiv0Msg("default_sub_div_0_msg")
        .subDiv1Msg("default_sub_div_1_msg")
        .divTable1CellMsg("default_div_table_1_cell_msg")
        .subDiv2Msg("default_sub_div_2_msg")
        .divTable2CellMsg("default_div_table_2_cell_msg")
        .sampleMsg("default_sample_msg")
        .newContent("default_new_content");
  }

  private String getTextContentById(final HtmlPage page, final String id) {
    DomElement element = page.getElementById(id);
    if (element == null) {
      return null;
    }
    return element.getTextContent();
  }

  private WebWindow openWindow(final WebClient webClient, final String url,
      final String windowName) {
    try {
      return webClient.openWindow(new URL(url), windowName);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Resolves Jetty server port.
   *
   * @param server
   *          The running Jetty server.
   */
  @ServiceRef(defaultValue = "(service.factoryPid=" + JettyServerConstants.FACTORY_PID + ")")
  public void setServer(final Server server) {
    Connector[] connectors = server.getConnectors();
    NetworkConnector connector = (NetworkConnector) connectors[0];
    jettyPort = connector.getLocalPort();
  }

  @Test
  @TestDuringDevelopment
  public void testAppend() throws IOException {
    try (WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11)) {
      webClient.setAjaxController(new NicelyResynchronizingAjaxController());
      webClient.getOptions().setUseInsecureSSL(true);
      WebWindow webWindow = openWindow(webClient, "https://localhost:" + jettyPort, WINDOW_NAME);
      Page page = webWindow.getEnclosedPage();

      HtmlPage htmlPage = cast(page);
      assertPageTexts(htmlPage, createDefaultExpectedTextMsgDTO());

      HtmlAnchor append = cast(htmlPage.getElementById("append"));
      append.click();
      webClient.waitForBackgroundJavaScript(ONE_HUNDRED);
      ExpectedTextMsgDTO expectedTextMsgDTO = createDefaultExpectedTextMsgDTO()
          .newContent("default_new_content_append");
      assertPageTexts(htmlPage, expectedTextMsgDTO);
      assertText(htmlPage, "append_new_content", "append_new_content");
      assertText(htmlPage, "after_append", "after_append");
    }
  }

  @Test
  @TestDuringDevelopment
  public void testComplex() throws IOException {
    try (WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11)) {
      webClient.setAjaxController(new NicelyResynchronizingAjaxController());
      webClient.getOptions().setUseInsecureSSL(true);
      WebWindow webWindow = openWindow(webClient, "https://localhost:" + jettyPort, WINDOW_NAME);
      Page page = webWindow.getEnclosedPage();

      HtmlPage htmlPage = cast(page);
      assertPageTexts(htmlPage, createDefaultExpectedTextMsgDTO());

      HtmlAnchor complex = cast(htmlPage.getElementById("complex"));
      complex.click();
      webClient.waitForBackgroundJavaScript(ONE_HUNDRED);
      ExpectedTextMsgDTO expectedTextMsgDTO = createDefaultExpectedTextMsgDTO()
          .newContent("prepend_default_new_content_append")
          .subDiv0Msg("replace_by_id_sub_div_0_msg")
          .subDiv1Msg("update__SUB_DIV_1_sub_div_1_msg")
          .divTable1CellMsg("update__SUB_DIV_1_div_table_1_cell_msg")
          .subDiv2Msg("replace_by_id_sub_div_2_msg")
          .divTable2CellMsg("replace_by_id_div_table_2_cell_msg");
      assertPageTexts(htmlPage, expectedTextMsgDTO);
    }
  }

  @Test
  @TestDuringDevelopment
  public void testPrepend() throws IOException {
    try (WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11)) {
      webClient.setAjaxController(new NicelyResynchronizingAjaxController());
      webClient.getOptions().setUseInsecureSSL(true);
      WebWindow webWindow = openWindow(webClient, "https://localhost:" + jettyPort, WINDOW_NAME);
      Page page = webWindow.getEnclosedPage();

      HtmlPage htmlPage = cast(page);
      assertPageTexts(htmlPage, createDefaultExpectedTextMsgDTO());

      HtmlAnchor prepend = cast(htmlPage.getElementById("prepend"));
      prepend.click();
      webClient.waitForBackgroundJavaScript(ONE_HUNDRED);
      ExpectedTextMsgDTO expectedTextMsgDTO = createDefaultExpectedTextMsgDTO()
          .newContent("prepend_default_new_content");
      assertPageTexts(htmlPage, expectedTextMsgDTO);
      assertText(htmlPage, "prepend_new_content", "prepend_new_content");
      assertText(htmlPage, "after_prepend", "after_prepend");
    }
  }

  @Test
  @TestDuringDevelopment
  public void testReplace() throws IOException {
    try (WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11)) {
      webClient.setAjaxController(new NicelyResynchronizingAjaxController());
      webClient.getOptions().setUseInsecureSSL(true);
      WebWindow webWindow = openWindow(webClient, "https://localhost:" + jettyPort, WINDOW_NAME);
      Page page = webWindow.getEnclosedPage();

      HtmlPage htmlPage = cast(page);
      assertPageTexts(htmlPage, createDefaultExpectedTextMsgDTO());

      HtmlAnchor replace = cast(htmlPage.getElementById("replace"));
      replace.click();
      webClient.waitForBackgroundJavaScript(ONE_HUNDRED);
      ExpectedTextMsgDTO expectedTextMsgDTO = createDefaultExpectedTextMsgDTO()
          .subDiv0Msg("update__SUB_DIV_0_sub_div_0_msg")
          .subDiv1Msg("update__SUB_DIV_1_sub_div_1_msg")
          .divTable1CellMsg("update__SUB_DIV_1_div_table_1_cell_msg")
          .subDiv2Msg("replace_sub_div_2_msg")
          .divTable2CellMsg("update__DIV_TABLE_2_div_table_2_cell_msg")
          .newContent("replace_new_content_with_hard_code_html");
      assertPageTexts(htmlPage, expectedTextMsgDTO);
    }
  }

  @Test
  @TestDuringDevelopment
  public void testReplaceById() throws IOException {
    try (WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11)) {
      webClient.setAjaxController(new NicelyResynchronizingAjaxController());
      webClient.getOptions().setUseInsecureSSL(true);
      WebWindow webWindow = openWindow(webClient, "https://localhost:" + jettyPort, WINDOW_NAME);
      Page page = webWindow.getEnclosedPage();

      HtmlPage htmlPage = cast(page);
      assertPageTexts(htmlPage, createDefaultExpectedTextMsgDTO());

      HtmlAnchor replaceById = cast(htmlPage.getElementById("replace_by_id"));
      replaceById.click();
      webClient.waitForBackgroundJavaScript(ONE_HUNDRED);
      ExpectedTextMsgDTO expectedTextMsgDTO = createDefaultExpectedTextMsgDTO()
          .subDiv0Msg("update__SUB_DIV_0_sub_div_0_msg")
          .subDiv1Msg("update__SUB_DIV_1_sub_div_1_msg")
          .divTable1CellMsg("update__SUB_DIV_1_div_table_1_cell_msg")
          .subDiv2Msg("replace_by_id_sub_div_2_msg")
          .divTable2CellMsg("update__DIV_TABLE_2_div_table_2_cell_msg")
          .newContent("replace_by_id_new_content_with_hard_code_html");
      assertPageTexts(htmlPage, expectedTextMsgDTO);
    }
  }
}
