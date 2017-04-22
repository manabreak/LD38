package me.manabreak.ld38;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Physics implements ContactListener {

    public static final float SCALE = 6f;
    public static final float INV_SCALE = 1f / SCALE;
    public static final float GRAVITY = 40f;

    private final Box2DDebugRenderer debugRenderer;
    private final Vector2 worldGravity;
    private World world;
    private boolean debugDrawEnabled = true;

    public Physics() {
        worldGravity = new Vector2(0f, 0f);
        world = new World(worldGravity, true);
        world.setContactListener(this);
        debugRenderer = new Box2DDebugRenderer();
    }

    public void act(float delta) {
        world.step(delta, 6, 2);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugDrawEnabled = !debugDrawEnabled;
        }
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
        shape.setAsBox((width * INV_SCALE) / 2f, (height * INV_SCALE) / 2f);
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
        if (debugDrawEnabled) {
            debugRenderer.render(world, combined);
        }
    }

    public void destroy(Body body) {
        world.destroyBody(body);
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        if (dataA instanceof PhysicsCallback) {
            ((PhysicsCallback) dataA).onCollisionBegin(contact, fixB);
        }
        if (dataB instanceof PhysicsCallback) {
            ((PhysicsCallback) dataB).onCollisionBegin(contact, fixA);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        Object dataA = fixA.getUserData();
        Object dataB = fixB.getUserData();

        if (dataA instanceof PhysicsCallback) {
            ((PhysicsCallback) dataA).onCollisionEnd(contact, fixB);
        }
        if (dataB instanceof PhysicsCallback) {
            ((PhysicsCallback) dataB).onCollisionEnd(contact, fixA);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
