package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pranav on 11/9/17.
 */

class MusicManager {
    private AudioManager mAudioManager;
    private static Context context;
    private Timer mMusicPlayerStartTimer = new Timer("MusicPlayerStartTimer", true);
    private int previousVolume = 0;
    private boolean muted = false;

    public MusicManager(Context context, AudioManager mAudioManager) {
        this.mAudioManager = mAudioManager;
        this.context = context;
    }

    public void nextSong() {

        int keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;

        if (!isSpotifyRunning()) {
            startMusicPlayer();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setPackage("com.spotify.music");
        synchronized (this) {
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            context.sendOrderedBroadcast(intent, null);

            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            context.sendOrderedBroadcast(intent, null);
        }
    }

    public void previousSong() {

        int keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;

        if (!isSpotifyRunning()) {
            startMusicPlayer();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setPackage("com.spotify.music");
        synchronized (this) {
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            context.sendOrderedBroadcast(intent, null);

            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            context.sendOrderedBroadcast(intent, null);
        }
    }


    public void playPauseMusic() {
        int keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;

        if (!mAudioManager.isMusicActive() && !isSpotifyRunning()) {
            startMusicPlayer();
        }

        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.setPackage("com.spotify.music");
        synchronized (this) {
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            context.sendOrderedBroadcast(i, null);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            context.sendOrderedBroadcast(i, null);
        }
    }

    private void startMusicPlayer() {
        Intent startPlayer = new Intent(Intent.ACTION_MAIN);
        startPlayer.setPackage("com.spotify.music");
        startPlayer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startPlayer);

        if (mMusicPlayerStartTimer != null) {
            mMusicPlayerStartTimer.cancel();
        }

        mMusicPlayerStartTimer = new Timer("MusicPlayerStartTimer", true);
        mMusicPlayerStartTimer.schedule(new MusicPlayerStartTimerTask(), DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS);
    }

    private boolean isSpotifyRunning() {
        Process ps = null;
        try {
            String[] cmd = {
                    "sh",
                    "-c",
                    "ps | grep com.spotify.music"
            };

            ps = Runtime.getRuntime().exec(cmd);
            ps.waitFor();

            return ps.exitValue() == 0;
        } catch (IOException e) {
            Log.e("mes", "IO error");
        } catch (InterruptedException e) {
            Log.e("mes", "Interrupted error");
        } finally {
            if (ps != null) {
                ps.destroy();
            }
        }

        return false;
    }

    private class MusicPlayerStartTimerTask extends TimerTask {
        @Override
        public void run() {
            if (isSpotifyRunning()) {
                playPauseMusic();
                cancel();
            }
        }
    }

    public void increaseVolume(int steps) {
        muted = false;
        for (int i = 0; i < steps; ++i) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    public void decreaseVolume(int steps) {
        for (int i = 0; i < steps; ++i) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    public void mute() {
        if (muted) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, previousVolume);
        } else {
            previousVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }
        muted = !muted;
    }



}
