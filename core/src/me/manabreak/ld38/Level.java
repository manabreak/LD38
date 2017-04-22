package me.manabreak.ld38;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class Level {

    protected final GameStage stage;
    protected final List<Body> staticBodies = new ArrayList<>();
    protected final List<Body> keys = new ArrayList<>();
    private final float playerStartAngle;
    private float playerStartY;
    private float playerStartX;
    private Body door;
    private Physics physics;

    private int keysTotal = 0;
    private int keysCollected = 0;

    private List<Body> toBeDestroyed = new ArrayList<>();

    public Level(GameStage stage, String jsonFile) {
        this.stage = stage;
        this.physics = stage.getPhysics();

        FileHandle f = Gdx.files.internal("levels/0001.json");
        JsonReader reader = new JsonReader();
        JsonValue value = reader.parse(f);
        playerStartX = value.getFloat("start_x");
        playerStartY = value.getFloat("start_y");
        playerStartAngle = value.getFloat("start_angle");

        JsonValue statics = value.get("statics");
        for (int i = 0; i < statics.size; ++i) {
            JsonValue stat = statics.get(i);
            createStatic(stat);
        }

        JsonValue keys = value.get("keys");
        keysTotal = keys.size;
        for (int i = 0; i < keys.size; ++i) {
            JsonValue key = keys.get(i);
            createKey(key);
        }

        createDoor(value);
    }

    private void createDoor(JsonValue value) {
        float doorX = value.getFloat("door_x");
        float doorY = value.getFloat("door_y");
        float doorAngle = value.getFloat("door_angle");

        door = physics.createBox(4f, 6f, BodyDef.BodyType.StaticBody);
        door.getFixtureList().get(0).setSensor(true);
        physics.setBodyPosition(door, doorX, doorY);
        door.setTransform(door.getPosition(), doorAngle * MathUtils.degRad);

        door.getFixtureList().get(0).setUserData(new PhysicsCallback() {
            @Override
            public void onCollisionBegin(Contact contact, Fixture other) {
                if (other.getUserData() == stage.getPlayer().getPlayerCallback()) {
                    System.out.println("Player touched door");
                    if (keysCollected == keysTotal) {
                        System.out.println("Level complete!");
                    } else {
                        System.out.println("Keys missing...");
                    }
                }
            }

            @Override
            public void onCollisionEnd(Contact contact, Fixture other) {

            }
        });
    }

    public void act(float dt) {
        for (Body body : toBeDestroyed) {
            physics.destroy(body);
        }
        toBeDestroyed.clear();
    }

    private void createKey(JsonValue value) {
        float x = value.getFloat("x");
        float y = value.getFloat("y");
        final Body body = physics.createBox(3f, 3f, BodyDef.BodyType.StaticBody);
        physics.setBodyPosition(body, x, y);
        body.getFixtureList().get(0).setSensor(true);
        Key key = new Key() {
            @Override
            public void onCollisionBegin(Contact contact, Fixture other) {
                if (other.getUserData() == stage.getPlayer().getPlayerCallback()) {
                    keysCollected++;
                    toBeDestroyed.add(body);
                    keys.remove(this);
                    if (keysCollected == keysTotal) {
                        System.out.println("All keys collected!");
                    } else {
                        System.out.println("Collected: " + keysCollected + "/" + keysTotal);
                    }
                }
            }
        };
        body.getFixtureList().get(0).setUserData(key);
        keys.add(body);
    }

    private void createStatic(JsonValue value) {
        Body body = null;
        String shape = value.getString("shape");
        float x = value.getFloat("x", 0f);
        float y = value.getFloat("y", 0f);
        float angle = value.getFloat("angle", 0f);
        if ("circle".equals(shape)) {
            float radius = value.getFloat("radius");
            body = physics.createSphere(radius, BodyDef.BodyType.StaticBody);
        } else if ("rect".equals(shape)) {
            float width = value.getFloat("width");
            float height = value.getFloat("height");
            body = physics.createBox(width, height, BodyDef.BodyType.StaticBody);
        }

        if (body != null) {
            physics.setBodyPosition(body, x, y);
            body.setTransform(body.getPosition(), angle * MathUtils.degRad);
            staticBodies.add(body);
        } else {
            System.out.println("Couldn't parse static from value: " + value.toString());
        }
    }

    public float getPlayerStartY() {
        return playerStartY;
    }

    public float getPlayerStartX() {
        return playerStartX;
    }
}
