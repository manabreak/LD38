package me.manabreak.ld38;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public interface PhysicsCallback {
    void onCollisionBegin(Contact contact, Fixture other);

    void onCollisionEnd(Contact contact, Fixture other);
}
