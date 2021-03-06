package entities;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import test.RoomTest;

public class Player extends Entity implements InputProcessor {
	private static final float FRAME_DURATION = 0.2f; 
	private static final float SPEED = 2f;
	
	private Vector2 velocity;
	private Vector3 cursorPos;
	private float direction;
	
	private boolean left, right, up, down;
	private boolean leftHold, rightHold, upHold, downHold;
	private boolean attacking;
	private boolean interacting;
	private boolean using;

	private byte xDir, yDir, prevXDir, prevYDir;
	
	private Animation<TextureRegion> currAnim; 
	private Animation<TextureRegion> upAnim, downAnim, leftAnim, rightAnim; 
	private Animation<TextureRegion> upLeftAnim, upRightAnim, downLeftAnim, downRightAnim;
	private Animation<TextureRegion> attackAnim;
	
	private OrthographicCamera cam;
	private ArrayList<Item> items;
	private int currItem;
	
	private float moveTime, attackTime;
	
	public Player(float x, float y, float width, float height, RoomTest test) {
		super(x, y, width, height);
		createBody(test.world);
		
		cam = (OrthographicCamera) test.viewport.getCamera();
		
		velocity = new Vector2();
		cursorPos = new Vector3();
		
		items = new ArrayList<Item>();
		
		Texture spriteSheet = new Texture("entities/player/spritesheet.png");
		TextureRegion[][] regions = TextureRegion.split(spriteSheet, 32, 32);
		
		downAnim = new Animation<TextureRegion>(FRAME_DURATION, regions[0]);
		upAnim = new Animation<TextureRegion>(FRAME_DURATION, regions[5]);
		leftAnim = new Animation<TextureRegion>(FRAME_DURATION, regions[3]);
		rightAnim = new Animation<TextureRegion>(FRAME_DURATION, regions[4]);
		upLeftAnim = new Animation<TextureRegion>(FRAME_DURATION, regions[6]);
		upRightAnim = new Animation<TextureRegion>(FRAME_DURATION, regions[7]);
		downLeftAnim = new Animation<TextureRegion>(FRAME_DURATION, regions[1]);
		downRightAnim = new Animation<TextureRegion>(FRAME_DURATION, regions[2]);
		
		downAnim.setPlayMode(PlayMode.LOOP);
		upAnim.setPlayMode(PlayMode.LOOP);
		leftAnim.setPlayMode(PlayMode.LOOP);
		rightAnim.setPlayMode(PlayMode.LOOP);
		upLeftAnim.setPlayMode(PlayMode.LOOP);
		upRightAnim.setPlayMode(PlayMode.LOOP);
		downLeftAnim.setPlayMode(PlayMode.LOOP);
		downRightAnim.setPlayMode(PlayMode.LOOP);
		
		attackAnim = RoomTest.makeAnimation(new Texture("entities/player/attackanim.png"), 1 / 10f, 2, 5, 32, 32);
		
		currAnim = upAnim;
	}
	
	public void act(float delta) {
		processDirection(delta);
		
		processAnimations(delta);
		
		if (using && currItem < items.size() && items.get(currItem) != null)
			items.get(currItem).use(this);
	}
	
