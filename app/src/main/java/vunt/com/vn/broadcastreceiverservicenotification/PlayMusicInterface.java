package vunt.com.vn.broadcastreceiverservicenotification;

public interface PlayMusicInterface {
    void create(int index);
    void start();
    void pause();
    int getDuration();
    int getCurrrentPosition();
    boolean isPlaying();
    void seek(int possition);
    void loop(boolean isLoop);
    int getSong();
    void stopService();
    void changeSong(int i);
}
