/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common;

/**
 *
 */
public class Booleans {
    /**
     * returns true iff the sequence of chars is one of "true","false".
     *
     * @param text   sequence to check
     * @param offset offset to start
     * @param length length to check
     */
    public static boolean isBoolean(char[] text, int offset, int length) {
        if (text == null || length == 0) {
            return false;
        }
        return isBoolean(new String(text, offset, length));
    }

    /**
     * @return true iff the provided value is either "true" or "false".
     */
    public static boolean isBoolean(String value) {
        return "false".equals(value) || "true".equals(value);
    }

    public static Boolean parseBoolean(String value) {
        if (isFalse(value)) {
            return false;
        }
        if (isTrue(value)) {
            return true;
        }

        throw new IllegalArgumentException("Failed to parse value [" + value + "] as only [true] or [false] are allowed.");
    }

    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (Strings.hasText(value)) {
            return parseBoolean(value);
        }
        return defaultValue;
    }
    /**
     * Returns <code>true</code> iff the value is neither of the following:
     *   <tt>false</tt>, <tt>0</tt>, <tt>off</tt>, <tt>no</tt>
     *   otherwise <code>false</code>
     */
    public static boolean parseBoolean(String value, boolean defaultValue) {
        if (Strings.hasText(value)) {
            return parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Returns <code>true</code> iff the value is either of the following:
     *   <tt>false</tt>, <tt>0</tt>, <tt>off</tt>, <tt>no</tt>
     *   otherwise <code>false</code>
     */
    public static boolean isFalse(String value) {
        return value != null && (value.equals("false"));
    }

    /**
     * Returns <code>true</code> iff the value is either of the following:
     *   <tt>true</tt>, <tt>1</tt>, <tt>on</tt>, <tt>yes</tt>
     *   otherwise <code>false</code>
     */
    public static boolean isTrue(String value) {
        return value != null && (value.equals("true"));
    }

}
