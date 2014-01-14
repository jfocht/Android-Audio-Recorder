package com.jfocht.AudioRecorderApp;

import android.app.Activity;
import android.widget.RelativeLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.SimpleCursorAdapter;
import android.database.Cursor;

import java.io.IOException;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class RecordAudioActivity extends Activity
{
    private static final long NANOS_PER_SECOND = 1000000000;
    private static final String LOG_TAG = "RecordAudio";
    private static String fileName = null;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    private long startTime = 0L;
    private AudioClipDatabase database = null;

    private SimpleCursorAdapter audioClipAdapter = null;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startPlaying(String fileName) {
        if (player != null) {
            stopPlaying();
        }
        player = new MediaPlayer();
        // TODO start playing an audio clip and use the filename for that clip
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        Log.i(LOG_TAG, "startRecording()");
        startTime = System.nanoTime();
        fileName = randomFileName();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed", e);
        }

        recorder.start();
    }



    private static long roundUpDivision(long dividend, long divisor) {
        return (dividend + divisor - 1) / divisor;
    }

    private void stopRecording() {
        Log.d(LOG_TAG, "stopRecording()");
        long durationNanos = System.nanoTime() - startTime;
        int duration = (int)roundUpDivision(durationNanos, NANOS_PER_SECOND);
        AudioClip clip = new AudioClip("Untitled Clip", fileName, duration);
        database.addAudioClip(clip);
        recorder.stop();
        recorder.release();
        recorder = null;
        audioClipAdapter.swapCursor(this.database.getAudioClipCursor());
        Log.d(LOG_TAG, "stopRecording()");
    }

    private boolean mStartRecording = true;

    OnClickListener recordClicker = new OnClickListener() {
        public void onClick(View v) {
            onRecord(mStartRecording);
            if (v == null) return;
            if (v instanceof Button) {
                Button b = (Button)v;
                if (mStartRecording) {
                    b.setText("Stop recording");
                } else {
                    b.setText("Start recording");
                }
            }
            mStartRecording = !mStartRecording;
        }
    };

    private String randomFileName() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/");
        sb.append(UUID.randomUUID().toString());
        sb.append(".3gp");
        return sb.toString();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        Button button = (Button)findViewById(R.id.record);
        if (button != null) {
            button.setOnClickListener(recordClicker);
        }

        database = new AudioClipDatabase(this);
        installListAdapter();
    }

    private static String secondsToTimeString(int totalSeconds) {
        return secondsToTimeString((long)totalSeconds);
    }

    private static String secondsToTimeString(long totalSeconds) {
        long hours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds)
            - TimeUnit.HOURS.toMinutes(hours);
        long seconds = totalSeconds
            - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(totalSeconds));
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private static SimpleCursorAdapter.ViewBinder audioListViewBinder =
        new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.duration) {
                    int duration = cursor.getInt(3);
                    if (view instanceof TextView) {
                        String timeString = secondsToTimeString(duration);
                        ((TextView)view).setText(timeString);
                        return true;
                    }
                }
                return false;
            }
        };

    private final AdapterView.OnItemClickListener playClickListener =
        new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                String fileName = database.getAudioClipFileName((int)id);
                if (fileName != null) {
                    startPlaying(fileName);
                }
            }
        };

    private final AdapterView.OnItemLongClickListener deleteClickListener =
        new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                        int position, long id)
            {
                String fileName = database.getAudioClipFileName((int)id);
                database.deleteAudioClipById((int)id);
                audioClipAdapter.swapCursor(database.getAudioClipCursor());
                if (fileName != null) {
                    try {
                        new File(fileName).delete();
                    } catch (Throwable e) {
                        android.util.Log.e(LOG_TAG, "Unable to delete file", e);
                    }
                }
                return true;
            }
        };

    public void installListAdapter() {
        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {AudioClipDatabase.KEY_NAME,
            AudioClipDatabase.KEY_CREATED, AudioClipDatabase.KEY_DURATION};
        int[] toViews = {R.id.name, R.id.date, R.id.duration};

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        audioClipAdapter = new SimpleCursorAdapter(this, R.layout.clip, null,
                fromColumns, toViews, 0);
        audioClipAdapter.setViewBinder(audioListViewBinder);
        audioClipAdapter.swapCursor(this.database.getAudioClipCursor());

        ListView listView = (ListView)findViewById(R.id.clips);
        listView.setAdapter(audioClipAdapter);
        listView.setOnItemClickListener(playClickListener);
        listView.setOnItemLongClickListener(deleteClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }

    }
}
