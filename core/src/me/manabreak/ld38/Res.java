package me.manabreak.ld38;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Res {

    public static TextureAtlas atlas;
    public static Sound door, fall, jump, key, land, rock, invert;

    public static Sprite create(String name) {
        return atlas.createSprite(name);
    }

    public static TextureRegion findRegion(String name) {
        return atlas.findRegion(name);
    }

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("graphics/game.atlas"));

        door = Gdx.audio.newSound(Gdx.files.internal("sfx/door.ogg"));
        fall = Gdx.audio.newSound(Gdx.files.internal("sfx/fall.ogg"));
        jump = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.ogg"));
        key = Gdx.audio.newSound(Gdx.files.internal("sfx/key.ogg"));
        land = Gdx.audio.newSound(Gdx.files.internal("sfx/land.ogg"));
        rock = Gdx.audio.newSound(Gdx.files.internal("sfx/rock.ogg"));
        invert = Gdx.audio.newSound(Gdx.files.internal("sfx/invert.ogg"));

        Music intro = Gdx.audio.newMusic(Gdx.files.internal("music/intro.ogg"));
        intro.setVolume(0.8f);
        final Music loop = Gdx.audio.newMusic(Gdx.files.internal("music/loop.ogg"));
        loop.setLooping(true);
        loop.setVolume(0.8f);

        intro.play();
        intro.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                loop.play();
                ;
            }
        });
    }
}
