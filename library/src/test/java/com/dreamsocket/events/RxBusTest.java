/*
 * Copyright (C) 2017 Dreamsocket, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreamsocket.events;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


import io.reactivex.disposables.Disposable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;


public class RxBusTest {

    private RxBus m_bus;
    private ArrayList<Event> m_events;
    private ArrayList<Listener> m_listeners;


    @Before public void setUp() throws Exception {
        this.m_bus = new RxBus();
        this.m_events = new ArrayList<>();
        this.m_listeners = new ArrayList<>();
    }


    @Test
    public void hasObservablesValid(){
        this.m_bus.on(Event1.class, this);

        assertEquals("Subscribers not added", true, this.m_bus.hasObservers());
    }


    @Test
    public void hasObservablesForClassValid(){
        this.m_bus.on(Event1.class, this);

        assertEquals("Subscribers not added for specific class", true, this.m_bus.hasObservers(Event1.class));
        assertEquals("Showing subscribers or incorrect class", false, this.m_bus.hasObservers(Event2.class));
    }


    @Test
    public void checkEventType(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        this.m_bus.post(new Event1(1));

        assertEquals("Expected Event1", true, this.m_events.get(0) instanceof Event1);
    }


    @Test
    public void checkEventCount(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event1(1));

        assertEquals("Expected 2 events to be added", 2, this.m_events.size());
    }


    @Test
    public void checkEventOrder(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event1(2));

        assertEquals("Expected 1st event index 1", 1, this.m_events.get(0).index);
        assertEquals("Expected 2nd event index 2", 2, this.m_events.get(1).index);
    }


    @Test
    public void checkEventPriority(){
        for(int i = 0; i < 20; i++){
            Listener listener = new Listener(this.m_events, this.m_listeners, i);

            this.m_bus.on(Event1.class, listener, i == 10 ? 1 : 0).subscribe(test -> listener.onEvent1Fired(test));
        }

        this.m_bus.post(new Event1(1));

        assertEquals("Expected 1st listener as index 10", 10, this.m_listeners.get(0).index);
        assertEquals("Expected 2nd listener as index 0", 0, this.m_listeners.get(1).index);
    }


    @Test
    public void checkMultipleEventsAreSubscribed(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);
        this.m_bus.on(Event2.class, this).subscribe(listener::onEvent2Fired);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event2(1));

        assertEquals("Expected 1st event as Event1", true, this.m_events.get(0) instanceof Event1);
        assertEquals("Expected 2nd event as Event2", true, this.m_events.get(1) instanceof Event2);
    }


    @Test
    public void checkAllListenersCleared(){
        for(int i = 0; i < 20; i++) {
            Listener listener = new Listener(this.m_events, this.m_listeners, i);

            this.m_bus.on(Event1.class, listener).subscribe(listener::onEvent1Fired);
            this.m_bus.on(Event2.class, listener).subscribe(listener::onEvent2Fired);
        }

        this.m_bus.off();
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event2(1));

        assertEquals("Expected no events to be fired", 0, this.m_events.size());
    }


    @Test
    public void checkContextListenersCleared(){
        Object context1 = new Object();
        Object context2 = new Object();

        Listener listener1 = new Listener(this.m_events, this.m_listeners, 1);
        Listener listener2 = new Listener(this.m_events, this.m_listeners, 2);

        this.m_bus.on(Event1.class, context1).subscribe(listener1::onEvent1Fired);
        this.m_bus.on(Event2.class, context2).subscribe(listener1::onEvent2Fired);

        this.m_bus.on(Event1.class, context1).subscribe(listener2::onEvent1Fired);
        this.m_bus.on(Event2.class, context2).subscribe(listener2::onEvent2Fired);

        this.m_bus.off(context1);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event2(1));

        for(Event event : this.m_events){
            if(event instanceof Event1){
                fail("Expected no Event1 events to be fired for context1");
                break;
            }
        }

        assertNotEquals("Expected Event2 events to be fired for context2", 0, this.m_events.size());
    }


    @Test
    public void checkContextClassListenersCleared(){
        Object context1 = new Object();
        Object context2 = new Object();

        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        this.m_bus.on(Event1.class, context1).subscribe(listener::onEvent1Fired);
        this.m_bus.on(Event2.class, context1).subscribe(listener::onEvent2Fired);

        this.m_bus.on(Event1.class, context2).subscribe(listener::onEvent1Fired);
        this.m_bus.on(Event2.class, context2).subscribe(listener::onEvent2Fired);

        this.m_bus.off(Event1.class, context1);
        this.m_bus.post(new Event1(1));

        int ct = 0;
        for(Event event : this.m_events){
            if(event instanceof Event1){
                ct++;
                break;
            }
        }


        assertEquals("Expected 1 Event1 events to be fired for context2", 1, ct);
    }


    @Test
    public void checkClassListenersCleared(){
        for(int i = 0; i < 20; i++) {
            Listener listener = new Listener(this.m_events, this.m_listeners, i);

            this.m_bus.on(Event1.class, listener).subscribe(listener::onEvent1Fired);
            this.m_bus.on(Event2.class, listener).subscribe(listener::onEvent2Fired);
        }

        this.m_bus.off(Event1.class);
        this.m_bus.post(new Event1(1));
        this.m_bus.post(new Event2(1));

        for(Event event : this.m_events){
            if(event instanceof  Event1){
                assertEquals("Expected no Event1 events to be fired", 0, this.m_events.size());
                break;
            }
        }
    }


    @Test
    public void checkReentrantListenersCleared(){
        for(int i = 0; i < 20; i++) {
            ReentrantListener listener = new ReentrantListener(this.m_events, event -> this.m_bus.off());

            this.m_bus.on(Event1.class, listener).subscribe(listener::onEvent1Fired);
        }

        this.m_bus.post(new Event1(1));


        assertEquals("Expected 1 event to be fired", 1, this.m_events.size());
    }



    @Test
    public void checkDisposedListener(){
        Listener listener = new Listener(this.m_events, this.m_listeners, 0);

        Disposable disposable = this.m_bus.on(Event1.class, this).subscribe(listener::onEvent1Fired);

        disposable.dispose();

        this.m_bus.post(new Event1(1));


        assertEquals("Expected no events to be fired", 0, this.m_events.size());
    }



    @Test
    public void checkCanceledEvent(){
        for(int i = 0; i < 20; i++) {
            ReentrantListener listener = new ReentrantListener(this.m_events, event -> event.cancel());

            this.m_bus.on(Event1.class, listener).subscribe(listener::onEvent1Fired);
        }

        this.m_bus.post(new Event1(1));


        assertEquals("Expected 1 event to be fired", 1, this.m_events.size());
    }



    // Simple Action lambda
    protected interface Consumer{
        void accept(Event p_value);
    }



    // Helper Listeners
    protected class Listener{

        protected final ArrayList<Event> m_events;
        protected final ArrayList<Listener> m_listeners;


        public Listener(ArrayList<Event> p_events, ArrayList<Listener> p_listeners, int p_index){
            this.m_events = p_events;
            this.m_listeners = p_listeners;
            this.index = p_index;
        }

        public final int index;

        protected void onEvent1Fired(Event1 p_event){
            this.m_events.add(p_event);
            this.m_listeners.add(this);
        }

        protected void onEvent2Fired(Event2 p_event){
            this.m_events.add(p_event);
            this.m_listeners.add(this);
        }
    }



    protected class ReentrantListener{

        protected final Consumer m_consumer;
        protected final ArrayList<Event> m_events;


        public ReentrantListener(ArrayList<Event> p_events, Consumer p_consumer){
            this.m_events = p_events;
            this.m_consumer = p_consumer;
        }

        protected void onEvent1Fired(Event1 p_event){
            this.m_events.add(p_event);
            this.m_consumer.accept(p_event);
        }
    }


    // Helper Events

    public class Event extends com.dreamsocket.events.Event{
        public Event(int p_index){
            this.index = p_index;
        }

        public final int index;
    }


    public class Event1 extends Event{
        public Event1(int p_index){super(p_index);}
    }


    public class Event2 extends Event{
        public Event2(int p_index){super(p_index);}
    }
}
