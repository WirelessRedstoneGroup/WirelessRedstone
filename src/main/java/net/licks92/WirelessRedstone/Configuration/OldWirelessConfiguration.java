package net.licks92.WirelessRedstone.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.naming.ConfigurationException;

import net.licks92.WirelessRedstone.StackableLogger;
import net.licks92.WirelessRedstone.WirelessRedstone;
import net.licks92.WirelessRedstone.channel.WirelessChannel;
import net.licks92.WirelessRedstone.channel.WirelessReceiver;
import net.licks92.WirelessRedstone.channel.WirelessTransmitter;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

public class OldWirelessConfiguration extends FileConfiguration
{
	private StackableLogger logger;
	private File dataFolder;
	private File configFile;
	public Yaml yaml;
	protected HashMap<String, Object> root;
	private String header = "#This is the configuration of the Wireless Redstone Plugin, Please don't edit it if you don't know what to do...";

	public OldWirelessConfiguration(File dataFolder)
	{
		this.configFile = new File(dataFolder, "settings.yml");
		this.dataFolder = dataFolder;
		logger = WirelessRedstone.getStackableLogger();
		root = new HashMap<String, Object>();

		Constructor constructor = new Constructor();
		Tag taggy = new Tag("!WirelessChannel");
		constructor.addTypeDescription(new TypeDescription(WirelessChannel.class, taggy));
		Representer representer = new Representer();
		representer.addClassTag(net.licks92.WirelessRedstone.channel.WirelessChannel.class, taggy);
		DumperOptions options = new DumperOptions();
		options.setIndent(4);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(constructor, representer, options);
		load();
	}
	
