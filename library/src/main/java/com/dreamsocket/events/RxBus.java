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

import com.dreamsocket.interfaces.ICancellable;
import com.jakewharton.rxrelay2.PublishRelay;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;


public class RxBus implements IRxBus{
    private final Map<Class<?>, ConcurrentSkipListSet<SubscriptionEntry>> m_dispatchers;

    public RxBus(){
        this.m_dispatchers = new ConcurrentHashMap<>();
    }


    public boolean hasObservers(){
        return this.m_dispatchers.size() > 0;
    }


    public <T> boolean hasObservers(Class<T> p_class){
        return this.m_dispatchers.containsKey(p_class);
    }


    @SuppressWarnings("unchecked")
    public <T> Observable<T> on(Class<T> p_class, Object p_context){
        return this.on(p_class, p_context, 0);
    }


    @SuppressWarnings("unchecked")
    public <T> Observable<T> on(Class<T> p_class, Object p_context, int p_priority){
        return this.getSubscriptionEntry(p_class, p_context, p_priority).subject;
    }


    @SuppressWarnings("unchecked")
    public <T> void post(T p_value){
        Set<SubscriptionEntry> entries = this.m_dispatchers.get(p_value.getClass());

        if (entries != null && entries.size() > 0) {
            Iterator<SubscriptionEntry> itr = entries.iterator();

            while(itr.hasNext() && entries.size() > 0){
                if(p_value instanceof ICancellable && ((ICancellable) p_value).isCancelled()){
                    break;
                }
                itr.next().subject.accept(p_value);
            }
        }
    }


    public void off(){
        Iterator<Class<?>> itr = this.m_dispatchers.keySet().iterator();

        while (itr.hasNext()) {
            this.off(itr.next());
        }

        this.m_dispatchers.clear();
    }


    public <T> void off(Class<T> p_class){
        Set<SubscriptionEntry> entries = this.m_dispatchers.remove(p_class);

        if (entries != null) {
            Iterator<SubscriptionEntry> iterator = entries.iterator();

            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }


    public <T> void off(Class<T> p_class, Object p_context){
        Set<SubscriptionEntry> entries = this.m_dispatchers.get(p_class);

        if (entries != null) {
            Iterator<SubscriptionEntry> iterator = entries.iterator();

            while (iterator.hasNext()) {
                SubscriptionEntry entry = iterator.next();
                Object ctx = entry.context.get();

                if (ctx == p_context || ctx == null) {
                    // event has a subscription from context remove it
                    iterator.remove();
                }
            }

            // specific event no longer has subscriptions, clear it
            if (entries.size() == 0) {
                this.m_dispatchers.remove(p_class);
            }
        }
    }


    public void off(Object p_context){
        Iterator<Class<?>> itr = this.m_dispatchers.keySet().iterator();

        while (itr.hasNext()) {
            this.off(itr.next(), p_context);
        }
    }


    @SuppressWarnings("unchecked")
    private <T> SubscriptionEntry<T> getSubscriptionEntry(Class<T> p_class, Object p_context, int p_priority){
        SubscriptionEntry<T> entry = new SubscriptionEntry(p_context, p_priority);
        ConcurrentSkipListSet observers;
        ConcurrentSkipListSet prevObservers;

        this.off(p_class, p_context);

        observers = this.m_dispatchers.get(p_class);

        if(observers == null){
            observers = new ConcurrentSkipListSet<>();
            prevObservers = ((ConcurrentHashMap<Class<?>, ConcurrentSkipListSet<SubscriptionEntry>>)this.m_dispatchers).putIfAbsent(p_class, observers);
            observers = prevObservers != null ? prevObservers : observers;
        }

        observers.add(entry);

        return entry;
    }



    private final static class SubscriptionEntry<T> implements Comparable<SubscriptionEntry<T>>{

        private final static AtomicInteger k_index = new AtomicInteger();

        public final WeakReference<Object> context;
        public final int index;
        public final int priority;
        public final PublishRelay<T> subject;

        public SubscriptionEntry(Object p_context, int p_priority){
            this.context = new WeakReference<>(p_context);
            this.index = k_index.incrementAndGet();
            this.priority = p_priority;
            this.subject = PublishRelay.create();
        }

        @Override
        public int compareTo(SubscriptionEntry<T> p_item){
            // add comparable to make sure listeners are ordered
            int priority = p_item.priority - this.priority;

            return priority == 0 ? this.index - p_item.index : priority;
        }
    }
}
