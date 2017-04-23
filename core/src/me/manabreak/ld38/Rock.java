package me.manabreak.ld38;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.JsonValue;

public class Rock extends SpriteActor {

    private final Body body;

    public Rock(GameStage stage, JsonValue value) {
        super(Res.create("rock"));
        Physics physics = stage.getPhysics();

        float x = value.getFloat("x", 0f) / 8f;
        float y = value.getFloat("y", 0f) / 8f;
        float w = value.getFloat("width") / 8f;
        float h = value.getFloat("height") / 8f;
        float angle = value.getFloat("rotation", 0f);

        float cx = w / 2f;
        float cy = h / 2f;

        float cosR = MathUtils.cosDeg(angle);
        float sinR = MathUtils.sinDeg(angle);
        float rotcx = cx * cosR - cy * sinR;
        float rotcy = cx * sinR + cy * cosR;

        x = x + rotcx;
        y = y + rotcy;

        setSize(w * Physics.INV_SCALE, h * Physics.INV_SCALE);
        setPosition(x * Physics.INV_SCALE - getWidth() / 2f, y * Physics.INV_SCALE - getHeight() / 2f);
        setOriginCenter();

        // body = physics.createSphere(w / 2f, BodyDef.BodyType.DynamicBody);
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.fixedRotation = false;
        body = physics.getWorld().createBody(def);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(w / 2f * Physics.INV_SCALE);
        fdef.density = 1f;
        fdef.friction = 0.6f;
        fdef.restitution = 0.3f;
        fdef.shape = shape;
        body.createFixture(fdef);

        shape.dispose();

        physics.setBodyPosition(body, x, y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Vector2 pos = body.getPosition();
        if (pos.x != 0f || pos.y != 0f) {
            pos.nor().scl(-Physics.GRAVITY);
            body.applyForce(pos.x, pos.y, 0f, 0f, true);
        }

        if (body.getAngularVelocity() < 5f) {
            body.applyTorque(5f, true);
        }


        float x = body.getPosition().x;
        float y = body.getPosition().y;
        setPosition(x - getWidth() / 2f, y - getHeight() / 2f);
        setRotation(body.getAngle() * MathUtils.radDeg);

        // setRotation((body.getAngle()) * radDeg);
        // setPosition(x, y);

    }
}
