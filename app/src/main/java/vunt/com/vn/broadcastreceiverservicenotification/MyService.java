package vunt.com.vn.broadcastreceiverservicenotification;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

public class MyService extends Service implements MediaPlayer.OnCompletionListener,
        PlayMusicInterface {
    public static final String EXTRA_REQUEST_CODE = "REQUEST_CODE";
    public static final int VALUE_NEXT_SONG = 9596;
    public static final int VALUE_PREVIOUS_SONG = 7300;
    public static final int VALUE_PAUSE_SONG = 9311;
    public static final int VALUE_PLAY_SONG = 9573;
    public static final int NUMBER_1 = 1;
    private static final int NOTIFI_ID = 1;
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer;
    private int mCurrentIndex;
    private OnSyncActivityListerner mListerner;
    private RemoteViews mNotificationLayout;
    private PendingIntent mPausePendingIntent;
    private PendingIntent mNextPendingIntent;
    private PendingIntent mPreviousPendingIntent;
    private PendingIntent mPlayPendingIntent;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        this.changeSong(MainActivity.NEXT_SONG);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int request = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);
            switch (request) {
                case VALUE_NEXT_SONG:
                    changeSong(MainActivity.NEXT_SONG);
                    updateNotification();
                    mListerner.syncNotification(true);
                    break;
                case VALUE_PREVIOUS_SONG:
                    changeSong(MainActivity.PREVIOUS_SONG);
                    mListerner.syncNotification(true);
                    updateNotification();
                    break;
                case VALUE_PAUSE_SONG:
                    this.pause();
                    mListerner.syncNotification(false);
                    updateNotification();
                    break;
                case VALUE_PLAY_SONG:
                    if (mMediaPlayer == null) {
                        this.create(0);
                        this.start();
                    } else {
                        this.start();
                    }
                    mListerner.syncNotification(true);
                    updateNotification();
                    break;
            }
        }
        initLayout4Notification(R.layout.notification_layout,
                MainActivity.SONG_NAMES[getCurrentIndex()], isPlaying());
        createNextPendingIntent();
        createPausePendingIntent();
        createPreviousPendingIntent();
        createPlayPendingIntent();
        createMusicNotification();
        return START_STICKY;
    }

    @Override
    public void create(int index) {
        mCurrentIndex = index;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        int resourceSong = MainActivity.SONG_RESOURCE[mCurrentIndex];
        mMediaPlayer = MediaPlayer.create(MyService.this, resourceSong);
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(MyService.this);
        }
    }

    @Override
    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mListerner.syncSeekbar(mMediaPlayer.getDuration());
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public int getCurrrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null ? mMediaPlayer.isPlaying() : false;
    }

    @Override
    public void seek(int possition) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(possition);
        }
    }

    @Override
    public void loop(boolean isLoop) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(isLoop);
        }
    }

    @Override
    public int getSong() {
        return mMediaPlayer != null ? mCurrentIndex : 0;
    }

    @Override
    public void stopService() {
        stopSelf();
    }

    @Override
    public void changeSong(int i) {
        mCurrentIndex += i;
        if (mCurrentIndex >= MainActivity.SONG_NAMES.length) {
            mCurrentIndex = 0;
        } else if (mCurrentIndex < 0) {
            mCurrentIndex = MainActivity.SONG_NAMES.length - NUMBER_1;
        }
        this.create(mCurrentIndex);
        this.start();
    }

    public void setSynSeekbarListerner(OnSyncActivityListerner listerner) {
        mListerner = listerner;
    }

    public void createMusicNotification() {
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_album_black_24dp)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContent(mNotificationLayout);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPenddingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPenddingIntent);
        startForeground(NOTIFI_ID, mBuilder.build());
    }

    private void createNextPendingIntent() {
        Intent nextIntent = new Intent(getApplicationContext(), MyService.class);
        nextIntent.putExtra(EXTRA_REQUEST_CODE, VALUE_NEXT_SONG);
        mNextPendingIntent = PendingIntent.getService(getApplicationContext(),
                VALUE_NEXT_SONG, nextIntent, 0);
        mNotificationLayout.setOnClickPendingIntent(R.id.image_next, mNextPendingIntent);
    }

    private void createPausePendingIntent() {
        Intent nextIntent = new Intent(getApplicationContext(), MyService.class);
        nextIntent.putExtra(EXTRA_REQUEST_CODE, VALUE_PAUSE_SONG);
        mPausePendingIntent = PendingIntent.getService(getApplicationContext(),
                VALUE_PAUSE_SONG, nextIntent, 0);
        mNotificationLayout.setOnClickPendingIntent(R.id.image_pause, mPausePendingIntent);
    }

    private void createPreviousPendingIntent() {
        Intent nextIntent = new Intent(getApplicationContext(), MyService.class);
        nextIntent.putExtra(EXTRA_REQUEST_CODE, VALUE_PREVIOUS_SONG);
        mPreviousPendingIntent = PendingIntent.getService(getApplicationContext(),
                VALUE_PREVIOUS_SONG, nextIntent, 0);
        mNotificationLayout.setOnClickPendingIntent(R.id.image_previous, mPreviousPendingIntent);
    }

    private void createPlayPendingIntent() {
        Intent nextIntent = new Intent(getApplicationContext(), MyService.class);
        nextIntent.putExtra(EXTRA_REQUEST_CODE, VALUE_PLAY_SONG);
        mPlayPendingIntent = PendingIntent.getService(getApplicationContext(),
                VALUE_PLAY_SONG, nextIntent, 0);
        mNotificationLayout.setOnClickPendingIntent(R.id.image_play, mPlayPendingIntent);
    }

    private void initLayout4Notification(int resourceLayout, String songName, boolean isPlaying) {
        mNotificationLayout = new RemoteViews(getPackageName(), resourceLayout);
        mNotificationLayout.setTextViewText(R.id.text_song_name, songName);
        if (isPlaying) {
            mNotificationLayout.setViewVisibility(R.id.image_play, View.GONE);
            mNotificationLayout.setViewVisibility(R.id.image_pause, View.VISIBLE);
        } else {
            mNotificationLayout.setViewVisibility(R.id.image_play, View.VISIBLE);
            mNotificationLayout.setViewVisibility(R.id.image_pause, View.GONE);
        }
    }

    public void updateNotification() {
        mNotificationLayout.setTextViewText(R.id.text_song_name,
                MainActivity.SONG_NAMES[mCurrentIndex]);
        if (isPlaying()) {
            mNotificationLayout.setViewVisibility(R.id.image_play, View.GONE);
            mNotificationLayout.setViewVisibility(R.id.image_pause, View.VISIBLE);
        } else {
            mNotificationLayout.setViewVisibility(R.id.image_play, View.VISIBLE);
            mNotificationLayout.setViewVisibility(R.id.image_pause, View.GONE);
        }
        mBuilder.setContent(mNotificationLayout);
        startForeground(NOTIFI_ID, mBuilder.build());
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    interface OnSyncActivityListerner {
        void syncSeekbar(int max);

        void syncNotification(boolean isPlaying);
    }
}
