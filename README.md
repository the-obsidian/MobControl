# MobControl [![Build Status](https://travis-ci.org/the-obsidian/MobControl.svg?branch=master)](https://travis-ci.org/the-obsidian/MobControl)

Tools to enable moderation of our Minecraft servers

## Dependencies

* none

## Installation

1. Download the [latest release](https://github.com/the-obsidian/MobControl/releases) from GitHub
1. Add it to your `plugins` folder
1. Either run Bukkit/Spigot once to generate `MobControl/config.yml` or create it using the guide below.
1. All done!

## Configuration

MobControl has several options that can be configured in the `config.yml` file:

```yaml
age-cap:
  baby: 300
  breed: 300

buff:
  drops: 0
  shear-drops: 0
  disabled-items:
  - 329

limits:
  blaze: 4
  cave_spider: 4
  chicken: 4
  cow: 4
  creeper: 4
  enderman: 4
  ghast: 1
  guardian: 4
  mushroom_cow: 4
  ocelot: 4
  pig_zombie: 4
  pig: 4
  rabbit: 4
  sheepblack: 2
  sheepblue: 2
  sheepbrown: 2
  sheepcyan: 2
  sheepgray: 2
  sheepgreen: 2
  sheeplight_blue: 2
  sheeplime: 2
  sheepmagenta: 2
  sheeporange: 2
  sheeppink: 2
  sheeppurple: 2
  sheepred: 2
  sheepsilver: 2
  sheepwhite: 4
  sheepyellow: 2
  silverfish: 6
  skeleton: 4
  spider: 4
  wolf: 4
  zombie: 1

settings:
  buff-drops: 0
  debug: false
  limit-natural-spawn: false
  limit-spawner-spawn: false
  spawn-limited: []
```

## Permissions

* `MobControl.cull` - use the cull command
* `MobControl.check` - use the check command

## Commands

* `/cull` - clears excess mobs
* `/check` - checks a Mob's owner

## Features

* Limits mobs per chunk

## Upcoming Features

* Better mob XP drop adjustments
