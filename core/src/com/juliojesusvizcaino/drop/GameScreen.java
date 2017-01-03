package com.juliojesusvizcaino.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
	final Drop game;

	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;

	private OrthographicCamera camera;

    private Vector3 touchPos = new Vector3();
    private Array<Rectangle> raindrops;
    private long lastDropTime;
    private int dropsGathered;
    private TextureAtlas atlas;
    private Sprite dropSprite, bucketSprite;

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800-64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

	public GameScreen(final Drop gam) {
    	this.game = gam;
    	atlas = new TextureAtlas(Gdx.files.internal("pack/game.atlas"));
		dropImage = new Texture(Gdx.files.internal("images/droplet.png"));
		dropSprite = atlas.createSprite("droplet");
		bucketSprite = atlas.createSprite("bucket");

		bucketSprite.setPosition(800/2 - 64/2, 20);

		dropSound = Gdx.audio.newSound(Gdx.files.internal("audios/drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("audios/rain.mp3"));
		rainMusic.setLooping(true);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	@Override
	public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();
		game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 480);
		bucketSprite.draw(game.batch);
		for(Rectangle raindrop: raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
		game.batch.end();

		if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucketSprite.setX(touchPos.x - 64/2);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
            bucketSprite.setX(bucketSprite.getX() - 200 * Gdx.graphics.getDeltaTime());
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            bucketSprite.setX(bucketSprite.getX() + 200 * Gdx.graphics.getDeltaTime());

		if(bucketSprite.getX() < 0) bucketSprite.setX(0);
		if(bucketSprite.getX() > 800-64) bucketSprite.setX(-64);

		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

        Iterator<Rectangle> iter = raindrops.iterator();
        while(iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + 64 < 0) iter.remove();
            if(raindrop.overlaps(bucketSprite.getBoundingRectangle())) {
                dropSound.play();
                iter.remove();
                dropsGathered++;
            }
        }
	}
	
	@Override
	public void dispose () {
        dropImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
	}

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }
}
