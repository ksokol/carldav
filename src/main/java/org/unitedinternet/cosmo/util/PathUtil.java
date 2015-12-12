/*
 * Copyright 2006 Open Source Applications Foundation
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
 */
package org.unitedinternet.cosmo.util;

/**
 * Provides utility methods for working with URL paths.
 */
public class PathUtil {

    /**
     * Returns the final segment of the path (the portion after the
     * final "/"). If the path does not contain a "/", returns the
     * empty string.
     */
    public static String getBasename(String path) {
        if (path == null) {
            throw new IllegalArgumentException("null path");
        }
        int pos = path.lastIndexOf('/');
        return pos >= 0 ? path.substring(pos + 1) : "";
    }

    /**
     * Returns the parent path of the given path. Removes the final
     * segment and its separator. If the path does not contain more
     * than one segment, returns "/".
     */
    public static String getParentPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("null path");
        }
        int idx = path.lastIndexOf('/', path.length() - 1);
        if (idx < 0) {
            return "/";
        }
        return (idx == 0) ? "/" : path.substring(0, idx);
    }
}
