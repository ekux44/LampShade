package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.persistence.ManagedBitSet;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;

public class HueUrlEncoderTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }


  public void testEncodeDecode() {
    BulbState[] states = new BulbState[10];
    for (int i = 0; i < states.length; i++) {
      states[i] = new BulbState();
    }

    states[0].setOn(true);

    states[1].set255Bri(44);

    states[2].setTransitionTime(33);

    states[3].setMiredCT(321);

    states[4].setXY(new float[]{.5f, .5f});

    states[5].setEffect(BulbState.Effect.NONE);

    states[6].setEffect(BulbState.Effect.COLORLOOP);

    states[7].setAlert(BulbState.Alert.NONE);

    states[8].setAlert(BulbState.Alert.FLASH_ONCE);

    states[8].setAlert(BulbState.Alert.FLASH_30SEC);

    states[9].setOn(false);
    states[9].set255Bri(0);
    states[9].setTransitionTime(0);
    states[9].setMiredCT(400);
    states[9].setXY(new float[]{.1f, .9f});
    states[9].setEffect(BulbState.Effect.NONE);
    states[9].setAlert(BulbState.Alert.NONE);

    try {
      for (int i = 0; i < states.length; i++) {
        Mood m = new Mood(states[i]);
        String encodedM = HueUrlEncoder.encode(m);
        Mood decodedM = HueUrlEncoder.decode(encodedM).second.first;

        assertEquals(m, decodedM);
      }
    } catch (InvalidEncodingException e) {
      fail();
    } catch (FutureEncodingException e) {
      fail();
    }
  }

  public void testFutureEncodingException() {
    ManagedBitSet bits = new ManagedBitSet();
    bits.addNumber(HueUrlEncoder.PROTOCOL_VERSION_NUMBER + 1, 3);
    String encoded = bits.getBase64Encoding();

    try {
      Mood decodedM = HueUrlEncoder.decode(encoded).second.first;
      fail();
    } catch (InvalidEncodingException e) {
      fail();
    } catch (FutureEncodingException e) {
      //success
    }
  }

  public void testInvalidEncodingExceptionV4() {
    ManagedBitSet bits = new ManagedBitSet();

    assertInvalidEncoding(bits);

    //version number
    bits.addNumber(4, 3);

    assertInvalidEncoding(bits);

    //flag no group
    bits.incrementingSet(false);

    assertInvalidEncoding(bits);

    //flag no brightness
    bits.incrementingSet(false);

    //TODO add more
  }

  private void assertInvalidEncoding(ManagedBitSet mbs){
    try {
      String encoded = mbs.getBase64Encoding();
      Mood decoded = HueUrlEncoder.decode(encoded).second.first;
      fail();
    } catch (InvalidEncodingException e) {
      //success
    } catch (FutureEncodingException e) {
      fail();
    }
  }

  /**
   * tests Off mood *
   */
  public void functionality1() {
    BulbState bs1 = new BulbState();
    bs1.setOn(false);

    Event e1 = new Event(bs1, 0, 0);

    Mood m = new Mood();
    m.setEvents(new Event[]{e1});

    String encoded = HueUrlEncoder.encode(m);
    String savedEncodedV4 = "AQQAAQAAAAMBAAQAAAA=";

    try {
      Mood decode = HueUrlEncoder.decode(encoded).second.first;

      assertEquals(m, decode);
      assertEquals(m, HueUrlEncoder.decode(savedEncodedV4).second.first);

      assertEquals(encoded, HueUrlEncoder.encode(decode));

    } catch (InvalidEncodingException e) {
      fail();
    } catch (FutureEncodingException e) {
      fail();
    }
  }
}
