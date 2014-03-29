package com.kuxhausen.huemore.net;

import android.content.Context;

public interface Connection {
	public abstract void initializeConnection(Context c);
	public abstract void onDestroy();
}
