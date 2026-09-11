package com.example.vtiu.data.remote

import android.content.Context
import android.content.Intent
import android.util.Log
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import android.view.SurfaceView

class AgoraManager(private val context: Context) {
    private var rtcEngine: RtcEngine? = null
    
    // PLACEHOLDER: Replace with your actual App ID from Agora Console
    private val appId = "c79f6fe95bad487cafec43820f0200cb"

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.i("AgoraManager", "User joined: $uid")
            // Notify UI to setup remote video
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i("AgoraManager", "User offline: $uid")
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.i("AgoraManager", "Join channel success: $channel, uid: $uid")
        }
    }

    init {
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            rtcEngine = RtcEngine.create(config)
            rtcEngine?.enableVideo()
        } catch (e: Exception) {
            Log.e("AgoraManager", "Initialization failed: ${e.message}")
        }
    }

    fun joinChannel(channelName: String, uid: Int = 0, token: String? = null, role: Int = Constants.CLIENT_ROLE_BROADCASTER) {
        val options = ChannelMediaOptions()
        options.clientRoleType = role
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        options.publishCameraTrack = true
        options.publishMicrophoneTrack = true
        
        rtcEngine?.joinChannel(token, channelName, uid, options)
    }

    fun startPreview() {
        rtcEngine?.startPreview()
    }

    fun stopPreview() {
        rtcEngine?.stopPreview()
    }

    fun startScreenSharing() {
        // Start Foreground Service for Screen Sharing (Required for Android 10+)
        val serviceIntent = Intent(context, MediaProjectionService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val params = ScreenCaptureParameters()
        params.captureVideo = true
        params.captureAudio = true
        
        rtcEngine?.startScreenCapture(params)
        
        val options = ChannelMediaOptions()
        options.publishCameraTrack = false
        options.publishScreenCaptureVideo = true
        options.publishScreenCaptureAudio = true
        rtcEngine?.updateChannelMediaOptions(options)
    }

    fun stopScreenSharing(publishCamera: Boolean = true) {
        rtcEngine?.stopScreenCapture()
        
        // Stop Foreground Service
        context.stopService(Intent(context, MediaProjectionService::class.java))

        val options = ChannelMediaOptions()
        options.publishCameraTrack = publishCamera
        options.publishScreenCaptureVideo = false
        options.publishScreenCaptureAudio = false
        rtcEngine?.updateChannelMediaOptions(options)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    fun setupLocalVideo(surfaceView: SurfaceView) {
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun muteLocalAudio(muted: Boolean) {
        rtcEngine?.muteLocalAudioStream(muted)
    }

    fun muteLocalVideo(muted: Boolean) {
        rtcEngine?.muteLocalVideoStream(muted)
    }

    fun release() {
        RtcEngine.destroy()
        rtcEngine = null
    }
}
