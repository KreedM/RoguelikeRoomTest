package test;

import java.util.ArrayList;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;

public class ContactListener implements com.badlogic.gdx.physics.box2d.ContactListener {

	private ArrayList<Contact> contacts;
	
	public ContactListener(ArrayList<Contact> contacts) {
		this.contacts = contacts;
	}
	
	public void beginContact(Contact contact) {
		contacts.add(contact);
	}
	
	public void endContact(Contact contact) {}

	public void preSolve(Contact contact, Manifold oldManifold) {}

	public void postSolve(Contact contact, ContactImpulse impulse) {}
}
