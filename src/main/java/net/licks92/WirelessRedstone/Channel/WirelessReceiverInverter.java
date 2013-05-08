package net.licks92.WirelessRedstone.Channel;

import java.util.Map;

import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("WirelessReceiverInverter")
public class WirelessReceiverInverter extends WirelessReceiver implements IWirelessPoint
{
	private static final long serialVersionUID = 1362491648424270188L;
	
	public WirelessReceiverInverter()
	{
		super();
	}
	
	public WirelessReceiverInverter(Map<String, Object> map)
	{
		super(map);
	}
	
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
