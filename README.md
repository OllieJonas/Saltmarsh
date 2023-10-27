# Saltmarsh

*A Discord bot which adds additional support for Discord Events*

![Ghosts of Saltmarsh](https://images.ctfassets.net/swt2dsco9mfe/4exp7HNbV2V929Nqj2Yjlz/3f632de545dda2031ef76c1ecaaa66fe/1023x550-saltmarsh.jpg)
(Artwork and name are taken from the [Ghosts of Saltmarsh](https://dnd.wizards.com/products/ghosts-saltmarsh) Dungeons & Dragons campaign)

## Features
- Automatically updating notifications for Discord Events
- Repeating Discord Events
- Polls with support for up to 10 options
- "Wizards" (Step-based surveys that execute some action using input collected over the steps upon completion)

## Installation
This bot makes a significant number of calls to the Discord API(mainly due to the more complicated features it offers,
namely Wizards), and therefore I have no intention of hosting this bot publicly.
However, it isn't too complicated to host it yourself!

### Docker-Compose
You can simply run this bot using `docker compose up`.


### Environment Variables

| Name                    | Required? | Type    | Default |
|-------------------------|-----------|---------|---------|
| SALTMARSH_DISCORD_TOKEN | Yes       | String  |         |
| DEVELOPER_MODE          | No        | Boolean | True    |