	@SuppressWarnings("unchecked")
	public Object get(String path)
	{
		if (!path.contains("."))
		{
			Object val = root.get(path);
			
			if (val == null)
			{
				return null;
			}
			return val;
		}
		String[] parts = path.split("\\.");
		Map<String, Object> node = root;
		
		for (int i = 0; i < parts.length; i++)
		{
			Object o = node.get(parts[i]);
			if (o == null)
			{
				return null;
			}
			if (i == parts.length - 1)
			{
				return o;
			}
			try
			{
				node = (Map<String, Object>) o;
			}
			catch (ClassCastException e)
			{
				return null;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void set(String path, Object value)
	{
		/**
		 * This will set the the path value as value. If value is null, it will remove path from the config.
		 */
		if (!path.contains("."))
		{
			if(value != null)
			{
				root.put(path, value);
			}
			else
			{
				root.remove(path);
			}
			return;
		}
		
		String[] parts = path.split("\\.");
		Map<String, Object> node = root;
		
		for (int i = 0; i < parts.length; i++)
		{
			Object o = node.get(parts[i]);
			// Found our target!
			if (i == parts.length - 1)
			{
				if(value != null)
				{
					node.put(parts[i], value);
				}
				else
				{
					node.remove(parts[i]);
				}
				return;
			}
		
			if (o == null || !(o instanceof Map))
			{
				// This will override existing configuration data!
				o = new HashMap<String, Object>();
				if(value != null)
				{
					node.put(parts[i], value);
				}
				else
				{
					node.remove(parts[i]);
				}
			}
		node = (Map<String, Object>) o;
		}
	}

	public void load()
	{
		this.configFile = configFile.getAbsoluteFile();
		if (!configFile.getParentFile().exists()) {
			if (!configFile.getParentFile().mkdirs()) {
				logger.severe("Error creating folders for Configuration.");
			}
		}
		if (!configFile.exists())
		{
			try
			{
				File oldconf = new File(dataFolder, "config.yml");
				if (oldconf.exists())
				{
					oldconf.renameTo(configFile);
					this.load();
					return;
				}
				logger.info("Creating empty config file. Loading default settings.");
				if (!configFile.createNewFile()) {
					logger.severe("Failed to create config file.");
				}
				GenerateDefaults();
			} catch (IOException ex) {
				logger.severe("Failed to create config file.");
			}
		}
		if(configFile.toString() == null)
		{
			GenerateDefaults();
		}

		if (this.get("WirelessObjects") != null)
		{
			ConvertOldToNew();
		}
		
		FileInputStream stream = null;
		
        try
        {
            stream = new FileInputStream(configFile);
            read(yaml.load(new UnicodeReader(stream)));
        }
        catch (IOException e)
        {
            root = new HashMap<String, Object>();
        }
        catch (ConfigurationException e)
        {
            root = new HashMap<String, Object>();
        }
        finally
        {
        	
            try
            {
                if (stream != null)
                {
                    stream.close();
                }
            }
            catch (IOException e)
            {
            	
            }
        }
	}

	@SuppressWarnings("unchecked")
	private boolean ConvertOldToNew()
	{
		this.logger.info("Converting old configuration to new configuration...");
		Object oldObject = this.get("WirelessObjects");
		Hashtable<String, WirelessChannel> newconf = new Hashtable<String, WirelessChannel>();
		if (oldObject instanceof Map<?, ?>) {
			Map<String, Object> oldchannelist = (Map<String, Object>) yaml.load(this.getString("WirelessObjects"));

			for (Map.Entry<String, Object> oldchannel : oldchannelist.entrySet())
			{
				WirelessChannel channel = new WirelessChannel();
				channel.setName(oldchannel.getKey());
				this.logger.info("Channel: " + oldchannel.getKey());

				if (!(oldchannel.getValue() instanceof Map)) {
					return false;
				}

				Map<Object, Object> signpoints = (Map<Object, Object>) oldchannel.getValue();
				for (Map.Entry<Object, Object> signpoint : signpoints.entrySet()) {
					this.logger.info("Found type:" + signpoint.getKey());
					if (signpoint.getKey().toString()
							.startsWith("Transmitters")) {
						if (signpoint.getValue() instanceof String) {
							this.logger.info("Found Transmitter with thingies:"
									+ signpoint.getValue());
							WirelessTransmitter newtransmitter = new WirelessTransmitter();
							String[] args = signpoint.getValue().toString()
									.split(",");
							newtransmitter.setWorld(args[3]);
							channel.addOwner(args[4]);
							newtransmitter.setOwner(args[4]);
							newtransmitter.setX(Integer.parseInt(args[0]));
							newtransmitter.setY(Integer.parseInt(args[1]));
							newtransmitter.setZ(Integer.parseInt(args[2]));
							channel.addTransmitter(newtransmitter);
						}
						for (String data : (List<String>) signpoint.getValue()) {
							this.logger.info("Found Transmitter with thingies:"
									+ data);
							WirelessTransmitter newtransmitter = new WirelessTransmitter();
							String[] args = data.split(",");
							newtransmitter.setWorld(args[3]);
							channel.addOwner(args[4]);
							newtransmitter.setOwner(args[4]);
							newtransmitter.setX(Integer.parseInt(args[0]));
							newtransmitter.setY(Integer.parseInt(args[1]));
							newtransmitter.setZ(Integer.parseInt(args[2]));
							channel.addTransmitter(newtransmitter);
						}
					} else if (signpoint.getKey().toString()
							.startsWith("Receivers")) {
						if (signpoint.getValue() instanceof String) {
							this.logger.info("Found Receiver with thingies:"
									+ signpoint.getValue());
							WirelessReceiver newreceiver = new WirelessReceiver();
							String[] args = signpoint.getValue().toString()
									.split(",");
							newreceiver.setWorld(args[3]);
							channel.addOwner(args[4]);
							newreceiver.setOwner(args[4]);
							newreceiver.setX(Integer.parseInt(args[0]));
							newreceiver.setY(Integer.parseInt(args[1]));
							newreceiver.setZ(Integer.parseInt(args[2]));
							channel.addReceiver(newreceiver);
						} else {
							for (String data : (List<String>) signpoint.getValue())
							{
								this.logger.info("Found Receiver with thingies:" + data);
								WirelessReceiver newreceiver = new WirelessReceiver();
								String[] args = data.split(",");
								newreceiver.setWorld(args[3]);
								channel.addOwner(args[4]);
								newreceiver.setOwner(args[4]);
								newreceiver.setX(Integer.parseInt(args[0]));
								newreceiver.setY(Integer.parseInt(args[1]));
								newreceiver.setZ(Integer.parseInt(args[2]));
								channel.addReceiver(newreceiver);
							}
						}
					}
				}
				newconf.put(channel.getName(), channel);
			}
		}

		this.set("WirelessChannels", newconf);
		this.set("WirelessObjects", null);
		this.set("AllowedWorlds", null);
		GenerateDefaults();
		this.save();
		return false;
	}
	
	public boolean save()
	{
		this.getRoot().set(this.getRoot().toString(), root);
		this.save(root);
		return true;
	}

	private void GenerateDefaults()
	{
		this.set("LogLevel", Level.INFO.getName().toUpperCase());
		this.set("cancelChunkUnloads", true);
		this.set("cancelChunkUnloadRange", 4);
		this.save();
	}

	public boolean isCancelChunkUnloads()
	{
		return this.getBoolean("cancelChunkUnloads", true);
	}

	public int getChunkUnloadRange()
	{
		return this.getInt("cancelChunkUnloadRange", 4);
	}

	public Level getLogLevel()
	{
		return Level.parse(this.get("LogLevel").toString());
	}

    /**
     * Set the header for the file as a series of lines that are terminated
     * by a new line sequence.
     * 
     * @param headerLines header lines to prepend
     */
    public void setHeader(String ... headerLines)
    {
        StringBuilder header = new StringBuilder();
        
        for (String line : headerLines) {
            if (header.length() > 0) {
                header.append("\r\n");
            }
            header.append(line);
        }
        
        setHeader(header.toString());
    }
    
    /**
     * Set the header for the file. A header can be provided to prepend the
     * YAML data output on configuration save. The header is 
     * printed raw and so must be manually commented if used. A new line will
     * be appended after the header, however, if a header is provided.
     * 
     * @param header header to prepend
     */
    public void setHeader(String header)
    {
        this.header = header;
    }
    
    /**
     * Return the set header.
     * 
     * @return
     */
    public String getHeader()
    {
        return header;
    }

    /**
     * Saves the configuration to disk. All errors are clobbered. 
     * 
     * @param header header to prepend
     * @return true if it was successful
     */
    public boolean save(Map<String, Object> root)
    {
        FileOutputStream stream = null;

        File parent = configFile.getParentFile();

        if (parent != null)
        {
            parent.mkdirs();
        }

        try
        {
            stream = new FileOutputStream(configFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
            if (header != null)
            {
                writer.append(header);
                writer.append("\r\n");
            }
            yaml.dump(root, writer);
            return true;
        }
        catch (IOException e)
        {}
        finally
        {
            try
            {
                if (stream != null)
                {
                    stream.close();
                }
            } catch (IOException e) {}
        }

        return false;
    }
    
    @SuppressWarnings("unchecked")
	private void read(Object input) throws ConfigurationException
    {
        try
        {
            root = (HashMap<String, Object>) input;
            this.set("", root);
        }
        catch (ClassCastException e)
        {
            throw new ConfigurationException("Root document must be an key-value structure");
        }
    }

	@Override
	protected String buildHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadFromString(String contents)
			throws InvalidConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String saveToString() {
		// TODO Auto-generated method stub
		return null;
	}
}
