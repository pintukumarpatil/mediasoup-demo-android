package com.example.mediasoupandroidsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.mediasoupandroidsample.permission.PermissionFragment;
import com.example.mediasoupandroidsample.request.Request;
import com.example.mediasoupandroidsample.room.RoomClient;
import com.example.mediasoupandroidsample.socket.ActionEvent;
import com.example.mediasoupandroidsample.socket.EchoSocket;
import com.example.mediasoupandroidsample.socket.MessageObserver;

import org.json.JSONArray;
import org.mediasoup.droid.Consumer;
import org.mediasoup.droid.Device;
import org.mediasoup.droid.Logger;
import org.mediasoup.droid.MediasoupClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MessageObserver.Observer {
    private static final String TAG = "MainActivity";

    private SurfaceViewRenderer mVideoView;
    private SurfaceViewRenderer mRemoteVideoView;
    private PermissionFragment mPermissionFragment;
    private RoomClient mClient;
    private  String peerId="pintu";
    private String roomId="room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Mediasoup
        mVideoView = findViewById(R.id.local_video_view);
        mRemoteVideoView = findViewById(R.id.remote_video_view);

	    EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();
	    runOnUiThread(() -> mRemoteVideoView.init(eglBaseContext, null));

        addPermissionFragment();
        // FIX: race problem, asking for permissions before fragment is full attached..
        getSupportFragmentManager().executePendingTransactions();

        // Connect to the websocket server
        this.connectWebSocket();
    }
    EchoSocket socket = new EchoSocket();
    private void connectWebSocket() {

        socket.register(this);

        try {
            // Connect to server
            socket.connect("wss://192.168.7.166:3001").get(3000, TimeUnit.SECONDS);

            // Initialize mediasoup client
            initializeMediasoupClient();

            // Get router rtp capabilities
            Request.sendCreateRoomGetRoomRtpCapabilitiesRequest(socket, roomId,peerId);

        } catch (Exception e) {
            Log.e(TAG, "Failed to connect to socket server error=", e);
        }
    }

	/**
	 * Initialize Mediasoup Client
	 */
	private void initializeMediasoupClient() {
        MediasoupClient.initialize(getApplicationContext());
        Log.d(TAG, "Mediasoup client initialized");

        // Set mediasoup log
        Logger.setLogLevel(Logger.LogLevel.LOG_TRACE);
        Logger.setDefaultHandler();
    }

	/**
	 * Capture and start producing local video/audio
	 */
	private void displayLocalVideo () {
        mPermissionFragment.setPermissionCallback(new PermissionFragment.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                try {
                    EglBase.Context context = EglBase.create().getEglBaseContext();
                    runOnUiThread(() -> mVideoView.init(context, null));

                    mClient.produceAudio();
                    mClient.produceVideo(getBaseContext(), mVideoView, context);
                    mVideoView.bringToFront();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to initialize local stream e=" + e.getLocalizedMessage());
                }
            }

            @Override
            public void onPermissionDenied() {
                Log.w(TAG, "User denied camera/mic permission");
            }
        });

        mPermissionFragment.checkCameraMicPermission();
    }

	/**
	 * Add Permission Headless Fragment
	 */
	private void addPermissionFragment() {
        mPermissionFragment = (PermissionFragment) getSupportFragmentManager().findFragmentByTag(PermissionFragment.TAG);

        if(mPermissionFragment == null) {
            mPermissionFragment = PermissionFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(mPermissionFragment, PermissionFragment.TAG)
                    .commit();
        }
    }

	@Override
	public void on(@ActionEvent.Event String event, JSONObject data) {
		Log.d(TAG, "Received event " + event);

		try {
			switch(event) {

                case ActionEvent.CREATE_ROOM:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handleCreateRoomResponse(data.getJSONObject("roomRtpCapabilities"),data.getJSONArray("peers"));
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                                             }
                    });
                    break;
                case ActionEvent.NEW_PRODUCER:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handleNewProducerEvent(data.getJSONObject("producerData"));
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });

					break;
				case ActionEvent.CONSUME:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handleNewConsumerEvent(data.getJSONObject("consumerData"));
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });

					break;
			}
		} catch (Exception je) {
            Log.e(TAG, "Failed to handle event", je);
        }
	}

    private void handleCreateRoomResponse(JSONObject roomRtpCapabilities,JSONArray peers) {

        try {

            Log.e(TAG, "peers="+peers.toString());
            Log.e(TAG, "roomRtpCapabilities="+roomRtpCapabilities.toString());

            // Initialize mediasoup device
            Device device = new Device();
            device.load(roomRtpCapabilities.toString());

            // Create a new room client
            mClient = new RoomClient(socket, device, roomId,peerId,peers);

            // Join the room
            mClient.join();

            // Create send WebRtcTransport
            mClient.createSendTransport();

            // Create recv WebRtcTransport
            mClient.createRecvTransport();

            // Produce local media
            displayLocalVideo();

        } catch (JSONException je) {
            Log.e(TAG, "Failed to handle event", je);
        } catch (Exception je) {
            Log.e(TAG, "Failed to handle event", je);
        }

    }
	/**
	 * Handle remote newconsumer event
	 * @param consumerInfo ConsumerInfo
	 */
	private void handleNewConsumerEvent(JSONObject consumerInfo) {
    	try {
		    Consumer kindConsumer = mClient.consumeTrack(consumerInfo);

		    // If the remote consumer is video attach to the remote video renderer
		    if (kindConsumer.getKind().equals("video")) {
			    VideoTrack videoTrack = (VideoTrack) kindConsumer.getTrack();
			    videoTrack.setEnabled(true);
			    videoTrack.addSink(mRemoteVideoView);
		    }
	    } catch (Exception e) {
    		Log.e(TAG, "Failed to consume remote track");
	    }
    }
    private void handleNewProducerEvent(JSONObject producerInfo) {
        try {
            mClient.consume(producerInfo);
        } catch (Exception e) {
            Log.e(TAG, "transport::onProduce failed", e);
        }
    }

}
