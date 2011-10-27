/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.stanbol.factstore.web.resource;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.stanbol.commons.testing.http.BundleContextMock;
import org.apache.stanbol.commons.testing.http.ServletContextMock;
import org.apache.stanbol.commons.web.base.resource.BaseStanbolResource;
import org.apache.stanbol.factstore.FactStoreMock;
import org.apache.stanbol.factstore.UriInfoMock;
import org.apache.stanbol.factstore.api.FactStore;
import org.apache.stanbol.factstore.web.resource.FactsResource;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

public class FactsResourceTest {

    private ServletContextMock servletContext;
    private UriInfoMock uriInfoMock;

    @Before
    public void initMocks() {
        this.servletContext = new ServletContextMock();
        this.servletContext.putAttribute(BaseStanbolResource.ROOT_URL, "http://localhost:8080");
        BundleContextMock bc = (BundleContextMock) this.servletContext.getAttribute(BundleContext.class
                .getName());
        bc.putService(FactStore.class.getName(), new FactStoreMock());
        this.uriInfoMock = new UriInfoMock();
    }

    @Test
    public void testGet() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);
        Response response = fr.get();
        assertTrue(response.getStatus() == Status.OK.getStatusCode());
    }

    @Test
    public void testPutFactSchemaNoSchemaURI() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);

        Response response = fr.putFactSchema("", null);
        assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPutFactSchemaNoJSON() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);

        Response response = fr.putFactSchema("no JSON-LD string", "test2");
        assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPutFactSchemaNoJSONSchema() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);

        Response response = fr.putFactSchema("{}", "test2");
        assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTooLongURN() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);

        Response response = fr
                .putFactSchema(
                    "{\"@context\":{\"iks\":\"http://iks-project.eu/ont/\",\"@types\":{\"person\":\"iks:person\",\"organization\":\"iks:organization\"}}}",
                    "http://www.test.de/this/urn/is/a/bit/too/long/to/be/used/in/this/fact/store/implementation/with/derby");
        assertTrue(response.getStatus() == Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPutFactSchemaValidInput() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);

        Response response = fr
                .putFactSchema(
                    "{\"@context\":{\"iks\":\"http://iks-project.eu/ont/\",\"@types\":{\"person\":\"iks:person\",\"organization\":\"iks:organization\"}}}",
                    "test2");
        assertTrue(response.getStatus() == Status.CREATED.getStatusCode());
    }

    @Test
    public void testPostSingleFact() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);

        Response response = fr
                .postFacts("{\"@context\":{\"iks\":\"http://iks-project.eu/ont/\",\"upb\":\"http://upb.de/persons/\"},\"@profile\":\"iks:employeeOf\",\"person\":{\"@iri\":\"upb:bnagel\"},\"organization\":{\"@iri\":\"http://uni-paderborn.de\"}}");

        assertTrue(response.getStatus() == Status.OK.getStatusCode());
        assertEquals("http://testhost:1234/factstore/facts/99", response.getEntity());
    }

    @Test
    public void testPostMultiFacts() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);

        Response response = fr
                .postFacts("{\"@context\":{\"iks\":\"http://iks-project.eu/ont/\",\"upb\":\"http://upb.de/persons/\"},\"@profile\":\"iks:employeeOf\",\"@subject\":[{\"person\":{\"@iri\":\"upb:bnagel\"},\"organization\":{\"@iri\":\"http://uni-paderborn.de\"}},{\"person\":{\"@iri\":\"upb:fchrist\"},\"organization\":{\"@iri\":\"http://uni-paderborn.de\"}}]}");

        assertTrue(response.getStatus() == Status.OK.getStatusCode());
        assertNull(response.getEntity());
    }

    @Test
    public void testPostMultiFactsDifferentTypes() {
        FactsResource fr = new FactsResourceWrapper(this.servletContext, this.uriInfoMock);

        Response response = fr
                .postFacts("{\"@context\":{\"iks\":\"http://iks-project.eu/ont/\",\"upb\":\"http://upb.de/persons/\"},\"@subject\":[{\"@profile\":\"iks:employeeOf\",\"person\":{\"@iri\":\"upb:bnagel\"},\"organization\":{\"@iri\":\"http://uni-paderborn.de\"}},{\"@profile\":\"iks:friendOf\",\"person\":{\"@iri\":\"upb:bnagel\"},\"friend\":{\"@iri\":\"upb:fchrist\"}}]}");

        assertTrue(response.getStatus() == Status.OK.getStatusCode());
    }
}
