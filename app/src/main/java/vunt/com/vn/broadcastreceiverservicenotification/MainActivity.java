package vunt.com.vn.broadcastreceiverservicenotification;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        MyAdapter.OnClickItemSongListener, MyService.OnSyncActivityListerner {
    public static final String[] SONG_NAMES = {"Đừng như thói quen", "Tận cùng nỗi nhớ"};
    public static final int[] SONG_RESOURCE = {R.raw.bai1, R.raw.bai2};
    public static final int MESSAGE_DELAY = 1000;
    public static final int NEXT_SONG = 1;
    public static final int PREVIOUS_SONG = -1;
    private MyService mService;
    private ImageView mPauseImage;
    private ImageView mNextImage;
    private ImageView mPreviousImage;
    private ImageView mPlayImage;
    private TextView mCurrentSongTime;
    private TextView mDurationSongTime;
    private SeekBar mSeekBar;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentPosition = mService.getCurrrentPosition();
            mSeekBar.setProgress(currentPosition);
            mCurrentSongTime.setText(TimeUtil.convertMilisecondToFormatTime(currentPosition));
            mHandler.sendMessageDelayed(new Message(), MESSAGE_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        Intent intent = new Intent(this, MyService.class);
        if (mService == null) {
            startService(intent);
        }
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void initViews() {
        RecyclerView recyclerView = findViewById(R.id.recycler_song);
        recyclerView.setHasFixedSize(true);
        final MyAdapter myAdapter = new MyAdapter(this);
        recyclerView.setAdapter(myAdapter);
        mPauseImage = findViewById(R.id.image_pause);
        mNextImage = findViewById(R.id.image_next);
        mPreviousImage = findViewById(R.id.image_previous);
        mPlayImage = findViewById(R.id.image_play);
        mPauseImage.setOnClickListener(this);
        mNextImage.setOnClickListener(this);
        mPreviousImage.setOnClickListener(this);
        mPlayImage.setOnClickListener(this);
        mPauseImage.setVisibility(View.GONE);
        mCurrentSongTime = findViewById(R.id.text_currrent_song_time);
        mDurationSongTime = findViewById(R.id.text_duration_song_time);
        mSeekBar = findViewById(R.id.seek_bar_song);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser) {
                    mService.seek(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.LocalBinder binder = (MyService.LocalBinder) iBinder;
            mService = binder.getService();
            mService.setSynSeekbarListerner(MainActivity.this);
            syncSeekbar(mService.getDuration());
            syncNotification(mService.isPlaying());
            mHandler.sendMessageDelayed(new Message(), MESSAGE_DELAY);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            unbindService(mConnection);
        }
    };

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.image_pause:
                mService.pause();
                mService.updateNotification();
                mPlayImage.setVisibility(View.VISIBLE);
                mPauseImage.setVisibility(View.GONE);
                break;
            case R.id.image_next:
                mService.changeSong(NEXT_SONG);
                mService.updateNotification();
                mPlayImage.setVisibility(View.GONE);
                mPauseImage.setVisibility(View.VISIBLE);
                break;
            case R.id.image_previous:
                mService.changeSong(PREVIOUS_SONG);
                mService.updateNotification();
                mPlayImage.setVisibility(View.GONE);
                mPauseImage.setVisibility(View.VISIBLE);
                break;
            case R.id.image_play:
                if (mService.getMediaPlayer() == null) {
                    mService.create(0);
                    mService.start();
                } else {
                    mService.start();
                }
                mService.updateNotification();
                mPlayImage.setVisibility(View.GONE);
                mPauseImage.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void clickItemSongListener(int position) {
        mService.create(position);
        mService.start();
        mService.updateNotification();
        mPauseImage.setVisibility(View.VISIBLE);
        mPlayImage.setVisibility(View.GONE);
    }

    @Override
    public void syncSeekbar(int max) {
        mSeekBar.setMax(max);
        mDurationSongTime.setText(TimeUtil.convertMilisecondToFormatTime(max));
    }

    @Override
    public void syncNotification(boolean isPlaying) {
        if (isPlaying) {
            mPauseImage.setVisibility(View.VISIBLE);
            mPlayImage.setVisibility(View.GONE);
        } else {
            mPauseImage.setVisibility(View.GONE);
            mPlayImage.setVisibility(View.VISIBLE);
        }
    }
}
