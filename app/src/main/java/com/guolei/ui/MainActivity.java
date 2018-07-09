package com.guolei.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.guolei.boardview.BoardView;
import com.guolei.boardview.R;

public class MainActivity extends AppCompatActivity {


    BoardView mBoardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBoardView = findViewById(R.id.boardview);
        mBoardView.setListener(new KanbanBoardViewListener());
        mBoardView.setAdapter(new BoardViewAdapter(mBoardView.getBoardViewHolder()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.less) {
            mBoardView.scale();
            return true;
        } else if (item.getItemId() == R.id.restore) {
            mBoardView.scale();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
