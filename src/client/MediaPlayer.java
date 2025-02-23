package client;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MediaPlayer {

    private final Map<SoundType, Clip> soundCache = new HashMap<>();

    public MediaPlayer() {
        preloadSounds();
    }

    private void preloadSounds() {
        for (SoundType sound : SoundType.values()) {
            try {
                File audioFile = new File("resource/" + sound.getFilePath());
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundCache.put(sound, clip);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.out.println("Error loading sound: " + sound);
            }
        }
    }

    public void playSound(SoundType sound) {
        Clip clip = soundCache.get(sound);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        } else {
            System.out.println("Invalid sound: " + sound);
        }
    }

    public void setVolume(SoundType sound, float volume) {
        if (volume < 0f || volume > 1f) {
            throw new IllegalArgumentException("Volume not valid: " + volume);
        }
        Clip clip = soundCache.get(sound);
        if (clip != null) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float dB = min + (max - min) * volume;
            gainControl.setValue(dB);
        }
    }

    public void stop(SoundType sound) {
        Clip clip = soundCache.get(sound);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}