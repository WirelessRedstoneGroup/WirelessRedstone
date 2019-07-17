### Status
[![Build Status](https://travis-ci.org/WirelessRedstoneGroup/WirelessRedstone.svg?branch=master)](https://travis-ci.org/WirelessRedstoneGroup/WirelessRedstone)

## WirelessRedstone
Welcome on the official page of the Wireless Redstone plugin. [Spigot page.](https://www.spigotmc.org/resources/wirelessredstone.8251/)

### Translation
Do you want to see the plugin in your favourite language / native language? 
It will be possible in the next version. 
The process is very simple for you and me, you just have to go on [this website](https://www.transifex.com/bart0110/wirelessredstone/), and fill the language of your choice.

## Config

| Key                         | Description                                                                                                                                                                                                                                                                                                                                                  |
|-------------------------    |----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------    |
| ConfigVersion               | You should NOT change this value yourself!                                                                                                                                                                                                                                                                                                                   |
| Language                    | Change the language of the plugin. It falls back to English if the language is not found. For more info see the Translate section.                                                                                                                                                                                                                           |
| ColourfulLogging            | Add colours to logging.                                                                                                                                                                                                                                                                                                                                      |
| CheckForUpdates             | Get a message when an update is available.                                                                                                                                                                                                                                                                                                                    |
| Metrics                     | Enable anonymous metrics.                                                                                                                                                                                                                                                                                                                                    |
| SilentMode                  | Disable most of the feedback if the user doesn't have permissions.                                                                                                                                                                                                                                                                                           |
| DebugMode                   | Print more information to the console.                                                                                                                                                                                                                                                                                                                       |
| DropSignWhenBroken          | Drop a sign item if a WirelessChannel is destroyed.                                                                                                                                                                                                                                                                                                          |
| InteractTransmitterTime     | Amount of time (in milliseconds) a WirelessChannel will be active if there's an interaction with a transmitter.                                                                                                                                                                                                                                              |
| CacheRefreshFrequency       | Frequency (in seconds) of refreshing the database. You should leave this to the default value.                                                                                                                                                                                                                                                               |
| gateLogic                   | The logic of the transmitters;  OR: If one of the transmitters is powered the channel will be activated. All transmitters must be off to power the channel down.  IGNORE: If one of the transmitters is powered the channel will be activated. If a transmitter is no longer powered the channel will be deactivated ignoring other transmitters.            |
| saveOption                  | Save WirelessRedstone data in YML or SQLITE.                                                                                                                                                                                                                                                                                                                 |

## License

WirelessRedstone is released under the [GPLv3](LICENSE).

```
Copyright (C) 2016  WirelessRedstoneGroup

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see http://www.gnu.org/licenses.
```

### Used licenses

Gson is released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

```
Copyright 2008 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.