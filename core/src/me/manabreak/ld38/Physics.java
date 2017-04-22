package me.manabreak.ld38;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Physics {

    public static final float SCALE = 6f;
    public static final float INV_SCALE = 1f / SCALE;
    public static final float GRAVITY = 20f;

    private final Box2DDebugRenderer debugRenderer;
    private final Vector2 worldGravity;
    private World world;

    public Physics() {
        worldGravity = new Vector2(0f, 0f);
        world = new World(worldGravity, true);
        debugRenderer = new Box2DDebugRenderer();
    }

    public void act(float delta) {
        world.step(delta, 6, 2);
    }

    public void setBodyPosition(Body body, float x, float y) {
        float a = body.getAngle();
        body.setTransform(x * INV_SCALE, y * INV_SCALE, a);
    }

    public Body createBox(float width, float height, BodyDef.BodyType type) {
        BodyDef def = new BodyDef();
        def.type = type;
        Body body = world.createBody(def);

        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * INV_SCALE / 2f, height * INV_SCALE / 2f);
        fdef.shape = shape;
        body.createFixture(fdef);
        shape.dispose();

        return body;
    }

    public Body createSphere(float radius, BodyDef.BodyType type) {
        BodyDef def = new BodyDef();
        def.type = type;
        Body body = world.createBody(def);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(radius * INV_SCALE);
        fdef.shape = shape;
        body.createFixture(fdef);
        shape.dispose();

        return body;
    }

    public void render(Matrix4 combined) {
        debugRenderer.render(world, combined);
    }
}