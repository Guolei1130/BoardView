package com.guolei.boardview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {


    BoardView mBoardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBoardView = findViewById(R.id.boardview);
        mBoardView.setCallback(new BoardViewCallback());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.less) {
            mBoardView.scale(true);
            return true;
        } else if (item.getItemId() == R.id.restore) {
            mBoardView.scale(false);
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
