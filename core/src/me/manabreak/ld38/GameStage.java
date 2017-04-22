package me.manabreak.ld38;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameStage extends Stage {

    private final Player player;
    private Physics physics;

    public GameStage() {
        super(new ExtendViewport(8f, 8f));
        physics = new Physics();
        initCamera();
        physics.createSphere(10f, BodyDef.BodyType.StaticBody);

        player = new Player(this);
    }

    private void initCamera() {
        Camera cam = getCamera();
        cam.position.x = 0f;
        cam.position.y = 0f;
    }

    @Override
    public void act(float dt) {
        super.act(dt);

        Vector2 pos = player.getBodyPosition().cpy();
        if (pos.x != 0f || pos.y != 0f) {
            pos.nor().scl(-Physics.GRAVITY);
            player.setPlayerGravity(pos.x, pos.y);
            player.setPlayerAngle(pos.angleRad());
        }

        player.act(dt);
        physics.act(dt);

        OrthographicCamera cam = (OrthographicCamera) getCamera();
        cam.up.set(0f, 1f, 0f);
        cam.direction.set(0f, 0f, -1f);
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
}
