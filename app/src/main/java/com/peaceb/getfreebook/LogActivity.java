package com.peaceb.getfreebook;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class LogActivity extends Activity {
    private TextView _textLog;
    private ProgressBar _progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        _textLog = (TextView)findViewById(R.id.textLog);
        _progress = (ProgressBar)findViewById(R.id.progress);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        loadLog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_log:
                clearLog();
                return true;

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearLog() {
        if (FileLog.delete(this)) {
            Toast.makeText(this, R.string.log_deleted, Toast.LENGTH_SHORT).show();
        }

        _textLog.setText("");
    }

    private void loadLog() {
        LoadTask task = new LoadTask();
        task.execute((Void)null);
    }

    class LoadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            _progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            return FileLog.getLogContents(LogActivity.this);
        }

        @Override
        protected void onPostExecute(String s) {
            _progress.setVisibility(View.GONE);
            _textLog.setText(s);
        }
    }
}
