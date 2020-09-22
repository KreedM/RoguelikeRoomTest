package test;

import java.util.ArrayList;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import entities.Bullet;
import entities.Entity;

public class TestListener implements ContactListener {
	private World world;
	private ArrayList<Contact> contacts;
	private ArrayList<Body> removing;
	
	public TestListener(World world) {
		this.world = world;
		contacts = new ArrayList<Contact>();
		removing = new ArrayList<Body>();
	}
	
	public void beginContact(Contact contact) {
		contacts.add(contact);
	}
	
	public void process() {
		removing.clear();
		
		for (Contact contact : contacts) { //A vs B collisions MUST BE DEFINED for it to work
			Entity a = (Entity) contact.getFixtureA().getBody().getUserData(), b = (Entity) contact.getFixtureB().getBody().getUserData();
			
			if(a instanceof Bullet && b instanceof Bullet) {
				((Bullet) a).processCollision(b);
				if(!removing.contains(contact.getFixtureA().getBody()))
					removing.add(contact.getFixtureA().getBody());
				((Bullet) b).processCollision(a);
				if(!removing.contains(contact.getFixtureB().getBody()))
					removing.add(contact.getFixtureB().getBody());
			}
			else if (a instanceof Bullet) {
				((Bullet) a).processCollision(b);
				if(!removing.contains(contact.getFixtureA().getBody()))
					removing.add(contact.getFixtureA().getBody());
			}
			else if(b instanceof Bullet) {
				((Bullet) b).processCollision(a);
				if(!removing.contains(contact.getFixtureB().getBody()))
					removing.add(contact.getFixtureB().getBody());
			}			
		}
		
		for (Body body : removing)
			world.destroyBody(body);
		
		contacts.clear();
	}
	
	public void endContact(Contact contact) {}

	public void preSolve(Contact contact, Manifold oldManifold) {}

	public void postSolve(Contact contact, ContactImpulse impulse) {}
}
