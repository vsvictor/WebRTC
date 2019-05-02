package com.education.rtclib;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.education.rtclib.client.PeerConnectionClient;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.List;


public abstract class RTCFragment extends BaseFragment {

    private OnRTCListener listener;

    private static final String TAG = RTCFragment.class.getSimpleName();

    private SurfaceViewRenderer vvLocal;
    private SurfaceViewRenderer vvRemote;
    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<VideoRenderer.Callbacks>();
    private PeerConnectionClient peerConnectionClient = null;

    public RTCFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rtc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vvLocal = view.findViewById(R.id.vvLocal);
        vvRemote = view.findViewById(R.id.vvRemote);

        remoteRenderers.add(remoteProxyRenderer);
        peerConnectionClient = new PeerConnectionClient();

        vvLocal.init(peerConnectionClient.getRenderContext(), null);
        vvLocal.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        vvRemote.init(peerConnectionClient.getRenderContext(), null);
        vvRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        vvLocal.setZOrderMediaOverlay(true);
        vvLocal.setEnableHardwareScaler(true);
        vvRemote.setEnableHardwareScaler(true);
        setSwappedFeeds(true);

        String randomRoomID = "myroom";
        Log.d(TAG, getString(R.string.room_id_caption) + randomRoomID);

        connectVideoCall(randomRoomID);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRTCListener) {
            listener = (OnRTCListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnRTCListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Log.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? vvRemote : vvLocal);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? vvLocal : vvRemote);
        vvRemote.setMirror(isSwappedFeeds);
        vvLocal.setMirror(!isSwappedFeeds);
    }
    public interface OnRTCListener {
    }

    private static class ProxyRenderer implements VideoRenderer.Callbacks {
        private VideoRenderer.Callbacks target;

        @Override
        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                Log.d(TAG, "Dropping frame in proxy because target is null.");
                VideoRenderer.renderFrameDone(frame);
                return;
            }

            target.renderFrame(frame);
        }

        synchronized public void setTarget(VideoRenderer.Callbacks target) {
            this.target = target;
        }
    }
}
