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

    private String currentLevel = null;
    private String nextLevel = null;
    private String levelToLoad = null;

    private List<SpriteActor> keyActors = new ArrayList<>();
    SpriteActor doorActor;
    private List<SpriteActor> eggActors = new ArrayList<>();
    private List<Body> eggBodies = new ArrayList<>();
    private List<Rock> rocks = new ArrayList<>();

    public Level(GameStage stage) {
        this.stage = stage;
        this.physics = stage.getPhysics();
    }

    public void load(String jsonFile) {
        currentLevel = jsonFile;
        clear();

        FileHandle f = Gdx.files.internal("levels/" + jsonFile + ".json");
        JsonReader reader = new JsonReader();
        JsonValue value = reader.parse(f);

        JsonValue layers = value.get("layers");
        for (int i = 0; i < layers.size; ++i) {
            JsonValue layer = layers.get(i);
            String name = layer.getString("name");
            if ("statics".equals(name)) {
                extractStatics(layer);
            } else if ("sensors".equals(name)) {
                extractSensors(layer);
            } else if ("spawn".equals(name)) {
                extractSpawn(layer);
            } else if ("rocks".equals(name)) {
                extractRocks(layer);
            }
        }

        Player player = stage.getPlayer();
        physics.setBodyPosition(player.getBody(), playerStartX, playerStartY);
        player.setPlayerAngle(playerStartAngle * MathUtils.degRad);

        nextLevel = value.get("properties").getString("next_level", jsonFile);
        stage.startLevel();
    }

    private void extractRocks(JsonValue layer) {
        JsonValue objects = layer.get("objects");
        for (int i = 0; i < objects.size; ++i) {
            createRock(objects.get(i));
        }
    }

    private void extractSpawn(JsonValue layer) {
        JsonValue value = layer.get("objects").get(0);
        float x = value.getFloat("x", 0f) / 8f;
        float y = value.getFloat("y", 0f) / 8f;
        float w = value.getFloat("width") / 8f;
        float h = value.getFloat("height") / 8f;

        x += w / 2f;
        y += h / 2f;

        float angle = -value.getFloat("rotation", 0f);

        playerStartX = x;
        playerStartY = y;
        playerStartAngle = angle;
    }

    private void extractSensors(JsonValue layer) {
        JsonValue objects = layer.get("objects");
        for (int i = 0; i < objects.size; ++i) {
            JsonValue obj = objects.get(i);
            String name = obj.getString("name");
            if ("door".equals(name)) {
                createDoor(obj);
            } else if ("key".equals(name)) {
                createKey(obj);
                keysTotal++;
            } else if ("egg".equals(name)) {
                createEgg(obj);
            }
        }
    }

    private void extractStatics(JsonValue layer) {
        JsonValue objects = layer.get("objects");
        for (int i = 0; i < objects.size; ++i) {
            JsonValue obj = objects.get(i);
            createStatic(obj);
        }
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

        for (Body body : eggBodies) {
            physics.destroy(body);
        }
        eggBodies.clear();

        for (SpriteActor actor : eggActors) {
            actor.remove();
        }

        for (Rock rock : rocks) {
            rock.remove();
            physics.destroy(rock.getBody());
        }
        rocks.clear();

        eggActors.clear();
    }

    private void createRock(JsonValue value) {
        Rock rock = new Rock(stage, value);
        stage.addActor(rock);
        rocks.add(rock);
    }

    private void createEgg(JsonValue value) {
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

        Body egg = physics.createSphere(1.f, BodyDef.BodyType.StaticBody);
        egg.getFixtureList().get(0).setSensor(true);
        physics.setBodyPosition(egg, x, y);

        eggBodies.add(egg);

        SpriteActor a = new SpriteActor(Res.create("egg0"));
        stage.getGameActors().addActorAt(0, a);
        float r = a.sprite.getWidth() / a.sprite.getHeight();
        a.setSize(3f * r * Physics.INV_SCALE, 3f * Physics.INV_SCALE);
        a.setOriginCenter();
        a.setPosition(x * Physics.INV_SCALE - a.getWidth() / 2f, y * Physics.INV_SCALE - a.getHeight() / 2f);

        eggActors.add(a);

        egg.getFixtureList().get(0).setUserData(new PhysicsCallback() {
            @Override
            public void onCollisionBegin(Contact contact, Fixture other) {
                if (other.getUserData() == stage.getPlayer().getPlayerCallback()) {
                    // stage.getPlayer().invertGravity();
                    stage.invert(false);
                    Res.invert.play();
                }
            }

            @Override
            public void onCollisionEnd(Contact contact, Fixture other) {

            }
        });
    }

    private void createDoor(JsonValue value) {
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

        door = physics.createBox(4f, 6f, BodyDef.BodyType.StaticBody);
        door.getFixtureList().get(0).setSensor(true);
        physics.setBodyPosition(door, x, y);
        door.setTransform(door.getPosition(), angle * MathUtils.degRad);

        doorActor = new SpriteActor(Res.create(value.getString("door_graphic", "door0")));
        stage.getGameActors().addActorAt(0, doorActor);
        doorActor.setSize(4f * Physics.INV_SCALE, 6f * Physics.INV_SCALE * 1.05f);
        doorActor.setOriginCenter();
        doorActor.setRotation(angle);
        doorActor.setPosition(x * Physics.INV_SCALE - doorActor.getWidth() / 2f, y * Physics.INV_SCALE - doorActor.getHeight() / 2f);

        door.getFixtureList().get(0).setUserData(new PhysicsCallback() {
            @Override
            public void onCollisionBegin(Contact contact, Fixture other) {
                if (other.getUserData() == stage.getPlayer().getPlayerCallback()) {
                    System.out.println("Player touched door");
                    if (keysCollected == keysTotal) {
                        System.out.println("Level complete!");
                        Res.door.play();
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
                    Res.key.play();
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
        Body body;

        boolean ellipse = value.getBoolean("ellipse", false);

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

        if (ellipse) {
            float radius = w / 2f;
            body = physics.createSphere(radius, BodyDef.BodyType.StaticBody);

            SpriteActor actor = new SpriteActor(Res.create("earth0"));
            actor.setSize(radius * 2f * Physics.INV_SCALE * 1.05f, radius * 2f * Physics.INV_SCALE * 1.05f);
            stage.getGameActors().addActor(actor);
            actor.setPosition(x * Physics.INV_SCALE - actor.getWidth() / 2f, y * Physics.INV_SCALE - actor.getHeight() / 2f);
            staticActors.add(actor);
        } else {
            body = physics.createBox(w, h, BodyDef.BodyType.StaticBody);

            String spr = "rect0";
            if (value.has("properties")) {
                JsonValue props = value.get("properties");
                spr = props.getString("sprite", "rect0");
            }

            SpriteActor actor = new SpriteActor(Res.create(spr));
            actor.setSize(w * Physics.INV_SCALE * 1.05f, h * Physics.INV_SCALE * 1.05f);
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

    public void reset() {

        if (stage.getPlayer().isInverted()) {
            stage.invert(true);
        }
        Body body = stage.getPlayer().getBody();
        physics.setBodyPosition(body, playerStartX, playerStartY);

        load(currentLevel);
    }
}
