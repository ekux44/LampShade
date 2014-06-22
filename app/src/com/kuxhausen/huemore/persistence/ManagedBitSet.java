package com.kuxhausen.huemore.persistence;

import java.util.BitSet;

import android.util.Base64;

public class ManagedBitSet {
  private BitSet set;
  private int index;
  private boolean littleEndian = false;

  public ManagedBitSet() {
    set = new BitSet();
    index = 0;
  }

  public ManagedBitSet(String base64Encoded) {
    byte[] intermediaryReverse = Base64.decode(base64Encoded, Base64.URL_SAFE);

    set = toBitSet(intermediaryReverse);
    index = 0;
  }

  public void incrementingSet(boolean value) {
    set.set(index, value);
    index++;
  }

  public String getBase64Encoding() {
    byte[] intermediaryResult = fromBitSet(set, index);
    return Base64.encodeToString(intermediaryResult, Base64.URL_SAFE);
  }

  private static byte[] fromBitSet(BitSet bits, int length) {
    while (length % 8 != 0) {
      length++;
    }

    byte[] bytes = new byte[length / 8];
    for (int i = 0; i < bytes.length; i++) {
      byte mask = 1;
      byte temp = 0;
      for (int j = 0; j < 8; j++) {
        if (bits.get(8 * i + j))
          temp |= mask;
        mask = (byte) (mask << 1);
      }
      bytes[i] = (temp);
    }

    return bytes;
  }

  public boolean incrementingGet() {
    return set.get(index++);
  }

  public static BitSet toBitSet(byte[] bytes) {
    BitSet bits = new BitSet();
    for (int i = 0; i < bytes.length; i++) {
      byte mask = 1;
      byte temp = bytes[i];
      for (int j = 0; j < 8; j++) {
        if ((temp & mask) != 0)
          bits.set(8 * i + j, true);
        mask = (byte) (mask << 1);
      }
    }
    return bits;
  }

  /**
   * @param set
   * @param index
   * @param value
   * @param length
   */
  public void addNumber(int value, int length) {
    if (!littleEndian) {
      int bitMask = 1 << (length - 1);
      for (int i = length - 1; i >= 0; i--) {
        this.incrementingSet(((value & bitMask) > 0));
        bitMask = bitMask >>> 1;
      }
    }
  }

  public int extractNumber(int length) {
    int result = 0;
    if (littleEndian) {
      int bitMask = 1;
      for (int i = 0; i < length; i++) {
        if (this.incrementingGet())
          result += bitMask;
        bitMask *= 2;
      }
    } else {
      int bitMask = 1 << (length - 1);
      for (int i = length - 1; i >= 0; i--) {
        if (this.incrementingGet())
          result += bitMask;
        bitMask = bitMask >>> 1;
      }
    }
    return result;
  }

  public void useLittleEndianEncoding(boolean little) {
    littleEndian = little;
  }
}
