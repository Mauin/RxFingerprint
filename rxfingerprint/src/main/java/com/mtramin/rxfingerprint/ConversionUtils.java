/*
 * Copyright 2015 Marvin Ramin
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

package com.mtramin.rxfingerprint;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Data conversion utility methods
 */
class ConversionUtils {

  // based on https://stackoverflow.com/a/9670279/115145

  static byte[] toBytes(char[] chars) {
    CharBuffer charBuffer = CharBuffer.wrap(chars);
    ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
    byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());

    Arrays.fill(charBuffer.array(), '\u0000'); // clear the cleartext
    Arrays.fill(byteBuffer.array(), (byte) 0); // clear the ciphertext

    return bytes;
  }

  static char[] toChars(byte[] bytes) {
    Charset charset = Charset.forName("UTF-8");
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    CharBuffer charBuffer = charset.decode(byteBuffer);
    char[] chars = Arrays.copyOf(charBuffer.array(), charBuffer.limit());

    Arrays.fill(charBuffer.array(), '\u0000'); // clear the cleartext
    Arrays.fill(byteBuffer.array(), (byte) 0); // clear the ciphertext

    return chars;
  }
}
