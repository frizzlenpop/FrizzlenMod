# FrizzlenMod

FrizzlenMod is a powerful, feature-rich moderation plugin for Minecraft 1.21 servers running Paper/Spigot. It provides server administrators with essential tools for effective server management and player moderation.

## Features

### Player Moderation
- **Ban Management**: Permanent and temporary bans
- **Mute System**: Control player chat privileges
- **Kick Command**: Remove disruptive players
- **Warning System**: Issue and track player warnings
- **Freeze System**: Immobilize players for investigation
- **Vanish Mode**: Invisible monitoring for staff
- **Jail System**: Designate and manage jail locations

### Chat Management
- **Chat Moderation**: Filter inappropriate content
- **Slow Mode**: Control chat spam
- **Chat Mute**: Disable server-wide chat
- **Chat Clear**: Clean up chat history

### Player Monitoring
- **Inventory Inspection**: View player inventories
- **Ender Chest Access**: Monitor ender chest contents
- **Player Reports**: Allow players to report rule violations
- **Moderation Logs**: Track all moderation actions

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/kick <player> <reason>` | Kicks a player | `frizzlenmod.kick` |
| `/ban <player> <reason>` | Bans a player | `frizzlenmod.ban` |
| `/tempban <player> <time> <reason>` | Temporarily bans a player | `frizzlenmod.tempban` |
| `/unban <player>` | Unbans a player | `frizzlenmod.unban` |
| `/mute <player>` | Mutes a player indefinitely | `frizzlenmod.mute` |
| `/tempmute <player> <time>` | Temporarily mutes a player | `frizzlenmod.tempmute` |
| `/unmute <player>` | Unmutes a player | `frizzlenmod.unmute` |
| `/warn <player> <reason>` | Warns a player | `frizzlenmod.warn` |
| `/freeze <player>` | Freezes a player in place | `frizzlenmod.freeze` |
| `/vanish` | Enables vanish mode | `frizzlenmod.vanish` |
| `/invsee <player>` | Views a player's inventory | `frizzlenmod.invsee` |
| `/endersee <player>` | Views a player's Ender Chest | `frizzlenmod.endersee` |
| `/chatmute` | Mutes the entire chat | `frizzlenmod.chatmute` |
| `/chatclear` | Clears the chat | `frizzlenmod.chatclear` |
| `/slowmode <seconds>` | Enables slow mode | `frizzlenmod.slowmode` |
| `/report <player> <reason>` | Reports a player | `frizzlenmod.report` |
| `/modlogs <player>` | Checks moderation history | `frizzlenmod.modlogs` |
| `/setjail <name>` | Sets a jail location | `frizzlenmod.setjail` |
| `/jail <player> <name> [time]` | Sends a player to jail | `frizzlenmod.jail` |
| `/unjail <player>` | Releases a player from jail | `frizzlenmod.unjail` |

## Installation

1. Download the latest version of FrizzlenMod from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in the generated `config.yml` file

## Configuration

The plugin creates several configuration files:

### config.yml
```yaml
mysql:
  enabled: false
  host: 'localhost'
  port: 3306
  database: 'frizzlenmod'
  username: 'root'
  password: 'password'

moderation:
  broadcast-punishments: true
  log-to-file: true
  default-temp-ban-duration: '24h'
  default-mute-duration: '1h'
```

## Storage Options

### YAML Storage (Default)
- Player data stored in `plugins/FrizzlenMod/players/<uuid>.yml`
- Jail locations stored in `plugins/FrizzlenMod/jails.yml`
- Plugin settings in `plugins/FrizzlenMod/config.yml`

### MySQL Storage (Optional)
- Enable in config.yml
- Stores all moderation data in a MySQL database
- Allows for cross-server synchronization

## Requirements

- Java 17 or higher
- Paper/Spigot/Bukkit 1.21+
- MySQL server (optional)

## Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/FrizzlenMod.git

# Navigate to the project directory
cd FrizzlenMod

# Build with Maven
mvn clean package
```

## Contributing

1. Fork the repository
2. Create a new branch for your feature
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## Support

For support:
- Create an issue on our GitHub repository
- Join our Discord server (link)
- Contact us via email at support@example.com

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Authors

- YourName - Initial work - [GitHub](https://github.com/yourusername)

## Acknowledgments

- Paper Team for the excellent server software
- Contributors who have helped improve the plugin
- Server administrators who provided valuable feedback

---
Made with ❤️ for the Minecraft community