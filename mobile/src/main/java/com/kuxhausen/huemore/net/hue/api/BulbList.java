package com.kuxhausen.huemore.net.hue.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BulbList {

  @SerializedName("1")
  private BulbAttributes b1;
  @SerializedName("2")
  private BulbAttributes b2;
  @SerializedName("3")
  private BulbAttributes b3;
  @SerializedName("4")
  private BulbAttributes b4;
  @SerializedName("5")
  private BulbAttributes b5;
  @SerializedName("6")
  private BulbAttributes b6;
  @SerializedName("7")
  private BulbAttributes b7;
  @SerializedName("8")
  private BulbAttributes b8;
  @SerializedName("9")
  private BulbAttributes b9;
  @SerializedName("10")
  private BulbAttributes b10;
  @SerializedName("11")
  private BulbAttributes b11;
  @SerializedName("12")
  private BulbAttributes b12;
  @SerializedName("13")
  private BulbAttributes b13;
  @SerializedName("14")
  private BulbAttributes b14;
  @SerializedName("15")
  private BulbAttributes b15;
  @SerializedName("16")
  private BulbAttributes b16;
  @SerializedName("17")
  private BulbAttributes b17;
  @SerializedName("18")
  private BulbAttributes b18;
  @SerializedName("19")
  private BulbAttributes b19;
  @SerializedName("20")
  private BulbAttributes b20;
  @SerializedName("21")
  private BulbAttributes b21;
  @SerializedName("22")
  private BulbAttributes b22;
  @SerializedName("23")
  private BulbAttributes b23;
  @SerializedName("24")
  private BulbAttributes b24;
  @SerializedName("25")
  private BulbAttributes b25;
  @SerializedName("26")
  private BulbAttributes b26;
  @SerializedName("27")
  private BulbAttributes b27;
  @SerializedName("28")
  private BulbAttributes b28;
  @SerializedName("29")
  private BulbAttributes b29;
  @SerializedName("30")
  private BulbAttributes b30;
  @SerializedName("31")
  private BulbAttributes b31;
  @SerializedName("32")
  private BulbAttributes b32;
  @SerializedName("33")
  private BulbAttributes b33;
  @SerializedName("34")
  private BulbAttributes b34;
  @SerializedName("35")
  private BulbAttributes b35;
  @SerializedName("36")
  private BulbAttributes b36;
  @SerializedName("37")
  private BulbAttributes b37;
  @SerializedName("38")
  private BulbAttributes b38;
  @SerializedName("39")
  private BulbAttributes b39;
  @SerializedName("40")
  private BulbAttributes b40;
  @SerializedName("41")
  private BulbAttributes b41;
  @SerializedName("42")
  private BulbAttributes b42;
  @SerializedName("43")
  private BulbAttributes b43;
  @SerializedName("44")
  private BulbAttributes b44;
  @SerializedName("45")
  private BulbAttributes b45;
  @SerializedName("46")
  private BulbAttributes b46;
  @SerializedName("47")
  private BulbAttributes b47;
  @SerializedName("48")
  private BulbAttributes b48;
  @SerializedName("49")
  private BulbAttributes b49;
  @SerializedName("50")
  private BulbAttributes b50;

  //experimental support for upto 64 hue bulbs. TODO support arbitrary bulbs once NFC rewrite
  @SerializedName("51")
  private BulbAttributes b51;
  @SerializedName("52")
  private BulbAttributes b52;
  @SerializedName("53")
  private BulbAttributes b53;
  @SerializedName("54")
  private BulbAttributes b54;
  @SerializedName("55")
  private BulbAttributes b55;
  @SerializedName("56")
  private BulbAttributes b56;
  @SerializedName("57")
  private BulbAttributes b57;
  @SerializedName("58")
  private BulbAttributes b58;
  @SerializedName("59")
  private BulbAttributes b59;
  @SerializedName("60")
  private BulbAttributes b60;
  @SerializedName("61")
  private BulbAttributes b61;
  @SerializedName("62")
  private BulbAttributes b62;
  @SerializedName("63")
  private BulbAttributes b63;
  @SerializedName("64")
  private BulbAttributes b64;

  public BulbList() {
  }

  public BulbAttributes[] getList() {
    ArrayList<BulbAttributes> ray = new ArrayList<BulbAttributes>();
    if (b1 != null) {
      b1.number = "1";
      ray.add(b1);
    }
    if (b2 != null) {
      b2.number = "2";
      ray.add(b2);
    }
    if (b3 != null) {
      b3.number = "3";
      ray.add(b3);
    }
    if (b4 != null) {
      b4.number = "4";
      ray.add(b4);
    }
    if (b5 != null) {
      b5.number = "5";
      ray.add(b5);
    }
    if (b6 != null) {
      b6.number = "6";
      ray.add(b6);
    }
    if (b7 != null) {
      b7.number = "7";
      ray.add(b7);
    }
    if (b8 != null) {
      b8.number = "8";
      ray.add(b8);
    }
    if (b9 != null) {
      b9.number = "9";
      ray.add(b9);
    }
    if (b10 != null) {
      b10.number = "10";
      ray.add(b10);
    }
    if (b11 != null) {
      b11.number = "11";
      ray.add(b11);
    }
    if (b12 != null) {
      b12.number = "12";
      ray.add(b12);
    }
    if (b13 != null) {
      b13.number = "13";
      ray.add(b13);
    }
    if (b14 != null) {
      b14.number = "14";
      ray.add(b14);
    }
    if (b15 != null) {
      b15.number = "15";
      ray.add(b15);
    }
    if (b16 != null) {
      b16.number = "16";
      ray.add(b16);
    }
    if (b17 != null) {
      b17.number = "17";
      ray.add(b17);
    }
    if (b18 != null) {
      b18.number = "18";
      ray.add(b18);
    }
    if (b19 != null) {
      b19.number = "19";
      ray.add(b19);
    }
    if (b20 != null) {
      b20.number = "20";
      ray.add(b20);
    }
    if (b21 != null) {
      b21.number = "21";
      ray.add(b21);
    }
    if (b22 != null) {
      b22.number = "22";
      ray.add(b22);
    }
    if (b23 != null) {
      b23.number = "23";
      ray.add(b23);
    }
    if (b24 != null) {
      b24.number = "24";
      ray.add(b24);
    }
    if (b25 != null) {
      b25.number = "25";
      ray.add(b25);
    }
    if (b26 != null) {
      b26.number = "26";
      ray.add(b26);
    }
    if (b27 != null) {
      b27.number = "27";
      ray.add(b27);
    }
    if (b28 != null) {
      b28.number = "28";
      ray.add(b28);
    }
    if (b29 != null) {
      b29.number = "29";
      ray.add(b29);
    }
    if (b30 != null) {
      b30.number = "30";
      ray.add(b30);
    }
    if (b31 != null) {
      b31.number = "31";
      ray.add(b31);
    }
    if (b32 != null) {
      b32.number = "32";
      ray.add(b32);
    }
    if (b33 != null) {
      b33.number = "33";
      ray.add(b33);
    }
    if (b34 != null) {
      b34.number = "34";
      ray.add(b34);
    }
    if (b35 != null) {
      b35.number = "35";
      ray.add(b35);
    }
    if (b36 != null) {
      b36.number = "36";
      ray.add(b36);
    }
    if (b37 != null) {
      b37.number = "37";
      ray.add(b37);
    }
    if (b38 != null) {
      b38.number = "38";
      ray.add(b38);
    }
    if (b39 != null) {
      b39.number = "39";
      ray.add(b39);
    }
    if (b40 != null) {
      b40.number = "40";
      ray.add(b40);
    }
    if (b41 != null) {
      b41.number = "41";
      ray.add(b41);
    }
    if (b42 != null) {
      b42.number = "42";
      ray.add(b42);
    }
    if (b43 != null) {
      b43.number = "43";
      ray.add(b43);
    }
    if (b44 != null) {
      b44.number = "44";
      ray.add(b44);
    }
    if (b45 != null) {
      b45.number = "45";
      ray.add(b45);
    }
    if (b46 != null) {
      b46.number = "46";
      ray.add(b46);
    }
    if (b47 != null) {
      b47.number = "47";
      ray.add(b47);
    }
    if (b48 != null) {
      b48.number = "48";
      ray.add(b48);
    }
    if (b49 != null) {
      b49.number = "49";
      ray.add(b49);
    }
    if (b50 != null) {
      b50.number = "50";
      ray.add(b50);
    }

    //experimental support for upto 64 hue bulbs. TODO support arbitrary bulbs once NFC rewrite
    if (b51 != null) {
      b51.number = "51";
      ray.add(b51);
    }
    if (b52 != null) {
      b52.number = "52";
      ray.add(b52);
    }
    if (b53 != null) {
      b53.number = "53";
      ray.add(b53);
    }
    if (b54 != null) {
      b54.number = "54";
      ray.add(b54);
    }
    if (b55 != null) {
      b55.number = "55";
      ray.add(b55);
    }
    if (b56 != null) {
      b56.number = "56";
      ray.add(b56);
    }
    if (b57 != null) {
      b57.number = "57";
      ray.add(b57);
    }
    if (b58 != null) {
      b58.number = "58";
      ray.add(b58);
    }
    if (b59 != null) {
      b59.number = "59";
      ray.add(b59);
    }
    if (b60 != null) {
      b60.number = "60";
      ray.add(b60);
    }
    if (b61 != null) {
      b61.number = "61";
      ray.add(b61);
    }
    if (b62 != null) {
      b62.number = "62";
      ray.add(b62);
    }
    if (b63 != null) {
      b63.number = "63";
      ray.add(b63);
    }
    if (b64 != null) {
      b64.number = "64";
      ray.add(b64);
    }

    BulbAttributes[] list = new BulbAttributes[ray.size()];
    for (int i = 0; i < list.length; i++) {
      list[i] = ray.get(i);
    }

    return list;
  }
}
