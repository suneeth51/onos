/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.rest.exceptions;

import org.onlab.osgi.ServiceNotFoundException;

import javax.ws.rs.core.Response;

/**
 * Mapper for service not found exceptions to the SERVICE_UNAVAILABLE response code.
 */
public class ServiceNotFoundMapper extends AbstractMapper<ServiceNotFoundException> {
    @Override
    protected Response.Status responseStatus() {
        return Response.Status.SERVICE_UNAVAILABLE;
    }
}
