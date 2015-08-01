package com.kuxhausen.huemore.net.dev;

import java.util.ArrayList;

/**
 * For debugging purposes. Accumulates a sequence of message numbers and can print a summary
 */
public class MessageAccumulator {

  ArrayList<Integer> messages;

  public MessageAccumulator() {
    messages = new ArrayList<>();
  }

  public void add(int i) {
    messages.add(i);
  }

  @Override
  public String toString() {
    if (messages.size() < 1) {
      return "Empty";
    }
    StringBuffer resultBuffer = new StringBuffer();

    Integer sequenceStart, sequenceStop;
    sequenceStart = sequenceStop = messages.get(0);

    for (int i = 1; i < messages.size(); i++) {
      if (messages.get(i) == sequenceStop + 1) {
        sequenceStop++;
      } else {
        if (sequenceStart != sequenceStop) {
          resultBuffer.append(sequenceStart);
          resultBuffer.append('-');
        }
        resultBuffer.append(sequenceStop);
        resultBuffer.append(',');
        sequenceStart = sequenceStop = messages.get(i);
      }
    }

    if (sequenceStart != sequenceStop) {
      resultBuffer.append(sequenceStart);
      resultBuffer.append('-');
    }
    resultBuffer.append(sequenceStop);

    return resultBuffer.toString();
  }

}
