package com.kuxhausen.huemore.persistence;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Alert;
import com.kuxhausen.huemore.state.BulbState.Effect;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class HueUrlEncoder {

  /**
   * zero indexed *
   */
  public final static Integer PROTOCOL_VERSION_NUMBER = 4;


  public static String encode(Mood mood) {
    return encodeLegacy(mood, null, null);
  }

  public static String encode(Mood m, Group g, Integer brightness, Context c) {

    Integer[] legacyArray = new Integer[50];
    String[] projections = {Definitions.NetBulbColumns.DEVICE_ID_COLUMN};

    for (Long l : g.getNetworkBulbDatabaseIds()) {
      String[] selectionArgs =
          {"" + l, "" + Definitions.NetBulbColumns.NetBulbType.PHILIPS_HUE};
      Cursor cursor =
          c.getContentResolver().query(
              Definitions.NetBulbColumns.URI,
              projections,
              Definitions.NetBulbColumns._ID + " =? AND "
              + Definitions.NetBulbColumns.TYPE_COLUMN + " =?", selectionArgs, null
          );

      if (cursor.moveToFirst()) {
        String s = cursor.getString(0);
        int hueBulbNum = Integer.parseInt(s);
        legacyArray[hueBulbNum] = hueBulbNum;
      }
    }

    return encodeLegacy(m, legacyArray, brightness);
  }


  public static String encodeLegacy(Mood m, Integer[] bulbsAffected, Integer brightness) {
    Mood mood = m.clone();

    if (mood == null) {
      return "";
    }

    ManagedBitSet mBitSet = new ManagedBitSet();

    // Set 3 bit protocol version
    mBitSet.addNumber(PROTOCOL_VERSION_NUMBER, 3);

    // Flag if optional bulblist included
    mBitSet.incrementingSet(bulbsAffected != null);

    // 50 bit optional bulb inclusion flags
    if (bulbsAffected != null) {
      boolean[] bulbs = new boolean[50];
      for (Integer i : bulbsAffected) {
        if (i != null) {
          bulbs[i - 1] = true;
        }
      }
      for (int i = 0; i < bulbs.length; i++) {
        mBitSet.incrementingSet(bulbs[i]);
      }
    }

    /** optional total brightness added in Protocol_Version_Number==3 **/
    {
      /**
       * The brightness value to set the light to. Brightness is a scale from 0 (the minimum the
       * light is capable of) to 255 (the maximum). Note: a brightness of 0 is not off.
       */

      // Flag if optional brightness included
      mBitSet.incrementingSet(brightness != null);

      // If optional brightness included, write it's 8 bits
      if (brightness != null) {
        mBitSet.addNumber(brightness, 8);
      }
    }

    // Set 6 bit number of channels
    mBitSet.addNumber(mood.getNumChannels(), 6);

    addTimingRepeatPolicy(mBitSet, mood);

    ArrayList<Integer> timeArray = generateTimesArray(mood);
    // Set 6 bit number of timestamps
    mBitSet.addNumber(timeArray.size(), 6);
    // Set variable size list of 20 bit timestamps
    for (Integer i : timeArray) {
      mBitSet.addNumber(i, 20);
    }

    ArrayList<BulbState> stateArray = generateStatesArray(mood);
    // Set 12 bit number of states
    mBitSet.addNumber(stateArray.size(), 12);

    for (BulbState state : stateArray) {
      addState(mBitSet, state);
    }

    // Set 12 bit number of events
    mBitSet.addNumber(mood.events.length, 12);

    addListOfEvents(mBitSet, mood, timeArray, stateArray);

    // Set 20 bit timestamps representing the loopIterationTimeLength
    mBitSet.addNumber(mood.loopIterationTimeLength, 20);

    String encoded = mBitSet.getBase64Encoding();
    // bug fix against newlines in encodeLegacy
    String corrected = encoded.replaceAll(("" + (char) 10), "");
    return corrected;

  }

  /**
   * Set 8 bit timing repeat policy *
   */
  private static void addTimingRepeatPolicy(ManagedBitSet mBitSet, Mood mood) {
    // 1 bit timing addressing reference mode
    mBitSet.incrementingSet(mood.timeAddressingRepeatPolicy);

    // 7 bit timing repeat number (max value specialcased to infinity)
    mBitSet.addNumber(mood.getNumLoops(), 7);
  }

  /**
   * Set variable length state *
   */
  private static void addState(ManagedBitSet mBitSet, BulbState bs) {
    /** Put 9 bit properties flags **/
    {
      // On/OFF flag always include in v1 implementation 1
      mBitSet.incrementingSet(bs.getOn() != null);

      // Put bri flag
      mBitSet.incrementingSet(bs.get255Bri() != null);

      // Put hue flag
      mBitSet.incrementingSet(false);

      // Put sat flag
      mBitSet.incrementingSet(false);

      // Put xy flag
      mBitSet.incrementingSet(bs.getXY() != null);

      // Put ct flag
      mBitSet.incrementingSet(bs.getMiredCT() != null);

      // Put alert flag
      mBitSet.incrementingSet(bs.getAlert() != null);

      // Put effect flag
      mBitSet.incrementingSet(bs.getEffect() != null);

      // Put transitiontime flag
      mBitSet.incrementingSet(bs.getTransitionTime() != null);
    }
    /** Put on bit **/
    if (bs.getOn() != null) {
      mBitSet.incrementingSet(bs.getOn());
    }

    /** Put 8 bit bri **/
    if (bs.get255Bri() != null) {
      mBitSet.addNumber(bs.get255Bri(), 8);
    }

    /** Put 64 bit xy **/
    if (bs.getXY() != null) {
      int x = Float.floatToIntBits(bs.getXY()[0]);
      mBitSet.addNumber(x, 32);

      int y = Float.floatToIntBits(bs.getXY()[1]);
      mBitSet.addNumber(y, 32);
    }

    /** Put 9 bit ct **/
    if (bs.getMiredCT() != null) {
      mBitSet.addNumber(bs.getMiredCT(), 9);
    }

    /** Put 2 bit alert **/
    if (bs.getAlert() != null) {
      int value = 0;
      if (bs.getAlert().equals(Alert.NONE)) {
        value = 0;
      } else if (bs.getAlert().equals(Alert.FLASH_ONCE)) {
        value = 1;
      } else if (bs.getAlert().equals(Alert.FLASH_30SEC)) {
        value = 2;
      }

      mBitSet.addNumber(value, 2);
    }

    /** Put 4 bit effect **/
    // three more bits than needed, reserved for future API
    // functionality
    if (bs.getEffect() != null) {
      int value = 0;
      if (bs.getEffect().equals(Effect.NONE)) {
        value = 0;
      } else if (bs.getEffect().equals(Effect.COLORLOOP)) {
        value = 1;
      }

      mBitSet.addNumber(value, 4);
    }

    /** Put 16 bit transitiontime **/
    if (bs.getTransitionTime() != null) {
      mBitSet.addNumber(bs.getTransitionTime(), 16);
    }
  }

  /**
   * Set variable length list of variable length events *
   */
  private static void addListOfEvents(ManagedBitSet mBitSet, Mood mood,
                                      ArrayList<Integer> timeArray,
                                      ArrayList<BulbState> stateArray) {
    String[] bulbStateToStringArray = new String[stateArray.size()];
    for (int i = 0; i < stateArray.size(); i++) {
      bulbStateToStringArray[i] = stateArray.get(i).toString();
    }
    ArrayList<String> bulbStateToStringList =
        new ArrayList<String>(Arrays.asList(bulbStateToStringArray));
    for (Event e : mood.events) {

      // add channel number
      mBitSet.addNumber(e.getChannel(), getBitLength(mood.getNumChannels()));

      // add timestamp lookup number
      mBitSet.addNumber(timeArray.indexOf(e.getLegacyTime()), getBitLength(timeArray.size()));

      // add mood lookup number
      mBitSet.addNumber(bulbStateToStringList.indexOf(e.getBulbState().toString()),
                        getBitLength(stateArray.size()));
    }
  }

  /**
   * calulate number of bits needed to address this many addresses *
   */
  private static int getBitLength(int addresses) {
    int length = 0;
    while (addresses != 0) {
      addresses = addresses >>> 1;
      length++;
    }
    return length;
  }

  private static ArrayList<Integer> generateTimesArray(Mood mood) {
    HashSet<Integer> timeset = new HashSet<Integer>();
    for (Event e : mood.events) {
      timeset.add(e.getLegacyTime());
    }
    ArrayList<Integer> timesArray = new ArrayList<Integer>();
    timesArray.addAll(timeset);
    return timesArray;
  }

  private static ArrayList<BulbState> generateStatesArray(Mood mood) {
    HashMap<String, BulbState> statemap = new HashMap<String, BulbState>();
    for (Event e : mood.events) {
      statemap.put(e.getBulbState().toString(), e.getBulbState());
    }
    ArrayList<BulbState> statesArray = new ArrayList<BulbState>();
    statesArray.addAll(statemap.values());
    return statesArray;
  }

  private static BulbState extractState(ManagedBitSet mBitSet) {
    BulbState bs = new BulbState();

    /**
     * On, Bri, Hue, Sat, XY, CT, Alert, Effect, Transitiontime
     */
    boolean[] propertiesFlags = new boolean[9];
    /** Get 9 bit properties flags **/
    for (int j = 0; j < 9; j++) {
      propertiesFlags[j] = mBitSet.incrementingGet();
    }

    /** Get on bit **/
    if (propertiesFlags[0]) {
      bs.setOn(mBitSet.incrementingGet());
    }

    /** Get 8 bit bri **/
    if (propertiesFlags[1]) {
      bs.set255Bri(mBitSet.extractNumber(8));
    }

    Integer hue = null;
    /** Get 16 bit hue **/
    if (propertiesFlags[2]) {
      hue = mBitSet.extractNumber(16);
    }

    Integer sat = null;
    /** Get 8 bit sat **/
    if (propertiesFlags[3]) {
      sat = mBitSet.extractNumber(8);
    }
    // convert hue, sat into xy approximation
    if (hue != null && sat != null) {
      float[] hsv = {(hue * 360) / 65535, sat / 255f, 1};
      float[] input = {hsv[0] / 360f, hsv[1]};
      bs.setXY(Utils.hsTOxy(input));
    }

    /** Get 64 bit xy **/
    if (propertiesFlags[4]) {
      Float x = Float.intBitsToFloat(mBitSet.extractNumber(32));
      Float y = Float.intBitsToFloat(mBitSet.extractNumber(32));
      bs.setXY(new float[]{x, y});
    }

    /** Get 9 bit ct **/
    if (propertiesFlags[5]) {
      bs.setMiredCT(mBitSet.extractNumber(9));
    }

    /** Get 2 bit alert **/
    if (propertiesFlags[6]) {
      int value = mBitSet.extractNumber(2);
      switch (value) {
        case 0:
          bs.setAlert(Alert.NONE);
          break;
        case 1:
          bs.setAlert(Alert.FLASH_ONCE);
          break;
        case 2:
          bs.setAlert(Alert.FLASH_30SEC);
          break;
      }
    }

    /** Get 4 bit effect **/
    // three more bits than needed, reserved for future API
    // functionality
    if (propertiesFlags[7]) {
      int value = mBitSet.extractNumber(4);
      switch (value) {
        case 0:
          bs.setEffect(Effect.NONE);
          break;
        case 1:
          bs.setEffect(Effect.COLORLOOP);
          break;
      }
    }

    /** Get 16 bit transitiontime **/
    if (propertiesFlags[8]) {
      int value = mBitSet.extractNumber(16);
      bs.setTransitionTime(value);
    }

    return bs;
  }

  public static Pair<Integer[], Pair<Mood, Integer>> decode(String code)
      throws InvalidEncodingException, FutureEncodingException {
    try {
      Mood mood = new Mood();
      ArrayList<Integer> bList = new ArrayList<Integer>();
      Integer brightness = null;
      ManagedBitSet mBitSet = new ManagedBitSet(code);

      // 3 bit encoding version
      int encodingVersion = mBitSet.extractNumber(3);

      // 1 bit optional bulb inclusion flags flag
      boolean hasBulbs = mBitSet.incrementingGet();
      if (hasBulbs) {
        // 50 bits of optional bulb inclusion flags
        for (int i = 0; i < 50; i++) {
          if (mBitSet.incrementingGet()) {
            bList.add(i + 1);
          }
        }
      }

      if (encodingVersion == 1 || encodingVersion == 2 || encodingVersion == 3
          || encodingVersion == 4) {
        boolean hasBrightness = false;

        if (encodingVersion >= 3) {
          // 1 bit optional brightness inclusion flag
          hasBrightness = mBitSet.incrementingGet();
          if (hasBrightness)
          // 8 bit optional global brightness
          {
            brightness = mBitSet.extractNumber(8);
          }
        }

        int numChannels = mBitSet.extractNumber(6);
        mood.setNumChannels(numChannels);

        // 1 bit timing addressing reference mode
        mood.timeAddressingRepeatPolicy = mBitSet.incrementingGet();

        // 7 bit timing repeat number
        mood.setNumLoops(mBitSet.extractNumber(7));
        // flag infinite looping if max numLoops
        mood.setInfiniteLooping(mood.getNumLoops() == 127);

        // 6 bit number of timestamps
        int numTimestamps = mBitSet.extractNumber(6);
        int[] timeArray = new int[numTimestamps];
        for (int i = 0; i < numTimestamps; i++) {
          // 20 bit timestamp
          timeArray[i] = mBitSet.extractNumber(20);
        }
        mood.setUsesTiming(!(timeArray.length == 0 || (timeArray.length == 1 && timeArray[0] == 0)));

        int numStates;
        if (encodingVersion >= 4) {
          // 12 bit number of states
          numStates = mBitSet.extractNumber(12);
        } else {
          // 6 bit number of states
          numStates = mBitSet.extractNumber(6);
        }

        BulbState[] stateArray = new BulbState[numStates];
        for (int i = 0; i < numStates; i++) {
          // decode each state
          stateArray[i] = extractState(mBitSet);

          if (encodingVersion < 3) {
            // convert from old brightness stuffing to new relative brightness + total brightness
            // system
            if (stateArray[i].get255Bri() != null) {
              brightness = stateArray[i].get255Bri();
              stateArray[i].set255Bri(null);
            }
          }
        }

        // number of events, 8 bits for encodings 1 & 2, 12 bits for 3+
        int numEvents;
        if (encodingVersion <= 2) {
          numEvents = mBitSet.extractNumber(8);
        } else {
          numEvents = mBitSet.extractNumber(12);
        }
        Event[] eList = new Event[numEvents];

        for (int i = 0; i < numEvents; i++) {
          int channel = mBitSet.extractNumber(getBitLength(mood.getNumChannels()));

          long milliseconds = 100l * timeArray[mBitSet.extractNumber(getBitLength(numTimestamps))];

          BulbState state = stateArray[mBitSet.extractNumber(getBitLength(numStates))];

          eList[i] = new Event(state, channel, milliseconds);
        }
        mood.events = eList;

        // 20 bit loopIterationTimeLength is only difference between encodingVersion=1 & =2
        if (encodingVersion >= 2) {
          mood.loopIterationTimeLength = mBitSet.extractNumber(20);
        }

      } else if (encodingVersion == 0) {
        mBitSet.useLittleEndianEncoding(true);

        // 7 bit number of states
        int numStates = mBitSet.extractNumber(7);
        Event[] eventArray = new Event[numStates];

        /** Decode each state **/
        for (int i = 0; i < numStates; i++) {
          // decode each state
          BulbState state = extractState(mBitSet);

          // convert from old brightness stuffing to new relative brightness + total brightness
          // system
          if (state.get255Bri() != null) {
            brightness = state.get255Bri();
            state.set255Bri(null);
          }

          eventArray[i] = new Event(state, i, 0l);
        }
        mood.events = eventArray;
        mood.setNumChannels(numStates);
        mood.timeAddressingRepeatPolicy = false;
        mood.setUsesTiming(false);

      } else {
        throw new FutureEncodingException();
      }

      Integer[] bulbs = null;
      if (hasBulbs) {
        bulbs = new Integer[bList.size()];
        for (int i = 0; i < bList.size(); i++) {
          bulbs[i] = bList.get(i);
        }
      }

      return new Pair<Integer[], Pair<Mood, Integer>>(bulbs, new Pair<Mood, Integer>(mood,
                                                                                     brightness));
    } catch (FutureEncodingException e) {
      throw new FutureEncodingException();
    } catch (Exception e) {
      throw new InvalidEncodingException();
    }
  }
}
