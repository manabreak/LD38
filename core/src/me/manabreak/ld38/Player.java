package me.manabreak.ld38;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.PI2;
import static com.badlogic.gdx.math.MathUtils.radDeg;

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

        }

        @Override
        public void onCollisionEnd(Contact contact, Fixture other) {

        }
    };

    private boolean grounded = false;
    private int groundedCounter = 0;

    private PhysicsCallback groundSensorCallback = new PhysicsCallback() {
        @Override
        public void onCollisionBegin(Contact contact, Fixture other) {
            if (!other.isSensor()) {
                grounded = true;
                groundedCounter++;
                actor.clearActions();
                actor.addAction(Actions.sequence(
                        Actions.scaleTo(1f, 0.7f, 0.08f, Interpolation.fade),
                        Actions.scaleTo(1f, 1f, 0.2f, Interpolation.fade)
                ));
            }
        }

        @Override
        public void onCollisionEnd(Contact contact, Fixture other) {
            if (!other.isSensor()) {
                groundedCounter--;
                if (groundedCounter <= 0) {
                    grounded = false;
                    groundedCounter = 0;
                }
            }
        }
    };
    private boolean jumping = false;
    private float jumpingForce = 0f;
    private Vector2 contJumpVec = new Vector2();

    private SpriteActor actor;
    private boolean facingRight = true;
    private boolean walking = false;
    private boolean inverted = false;

    public Player(GameStage stage) {
        this.stage = stage;

        actor = new SpriteActor(Res.create("chicken"));
        float r = actor.sprite.getWidth() / actor.sprite.getHeight();
        actor.setSize(3f * r * Physics.INV_SCALE, 3f * Physics.INV_SCALE);
        actor.setOriginCenter();
        stage.addActor(actor);

        body = stage.getPhysics().createBox(3f * r, 3.f, BodyDef.BodyType.DynamicBody);
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
        if (inverted) {
            body.applyForce(-playerGravity.x, -playerGravity.y, 0f, 0f, true);
        } else {
            body.applyForce(playerGravity.x, playerGravity.y, 0f, 0f, true);
        }

        float velX = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velX -= MOVE_VEL;
            facingRight = false;
            if (!walking) {
                startWalkAnimation();
            }
            walking = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velX += MOVE_VEL;
            facingRight = true;
            if (!walking) {
                startWalkAnimation();
            }
            walking = true;
        }

        if (!Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (walking) {
                walking = false;
                stopWalkAnimation();
            }
        }

        if (grounded) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                grounded = false;
                jumping = true;
                Vector2 jumpVec = new Vector2(0f, 3f);
                jumpingForce = 100f;
                jumpVec.rotateRad(body.getAngle());
                body.applyLinearImpulse(jumpVec.x, jumpVec.y, 0f, 0f, true);
                actor.clearActions();
                actor.setScale(0.9f, 1.2f);
                actor.addAction(Actions.sequence(
                        Actions.scaleTo(1f, 1f, 0.5f, Interpolation.circleOut)
                ));
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

        float x = body.getPosition().x - actor.getWidth() / 2f;
        float y = body.getPosition().y - actor.getHeight() / 2f;

        /*
        x += MathUtils.cos(-body.getAngle()) * actor.getWidth();
        y += MathUtils.sin(body.getAngle()) * actor.getHeight();
        */

        if (!stage.isInverting()) {
            actor.setRotation((body.getAngle()) * radDeg);
            actor.sprite.setFlip(!facingRight, false);

            actor.setPosition(x, y);
        }
    }

    private void stopWalkAnimation() {
        actor.clearActions();
        actor.setScale(1f);
    }

    private void startWalkAnimation() {
        actor.clearActions();
        actor.setScale(1f);
        actor.addAction(Actions.forever(
                Actions.sequence(
                        Actions.scaleBy(0f, -0.1f, 0.1f, Interpolation.fade),
                        Actions.scaleBy(0f, 0.1f, 0.1f, Interpolation.fade)
                )
        ));
    }

    public Vector2 getBodyPosition() {
        return body.getPosition();
    }

    public void setPlayerAngle(float angleRad) {
        float angle = angleRad + PI / 2f;
        if (inverted) {
            angle += PI;
        }
        while (angle >= PI2) angle -= PI2;
        while (angle < 0f) angle += PI2;
        body.setTransform(body.getPosition(), angle);
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

    public void invertGravity() {
        inverted = !inverted;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setSpriteRotation(float a) {
        actor.setRotation(a);
    }
}
