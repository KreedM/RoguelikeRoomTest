package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Door extends Entity implements Interactable {
	
	private boolean opened;
	private Texture doorOpened, doorClosed;
	private Filter filter;
	
	public Door(float x, float y, float width, float height, World world) {
		super(x, y, width, height);
		
		doorOpened = new Texture("entities/door/opened_door.png");
		doorClosed = new Texture("entities/door/closed_door.png");
		
		filter = new Filter();
		
		createBody(world);
	}

	public void interact(Player player) {
		opened = !opened;
		
		if(opened) 
			filter.maskBits = 0;
		else
			filter.maskBits = -1;

		getBody().getFixtureList().get(0).setFilterData(filter);
	}

	public void processCollision() {}

	public void createBody(World world) {
		BodyDef doorDef = new BodyDef();
		doorDef.type = BodyType.StaticBody;
		
		doorDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
		
		Body door = world.createBody(doorDef);
		door.setUserData(this);

		PolygonShape box = new PolygonShape();
		box.setAsBox(getWidth() / 2, getHeight() / 2);
		
		door.createFixture(box, 0);

		box.dispose();
		
		setBody(door);
	}

	public void draw(Batch batch) {
		if (opened)
			batch.draw(doorOpened, getX(), getY(), getWidth(), getHeight());
		else
			batch.draw(doorClosed, getX(), getY(), getWidth(), getHeight());
	}

	public void act(float delta) {}
	
	public void dispose() {
		doorOpened.dispose();
		doorClosed.dispose();
	}
}
