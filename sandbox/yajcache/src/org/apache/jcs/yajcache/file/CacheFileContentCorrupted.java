
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package org.apache.jcs.yajcache.file;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.jcs.yajcache.lang.annotation.*;

/**
 * Corrupted Cache File Content.
 *
 * @author Hanson Char
 */
@CopyRightApache
public class CacheFileContentCorrupted extends CacheFileContent {
    static CacheFileContentCorrupted inst = new CacheFileContentCorrupted();

    private CacheFileContentCorrupted() {}
    
    @Override void write(@NonNullable RandomAccessFile raf) throws IOException 
    {
    }

    @Override public byte[] getContent() {
        return null;
    }

    @Override public void setContent(byte[] content) {
    }

    @Override public byte getContentType() {
        return 0;
    }

    @Override public void setContentType(byte contentType) {
    }

    @Override public int getContentLength() {
        return 0;
    }

    @Override void setContentLength(int contentLength) {
    }

    @Override public int getContentHashCode() {
        return 0;
    }
    
    @Override void setContentHashCode(int contentHashCode) {
    }
    @Override public boolean isValid() {
        return false;
    }
    /** Returns the deserialized content. */
    @Override public Object deserialize() {
        return null;
    }
}