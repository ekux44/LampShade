package com.kuxhausen.huemore.net;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

import alt.android.os.CountDownTimer;
import android.content.Context;
import android.util.Pair;

public class DeviceManager {
	
	private ArrayList<Connection> mConnections;
	private Context mContext;
	private Group selectedGroup;
	private String selectedGroupName;
	
	public DeviceManager(Context c){
		mContext = c;
		
		//load all connections from the database 
		mConnections = new ArrayList<Connection>();
		mConnections.addAll(HubConnection.loadHubConnections(c));
		
		//do something with bulbs
		
		
		//junk?
		me = (MoodExecuterService)c;
		volleyRQ = Volley.newRequestQueue(mContext);
		restartCountDownTimer();
	}
	
	
	
	public void onDestroy() {
		if(countDownTimer!=null)
			countDownTimer.cancel();
		volleyRQ.cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		volleyRQ.cancelAll(InternalArguments.PERMANENT_NETWORK_REQUEST);
	}
	
	
	public Group getSelectedGroup(){
		return selectedGroup;
	}
	public String getSelectedGroupName(){
		return selectedGroupName;
	}
	
	
	
	//TODO clean up/remove everything below this line
	
		public void transmit(int bulb, BulbState bs){
			queue.add(new Pair<Integer,BulbState>(bulb,bs));
		}
	
	
	
		private MoodExecuterService me;
		
		private RequestQueue volleyRQ;
		private static CountDownTimer countDownTimer;
		private final static int TRANSMITS_PER_SECOND = 10;
		private final static int MAX_STOP_SELF_COUNDOWN = TRANSMITS_PER_SECOND*3;
		private static int countDownToStopSelf = MAX_STOP_SELF_COUNDOWN;
		private static boolean suspendingTillNextEvent = false;
		public enum KnownState {Unknown, ToSend, Getting, Synched};	
		public Integer maxBrightness;
		public int[] bulbBri;
		public int[] bulbRelBri;
		public KnownState[] bulbKnown;
		public String groupName;
		private static int MAX_REL_BRI = 255;
		
		boolean groupIsColorLooping=false;
		boolean groupIsAlerting=false;
		Queue<Pair<Integer,BulbState>> queue = new LinkedList<Pair<Integer,BulbState>>();
		int transientIndex = 0;
		
		
		public String getGroupName(){
			return groupName;
		}
		
		public Integer getMaxBrightness(){
			return maxBrightness;
		}
		
		public RequestQueue getRequestQueue() {
			return volleyRQ;
		}
		
		public synchronized void onGroupSelected(int[] bulbs, Integer optionalBri, String groupName){
			selectedGroup = new Group();
			selectedGroup.groupAsLegacyArray = bulbs;
			
			
			groupIsAlerting = false;
			groupIsColorLooping = false;
			maxBrightness = null;
			bulbBri = new int[selectedGroup.groupAsLegacyArray.length];
			bulbRelBri = new int[selectedGroup.groupAsLegacyArray.length];
			bulbKnown = new KnownState[selectedGroup.groupAsLegacyArray.length];
			for(int i = 0; i < bulbRelBri.length; i++){
				bulbRelBri[i] = MAX_REL_BRI;
				bulbKnown[i] = KnownState.Unknown;
			}
			
			this.groupName = groupName;
			
			if(optionalBri==null){
				for(int i = 0; i< selectedGroup.groupAsLegacyArray.length; i++){
					bulbKnown[i] = KnownState.Getting;
					NetworkMethods.PreformGetBulbAttributes(mContext, getRequestQueue(), me, me, selectedGroup.groupAsLegacyArray[i]);
				}
			} else {
				maxBrightness = optionalBri;
				for(int i = 0; i< selectedGroup.groupAsLegacyArray.length; i++)
					bulbKnown[i] = KnownState.ToSend;
				me.onBrightnessChanged();
			}
		}
		
		/** doesn't notify listeners **/
		public synchronized void setBrightness(int brightness){
			
			maxBrightness = brightness;
			if(selectedGroup.groupAsLegacyArray!=null){
				for(int i = 0; i< selectedGroup.groupAsLegacyArray.length; i++){
					bulbBri[i] = (maxBrightness * bulbRelBri[i])/MAX_REL_BRI; 
					bulbKnown[i] = KnownState.ToSend;
				}
			}
		}
		
