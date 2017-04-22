package me.manabreak.ld38;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class Player {

    public static final float MOVE_VEL = 0.4f;
    private GameStage stage;

    private Body body;

    private Vector2 playerGravity;
    private Vector2 playerInput = new Vector2();

    public Player(GameStage stage) {
        this.stage = stage;
        body = stage.getPhysics().createBox(2.5f, 4.f, BodyDef.BodyType.DynamicBody);

        playerGravity = new Vector2(0f, 0f);

        stage.getPhysics().setBodyPosition(body, 0f, 15f);
    }

    public void setPlayerGravity(float x, float y) {
        playerGravity.set(x, y);
    }

    public void act(float dt) {
        body.applyForce(playerGravity.x, playerGravity.y, 0f, 0f, true);

        float velX = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velX -= MOVE_VEL;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velX += MOVE_VEL;
        }

        if (velX != 0f) {
            playerInput.set(velX, 0f);
            playerInput.rotateRad(body.getAngle());

            Vector2 vel = body.getLinearVelocity();
            vel.add(playerInput);
            body.setLinearVelocity(vel);
        } else {
            Vector2 vel = body.getLinearVelocity();
            vel.rotateRad(body.getAngle());
            vel.scl(0.9f, 1f);
            vel.rotateRad(-body.getAngle());
            body.setLinearVelocity(vel);
        }
    }

    public Vector2 getBodyPosition() {
        return body.getPosition();
    }

    public void setPlayerAngle(float angleRad) {
        body.setTransform(body.getPosition(), angleRad + MathUtils.PI / 2f);
    }

    public float getAngleRad() {
        return body.getAngle();
    }
}
