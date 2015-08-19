partialresponse
===============

## Introduction
Using PartialResponseBuilder class you can create partial response. Use epr_process_ajax_response(responseContent) JS function to process partial response.

## Dependency
Use [webresource](https://github.com/everit-org/webresource) dependency to provide JQuery.
```xml
<Provide-Capability>
  everit.webresource;name=JQuery;resourceFolder=/META-INF/jQuery;libraryPrefix=JQuery
</Provide-Capability>
```

## Usage
See in tests project. Build (mvn clean bundle:install) and run (./tests/target/eosgi-dist/equinoxtest/bin/runConsole.sh or runConsole.bat) tests project.

Create partial response:
```java
try (PartialResponseBuilder prb = new PartialResponseBuilder(response)) {
  prb.prepend("#sub_div_0_msg", "prepend_");
  prb.append("#sub_div_0_msg", "_append");
}
```

Call and process partial response.
```js
$.ajax({
  // call servlet
}).done(function(msg) {
  epr_process_ajax_response(msg);
});
```

To full sample to see IndexServlet.java and index.html in tests project.
