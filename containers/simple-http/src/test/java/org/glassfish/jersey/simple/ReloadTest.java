/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.simple;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientFactory;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;


/**
 * Simple Reload Test
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ReloadTest extends AbstractSimpleServerTester {

    @Path("/one")
    public static class One {
        @GET
        public String get() {
            return "one";
        }
    }

    @Path("/two")
    public static class Two {
        @GET
        public String get() {
            return "two";
        }
    }

    public static class Reloader extends AbstractContainerLifecycleListener {
        Container container;

        public void reload(ResourceConfig newConfig) {
            container.reload(newConfig);
        }

        public void reload() {
            container.reload();
        }

        @Override
        public void onStartup(Container container) {
            this.container = container;
        }

    }

    @Test
    public void testReload() {
        final ResourceConfig rc = new ResourceConfig(One.class);

        Reloader reloader = new Reloader();
        rc.registerInstances(reloader);

        startServer(rc);

        WebTarget r = ClientFactory.newClient().target(getUri().path("/").build());

        assertEquals("one", r.path("one").request().get(String.class));
        assertEquals(404, r.path("two").request().get(Response.class).getStatus());

        // add Two resource
        reloader.reload(new ResourceConfig(One.class, Two.class));

        assertEquals("one", r.path("one").request().get(String.class));
        assertEquals("two", r.path("two").request().get(String.class));
    }


}