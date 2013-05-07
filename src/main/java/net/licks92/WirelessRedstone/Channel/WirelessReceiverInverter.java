package net.licks92.WirelessRedstone.Channel;

import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("WirelessReceiverInverter")
public class WirelessReceiverInverter extends WirelessReceiver
{
	private static final long serialVersionUID = 1362491648424270188L;
	
	@Override
	public void turnOn(String channelName)
	{
		super.turnOff(channelName);
	}
	
	@Override
	public void turnOff(String channelName)
	{
		super.turnOn(channelName);
	}
}