	public void draw(Batch batch) {
		batch.draw(currAnim.getKeyFrame(moveTime), getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
		
		if (attacking)
			batch.draw(attackAnim.getKeyFrame(attackTime), getX() + 4, getY(), 4, 4);
	}

	public void processDirection(float delta) {		
		if(leftHold) {
			if(right)
				xDir = 1;
			else
				xDir = -1;
		}
		
		else if(rightHold) {
			if(left)
				xDir = -1;
			else
				xDir = 1;
		}
		
		if(!left && !right)
			xDir = 0;
		
		if(downHold) {
			if(up)
				yDir = 1;
			else
				yDir = -1;
		}
		else if(upHold) {
			if(down)
				yDir = -1;
			else
				yDir = 1;
		}
		
		if(!up && !down)
			yDir = 0;

		velocity.x = SPEED;
		velocity.y = 0;
		
		if (yDir == 0 && xDir == 0)
			velocity.x = 0;
		else if (yDir == 1) {
			if (xDir == 1) 
				direction = 45;
			else if(xDir == -1)
				direction = 135;
			else
				direction = 90;
		}
		else if (yDir == -1) {
			if (xDir == 1) 
				direction = 315;
			else if(xDir == -1)
				direction = 225;
			else
				direction = 270;
		}
		else {
			if (xDir == 1) 
				direction = 0;
			else if(xDir == -1)
				direction = 180;
		}
		
		velocity.rotate(direction);
		
		getBody().setLinearVelocity(velocity);
	}
	
	public void processAnimations(float delta) {
		moveTime += delta;
		
		if (attacking) {
			attackTime += delta;
			if (attackAnim.isAnimationFinished(attackTime)) {
				attackTime = 0; 
				attacking = false; 
			}
		}
		
		if (prevXDir != xDir) { 
			prevXDir = xDir;
			moveTime = 0;
		}
		
		if (prevYDir != yDir) {
			prevYDir = yDir;
			moveTime = 0;
		}

		if (xDir == -1) {
			if (yDir == 0)
				currAnim = leftAnim;
			else if (yDir == -1)
				currAnim = downLeftAnim;
			else if (yDir == 1)
				currAnim = upLeftAnim;
		}
		
		else if (xDir == 0) {
			if (yDir == 0) {
				moveTime -= delta;
				return;
			}
			else if (yDir == -1)
				currAnim = downAnim;
			else if (yDir == 1)
				currAnim = upAnim;
		}
		
		else if (xDir == 1) {
			if (yDir == 0)
				currAnim = rightAnim;
			else if (yDir == -1)
				currAnim = downRightAnim;
			else if (yDir == 1)
				currAnim = upRightAnim;
		}
	}
	
	public void updatePosition() {
		setPosition(getBody().getPosition().x - getWidth() / 2, getBody().getPosition().y - getHeight() / 2);
	}
	
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.LEFT: 
			case Input.Keys.A: 
				left = true; 
				if(!rightHold) 
					leftHold = true;
				break;
			case Input.Keys.RIGHT:
			case Input.Keys.D: 
				right = true;
				if(!leftHold)
					rightHold = true;
				break;
			case Input.Keys.UP:
			case Input.Keys.W: 
				up = true; 
				if(!downHold) 
					upHold = true;
				break;
			case Input.Keys.DOWN:
			case Input.Keys.S: 
				down = true;
				if(!upHold)
					downHold = true;
				break;
			case Input.Keys.SPACE:
				attacking = true;
				break;
			case Input.Keys.E:
				interacting = true;
		}
		return true;
	}

	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.LEFT:
			case Input.Keys.A: 
				left = false; 
				leftHold = false; 
				if (right)
					rightHold = true;
				break;
			case Input.Keys.RIGHT:
			case Input.Keys.D: 
				right = false; 
				rightHold = false;
				if (left)
					leftHold = true;
				break;
			case Input.Keys.UP:
			case Input.Keys.W: 
				up = false; 
				upHold = false; 
				if (down)
					downHold = true;
				break;
			case Input.Keys.DOWN:
			case Input.Keys.S: 
				down = false; 
				downHold = false;
				if (up)
					upHold = true;
		}
		return false;
	}
	
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(button == Input.Buttons.LEFT)
			using = true;
		
		return true;
	}

	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(button == Input.Buttons.LEFT)
			using = false;
		
		return true;
	}
	
	public void createBody(World world) {
		BodyDef playerDef = new BodyDef();
		playerDef.type = BodyType.DynamicBody;
		
		playerDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
		
		Body player = world.createBody(playerDef);
		
		player.setUserData(this);
		
		CircleShape playerShape = new CircleShape();
		playerShape.setRadius(0.5f);
		
		player.createFixture(playerShape, 0);
		
		playerShape.dispose();
		
		setBody(player);
	}
	
	public float getCursorDirection() {
		cursorPos.x = Gdx.input.getX();
		cursorPos.y = Gdx.input.getY();
		
		cam.unproject(cursorPos);
		
		return MathUtils.radiansToDegrees * MathUtils.atan2(cursorPos.y - getY() - getHeight() / 2, cursorPos.x - getX() - getWidth() / 2);
	}
	
	public void addItem(Item item) {
		items.add(item);
	}
	
	public void removeItem(Item item) {
		items.remove(item);
	}
	
	public boolean getInteracting() {
		return interacting;
	}
	
	public void setInteracting(boolean interacting) {
		this.interacting = interacting;
	}
	
	public boolean keyTyped(char character) {return false;}

	public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}

	public boolean mouseMoved(int screenX, int screenY) {return false;}

	public boolean scrolled(int amount) {return false;}
	
	public void processCollision(Entity entity) {}
	
	public void dispose() {}
}
