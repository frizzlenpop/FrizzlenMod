# FrizzlenMod

FrizzlenMod is a comprehensive moderation plugin for Minecraft servers with a web-based admin panel.

## Features

- **Player Management**: Ban, mute, warn, and kick players with reason tracking
- **Temporary Punishments**: Set expirations for bans and mutes
- **Appeal System**: Allow players to appeal bans through the web panel
- **Moderation Logs**: Keep track of all moderation actions
- **Jail System**: Create jail locations to temporarily hold rule breakers
- **Freeze System**: Freeze players in place during investigations
- **Vanish Mode**: Admins can become invisible to players
- **Chat Management**: Mute chat, clear chat, and enable slow mode
- **Web Admin Panel**: Manage all aspects of server moderation via a web interface
- **User Management**: Create and manage admin accounts with different permission levels

## Installation

### Prerequisites

- Java 8 or higher
- Bukkit/Spigot/Paper server 1.16 or higher

### Plugin Installation

1. Download the latest release from the [releases page](https://github.com/frizzlenpop/frizzlenMod/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Edit the configuration file in `plugins/FrizzlenMod/config.yml` to your liking
5. Restart your server again

### Web Panel Setup

1. The web panel is embedded in the plugin and will automatically start when the plugin loads
2. By default, the web panel runs on port 8080. You can change this in the config.yml file
3. Access the web panel by navigating to `http://your-server-ip:8080` in your web browser
4. Log in with the default admin credentials (see below)

#### Default Admin Credentials

- Username: `admin`
- Password: `admin`

**IMPORTANT**: Change the default password immediately after your first login!

### Security Considerations

- The web panel uses JWT for authentication. Make sure to change the default JWT secret in the config.yml file
- Consider setting up a reverse proxy (like Nginx or Apache) for HTTPS and additional security
- If exposing the web panel to the internet, consider using a firewall to restrict access

## Configuration

The main configuration file is located at `plugins/FrizzlenMod/config.yml`. Here you can configure:

- Web panel port and JWT secret
- Warning thresholds for automatic punishments
- Data save intervals
- Appeal system settings
- Chat management settings
- And more...

Example configuration:

```yaml
general:
  save-interval: 5  # in minutes

web-api:
  enabled: true
  port: 8080
  jwt-secret: "change-this-to-a-secure-random-string"
  cors-enabled: false

warnings:
  mute-threshold: 3
  kick-threshold: 5
  temp-ban-threshold: 7
  ban-threshold: 10
  temp-ban-duration: "1d"

appeals:
  enabled: true
  cooldown: 7  # days between appeals
```

## Commands & Permissions

### Punishment Commands

| Command | Description | Usage | Permission |
|---------|-------------|-------|------------|
| /ban | Permanently ban a player | /ban \<player\> [reason] | frizzlenmod.ban |
| /tempban | Temporarily ban a player | /tempban \<player\> \<time\> [reason] | frizzlenmod.tempban |
| /unban | Unban a player | /unban \<player\> | frizzlenmod.unban |
| /mute | Permanently mute a player | /mute \<player\> [reason] | frizzlenmod.mute |
| /tempmute | Temporarily mute a player | /tempmute \<player\> \<time\> [reason] | frizzlenmod.tempmute |
| /unmute | Unmute a player | /unmute \<player\> | frizzlenmod.unmute |
| /warn | Warn a player | /warn \<player\> [reason] | frizzlenmod.warn |
| /clearwarnings | Clear warnings for a player | /clearwarnings \<player\> | frizzlenmod.clearwarnings |
| /kick | Kick a player | /kick \<player\> [reason] | frizzlenmod.kick |

### Management Commands

| Command | Description | Usage | Permission |
|---------|-------------|-------|------------|
| /freeze | Freeze a player in place | /freeze \<player\> | frizzlenmod.freeze |
| /unfreeze | Unfreeze a player | /unfreeze \<player\> | frizzlenmod.unfreeze |
| /vanish | Toggle vanish mode | /vanish | frizzlenmod.vanish |
| /setjail | Set a jail location | /setjail \<number\> | frizzlenmod.setjail |
| /jail | Send a player to jail | /jail \<player\> \<number\> [time] | frizzlenmod.jail |
| /unjail | Release a player from jail | /unjail \<player\> | frizzlenmod.unjail |

### Chat Commands

| Command | Description | Usage | Permission |
|---------|-------------|-------|------------|
| /chatmute | Toggle global chat mute | /chatmute | frizzlenmod.chatmute |
| /chatclear | Clear the chat | /chatclear | frizzlenmod.chatclear |
| /slowmode | Set chat slow mode | /slowmode \<seconds\> | frizzlenmod.slowmode |

### Information Commands

| Command | Description | Usage | Permission |
|---------|-------------|-------|------------|
| /modlogs | View moderation logs | /modlogs \<player\> [page] | frizzlenmod.modlogs |
| /report | Report a player | /report \<player\> \<reason\> | frizzlenmod.report |
| /invsee | View player inventory | /invsee \<player\> | frizzlenmod.invsee |
| /endersee | View player enderchest | /endersee \<player\> | frizzlenmod.endersee |

### Permission Groups

| Permission | Description | Included Permissions |
|------------|-------------|----------------------|
| frizzlenmod.admin | All administrative permissions | All plugin permissions |
| frizzlenmod.mod | Basic moderation permissions | kick, tempmute, unmute, warn, freeze, vanish, invsee, chatclear, modlogs |
| frizzlenmod.report | Player report permission | Just report command (default: true) |

## Web Panel

The web panel provides a user-friendly interface for managing server moderation. Here are the main sections and features:

### Dashboard

- Overview of server statistics
- Recent moderation activities
- Quick action buttons for common tasks

### Punishments Management

- View and manage all active punishments
- Add new punishments through a user-friendly interface
- Remove or modify existing punishments
- Search and filter punishments

### Appeals System

- Review pending ban appeals
- Approve or deny appeals with comments
- View appeal history and details
- Track appeal status changes

### Moderation Logs

- Complete history of all moderation actions
- Filter logs by player, action type, or date range
- Export logs for record keeping

### User Management

- Create and manage admin accounts
- Set permission levels for staff members
- Reset passwords and manage account status

## Setting Up the Frontend and Backend

### Backend Setup

1. **Server Requirements**:
   - Java 8 or higher
   - Bukkit/Spigot/Paper server (1.16+)
   - At least 512MB of RAM dedicated to the plugin

2. **Installation**:
   - Place the plugin JAR in your plugins folder
   - Restart your server to generate configuration files
   - Edit the config.yml file to change any settings
   - Restart again for changes to take effect

3. **Network Configuration**:
   - The web API runs on port 8080 by default
   - Ensure this port is open in your firewall if accessing from outside the server
   - Consider setting up a reverse proxy for HTTPS support

4. **Database Configuration**:
   - FrizzlenMod uses file-based storage by default
   - No external database setup is required
   - Data is stored in the plugin's data folder

### Frontend Setup

1. **Accessing the Web Panel**:
   - The web panel files are automatically installed with the plugin
   - Access via http://your-server-ip:8080
   - Login with default credentials (admin/admin)

2. **Custom Frontend Development** (Optional):
   - Web files are located in plugins/FrizzlenMod/web/
   - You can modify these files to customize the interface
   - After modifications, restart the server or reload the plugin

3. **HTTPS and Security**:
   - Consider setting up Nginx or Apache as a reverse proxy
   - Configure HTTPS for secure connections
   - Example Nginx configuration:
   
   ```
   server {
       listen 443 ssl;
       server_name your-domain.com;
       
       ssl_certificate /path/to/cert.pem;
       ssl_certificate_key /path/to/key.pem;
       
       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

## Troubleshooting

### Common Issues

1. **Web panel not accessible**:
   - Check if the correct port is configured and open in your firewall
   - Verify the plugin started successfully in the server logs
   - Try restarting your server

2. **Login issues**:
   - If you've forgotten the admin password, you can reset it by editing the users.yml file
   - Check the server logs for authentication errors

3. **Permission issues**:
   - Verify permission nodes are correctly assigned in your permissions plugin
   - Check command syntax in the console logs

4. **Data not saving**:
   - Ensure the plugin has write access to its data directory
   - Check for errors in the server logs related to file operations

## Support and Community

For support, bug reports, and feature requests, please join our Discord server:
[Join the FrizzlenMod Discord](https://discord.com/invite/uAsvGGGZU8)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

Developed by FrizzlenPop