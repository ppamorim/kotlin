/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.jvm.internal;

import kotlin.jvm.KotlinReflectionNotSupportedError;
import kotlin.reflect.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * A superclass for all classes generated by Kotlin compiler for callable references.
 *
 * All methods from KCallable should be implemented here and should delegate to the actual implementation, loaded dynamically
 * and stored in the {@link CallableReference#reflected} field.
 */
@SuppressWarnings({"unchecked", "NullableProblems"})
public abstract class CallableReference implements KCallable {
    protected KCallable reflected;

    protected abstract KCallable computeReflected();

    public KCallable compute() {
        KCallable result = reflected;
        if (result == null) {
            result = computeReflected();
            reflected = result;
        }
        return result;
    }

    protected KCallable getReflected() {
        KCallable result = compute();
        if (result == this) {
            throw new KotlinReflectionNotSupportedError();
        }
        return result;
    }

    // The following methods provide the information identifying this callable, which is used by the reflection implementation.
    // They are supposed to be overridden in each subclass (each anonymous class generated for a callable reference).

    /**
     * @return the class or package where the callable should be located, usually specified on the LHS of the '::' operator
     */
    public KDeclarationContainer getOwner() {
        throw new AbstractMethodError();
    }

    /**
     * @return Kotlin name of the callable, the one which was declared in the source code (@JvmName doesn't change it)
     */
    @Override
    public String getName() {
        throw new AbstractMethodError();
    }

    /**
     * @return JVM signature of the callable, e.g. "println(Ljava/lang/Object;)V". If this is a property reference,
     * returns the JVM signature of its getter, e.g. "getFoo(Ljava/lang/String;)I". If the property has no getter in the bytecode
     * (e.g. private property in a class), it's still the signature of the imaginary default getter that would be generated otherwise.
     *
     * Note that technically the signature itself is not even used as a signature per se in reflection implementation,
     * but only as a unique and unambiguous way to map a function/property descriptor to a string.
     */
    public String getSignature() {
        throw new AbstractMethodError();
    }

    // The following methods are the delegating implementations of reflection functions. They are called when you're using reflection
    // on a callable reference. Without kotlin-reflect.jar in the classpath, getReflected() throws an exception.

    @Override
    public List<KParameter> getParameters() {
        return getReflected().getParameters();
    }

    @Override
    public KType getReturnType() {
        return getReflected().getReturnType();
    }

    @Override
    public List<Annotation> getAnnotations() {
        return getReflected().getAnnotations();
    }

    @NotNull
    @Override
    public List<KTypeParameter> getTypeParameters() {
        return getReflected().getTypeParameters();
    }

    @Override
    public Object call(@NotNull Object... args) {
        return getReflected().call(args);
    }

    @Override
    public Object callBy(@NotNull Map args) {
        return getReflected().callBy(args);
    }

    @Nullable
    @Override
    public KVisibility getVisibility() {
        return getReflected().getVisibility();
    }

    @Override
    public boolean isFinal() {
        return getReflected().isFinal();
    }

    @Override
    public boolean isOpen() {
        return getReflected().isOpen();
    }

    @Override
    public boolean isAbstract() {
        return getReflected().isAbstract();
    }
}
