package com.example.mediasoupandroidsample.request;

import android.util.Log;

import com.example.mediasoupandroidsample.socket.ActionEvent;
import com.example.mediasoupandroidsample.socket.EchoSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Socket Request util class
 */
public class Request {
	/**
	 * Socket request acknowledgement response timeout
	 */
	private static final int REQUEST_TIMEOUT_SECONDS = 3000;

	// Send getRoomRtpCapabilities request
	public static JSONObject sendCreateRoomGetRoomRtpCapabilitiesRequest(EchoSocket socket, String roomId,String peerId)
	throws JSONException, InterruptedException, ExecutionException, TimeoutException {
		JSONObject getRoomRtpCapabilitiesRequest = new JSONObject();
		getRoomRtpCapabilitiesRequest.put("request", ActionEvent.CREATE_ROOM);
		getRoomRtpCapabilitiesRequest.put("roomId", roomId);
		getRoomRtpCapabilitiesRequest.put("peerId", peerId);
		getRoomRtpCapabilitiesRequest.put("videoCodec", "vp8");

		return socket.sendWithFuture(getRoomRtpCapabilitiesRequest).get(Request.REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}


	// Send createWebRtcTransport request
	public static JSONObject sendCreateWebRtcTransportRequest(EchoSocket socket, String roomId,String peerId, String type)
	throws JSONException, InterruptedException, ExecutionException, TimeoutException {
		JSONObject createWebRtcTransportRequest = new JSONObject();
		createWebRtcTransportRequest.put("request", ActionEvent.CREATE_WEBRTC_TRANSPORT);
		createWebRtcTransportRequest.put("roomId", roomId);
		createWebRtcTransportRequest.put("peerId", peerId);
		createWebRtcTransportRequest.put("type", type);

		return socket.sendWithFuture(createWebRtcTransportRequest).get(Request.REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}

	// Send connectWebRtcTransport request
	public static void sendConnectWebRtcTransportRequest(EchoSocket socket, String roomId,String peerId, String transportId, String dtlsParameters)
	throws JSONException {
		JSONObject connectWebRtcTransportRequest = new JSONObject();
		connectWebRtcTransportRequest.put("request", ActionEvent.CONNECT_WEBRTC_TRANSPORT);
		connectWebRtcTransportRequest.put("roomId", roomId);
		connectWebRtcTransportRequest.put("peerId", peerId);
		connectWebRtcTransportRequest.put("transportId", transportId);
		connectWebRtcTransportRequest.put("dtlsParameters", new JSONObject(dtlsParameters));

		socket.send(connectWebRtcTransportRequest);
	}

	// Send produce request
	public static JSONObject sendProduceWebRtcTransportRequest(EchoSocket socket, String roomId,String peerId, String transportId, String kind, String rtpParameters)
	throws JSONException, InterruptedException, ExecutionException, TimeoutException {
		JSONObject produceWebRtcTransportRequest = new JSONObject();
		produceWebRtcTransportRequest.put("request", ActionEvent.PRODUCE);
		produceWebRtcTransportRequest.put("roomId", roomId);
		produceWebRtcTransportRequest.put("peerId", peerId);
		produceWebRtcTransportRequest.put("transportId", transportId);
		produceWebRtcTransportRequest.put("kind", kind);
		produceWebRtcTransportRequest.put("rtpParameters", new JSONObject(rtpParameters));

		return socket.sendWithFuture(produceWebRtcTransportRequest).get(Request.REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}

	// Send consume request
	public static JSONObject sendConsumeWebRtcTransportRequest(EchoSocket socket, String roomId,String consumerPeerId,String producerPeerId,String producerId, String rtpCapabilities, String transportId)
			throws JSONException, InterruptedException, ExecutionException, TimeoutException {
		JSONObject produceWebRtcTransportRequest = new JSONObject();
		produceWebRtcTransportRequest.put("request", ActionEvent.CONSUME);
		produceWebRtcTransportRequest.put("roomId", roomId);
		produceWebRtcTransportRequest.put("consumerPeerId", consumerPeerId);
		produceWebRtcTransportRequest.put("producerPeerId", producerPeerId);
		produceWebRtcTransportRequest.put("producerId", producerId);
		produceWebRtcTransportRequest.put("rtpCapabilities", rtpCapabilities);
		produceWebRtcTransportRequest.put("transportId", transportId);

		Log.e("Consume request",produceWebRtcTransportRequest.toString());

		return socket.sendWithFuture(produceWebRtcTransportRequest).get(Request.REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}
}
