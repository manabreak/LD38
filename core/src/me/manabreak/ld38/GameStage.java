package me.manabreak.ld38;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.PI2;

public class GameStage extends Stage {

    private static final float CAM_LERP_SPEED = 1f;
    private final Player player;
    private Physics physics;
    private Level level;

    Sprite s;
    private boolean inverting = false;
    private float invertTimer = 0f;
    private float invertStartAngle;

    private SpriteActor sky;
    private Group actors;
    private boolean resetting = false;
    private float resetTimer = 0f;

    public GameStage() {
        super(new ExtendViewport(5f, 5f));
        physics = new Physics();
        initCamera();

        sky = new SpriteActor(Res.create("sky"));
        float max = Math.max(getWidth(), getHeight());
        max *= 2f;
        sky.setSize(max, max);
        sky.setPosition(-max / 2f, -max / 2f);
        addActor(sky);

        actors = new Group();
        addActor(actors);

        s = Res.create("chicken");

        actors.setColor(1f, 1f, 1f, 0f);

        player = new Player(this);
        level = new Level(this);

        level.load("lvl_0004");
    }

    private void initCamera() {
        Camera cam = getCamera();
        cam.position.x = 0f;
        cam.position.y = 0f;
    }

    @Override
    public void act(float dt) {
        super.act(dt);

        if (inverting) {
            float a = Interpolation.elastic.apply(invertStartAngle, invertStartAngle + PI, invertTimer / 2f);
            setCameraRotation(a);
            player.setSpriteRotation(a);
            invertTimer += dt;
            if (invertTimer >= 2f) {
                player.invertGravity();
                inverting = false;
            }
            return;
        }

        if (resetting) {
            resetTimer += dt;
            if (resetTimer >= 1f) {
                Body body = player.getBody();
                body.setLinearVelocity(0f, 0f);
                level.reset();
                resetting = false;
            }
        }

        level.act(dt);

        Vector2 pos = player.getBodyPosition().cpy();
        if (pos.x != 0f || pos.y != 0f) {
            pos.nor().scl(-Physics.GRAVITY);
            player.setPlayerGravity(pos.x, pos.y);
            player.setPlayerAngle(pos.angleRad());
        }

        player.act(dt);
        physics.act(dt);

        Vector2 playerPos = player.getBodyPosition();
        if (playerPos.len2() < 160f) {
            setCameraRotation(player.getAngleRad());
        } else if (!resetting) {
            resetting = true;
            resetTimer = 0f;
        }
    }

    public Group getGameActors() {
        return actors;
    }

    private void setCameraRotation(float a) {
        OrthographicCamera cam = (OrthographicCamera) getCamera();
        cam.up.set(0f, 1f, 0f);
        cam.direction.set(0f, 0f, -1f);
        cam.position.x = player.getBodyPosition().x;
        cam.position.y = player.getBodyPosition().y;
        while (a >= PI2) a -= PI2;
        while (a < 0f) a += PI2;
        cam.rotate(-a * MathUtils.radDeg);
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
        actors.addAction(Actions.sequence(Actions.fadeOut(2f, Interpolation.fade),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        level.loadNext();
                    }
                })));
    }

    public void startLevel() {
        actors.addAction(Actions.fadeIn(2f, Interpolation.fade));
    }

    public void invert(boolean immediate) {
        if (immediate) {
            player.invertGravity();
        } else {
            inverting = true;
            invertTimer = 0f;
            invertStartAngle = player.getBody().getAngle();
        }
    }

    public boolean isInverting() {
        return inverting;
    }
}
