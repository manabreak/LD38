package me.manabreak.ld38;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameStage extends Stage {

    private static final float CAM_LERP_SPEED = 1f;
    private final Player player;
    private Physics physics;

    private Level level;

    public GameStage() {
        super(new ExtendViewport(8f, 8f));
        physics = new Physics();
        initCamera();

        player = new Player(this);
        level = new Level(this, "0001.json");

        physics.setBodyPosition(player.getBody(), level.getPlayerStartX(), level.getPlayerStartY());
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
        cam.up.set(0f, 1f, 0f);
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
}
