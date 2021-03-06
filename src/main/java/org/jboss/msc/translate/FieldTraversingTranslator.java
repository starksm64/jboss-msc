/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.msc.translate;

import java.lang.reflect.Field;
import org.jboss.msc.value.Value;

/**
 * A translator which translates by fetching the value of a field on the target object.
 *
 * @param <I> the input type
 * @param <O> the output type
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class FieldTraversingTranslator<I, O> implements Translator<I, O> {
    private final Value<Field> field;

    /**
     * Construct a new instance.
     *
     * @param field the field to traverse
     */
    public FieldTraversingTranslator(final Value<Field> field) {
        this.field = field;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked" })
    public O translate(final I input) {
        try {
            return (O) field.getValue().get(input);
        } catch (Exception e) {
            throw new TranslationException(e);
        }
    }
}