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

    private final GameStage stage;
    private final List<Body> staticBodies = new ArrayList<>();
    private final List<SpriteActor> staticActors = new ArrayList<>();
    private float playerStartAngle;
    private float playerStartY;
    private float playerStartX;
    private Body door;
    private Physics physics;

    private int keysTotal = 0;
    private int keysCollected = 0;

    private List<Body> toBeDestroyed = new ArrayList<>();
    private List<Body> keys = new ArrayList<>();
    private String nextLevel = null;
    private String levelToLoad = null;

    private List<SpriteActor> keyActors = new ArrayList<>();
    SpriteActor doorActor;

    public Level(GameStage stage) {
        this.stage = stage;
        this.physics = stage.getPhysics();
    }

    public void load(String jsonFile) {
        clear();

        FileHandle f = Gdx.files.internal("levels/" + jsonFile + ".json");
        JsonReader reader = new JsonReader();
        JsonValue value = reader.parse(f);
        playerStartX = value.getFloat("start_x");
        playerStartY = value.getFloat("start_y");
        playerStartAngle = value.getFloat("start_angle");

        Player player = stage.getPlayer();
        physics.setBodyPosition(player.getBody(), playerStartX, playerStartY);

        JsonValue statics = value.get("statics");
        for (int i = 0; i < statics.size; ++i) {
            JsonValue stat = statics.get(i);
            createStatic(stat);
        }

        if (value.has("keys")) {
            JsonValue keys = value.get("keys");
            keysTotal = keys.size;
            for (int i = 0; i < keys.size; ++i) {
                JsonValue key = keys.get(i);
                createKey(key);
            }
        }

        if (value.has("eggs")) {
            JsonValue eggs = value.get("eggs");
            for (int i = 0; i < eggs.size; ++i) {
                JsonValue egg = eggs.get(i);
                createEgg(egg);
            }
        }

        nextLevel = value.getString("next_level");

        createDoor(value);

        stage.startLevel();
    }

    public void clear() {
        keysCollected = 0;
        keysTotal = 0;
        playerStartX = 0f;
        playerStartY = 0f;
        playerStartAngle = 0f;
        nextLevel = null;

        if (doorActor != null) {
            doorActor.remove();
            doorActor = null;
        }

        for (SpriteActor actor : staticActors) {
            actor.remove();
        }
        staticActors.clear();

        stage.getPlayer().getBody().setLinearVelocity(0f, 0f);

        for (Body body : toBeDestroyed) {
            physics.destroy(body);
        }
        toBeDestroyed.clear();

        if (door != null) {
            physics.destroy(door);
            door = null;
        }

        for (Body body : staticBodies) {
            physics.destroy(body);
        }
        staticBodies.clear();

        for (Body body : keys) {
            physics.destroy(body);
        }
        keys.clear();

    }

    private void createEgg(JsonValue value) {
        float x = value.getFloat("x");
        float y = value.getFloat("y");

        Body egg = physics.createSphere(1.f, BodyDef.BodyType.StaticBody);
        egg.getFixtureList().get(0).setSensor(true);
        physics.setBodyPosition(egg, x, y);

        SpriteActor a = new SpriteActor(Res.create("egg0"));
        stage.getGameActors().addActorAt(0, a);
        float r = a.sprite.getWidth() / a.sprite.getHeight();
        a.setSize(3f * r * Physics.INV_SCALE, 3f * Physics.INV_SCALE);
        a.setOriginCenter();
        a.setPosition(x * Physics.INV_SCALE - a.getWidth() / 2f, y * Physics.INV_SCALE - a.getHeight() / 2f);

        egg.getFixtureList().get(0).setUserData(new PhysicsCallback() {
            @Override
            public void onCollisionBegin(Contact contact, Fixture other) {
                if (other.getUserData() == stage.getPlayer().getPlayerCallback()) {
                    // stage.getPlayer().invertGravity();
                    stage.invert();
                }
            }

            @Override
            public void onCollisionEnd(Contact contact, Fixture other) {

            }
        });
    }

    private void createDoor(JsonValue value) {
        float doorX = value.getFloat("door_x");
        float doorY = value.getFloat("door_y");
        float doorAngle = value.getFloat("door_angle");

        door = physics.createBox(4f, 6f, BodyDef.BodyType.StaticBody);
        door.getFixtureList().get(0).setSensor(true);
        physics.setBodyPosition(door, doorX, doorY);
        door.setTransform(door.getPosition(), doorAngle * MathUtils.degRad);

        doorActor = new SpriteActor(Res.create(value.getString("door_graphic", "door0")));
        stage.getGameActors().addActorAt(0, doorActor);
        doorActor.setSize(4f * Physics.INV_SCALE, 6f * Physics.INV_SCALE * 1.05f);
        doorActor.setOriginCenter();
        doorActor.setRotation(doorAngle);
        doorActor.setPosition(doorX * Physics.INV_SCALE - doorActor.getWidth() / 2f, doorY * Physics.INV_SCALE - doorActor.getHeight() / 2f);

        door.getFixtureList().get(0).setUserData(new PhysicsCallback() {
            @Override
            public void onCollisionBegin(Contact contact, Fixture other) {
                if (other.getUserData() == stage.getPlayer().getPlayerCallback()) {
                    System.out.println("Player touched door");
                    if (keysCollected == keysTotal) {
                        System.out.println("Level complete!");
                        stage.levelComplete();
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

        if (levelToLoad != null) {
            load(levelToLoad);
            levelToLoad = null;
        }
    }

    private void createKey(JsonValue value) {
        float x = value.getFloat("x");
        float y = value.getFloat("y");
        final Body body = physics.createBox(1.5f, 3f, BodyDef.BodyType.StaticBody);
        physics.setBodyPosition(body, x, y);
        body.getFixtureList().get(0).setSensor(true);
        keys.add(body);

        final SpriteActor a = new SpriteActor(Res.create("key"));
        stage.getGameActors().addActor(a);
        a.setSize(1.5f * Physics.INV_SCALE, 3f * Physics.INV_SCALE);
        a.setPosition(x * Physics.INV_SCALE - a.getWidth() / 2f, y * Physics.INV_SCALE - a.getHeight() / 2f);

        Key key = new Key() {
            @Override
            public void onCollisionBegin(Contact contact, Fixture other) {
                if (other.getUserData() == stage.getPlayer().getPlayerCallback()) {
                    keysCollected++;
                    toBeDestroyed.add(body);
                    keys.remove(body);
                    a.remove();
                    if (keysCollected == keysTotal) {
                        System.out.println("All keys collected!");
                    } else {
                        System.out.println("Collected: " + keysCollected + "/" + keysTotal);
                    }
                }
            }
        };
        body.getFixtureList().get(0).setUserData(key);
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

            SpriteActor actor = new SpriteActor(Res.create("earth0"));
            actor.setSize(radius * 2f * Physics.INV_SCALE * 1.05f, radius * 2f * Physics.INV_SCALE * 1.05f);
            stage.getGameActors().addActor(actor);
            actor.setPosition(x * Physics.INV_SCALE - actor.getWidth() / 2f, y * Physics.INV_SCALE - actor.getHeight() / 2f);
            staticActors.add(actor);
        } else if ("rect".equals(shape)) {
            float width = value.getFloat("width");
            float height = value.getFloat("height");
            body = physics.createBox(width, height, BodyDef.BodyType.StaticBody);

            SpriteActor actor = new SpriteActor(Res.create(value.getString("sprite", "rect0")));
            actor.setSize(width * Physics.INV_SCALE * 1.05f, height * Physics.INV_SCALE * 1.05f);
            stage.getGameActors().addActor(actor);
            actor.setOriginCenter();
            actor.setRotation(angle);
            actor.setPosition(x * Physics.INV_SCALE - actor.getWidth() / 2f, y * Physics.INV_SCALE - actor.getHeight() / 2f);
            staticActors.add(actor);
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

    public void loadNext() {
        levelToLoad = nextLevel;
    }
}
