package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;


public class GameScreen implements Screen {

    final MyGdxGame game;

    Texture dropImage;
    Texture bucketImage;
    Texture yellowDrop;
    Texture soundOn;
    Texture soundOff;
    Sound dropSound;
    Sound gagging;
    Music rainMusic;
    OrthographicCamera camera;
    Rectangle bucket;
    Array<Rectangle> raindrops;
    Array<Rectangle> yellowDrops;
    long lastDropTime;
    int dropsGathered;
    int count = 0;
    int random;
    boolean playSound;
    float inputTime = 0;

    public GameScreen(MyGdxGame game) {
        this.game = game;

        playSound = true;

        dropImage = new Texture(Gdx.files.internal("drop.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));
        yellowDrop = new Texture(Gdx.files.internal("yellowDrop.png"));
        soundOn = new Texture(Gdx.files.internal("soundOn.png"));
        soundOff = new Texture(Gdx.files.internal("soundOff.png"));

        dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
        gagging = Gdx.audio.newSound(Gdx.files.internal("gagging.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        rainMusic.setLooping(true);


        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;

        bucket.width = 64;
        bucket.height = 64;

        raindrops = new Array<>();
        yellowDrops = new Array<>();
        spawnRaindrop();
    }

    private void spawnRaindrop() {
        spawnDrop(raindrops);
    }

    private void spawnYellowDrop() {
        spawnDrop(yellowDrops);
    }

    private void spawnDrop(Array<Rectangle> drops) {
        Rectangle drop = new Rectangle();
        drop.x = MathUtils.random(0, 800 - 64);
        drop.y = 480;
        drop.height = 64;
        drop.width = 64;
        drops.add(drop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.font.draw(game.batch, "Drops collected: " + dropsGathered, 0, 480);
        game.batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        if (playSound) {
            game.batch.draw(soundOn, 750, 440, 30, 30);
        } else {
            game.batch.draw(soundOff, 750, 440, 30, 30);
        }
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        for (Rectangle yellow : yellowDrops) {
            game.batch.draw(yellowDrop, yellow.x, yellow.y);
        }
        game.batch.end();

        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucket.x += 200 * Gdx.graphics.getDeltaTime();
        }

        inputTime += delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            if (inputTime > 0.2f && inputTime - delta > 0.1) {
                playSound = !playSound;
                inputTime = 0;
            }
        }

        if (Gdx.input.isTouched()) {
            Vector3 vector3 = new Vector3();
            if ((vector3.x > 750 && vector3.x < 780)
                    && (vector3.y > 440 && vector3.y < 480)) {
                if (inputTime > 0.2f && inputTime - delta > 0.1) {
                    playSound = !playSound;
                    inputTime = 0;
                }
            }
        }

        if (bucket.x < 0)
            bucket.x = 0;

        if (bucket.x > 800 - 64)
            bucket.x = 800 - 64;

        if (TimeUtils.nanoTime() - lastDropTime > 1000_000_000) {
            if (count == random) {
                spawnYellowDrop();
                count = 0;
                random = MathUtils.random(1, 10);
            } else {
                spawnRaindrop();
            }
            count++;
        }

        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle drop = iter.next();
            drop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (drop.y + 64 < 0)
                iter.remove();
            if (drop.overlaps(bucket)) {
                dropsGathered++;
                iter.remove();
                if (playSound) {
                    dropSound.play();
                }
            }
        }

        Iterator<Rectangle> yellowIter = yellowDrops.iterator();
        while (yellowIter.hasNext()) {
            Rectangle yellowDrop = yellowIter.next();
            yellowDrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (yellowDrop.y + 64 < 0)
                yellowIter.remove();
            if (yellowDrop.overlaps(bucket)) {
                dropsGathered -= 5;
                yellowIter.remove();
                if (playSound) {
                    gagging.play();
                }
            }
        }
    }


    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void show() {
        if (playSound) {
            rainMusic.play();
        }
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        yellowDrop.dispose();
        rainMusic.dispose();
        dropSound.dispose();
        gagging.dispose();
        soundOn.dispose();
        soundOff.dispose();
    }
}
