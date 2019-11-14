package com.example.mediasoupandroidsample.socket;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Socket Events Enum
 */
public class ActionEvent {
	// Socket connected to the server
	public static final String OPEN = "open";
	// Create new WebRtcTransport
	public static final String CREATE_WEBRTC_TRANSPORT = "create-transport";
	// Connect WebRtcTransport
	public static final String CONNECT_WEBRTC_TRANSPORT = "connect-transport";
	// Send media to mediasoup
	public static final String PRODUCE = "produce";
	// newuser notification
	public static final String NEW_PRODUCER = "new-producer";
	// Send media to mediasoup
	public static final String CONSUME = "consume";

	public static final String CREATE_ROOM="create-room";

	@StringDef({ OPEN, CREATE_WEBRTC_TRANSPORT, CONNECT_WEBRTC_TRANSPORT, PRODUCE, NEW_PRODUCER,CONSUME,CREATE_ROOM})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Event {}
}
