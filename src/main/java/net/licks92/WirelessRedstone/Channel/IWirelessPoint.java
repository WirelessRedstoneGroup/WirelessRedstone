package net.licks92.WirelessRedstone.Channel;

public interface IWirelessPoint {
	String getOwner();

	int getX();

	int getY();

	int getZ();

	String getWorld();

	int getDirection();

	boolean getisWallSign();

	void setOwner(String owner);

	void setX(int x);

	void setY(int y);

	void setZ(int z);

	void setWorld(String world);

	void setDirection(int direction);

	void setisWallSign(boolean iswallsign);
}
