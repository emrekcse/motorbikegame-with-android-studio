package com.example.motorbikegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SelectLevelActivity extends AppCompatActivity {

    private Button level1Button, level2Button, level3Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_level);

        level1Button = findViewById(R.id.level1Button);
        level2Button = findViewById(R.id.level2Button);
        level3Button = findViewById(R.id.level3Button);

        // Level 1
        level1Button.setOnClickListener(v -> {
            Intent intent = new Intent(SelectLevelActivity.this, MainActivity.class);
            intent.putExtra("level", 1);
            startActivity(intent);
        });

        // Level 2
        level2Button.setOnClickListener(v -> {
            Intent intent = new Intent(SelectLevelActivity.this, MainActivity.class);
            intent.putExtra("level", 2);
            startActivity(intent);
        });

        // Level 3
        level3Button.setOnClickListener(v -> {
            Intent intent = new Intent(SelectLevelActivity.this, MainActivity.class);
            intent.putExtra("level", 3);
            startActivity(intent);
        });
    }
}
