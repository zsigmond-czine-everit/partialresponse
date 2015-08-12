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
function epr_process_ajax_response(responseContent) {
  var responseContentType = $.type(responseContent);
  var responseObj;
  if (responseContentType == 'string') {
    responseObj = $($.parseHTML(responseContent));
  } else {
    responseObj = responseContent.documentElement;
  }

  if (responseObj.prop('nodeName').toUpperCase() != 'PARTIAL-RESPONSE') {
    responseObj.each(function() {
      var rootElementId = $(this).attr('id');
      $('#' + rootElementId).replaceWith(this.outerHTML);
    });
  } else {
    responseObj.children('partial-replace').each(function() {
      var replaceObj = $(this);
      var selector = replaceObj.attr('selector');
      if (typeof selector !== typeof undefined && selector !== false) {
        $(selector).replaceWith(replaceObj.html());
      } else {
        $(this).children().each(function() {
          var newContentObj = $(this);
          var elementId = newContentObj.attr('id');
          var newContentOuterHTML = this.outerHTML;
          $('#' + elementId).replaceWith(newContentOuterHTML);
        });
      }
    });

    responseObj.children('partial-append').each(function() {
      var appendObj = $(this);
      var selector = appendObj.attr('selector');
      $(selector).append(appendObj.html());
    });

    responseObj.children('partial-prepend').each(function() {
      var prependObj = $(this);
      var selector = prependObj.attr('selector');
      $(selector).prepend(prependObj.html());
    });
  }
}
