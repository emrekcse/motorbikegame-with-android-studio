package com.example.motorbikegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private Button playButton, selectLevelButton, aboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        playButton = findViewById(R.id.playButton);
        selectLevelButton = findViewById(R.id.selectLevelButton);
        aboutButton = findViewById(R.id.aboutButton);


        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.putExtra("level", 1);
            startActivity(intent);
        });


        selectLevelButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, SelectLevelActivity.class);
            startActivity(intent);
        });


        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }
}
