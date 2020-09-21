package entities;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface Item {
	public void use(Player player);
	
	public void drawPortrait(Batch batch, float x, float y);
}
