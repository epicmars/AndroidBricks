package com.androidpi.app.bricks.exoplayer.renderer;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

/**
 * Created by jastrelax on 2018/7/7.
 */
public class AudioRenderer extends MediaCodecAudioRenderer {

    public AudioRenderer(Context context, MediaCodecSelector mediaCodecSelector) {
        super(context, mediaCodecSelector);
    }

    public AudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys) {
        super(context, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys);
    }

    public AudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, @Nullable Handler eventHandler, @Nullable AudioRendererEventListener eventListener) {
        super(context, mediaCodecSelector, eventHandler, eventListener);
    }

    public AudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys, @Nullable Handler eventHandler, @Nullable AudioRendererEventListener eventListener) {
        super(context, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener);
    }

    public AudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys, @Nullable Handler eventHandler, @Nullable AudioRendererEventListener eventListener, @Nullable AudioCapabilities audioCapabilities, AudioProcessor... audioProcessors) {
        super(context, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener, audioCapabilities, audioProcessors);
    }

    public AudioRenderer(Context context, MediaCodecSelector mediaCodecSelector, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys, @Nullable Handler eventHandler, @Nullable AudioRendererEventListener eventListener, AudioSink audioSink) {
        super(context, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener, audioSink);
    }
}
