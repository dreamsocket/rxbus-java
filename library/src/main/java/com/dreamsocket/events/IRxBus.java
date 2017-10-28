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


import io.reactivex.Observable;

public interface IRxBus {

    // SUBSCRIBE - only allows one subscription for a specific event/context combo
    @SuppressWarnings("unchecked")
    <T> Observable<T> on(Class<T> p_class, Object p_context);

    @SuppressWarnings("unchecked")
    <T> Observable<T> on(Class<T> p_class, Object p_context, int p_priority);

    // REMOVE ALL
    void off();

    // REMOVE ALL EVENTS FOR A TYPE
    <T> void off(Class<T> p_class);

    // REMOVE SPECIFIC EVENT FOR A CONTEXT
    <T> void off(Class<T> p_class, Object p_context);

    // REMOVE ALL EVENTS FOR A CONTEXT
    void off(Object p_context);

}