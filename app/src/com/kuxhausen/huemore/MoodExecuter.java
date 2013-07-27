package com.kuxhausen.huemore;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MoodExecuter extends Service {
	public MoodExecuter() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		//throw new UnsupportedOperationException("Not yet implemented");
		return null;
	}
}
