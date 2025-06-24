package com.example.motorbikegame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView playerBike, roadBackground1, roadBackground2;
    private TextView scoreText, speedText;
    private Button restartButton;

    private int score = 0;
    private int baseRoadSpeed = 15;
    private int dynamicRoadSpeed = baseRoadSpeed;
    private int maxSpeed = 180;
    private boolean gameRunning = true;

    private final List<ImageView> enemyBikes = new ArrayList<>();
    private final List<ImageView> starItems = new ArrayList<>();
    private final List<Integer> enemySpeeds = new ArrayList<>();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float sensorX = 0;

    private final Handler handler = new Handler();
    private Runnable speedUpRunnable;
    private Runnable speedDownRunnable;

    private int currentLevel = 1;

    private float leftSide = 200f;
    private float rightSide = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLevel = getIntent().getIntExtra("level", 1);

        playerBike = findViewById(R.id.playerBike);
        roadBackground1 = findViewById(R.id.roadBackground1);
        roadBackground2 = findViewById(R.id.roadBackground2);
        scoreText = findViewById(R.id.scoreText);
        speedText = findViewById(R.id.speedText);
        restartButton = findViewById(R.id.restartButton);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        setupLevelConfigs(currentLevel);

        startNewGame();

        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupLevelConfigs(int level) {
        switch (level) {
            case 1:
                baseRoadSpeed = 15;
                maxSpeed = 180;
                roadBackground1.setImageResource(R.drawable.road1);
                roadBackground2.setImageResource(R.drawable.road1);
                playerBike.setImageResource(R.drawable.redbike);
                break;

            case 2:
                baseRoadSpeed = 20;
                maxSpeed = 200;
                roadBackground1.setImageResource(R.drawable.road2);
                roadBackground2.setImageResource(R.drawable.road2);
                playerBike.setImageResource(R.drawable.redbike2);
                break;

            case 3:
                baseRoadSpeed = 25;
                maxSpeed = 220;
                roadBackground1.setImageResource(R.drawable.road3);
                roadBackground2.setImageResource(R.drawable.road3);
                playerBike.setImageResource(R.drawable.redbike3);
                break;
        }
        dynamicRoadSpeed = baseRoadSpeed;
    }

    private void startNewGame() {
        score = 0;
        gameRunning = true;

        scoreText.setText("Score: " + score);
        speedText.setText("Speed: " + dynamicRoadSpeed + " km/h");
        restartButton.setVisibility(Button.GONE);

        RelativeLayout rootLayout = findViewById(R.id.rootLayout);
        for (ImageView e : enemyBikes) rootLayout.removeView(e);
        enemyBikes.clear();
        enemySpeeds.clear();

        for (ImageView s : starItems) rootLayout.removeView(s);
        starItems.clear();

        roadBackground1.setY(0f);
        roadBackground2.setY(-roadBackground1.getHeight());

        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        rightSide = screenWidth - 200f;

        int numberOfEnemies = (currentLevel == 2) ? 5 : (currentLevel == 3) ? 7 : 3;
        int numberOfStars = 3;

        Random random = new Random();

        int[] enemyDrawables;
        int starDrawable;

        if (currentLevel == 1) {
            enemyDrawables = new int[]{R.drawable.bluebike, R.drawable.redcar};
            starDrawable = R.drawable.star;
        } else if (currentLevel == 2) {
            enemyDrawables = new int[]{R.drawable.bluebike2, R.drawable.redcar2};
            starDrawable = R.drawable.star2;
        } else {
            enemyDrawables = new int[]{R.drawable.bluebike3, R.drawable.redcar3};
            starDrawable = R.drawable.star3;
        }

        for (int i = 0; i < numberOfEnemies; i++) {
            ImageView enemyBike = new ImageView(this);
            int randomDrawable = enemyDrawables[random.nextInt(enemyDrawables.length)];
            enemyBike.setImageResource(randomDrawable);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(140, 200);
            params.topMargin = -300;
            enemyBike.setLayoutParams(params);

            float randomX = leftSide + random.nextInt((int) (rightSide - leftSide - 140));
            enemyBike.setX(randomX);
            enemyBike.setY(-300f);

            rootLayout.addView(enemyBike);
            enemyBikes.add(enemyBike);
            enemySpeeds.add(10 + i * 3 + currentLevel * 2);
        }

        for (int i = 0; i < numberOfStars; i++) {
            ImageView star = new ImageView(this);
            star.setImageResource(starDrawable);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(80, 80);
            params.topMargin = -300;
            star.setLayoutParams(params);

            float randomX = leftSide + random.nextInt((int) (rightSide - leftSide - 80));
            star.setX(randomX);
            star.setY(-300f);

            rootLayout.addView(star);
            starItems.add(star);
        }

        playerBike.post(() -> {
            float screenHeight = getResources().getDisplayMetrics().heightPixels;
            float startX = (screenWidth / 2f) - (playerBike.getWidth() / 2f);
            float startY = screenHeight - playerBike.getHeight() - 200f;

            if (startX < leftSide) startX = leftSide;
            if (startX + playerBike.getWidth() > rightSide) {
                startX = rightSide - playerBike.getWidth();
            }

            playerBike.setX(startX);
            playerBike.setY(startY);
        });

        setupTouchControls();
        setupScrollingBackground();
        moveEnemyBikesAndStars();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchControls() {
        RelativeLayout rootLayout = findViewById(R.id.rootLayout);

        speedUpRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning && dynamicRoadSpeed < maxSpeed) {
                    dynamicRoadSpeed += 2;
                    updateSpeedText();
                    updateEnemySpeeds();
                    handler.postDelayed(this, 100);
                }
            }
        };

        speedDownRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning && dynamicRoadSpeed > baseRoadSpeed) {
                    dynamicRoadSpeed -= 2;
                    updateSpeedText();
                    updateEnemySpeeds();
                    handler.postDelayed(this, 100);
                }
            }
        };

        rootLayout.setOnTouchListener((v, event) -> {
            if (!gameRunning) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(speedDownRunnable);
                    handler.post(speedUpRunnable);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(speedUpRunnable);
                    handler.post(speedDownRunnable);
                    break;
            }
            return true;
        });
    }

    private void updateSpeedText() {
        String speed = "Speed: " + dynamicRoadSpeed + " km/h";
        speedText.setText(speed);
    }

    private void updateEnemySpeeds() {
        for (int i = 0; i < enemySpeeds.size(); i++) {
            enemySpeeds.set(i, (dynamicRoadSpeed - baseRoadSpeed) / 2
                    + 10 + i * 3 + currentLevel * 2);
        }
    }

    private void moveEnemyBikesAndStars() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!gameRunning) return;

                Random random = new Random();
                float screenHeight = getResources().getDisplayMetrics().heightPixels;

                for (int i = 0; i < enemyBikes.size(); i++) {
                    ImageView enemyBike = enemyBikes.get(i);
                    float newY = enemyBike.getY() + enemySpeeds.get(i);

                    if (newY > screenHeight) {
                        enemyBike.setY(-300f);
                        float randomX = leftSide + random.nextInt((int) (rightSide - leftSide - 140));
                        enemyBike.setX(randomX);

                        score++;
                        scoreText.setText("Score: " + score);
                    } else {
                        enemyBike.setY(newY);
                    }
                }

                for (ImageView star : starItems) {
                    float newY = star.getY() + dynamicRoadSpeed;
                    if (newY > screenHeight) {
                        star.setY(-300f);
                        float randomX = leftSide + random.nextInt((int) (rightSide - leftSide - 80));
                        star.setX(randomX);
                    } else {
                        star.setY(newY);
                    }
                }

                checkCollisions();
                handler.postDelayed(this, 30);
            }
        });
    }

    private void checkCollisions() {
        if (!gameRunning) return;

        Rect playerRect = new Rect(
                (int) (playerBike.getX() + 20),
                (int) (playerBike.getY() + 20),
                (int) (playerBike.getX() + playerBike.getWidth() - 20),
                (int) (playerBike.getY() + playerBike.getHeight() - 20)
        );

        for (ImageView enemyBike : enemyBikes) {
            Rect enemyRect = new Rect(
                    (int) (enemyBike.getX() + 20),
                    (int) (enemyBike.getY() + 20),
                    (int) (enemyBike.getX() + enemyBike.getWidth() - 20),
                    (int) (enemyBike.getY() + enemyBike.getHeight() - 20)
            );

            if (Rect.intersects(playerRect, enemyRect)) {
                endGame();
                return;
            }
        }

        for (ImageView star : starItems) {
            Rect starRect = new Rect(
                    (int) (star.getX() + 10),
                    (int) (star.getY() + 10),
                    (int) (star.getX() + star.getWidth() - 10),
                    (int) (star.getY() + star.getHeight() - 10)
            );

            if (Rect.intersects(playerRect, starRect)) {
                score += 5;
                scoreText.setText("Score: " + score);

                star.setY(-300f);
                float randomX = leftSide + new Random().nextInt((int) (rightSide - leftSide - 80));
                star.setX(randomX);
            }
        }
    }

    private void endGame() {
        gameRunning = false;
        handler.removeCallbacksAndMessages(null);
        scoreText.setText("OYUN BİTTİ Score: " + score);
        restartButton.setVisibility(Button.VISIBLE);
    }

    private void setupScrollingBackground() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!gameRunning) return;

                roadBackground1.setY(roadBackground1.getY() + dynamicRoadSpeed);
                roadBackground2.setY(roadBackground2.getY() + dynamicRoadSpeed);

                float screenHeight = getResources().getDisplayMetrics().heightPixels;
                if (roadBackground1.getY() >= screenHeight) {
                    roadBackground1.setY(roadBackground2.getY() - roadBackground1.getHeight());
                }
                if (roadBackground2.getY() >= screenHeight) {
                    roadBackground2.setY(roadBackground1.getY() - roadBackground2.getHeight());
                }

                handler.postDelayed(this, 30);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0];
            movePlayerBike(sensorX);
        }
    }

    private void movePlayerBike(float sensorX) {
        if (!gameRunning) return;

        float newX = playerBike.getX() - (sensorX * 10);

        if (newX < leftSide) newX = leftSide;
        if (newX + playerBike.getWidth() > rightSide) {
            newX = rightSide - playerBike.getWidth();
        }

        playerBike.setX(newX);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
