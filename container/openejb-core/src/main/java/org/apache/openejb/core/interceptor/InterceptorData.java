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
package org.apache.openejb.core.interceptor;

import org.apache.openejb.core.Operation;
import org.apache.openejb.util.SetAccessible;
import org.apache.xbean.finder.ClassFinder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.AfterBegin;
import javax.ejb.AfterCompletion;
import javax.ejb.BeforeCompletion;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorData {
    private static final Map<Class<?>, InterceptorData> CACHE = new ConcurrentHashMap<Class<?>, InterceptorData>();

    private Class clazz;

    private final Set<Method> aroundInvoke = new LinkedHashSet<Method>();

    private final Set<Method> postConstruct = new LinkedHashSet<Method>();
    private final Set<Method> preDestroy = new LinkedHashSet<Method>();

    private final Set<Method> postActivate = new LinkedHashSet<Method>();
    private final Set<Method> prePassivate = new LinkedHashSet<Method>();

    private final Set<Method> afterBegin = new LinkedHashSet<Method>();
    private final Set<Method> beforeCompletion = new LinkedHashSet<Method>();
    private final Set<Method> afterCompletion = new LinkedHashSet<Method>();

    private final Set<Method> aroundTimeout = new LinkedHashSet<Method>();

    private final Map<Class<?>, Object> data = new HashMap<Class<?>, Object>();

    public InterceptorData(Class clazz) {
        this.clazz = clazz;
    }

    public Class getInterceptorClass() {
        return clazz;
    }

    public Set<Method> getAroundInvoke() {
        return aroundInvoke;
    }

    public Set<Method> getPostConstruct() {
        return postConstruct;
    }

    public Set<Method> getPreDestroy() {
        return preDestroy;
    }

    public Set<Method> getPostActivate() {
        return postActivate;
    }

    public Set<Method> getPrePassivate() {
        return prePassivate;
    }

    public Set<Method> getAfterBegin() {
        return afterBegin;
    }

    public Set<Method> getBeforeCompletion() {
        return beforeCompletion;
    }

    public Set<Method> getAfterCompletion() {
        return afterCompletion;
    }

    public Set<Method> getAroundTimeout(){
        return aroundTimeout;
    }

    public Set<Method> getMethods(Operation operation) {
        switch(operation) {
            case BUSINESS: return getAroundInvoke();
            case BUSINESS_WS: return getAroundInvoke();
            case REMOVE: return getAroundInvoke();
            case POST_CONSTRUCT: return getPostConstruct();
            case PRE_DESTROY: return getPreDestroy();
            case ACTIVATE: return getPostActivate();
            case PASSIVATE: return getPrePassivate();
            case AFTER_BEGIN: return getAfterBegin();
            case AFTER_COMPLETION: return getAfterCompletion();
            case BEFORE_COMPLETION: return getBeforeCompletion();
            case TIMEOUT: return getAroundTimeout();
        }
        return Collections.EMPTY_SET;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InterceptorData that = (InterceptorData) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;

        return true;
    }

    public int hashCode() {
        return (clazz != null ? clazz.hashCode() : 0);
    }

    public static void cacheScan(final Class<?> clazz) {
        CACHE.put(clazz, scan(clazz));
    }

    public static InterceptorData scan(Class<?> clazz) {
        final InterceptorData model = CACHE.get(clazz);
        if (model != null) {
            final InterceptorData data = new InterceptorData(clazz);
            data.aroundInvoke.addAll(model.getAroundInvoke());
            data.postConstruct.addAll(model.getPostConstruct());
            data.preDestroy.addAll(model.getPreDestroy());
            data.postActivate.addAll(model.getPostActivate());
            data.prePassivate.addAll(model.getPrePassivate());
            data.afterBegin.addAll(model.getAfterBegin());
            data.beforeCompletion.addAll(model.getBeforeCompletion());
            data.afterCompletion.addAll(model.getAfterCompletion());
            data.aroundTimeout.addAll(model.getAroundTimeout());
            return data;
        }

        final ClassFinder finder = new ClassFinder(clazz);
        final InterceptorData data = new InterceptorData(clazz);

        add(finder, data.aroundInvoke, AroundInvoke.class);
        add(finder, data.postConstruct, PostConstruct.class);
        add(finder, data.preDestroy, PreDestroy.class);
        add(finder, data.postActivate, PostActivate.class);
        add(finder, data.prePassivate, PrePassivate.class);
        add(finder, data.afterBegin, AfterBegin.class);
        add(finder, data.beforeCompletion, BeforeCompletion.class);
        add(finder, data.afterCompletion, AfterCompletion.class);
        add(finder, data.aroundTimeout, AroundTimeout.class);

        return data;
    }

    private static void add(ClassFinder finder, Set<Method> methods, Class<? extends java.lang.annotation.Annotation> annotation) {

        final List<Method> annotatedMethods = finder.findAnnotatedMethods(annotation);
        for (Method method : annotatedMethods) {
            SetAccessible.on(method);
            methods.add(method);
        }
    }

    public <T> void set(final Class<T> clazz, final T value) {
        data.put(clazz, value);
    }

    public <T> T get(final Class<T> clazz) {
        return clazz.cast(data.get(clazz));
    }

    @Override
    public String toString() {
        return "InterceptorData{" +
                "clazz=" + clazz.getSimpleName() + '}';
    }
}
