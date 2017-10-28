RxBus - An event bus by Dreamsocket
=============================

RxBus is an event bus that leverages RxJava.

RxBus was designed with an interface very similar to jQuery or Backbone's event API. 
It includes guaranteed event ordering based on time of subscription, ability to prioritize subscribers, cancellable events, overloaded unsubscribe methods (off) that allow unsubscribing in different ways (all events for a bus, all events for an event type, all events for a context, or a specific event for a specific context). 

## Getting RxBus
You may download RxBus as a Gradle dependency:

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies{
    implementation 'com.github.dreamsocket:rxbus-java:v1.0'
}
```
### Usage
Instances or subclass instances of RxBus serve as an event emitter that can post events and have them subscribed to. The event interface is similar to the APIs found in jQuery or Backbone.

To create a simple instance of RxBus

```java
RxBus emitter = new RxBus();
```

Any object (event) can be posted from the bus. Subscribers can determine what events they want to listen to. They do this by subscribing to specific classes of the objects they care about. For example, if a user posts an object that is an Integer vs one that is a String, those are two different types of events that can be listened to. Thus, it is good practice to create unique events/classes for things you want to post (ex: AdStartedEvent, AdStoppedEvent).

Example of basic subscriptions

```java
emmiter.on(Integer.class, this).subscribe(data -> Log.i(TAG, "integer:" + data));
emmiter.on(String.class, this).subscribe(data -> Log.i(TAG, "string:" + data));
emmiter.on(AdStartedEvent.class, this).subscribe(event -> Log.i(TAG, "ad started"));
emmiter.on(AdStoppedEvent.class, this).subscribe(event -> Log.i(TAG, "ad stopped"));
```

Example of posting events

```java
emmiter.post(25);
emmiter.post("dude");
emmiter.post(new AdStartedEvent());
emmiter.post(new AdStoppedEvent());
```

Events can be any object type. However, an event has the option of having extended functionality where it can be cancellable. This is available via implementing an ICancellable interface or extending Event (which implements ICancellable). The interface has a method cancel() which can be called to stop the event from being fired. For example, if you had 4 subscribers and the 2nd subscriber called cancel the final 2 subscribers would never receive the event.

The Event class provides an example of how to implement the ICancellable interface

```java
package com.dreamsocket.events;

import com.dreamsocket.interfaces.ICancellable;

public class Event implements ICancellable {

    private boolean m_canceled;

    public boolean isCancelled(){
        return this.m_canceled;
    }

    public void cancel(){
        this.m_canceled = true;
    }
}

```

An example of cancelling an event

```java
emmiter.on(AdFailedEvent.class, this).subscribe(event -> event.cancel());
```

The listening interface (on) has two required parameters and an optional third. The first is the class of the event you want to listen to. The second is the context that you want to assign the event to. The third (optional) is the priority of the subscription. Subscriptions are added in order, however if you add a priority it moves that subscription to the front of the list. That means that subscriber will receive the event before the others. The number with the highest priority falls at the front of the list.

Example of a standard subscription

```java
emmiter.on(AdFailedEvent.class, this).subscribe(event -> Log.i(TAG, "ad failed"));
```

Example of a subscription with a higher priority

```java
emmiter.on(AdFailedEvent.class, this, 1).subscribe(event -> Log.i(TAG, "ad failed"));
```

Check if there are any observers 

```java
emmiter.hasObservers();
```

Check if there are any observers for a specific event

```java
emmiter.hasObservers(AdStartedEvent.class);
```

The interface to remove listeners (off) is overloaded, giving you several options on how you can detach subscriptions. These are based on the parameters that were used in the on subscription calls. 

A specific event can be removed from a specific context 

```java
emmiter.off(AdFailedEvent.class, this);
```

A specific event for all contexts can be removed

```java
emmiter.off(AdFailedEvent.class);
```

All events for a specific context can be removed. This is helpful for removing events based on lifecycle states or items that are being purged or destroyed

```java
emmiter.off(this);
```

All events for all contexts can be removed

```java
emmiter.off();
```

License
-------

    Copyright 2017 Dreamsocket, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.