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

package org.jboss.msc.ref;

import java.lang.ref.ReferenceQueue;

public final class References {
    private static final Reference NULL = new Reference() {
        public Object get() {
            return null;
        }

        public Object getAttachment() {
            return null;
        }

        public void clear() {
        }

        public Type getType() {
            return Type.NULL;
        }

        public String toString() {
            return "NULL reference";
        }
    };

    private References() {
    }

    static final class ReaperThread extends Thread {
        static final ReferenceQueue<Object> REAPER_QUEUE = new ReferenceQueue<Object>();

        static {
            final ReaperThread thr = new ReaperThread();
            thr.setName("Reference Reaper");
            thr.setDaemon(true);
            thr.start();
        }

        public void run() {
            for (;;) try {
                final java.lang.ref.Reference<? extends Object> ref = REAPER_QUEUE.remove();
                if (ref instanceof Reapable) {
                    reap((Reapable<?, ?>) ref);
                }
            } catch (InterruptedException ignored) {
            } catch (Throwable ignored) {
            }
        }

        @SuppressWarnings({ "unchecked" })
        private static <T, A> void reap(final Reapable<T, A> reapable) {
            reapable.getReaper().reap((Reference<T, A>) reapable);
        }
    }

    public static <T, A> Reference<T, A> create(Reference.Type type, T value, A attachment, Reaper<T, A> reaper) {
        switch (type) {
            case STRONG:
                return new StrongReference<T, A>(value, attachment);
            case WEAK:
                return new WeakReference<T, A>(value, attachment, reaper);
            case PHANTOM:
                return new PhantomReference<T, A>(value, attachment, reaper);
            case SOFT:
                return new SoftReference<T, A>(value, attachment, reaper);
            case NULL:
                return getNullReference();
            default:
                throw new IllegalStateException();
        }
    }

    public static <T, A> Reference<T, A> create(Reference.Type type, T value, A attachment, ReferenceQueue<? super T> referenceQueue) {
        switch (type) {
            case STRONG:
                return new StrongReference<T, A>(value, attachment);
            case WEAK:
                return new WeakReference<T, A>(value, attachment, referenceQueue);
            case PHANTOM:
                return new PhantomReference<T, A>(value, attachment, referenceQueue);
            case SOFT:
                return new SoftReference<T, A>(value, attachment, referenceQueue);
            case NULL:
                return getNullReference();
            default:
                throw new IllegalStateException();
        }
    }

    public static <T, A> Reference<T, A> create(Reference.Type type, T value, A attachment) {
        switch (type) {
            case STRONG:
                return new StrongReference<T, A>(value, attachment);
            case WEAK:
                return new WeakReference<T, A>(value, attachment);
            case PHANTOM:
                throw new IllegalArgumentException("Phantom reference may not be created without a queue or reaper");
            case SOFT:
                return new SoftReference<T, A>(value, attachment);
            case NULL:
                return getNullReference();
            default:
                throw new IllegalStateException();
        }
    }

    @SuppressWarnings({ "unchecked" })
    public static <T, A> Reference<T, A> getNullReference() {
        return (Reference<T, A>) NULL;
    }
}
