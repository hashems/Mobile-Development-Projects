package hashems.mobile_development_projects;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // LinearLayout Vertical
        TextView listViewV = (TextView) findViewById(R.id.Layout1);
        listViewV.setOnClickListener(new View.OnClickListener(){

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
        TextView gridView = (TextView) findViewById(R.id.Layout3);
        gridView.setOnClickListener(new View.OnClickListener(){

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
