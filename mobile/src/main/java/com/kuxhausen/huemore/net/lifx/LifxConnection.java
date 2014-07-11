package com.kuxhausen.huemore.net.lifx;

import android.content.Context;

import com.kuxhausen.huemore.net.Connection;
import com.kuxhausen.huemore.net.NetworkBulb;

import java.util.ArrayList;

public class LifxConnection implements Connection {
    @Override
    public void initializeConnection(Context c) {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public ArrayList<NetworkBulb> getBulbs() {
        return new ArrayList<NetworkBulb>();
    }

    @Override
    public String mainDescription() {
        return "";
    }

    @Override
    public String subDescription() {
        return "";
    }

    @Override
    public boolean hasPendingWork() {
        return false;
    }

    @Override
    public void delete() {

    }

  public static class ExtraData{

  }
}
