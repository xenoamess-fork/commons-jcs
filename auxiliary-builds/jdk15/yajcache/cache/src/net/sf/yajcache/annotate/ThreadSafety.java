/* ========================================================================
 * Copyright 2005 The Apache Software Foundation
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
 * ========================================================================
 */
/*
 * $Revision$ $Date$
 */

package net.sf.yajcache.annotate;

import java.lang.annotation.*;

/**
 * Characterizing thread safety.
 *
 * http://www-106.ibm.com/developerworks/java/library/j-jtp09263.html
 *
 * @author Hanson Char
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadSafety {
    ThreadSafetyType value();
    String caveat() default "";
    String note() default "";
}