# Saltmarsh

*A Discord bot which adds additional support for Discord Events*

![Ghosts of Saltmarsh](https://images.ctfassets.net/swt2dsco9mfe/4exp7HNbV2V929Nqj2Yjlz/3f632de545dda2031ef76c1ecaaa66fe/1023x550-saltmarsh.jpg)
(Artwork and name are taken from the [Ghosts of Saltmarsh](https://dnd.wizards.com/products/ghosts-saltmarsh) Dungeons & Dragons campaign)

## Features
- Automatically updating notifications for Discord Events
- Repeating Discord Events
- Music playback from YouTube and Spotify ([Lavaplayer](https://github.com/sedmelluq/lavaplayer) for YouTube, a [Java wrapper](https://github.com/spotify-web-api-java/spotify-web-api-java) for [Spotify's API](https://developer.spotify.com/documentation/web-api) for Spotify)
- Polls (including support for Anonymous and Singular / Multiple Votes)
- Adds support for a home-brewed version of [Kingdom](http://www.scottymakesgames.com/scotty-talks-games/2016/10/12/mtg-kingdoms) for the card game [Magic: the Gathering](https://magic.wizards.com/en).
- "Wizards" (Step-based surveys that execute some action using input collected over the steps upon completion, allows for the specification of types upfront and performs casting / type checking of user input automatically).

## Motivation

Starting off aimed as a replacement for larger music bots, this bot is now primarily designed to provide extra functionality to the Discord events system for usage amongst friends online (who have varying degrees of experience with Discord).

## Installation
This bot makes a significant number of calls to the Discord API (mainly due to the more complicated features it offers,
namely Wizards), and therefore I have no intention of hosting this bot publicly.
However, it isn't too complicated to host it yourself!

### Without Docker
You can build the project using Maven (for example, running `mvn clean package` in the root directory), then run it using `java -jar <outputted-jar-name>.jar`.

To enable support for scheduled event messages, recurring events and polls to persist when starting / stopping the bot, 
you need to [host a MySQL server](https://www.prisma.io/dataguide/mysql/setting-up-a-local-mysql-database).

### Docker-Compose
You can simply run this bot using `docker compose up (--build) (--detach)`.
To specify environment variables, create a file called `env_file` in the root directory of the project.

### Environment Variables

The project makes use of environment variables. A summary of them can be found below.

Having a MySQL database will allow for scheduled event messages, recurring events and polls to persist when restarting the bot.

Specifying a Spotify Client ID and Secret will enable Spotify support for playing music.

| Name                    | Required? | Type    | Default   |
|-------------------------|-----------|---------|-----------|
| SALTMARSH_DISCORD_TOKEN | Yes       | String  |           |
| DEVELOPER_MODE          | No        | Boolean | False     |
| MYSQL_USER              | No        | String  |           |
| MYSQL_PASSWORD          | No        | String  |           |
| MYSQL_HOST              | No        | String  | localhost |
| SPOTIFY_CLIENT_ID       | No        | String  |           |
| SPOTIFY_CLIENT_SECRET   | No        | String  |           |
