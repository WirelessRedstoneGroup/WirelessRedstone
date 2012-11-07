package net.licks92.WirelessRedstone;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class WRLogger
{
	private Logger logger;
	private String prefix;
	private boolean debug;
	public static final String MINECRAFT_LOGGER = "Minecraft";

	public WRLogger(String prefix, boolean debug)
	{
		this.logger = Bukkit.getLogger();
		this.prefix = prefix;
		this.debug = debug;
	}
	
	public void debug(String msg)
	{
		if(debug)
			logger.info("[WRDebug] " + msg);
	}

	public void config(String msg)
	{
		logger.config(prefix + " " + msg);
	}

	public void fine(String msg)
	{
		logger.fine(prefix + " " + msg);
	}

	public void finer(String msg)
	{
		logger.finer(prefix + " " + msg);
	}

	public void finest(String msg)
	{
		logger.finest(prefix + " " + msg);
	}

	public void info(String msg)
	{
		logger.info(prefix + " " + msg);
	}

	public void severe(String msg)
	{
		logger.severe(prefix + " " + msg);
	}

	public void warning(String msg)
	{
		logger.warning(prefix + " " + msg);
	}
	
	public void setLogLevel(Level level)
	{
		logger.setLevel(level);
	}
}