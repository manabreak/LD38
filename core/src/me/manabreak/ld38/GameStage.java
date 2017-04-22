package me.manabreak.ld38;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameStage extends Stage {

    private static final float CAM_LERP_SPEED = 1f;
    private final Player player;
    private Physics physics;

    private Level level;

    Sprite s;

    public GameStage() {
        super(new ExtendViewport(6f, 6f));
        physics = new Physics();
        initCamera();

        s = Res.create("chicken");

        getRoot().setColor(1f, 1f, 1f, 0f);

        player = new Player(this);
        level = new Level(this);

        level.load("0003");
    }

    private void initCamera() {
        Camera cam = getCamera();
        cam.position.x = 0f;
        cam.position.y = 0f;
    }

    @Override
    public void act(float dt) {
        super.act(dt);

        level.act(dt);

        Vector2 pos = player.getBodyPosition().cpy();
        if (pos.x != 0f || pos.y != 0f) {
            pos.nor().scl(-Physics.GRAVITY);
            player.setPlayerGravity(pos.x, pos.y);
            player.setPlayerAngle(pos.angleRad());
        }

        player.act(dt);
        physics.act(dt);

        OrthographicCamera cam = (OrthographicCamera) getCamera();
        cam.up.set(0f, player.isInverted() ? -1f : 1f, 0f);
        cam.direction.set(0f, 0f, -1f);
        // cam.position.x = MathUtils.lerp(cam.position.x, player.getBodyPosition().x, dt * CAM_LERP_SPEED);
        // cam.position.y = MathUtils.lerp(cam.position.y, player.getBodyPosition().y, dt * CAM_LERP_SPEED);
        cam.position.x = player.getBodyPosition().x;
        cam.position.y = player.getBodyPosition().y;
        cam.rotate(-player.getAngleRad() * MathUtils.radDeg);
    }

    @Override
    public void draw() {
        super.draw();
        physics.render(getCamera().combined);
    }

    public Physics getPhysics() {
        return physics;
    }

    public Player getPlayer() {
        return player;
    }

    public void levelComplete() {
        getRoot().addAction(Actions.sequence(Actions.fadeOut(2f, Interpolation.fade),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        level.loadNext();
                    }
                })));
    }

    public void startLevel() {
        getRoot().addAction(Actions.fadeIn(2f, Interpolation.fade));
    }
}
