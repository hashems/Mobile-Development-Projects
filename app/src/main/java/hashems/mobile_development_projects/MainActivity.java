package hashems.mobile_development_projects;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

// CITE: CS 496 Week 6 Content (various)
// CITE: https://developer.android.com/index.html (various)


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // LinearLayout Vertical
        TextView listViewItem = (TextView) findViewById(R.id.Layout1);
        listViewItem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListViewVActivity.class);
                startActivity(intent);
            }
        });

        // LinearLayout Horizontal
        TextView listViewH = (TextView) findViewById(R.id.Layout2);
        listViewH.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListViewHActivity.class);
                startActivity(intent);
            }
        });

        // GridLayout
        TextView gridViewItem = (TextView) findViewById(R.id.Layout3);
        gridViewItem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GridViewActivity.class);
                startActivity(intent);
            }
        });

        // RelativeLayout
        TextView relativeView = (TextView) findViewById(R.id.Layout4);
        relativeView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RelativeViewActivity.class);
                startActivity(intent);
            }
        });
    }
}
