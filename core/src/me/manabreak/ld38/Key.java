package me.manabreak.ld38;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public abstract class Key implements PhysicsCallback {
    @Override
    public void onCollisionEnd(Contact contact, Fixture other) {

    }
}