		public void onAttributesReturned(BulbAttributes result, int bulbNumber) {
			//figure out which bulb in group (if that group is still selected)
			int index = calculateBulbPositionInGroup(bulbNumber);
			//if group is still expected this, save 
			if(index>-1 && bulbKnown[index]==KnownState.Getting){
				bulbKnown[index] = KnownState.Synched;
				bulbBri[index] = result.state.bri;
				
				//if all expected get brightnesses have returned, compute maxbri and notify listeners
				boolean anyOutstandingGets = false;
				for(KnownState ks : bulbKnown)
					anyOutstandingGets |= (ks == KnownState.Getting);
				if(!anyOutstandingGets){
					//todo calc more intelligent bri when mood known
					int briSum = 0;
					for(int bri : bulbBri)
						briSum +=bri;
					maxBrightness = briSum/selectedGroup.groupAsLegacyArray.length;
					
					for(int i = 0; i< selectedGroup.groupAsLegacyArray.length; i++){
						bulbBri[i]= maxBrightness;
						bulbRelBri[i] = MAX_REL_BRI;
					}
					
					me.onBrightnessChanged();
				}	
			}	
		}
		/** finds bulb index within group[] **/
		private int calculateBulbPositionInGroup(int bulbNumber){
			int index = -1;
			for(int j = 0; j< selectedGroup.groupAsLegacyArray.length; j++){
				if(selectedGroup.groupAsLegacyArray[j]==bulbNumber)
					index = j;
			}
			return index;
		}
		
		private boolean hasTransientChanges() {
			if(bulbKnown==null)
				return false;		
			boolean result = false;
			for (KnownState ks : bulbKnown)
				result |= (ks==KnownState.ToSend);
			return result;
		}
		
		public void restartCountDownTimer() {
			
			if (countDownTimer != null)
				countDownTimer.cancel();

			transientIndex = 0;
			countDownToStopSelf = MAX_STOP_SELF_COUNDOWN;
			suspendingTillNextEvent = false;
			// runs at the rate to execute 15 op/sec
			countDownTimer = new CountDownTimer(Integer.MAX_VALUE, (1000 / TRANSMITS_PER_SECOND)) {

				@Override
				public void onFinish() {
				}

				@Override
				public void onTick(long millisUntilFinished) {
					if (queue.peek()!=null) {
						Pair<Integer,BulbState> e = queue.poll();
						int bulb = e.first;
						BulbState state = e.second;
						
						//remove effect=none except when meaningful (after spotting an effect=colorloop)
						if(state.effect!=null && state.effect.equals("colorloop")){
							groupIsColorLooping = true;
						}else if(!groupIsColorLooping){
							state.effect = null;
						}
						//remove alert=none except when meaningful (after spotting an alert=colorloop)
						if(state.alert!=null && (state.alert.equals("select")||state.alert.equals("lselect"))){
							groupIsAlerting = true;
						}else if(!groupIsAlerting){
							state.alert = null;
						}
						
						
						int bulbInGroup = calculateBulbPositionInGroup(bulb);
						if(bulbInGroup>-1 && maxBrightness!=null){
							//convert relative brightness into absolute brightness
							if(state.bri!=null)
								bulbRelBri[bulbInGroup] = state.bri;
							else
								bulbRelBri[bulbInGroup] = MAX_REL_BRI;
							bulbBri[bulbInGroup] = (bulbRelBri[bulbInGroup] * maxBrightness)/ MAX_REL_BRI;
							state.bri = bulbBri[bulbInGroup];
							bulbKnown[bulbInGroup] = KnownState.Synched;
						}					
						NetworkMethods.PreformTransmitGroupMood(mContext, getRequestQueue(), me, bulb, state);
					} else if (hasTransientChanges()) {
						boolean sentSomething = false;
						while (!sentSomething) {
							if(bulbKnown[transientIndex] == KnownState.ToSend){
								BulbState bs = new BulbState();
								bulbBri[transientIndex] = (bulbRelBri[transientIndex] * maxBrightness)/ MAX_REL_BRI;
								bs.bri = bulbBri[transientIndex];
								
								NetworkMethods.PreformTransmitGroupMood(mContext, getRequestQueue(), me, selectedGroup.groupAsLegacyArray[transientIndex], bs);
								bulbKnown[transientIndex] = KnownState.Synched;
								sentSomething = true;
							}
							transientIndex = (transientIndex + 1) % selectedGroup.groupAsLegacyArray.length;
						}
					} 
				}
			};
			countDownTimer.start();
		}

}
