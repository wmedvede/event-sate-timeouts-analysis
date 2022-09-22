/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonCloudEventData;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("event-producer")
@ApplicationScoped
public class EventProducerResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProducerResource.class);

    private static final String EVENT_STATE_EVENT1_OUT = "event_state_event1_out";

    private static final String EVENT_STATE_EVENT2_OUT = "event_state_event2_out";

    private static final String EVENT_STATE_EVENT3_OUT = "event_state_event3_out";

    @Inject
    @Channel(EVENT_STATE_EVENT1_OUT)
    Emitter<String> eventStateEvent1;

    @Inject
    @Channel(EVENT_STATE_EVENT2_OUT)
    Emitter<String> eventStateEvent2;

    @Inject
    @Channel(EVENT_STATE_EVENT3_OUT)
    Emitter<String> eventStateEvent3;

    @Inject
    ObjectMapper objectMapper;

    @Path("fire-event")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response fireEventForProcessInstance(@QueryParam("processInstanceId") String processInstanceId, @QueryParam("eventType") String eventType, @QueryParam("emitter") String emitter) {
        LOGGER.debug("Firing event for , for processInstanceId:{}, eventType: {}, emitter: {}", processInstanceId, eventType, emitter);
        String event = generateCloudEventForEventState(processInstanceId, eventType);
        if (EVENT_STATE_EVENT1_OUT.equals(emitter)) {
            eventStateEvent1.send(event);
        } else if (EVENT_STATE_EVENT2_OUT.equals(emitter)) {
            eventStateEvent2.send(event);
        } else if (EVENT_STATE_EVENT3_OUT.equals(emitter)) {
            eventStateEvent3.send(event);
        } else {
            throw new IllegalArgumentException("Emitter: " + emitter + " was not found");
        }
        return Response.ok("{}").build();
    }

    public String generateCloudEventForEventState(String processInstanceId, String eventType) {
        try {
            return objectMapper.writeValueAsString(CloudEventBuilder.v1()
                                                           .withId(UUID.randomUUID().toString())
                                                           .withSource(URI.create(""))
                                                           .withType(eventType)
                                                           .withTime(OffsetDateTime.now())
                                                           .withExtension("kogitoprocrefid", processInstanceId)
                                                           .withData(JsonCloudEventData.wrap(objectMapper.createObjectNode()))
                                                           .build());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}