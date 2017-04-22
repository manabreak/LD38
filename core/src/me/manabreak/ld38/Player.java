package me.manabreak.ld38;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Player {

    public static final float MOVE_VEL = 0.4f;
    private final Fixture gsLeft;
    private GameStage stage;

    private Body body;

    private Vector2 playerGravity;
    private Vector2 playerInput = new Vector2();

    private PhysicsCallback bodyCallback = new PhysicsCallback() {
        @Override
        public void onCollisionBegin(Contact contact, Fixture other) {
            System.out.println("Body begin");
            Object data = other.getUserData();
            if (data instanceof Key) {
                System.out.println("Picked up a key!");
            }
        }

        @Override
        public void onCollisionEnd(Contact contact, Fixture other) {
            System.out.println("Body end");
        }
    };

    private boolean grounded = false;
    private PhysicsCallback groundSensorCallback = new PhysicsCallback() {
        @Override
        public void onCollisionBegin(Contact contact, Fixture other) {
            System.out.println("GS begin");
            grounded = true;
        }

        @Override
        public void onCollisionEnd(Contact contact, Fixture other) {
            System.out.println("GS end");
            grounded = false;
        }
    };
    private boolean jumping = false;
    private float jumpingForce = 0f;
    private Vector2 contJumpVec = new Vector2();

    public Player(GameStage stage) {
        this.stage = stage;
        body = stage.getPhysics().createBox(2.5f, 4.f, BodyDef.BodyType.DynamicBody);
        body.getFixtureList().get(0).setUserData(bodyCallback);
        body.getFixtureList().get(0).setFriction(0f);

        FixtureDef groundSensorDef = new FixtureDef();
        groundSensorDef.isSensor = true;
        CircleShape shape = new CircleShape();
        shape.setRadius(0.8f * Physics.INV_SCALE);
        shape.setPosition(new Vector2(0f, -2f * Physics.INV_SCALE));
        groundSensorDef.shape = shape;
        gsLeft = body.createFixture(groundSensorDef);
        gsLeft.setUserData(groundSensorCallback);
        shape.dispose();

        playerGravity = new Vector2(0f, 0f);
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

        if (grounded) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                grounded = false;
                jumping = true;
                Vector2 jumpVec = new Vector2(0f, 3f);
                jumpingForce = 100f;
                jumpVec.rotateRad(body.getAngle());
                body.applyLinearImpulse(jumpVec.x, jumpVec.y, 0f, 0f, true);
            }
        }

        if (jumping) {
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                jumpingForce *= 0.85f;
                contJumpVec.set(0f, jumpingForce);
                contJumpVec.rotateRad(body.getAngle());
                body.applyForce(contJumpVec.x, contJumpVec.y, 0f, 0f, true);
            } else {
                jumping = false;
            }
        }

        if (velX != 0f) {
            playerInput.set(velX, 0f);
            // playerInput.rotateRad(body.getAngle());

            Vector2 vel = body.getLinearVelocity();
            vel.rotateRad(-body.getAngle());
            vel.add(playerInput);
            if (vel.x > 3f) vel.x = 3f;
            if (vel.x < -3f) vel.x = -3f;
            vel.rotateRad(body.getAngle());
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

    public Body getBody() {
        return body;
    }

    public PhysicsCallback getPlayerCallback() {
        return bodyCallback;
    }
}
