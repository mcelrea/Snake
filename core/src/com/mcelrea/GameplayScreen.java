package com.mcelrea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Tech on 11/30/2015.
 */
public class GameplayScreen implements Screen {

    //allows us to draw 2D graphics onto the screen
    SpriteBatch batch;

    private static final int GAMEOVER = 1, PLAYING = 2;
    int gameState = PLAYING;
    int score = 0;
    BitmapFont font;

    Texture snakeHead;
    Texture apple;
    Texture body;
    Texture back;

    ArrayList<BodyPart> bodyParts = new ArrayList<BodyPart>();

    private final int MOVESIZE = 32;
    private int snakeX = 0;
    private int snakeY = 0;
    private int oldSnakeX = 0;
    private int oldSnakeY = 0;

    private int appleX = 0;
    private int appleY = 0;
    private boolean appleReady = false;

    private static final int RIGHT=1, LEFT=2, UP=3, DOWN=4;
    private int dir = RIGHT;

    private float gameSpeed = 0.2f;
    private float timer = gameSpeed;

    //runs one time, when this screen is first loaded
    @Override
    public void show() {

        //initialize the SpriteBatch
        batch = new SpriteBatch();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        //load the snakeHead.png image
        snakeHead = new Texture(Gdx.files.internal("snakeHead.png"));
        //load the apple.png image
        apple = new Texture(Gdx.files.internal("apple.png"));
        //load the snakeBody.png image
        body = new Texture(Gdx.files.internal("snakeBody.png"));
        //load the back.png image
        back = new Texture(Gdx.files.internal("back.png"));
    }

    //move the snake
    public void updateSnake() {

        oldSnakeX = snakeX;
        oldSnakeY = snakeY;

        if(dir == RIGHT) {
            snakeX += MOVESIZE;
            if(snakeX >= Gdx.graphics.getWidth()) {
                snakeX = 0;
            }
        }
        else if(dir == LEFT) {
            snakeX -= MOVESIZE;
            if(snakeX < 0) {
                snakeX = Gdx.graphics.getWidth() - MOVESIZE;
            }
        }
        else if(dir == UP) {
            snakeY += MOVESIZE;
            if(snakeY >= Gdx.graphics.getHeight()) {
                snakeY = 0;
            }
        }
        else if(dir == DOWN) {
            snakeY -= MOVESIZE;
            if(snakeY < 0) {
                snakeY = Gdx.graphics.getHeight() - MOVESIZE;
            }
        }

        if(snakeX == appleX && snakeY == appleY) {
            appleReady = false;
            BodyPart b = new BodyPart(body);
            b.x = snakeX;
            b.y = snakeY;
            bodyParts.add(0,b);
            score++;
            if(bodyParts.size() % 30 == 0) {
                gameSpeed -= 0.05f;
            }
        }

        checkForBodyCollision();
    }

    private void checkForBodyCollision() {

        for(int k=1; k < bodyParts.size(); k++) {
            BodyPart b = bodyParts.get(k);

            if(b.x == snakeX && b.y == snakeY) {
                gameState = GAMEOVER;
            }
        }
    }


    @Override
    public void render(float delta) {
        //set the color to clear the screen to
        Gdx.gl.glClearColor(0, 0, 0, 1);
        //clear the screen with the above color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        getUserInput();
        if(gameState == PLAYING) {
            updateApple();

            timer -= delta;
            if (timer <= 0) {
                updateSnake();
                updateBody();
                timer = gameSpeed;
            }
        }

        batch.begin();
        batch.draw(back, 0, 0);
        batch.draw(apple, appleX, appleY);
        drawBody(batch);
        batch.draw(snakeHead, snakeX, snakeY);
        font.draw(batch, "Score: " + score, 380, 580);
        if(gameState == GAMEOVER) {
            font.draw(batch, "GAME OVER", 380, 350);
            font.draw(batch, "PRESS SPACEBAR TO PLAY AGAIN", 315, 330);
        }
        batch.end();
    }

    private void updateBody() {

        //make sure there is a body
        if(bodyParts.size() > 0) {
            //remove the last bodyPart
            BodyPart b = bodyParts.remove(bodyParts.size()-1);
            b.x = oldSnakeX;
            b.y = oldSnakeY;
            bodyParts.add(0,b);
        }
    }

    private void drawBody(SpriteBatch batch) {
        for(int k=0; k < bodyParts.size(); k++) {
            BodyPart b = bodyParts.get(k);
            batch.draw(b.texture,b.x,b.y);
        }
    }

    private void updateApple() {

        Random rand = new Random();

        while(!appleReady) {
            appleX = rand.nextInt(Gdx.graphics.getWidth()/MOVESIZE)*MOVESIZE;
            appleY = rand.nextInt(Gdx.graphics.getHeight()/MOVESIZE)*MOVESIZE;
            if(appleY != snakeY || appleX != snakeX) {
                boolean onBody = false;
                for(int k=0; k < bodyParts.size(); k++) {
                    BodyPart b = bodyParts.get(k);
                    if(b.x == appleX && b.y == appleY) {
                        onBody = true;
                    }
                }
                if(!onBody) {
                    appleReady = true;
                }
            }
        }
    }

    private void getUserInput() {

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && dir != LEFT) {
            dir = RIGHT;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && dir != RIGHT) {
            dir = LEFT;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && dir != DOWN) {
            dir = UP;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && dir != UP) {
            dir = DOWN;
        }
        if(gameState == GAMEOVER && Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            score = 0;
            gameState = PLAYING;
            bodyParts = new ArrayList<BodyPart>();
            snakeX = 0;
            snakeY = 0;
            gameSpeed = 0.2f;
            dir = RIGHT;
            appleReady = false;
        }
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

    @Override
    public void dispose() {

    }
}
