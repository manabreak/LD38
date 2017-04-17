package me.manabreak.ld38;

import com.badlogic.gdx.Game;

public class MyGdxGame extends Game {

    private GameScreen gameScreen;

    @Override
    public void create() {
        Res.load();
        gameScreen = new GameScreen();
        setScreen(gameScreen);
    }
}
