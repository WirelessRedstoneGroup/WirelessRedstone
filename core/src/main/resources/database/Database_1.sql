DROP TABLE IF EXISTS [transmitter];
DROP TABLE IF EXISTS [receiver];
DROP TABLE IF EXISTS [screen];
DROP TABLE IF EXISTS [inverter];
DROP TABLE IF EXISTS [delayer];
DROP TABLE IF EXISTS [switch];
DROP TABLE IF EXISTS [clock];
DROP TABLE IF EXISTS [owner];
DROP TABLE IF EXISTS [channel];

CREATE TABLE [channel](
  [name] VARCHAR(255) NOT NULL,
  [locked] INTEGER NOT NULL,
  PRIMARY KEY (name)
);
CREATE TABLE [owner](
  [channel_name] VARCHAR(255) NOT NULL,
  [user] INTEGER NOT NULL,
  PRIMARY KEY(channel_name, user),
  FOREIGN KEY(channel_name) REFERENCES channel(name)
);
CREATE TABLE [transmitter](
  [x] INTEGER NOT NULL,
  [y] INTEGER NOT NULL,
  [z] INTEGER NOT NULL,
  [world] VARCHAR(255) NOT NULL,
  [channel_name] VARCHAR(255) NOT NULL,
  [direction] VARCHAR(255) NOT NULL,
  [owner] VARCHAR(255) NOT NULL,
  [is_wallsign] INTEGER DEFAULT 0,
  PRIMARY KEY (x, y, z, world),
  FOREIGN KEY(channel_name) REFERENCES channel(name)
);
CREATE TABLE [receiver](
  [x] INTEGER NOT NULL,
  [y] INTEGER NOT NULL,
  [z] INTEGER NOT NULL,
  [world] VARCHAR(255) NOT NULL,
  [channel_name] VARCHAR(255) NOT NULL,
  [direction] VARCHAR(255) NOT NULL,
  [owner] VARCHAR(255) NOT NULL,
  [is_wallsign] INTEGER DEFAULT 0,
  PRIMARY KEY (x, y, z, world),
  FOREIGN KEY(channel_name) REFERENCES channel(name)
);
CREATE TABLE [screen](
  [x] INTEGER NOT NULL,
  [y] INTEGER NOT NULL,
  [z] INTEGER NOT NULL,
  [world] VARCHAR(255) NOT NULL,
  [channel_name] VARCHAR(255) NOT NULL,
  [direction] VARCHAR(255) NOT NULL,
  [owner] VARCHAR(255) NOT NULL,
  [is_wallsign] INTEGER DEFAULT 0,
  PRIMARY KEY (x, y, z, world),
  FOREIGN KEY(channel_name) REFERENCES channel(name)
);
CREATE TABLE [inverter](
  [x] INTEGER NOT NULL,
  [y] INTEGER NOT NULL,
  [z] INTEGER NOT NULL,
  [world] VARCHAR(255) NOT NULL,
  [channel_name] VARCHAR(255) NOT NULL,
  [direction] VARCHAR(255) NOT NULL,
  [owner] VARCHAR(255) NOT NULL,
  [is_wallsign] INTEGER DEFAULT 0,
  PRIMARY KEY (x, y, z, world),
  FOREIGN KEY(channel_name) REFERENCES channel(name)
);
CREATE TABLE [delayer](
  [x] INTEGER NOT NULL,
  [y] INTEGER NOT NULL,
  [z] INTEGER NOT NULL,
  [world] VARCHAR(255) NOT NULL,
  [channel_name] VARCHAR(255) NOT NULL,
  [direction] VARCHAR(255) NOT NULL,
  [owner] VARCHAR(255) NOT NULL,
  [is_wallsign] INTEGER DEFAULT 0,
  [delay] INTEGER DEFAULT 1000,
  PRIMARY KEY (x, y, z, world),
  FOREIGN KEY(channel_name) REFERENCES channel(name),
  CHECK (delay >= 50)
);
CREATE TABLE [switch](
  [x] INTEGER NOT NULL,
  [y] INTEGER NOT NULL,
  [z] INTEGER NOT NULL,
  [world] VARCHAR(255) NOT NULL,
  [channel_name] VARCHAR(255) NOT NULL,
  [direction] VARCHAR(255) NOT NULL,
  [owner] VARCHAR(255) NOT NULL,
  [is_wallsign] INTEGER DEFAULT 0,
  [powered] INTEGER DEFAULT 0,
  PRIMARY KEY (x, y, z, world),
  FOREIGN KEY(channel_name) REFERENCES channel(name)
);
CREATE TABLE [clock](
  [x] INTEGER NOT NULL,
  [y] INTEGER NOT NULL,
  [z] INTEGER NOT NULL,
  [world] VARCHAR(255) NOT NULL,
  [channel_name] VARCHAR(255) NOT NULL,
  [direction] VARCHAR(255) NOT NULL,
  [owner] VARCHAR(255) NOT NULL,
  [is_wallsign] INTEGER DEFAULT 0,
  [delay] INTEGER DEFAULT 1000,
  PRIMARY KEY (x, y, z, world),
  FOREIGN KEY(channel_name) REFERENCES channel(name),
  CHECK (delay >= 50)
);
