package hashems.project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OAuth flow
        Button sign_in = (Button) findViewById(R.id.button_sign_in);
        sign_in.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this, APIActivity.class);
                    startActivity(intent);
                } catch (RuntimeException ex) {
                    Log.e("EXCEPT", ex.toString());
                }
            }
        });
    }
}
