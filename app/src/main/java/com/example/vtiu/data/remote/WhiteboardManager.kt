package com.example.vtiu.data.remote

import android.content.Context
import io.agora.board.fast.Fastboard
import io.agora.board.fast.FastboardView
import io.agora.board.fast.FastRoom
import io.agora.board.fast.model.FastRoomOptions
import io.agora.board.fast.model.FastRegion

class WhiteboardManager(private val context: Context) {
    private var fastboard: Fastboard? = null
    private var fastRoom: FastRoom? = null

    fun setupWhiteboard(
        fastboardView: FastboardView,
        appId: String,
        roomUuid: String,
        roomToken: String,
        userId: String,
        region: String = "us"
    ) {
        fastboard = fastboardView.fastboard
        
        val fastRegion = when(region.lowercase()) {
            "cn" -> FastRegion.CN_HZ
            "in" -> FastRegion.IN_MUM
            "sg" -> FastRegion.SG
            else -> FastRegion.US_SV
        }

        val options = FastRoomOptions(
            appId,
            roomUuid,
            roomToken,
            userId,
            fastRegion
        )

        fastRoom = fastboard?.createFastRoom(options)
        fastRoom?.join()
    }

    fun setWritable(writable: Boolean) {
        fastRoom?.setWritable(writable)
    }

    fun release() {
        fastRoom?.destroy()
        fastRoom = null
        fastboard = null
    }
}
