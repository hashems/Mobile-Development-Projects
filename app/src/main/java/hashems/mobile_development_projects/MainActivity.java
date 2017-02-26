package hashems.mobile_development_projects;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.R.id.edit;
import static android.R.id.input;

public class MainActivity extends AppCompatActivity {

//    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Location
//        TextView listViewItem = (TextView) findViewById(R.id.MainLayout);
//        listViewItem.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
//                startActivity(intent);
//            }
//        });
//    }

        // Input and Location
////        input = (EditText) findViewById(R.id.Input);
//
        Button button = (Button) findViewById(R.id.Start);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });
    }
}
