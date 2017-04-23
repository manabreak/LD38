package me.manabreak.ld38;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.ArrayList;
import java.util.List;

public class FeatherSpawner {

    private final List<SpriteActor> feathers = new ArrayList<>();
    private final GameStage stage;
    private int index = 0;
    private Vector2 dir = new Vector2();

    public FeatherSpawner(GameStage stage) {
        this.stage = stage;

        String[] sprites = new String[]{
                "feather0", "feather1", "feather2"
        };

        for (int i = 0; i < 50; ++i) {
            SpriteActor s = new SpriteActor(Res.create(sprites[i % 3]));
            float w = s.sprite.getWidth();
            float h = s.sprite.getHeight();
            float ratio = w / h;

            s.setSize(ratio * Physics.INV_SCALE, 1f * Physics.INV_SCALE);
            s.setOriginCenter();
            feathers.add(s);
        }
    }

    public void spawn(float x, float y, Vector2 normal) {

        for (int i = 0; i < 5; ++i) {
            SpriteActor s = feathers.get(index++);
            index %= feathers.size();

            s.setColor(1f, 1f, 1f, 1f);
            s.setPosition(x - s.getWidth() / 2f, y - s.getHeight() / 2f);
            s.setRotation(MathUtils.random(0f, 360f));

            dir.setToRandomDirection().scl(MathUtils.random(0.4f, 1f));

            s.clearActions();
            s.addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.moveBy(dir.x, dir.y, 0.8f, Interpolation.circleOut),
                            Actions.alpha(0f, 0.8f, Interpolation.fade)
                    ),
                    Actions.removeActor()
            ));

            stage.addActor(s);
        }
    }
}
