package com.example.vtiu.data.remote

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import android.view.SurfaceView

class AgoraManager(private val context: Context) {
    private var rtcEngine: RtcEngine? = null
    private var isInitialized = false

    // State to track remote users
    private val _remoteUsers = mutableStateListOf<Int>()
    val remoteUsers: List<Int> = _remoteUsers

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.i("AgoraManager", "User joined: $uid")
            if (!_remoteUsers.contains(uid)) {
                _remoteUsers.add(uid)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i("AgoraManager", "User offline: $uid")
            _remoteUsers.remove(uid)
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.i("AgoraManager", "Join channel success: $channel, uid: $uid")
        }
    }

    fun init(appId: String) {
        if (isInitialized) return
        Log.d("AgoraManager", "Initializing with App ID: $appId")
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            rtcEngine = RtcEngine.create(config)
            
            // Critical for Cross-Platform Compatibility
            rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            rtcEngine?.enableVideo()
            Log.d("AgoraManager", "Video enabled")
            
            // Set Video Encoder Configuration for better web compatibility
            val videoConfig = VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            )
            rtcEngine?.setVideoEncoderConfiguration(videoConfig)
            isInitialized = true
            Log.d("AgoraManager", "Initialization complete")
        } catch (e: Exception) {
            Log.e("AgoraManager", "Initialization failed: ${e.message}")
        }
    }

    fun joinChannel(
        channelName: String, 
        uid: Int = 0, 
        token: String? = null, 
        role: Int = Constants.CLIENT_ROLE_BROADCASTER,
        publishCamera: Boolean = false, // Changed to false by default to allow preview before live
        publishMic: Boolean = false
    ) {
        if (!isInitialized) {
            Log.e("AgoraManager", "Cannot join channel: RTC Engine not initialized")
            return
        }
        rtcEngine?.setClientRole(role)
        
        val options = ChannelMediaOptions()
        options.clientRoleType = role
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        
        // Use provided flags if role is Broadcaster, else don't publish
        val isBroadcaster = role == Constants.CLIENT_ROLE_BROADCASTER
        options.publishCameraTrack = isBroadcaster && publishCamera
        options.publishMicrophoneTrack = isBroadcaster && publishMic
        options.autoSubscribeAudio = true
        options.autoSubscribeVideo = true
        
        Log.d("AgoraManager", "Joining channel: $channelName as ${if(isBroadcaster) "Broadcaster" else "Audience"}")
        rtcEngine?.joinChannel(token, channelName, uid, options)
    }

    fun updatePublishState(publishCamera: Boolean, publishMic: Boolean) {
        val options = ChannelMediaOptions()
        options.publishCameraTrack = publishCamera
        options.publishMicrophoneTrack = publishMic
        rtcEngine?.updateChannelMediaOptions(options)
        Log.d("AgoraManager", "Updated publish state: Camera=$publishCamera, Mic=$publishMic")
    }

    fun startPreview() {
        Log.d("AgoraManager", "Starting preview")
        rtcEngine?.startPreview()
    }

    fun stopPreview() {
        Log.d("AgoraManager", "Stopping preview")
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
        Log.d("AgoraManager", "Setting up local video")
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

    fun setRole(role: Int) {
        rtcEngine?.setClientRole(role)
    }

    fun release() {
        RtcEngine.destroy()
        rtcEngine = null
    }
}
