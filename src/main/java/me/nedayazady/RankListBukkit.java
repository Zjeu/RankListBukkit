package me.nedayazady;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankListBukkit extends JavaPlugin implements CommandExecutor, TabCompleter {

    private LuckPerms luckPerms;
    private boolean hasPlaceholderAPI = false;

    // Config options
    private String defaultPrefix;
    private List<String> listFormat;
    private String playerFormat;
    private String playerDelimiter;
    private String moreFormat;
    private int maxDisplay;
    private String groupsHeader;
    private List<String> sortOrder;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigurationValues();

        // Setup LuckPerms
        setupLuckPerms();

        // Optional PlaceholderAPI Hook
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            hasPlaceholderAPI = true;
            getLogger().info("Successfully hooked into PlaceholderAPI.");
        }

        // Register command and tab completer
        PluginCommand listCommand = getCommand("list");
        if (listCommand != null) {
            listCommand.setExecutor(this);
            listCommand.setTabCompleter(this);
        } else {
            getLogger().severe("Command 'list' could not be found in plugin.yml!");
        }

        getLogger().info("RankListBukkit has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("RankListBukkit has been disabled.");
    }

    private void setupLuckPerms() {
        try {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
                getLogger().info("Hooked into LuckPerms service.");
                return;
            }
        } catch (NoClassDefFoundError ignored) {
        }

        try {
            luckPerms = LuckPermsProvider.get();
            getLogger().info("Hooked into LuckPerms via LuckPermsProvider.");
        } catch (IllegalStateException | NoClassDefFoundError e) {
            getLogger().warning("LuckPerms not found! Player ranks and prefixes will fall back to defaults.");
        }
    }

    public void loadConfigurationValues() {
        reloadConfig();
        FileConfiguration config = getConfig();

        defaultPrefix = config.getString("default-prefix", "&7[Default]");
        listFormat = config.getStringList("list-format");
        if (listFormat.isEmpty()) {
            listFormat = Arrays.asList("{header}", " ", "{players}");
        }

        playerFormat = config.getString("player-format", "{prefix} {name}");
        playerDelimiter = config.getString("player-delimiter", "&8, ");
        moreFormat = config.getString("more-format", "&7(+{more} more)");
        maxDisplay = config.getInt("max-display", 30);
        groupsHeader = config.getString("groups-header", "&cOwner&7, &4Manager&7, &cAdmin&7, &5Moderator&7, &9Helper&7, &aBuilder&7, &dPartner&7, &6Famous&7, &dMedia&7, &bACE&7, &bMVP&7, &bPRO&7, &bVIP&7, &7Default &8(&e{online}&8/&e{limit}&8)&8:");

        sortOrder = new ArrayList<>(config.getStringList("sort-order"));
        if (sortOrder.isEmpty()) {
            sortOrder = Arrays.asList(
                    "owner", "manager", "admin", "moderator", "helper", "builder",
                    "partner", "famous", "media", "ace", "mvp", "pro", "vip", "default"
            );
        }
        sortOrder.replaceAll(String::toLowerCase);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ranklist.reload")) {
                sender.sendMessage(color("&cYou do not have permission to execute this command."));
                return true;
            }

            loadConfigurationValues();
            if (luckPerms == null) {
                setupLuckPerms();
            }

            sender.sendMessage(color("&aRankList configuration reloaded successfully."));
            return true;
        }

        // Prepare player list
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        players.sort((p1, p2) -> {
            String group1 = getPrimaryGroup(p1);
            String group2 = getPrimaryGroup(p2);

            int index1 = sortOrder.indexOf(group1.toLowerCase());
            int index2 = sortOrder.indexOf(group2.toLowerCase());

            if (index1 == -1) index1 = Integer.MAX_VALUE;
            if (index2 == -1) index2 = Integer.MAX_VALUE;

            if (index1 != index2) {
                return Integer.compare(index1, index2);
            }

            return p1.getName().compareToIgnoreCase(p2.getName());
        });

        int online = players.size();
        int max = Bukkit.getMaxPlayers();

        List<String> formattedPlayers = new ArrayList<>();
        int count = 0;
        for (Player p : players) {
            if (count >= maxDisplay) {
                break;
            }
            formattedPlayers.add(formatPlayer(p));
            count++;
        }

        String playerListStr = String.join(color(playerDelimiter), formattedPlayers);

        if (online > maxDisplay) {
            int more = online - maxDisplay;
            String moreStr = moreFormat.replace("{more}", String.valueOf(more));
            playerListStr += " " + color(moreStr);
        }

        String header = groupsHeader.replace("{online}", String.valueOf(online))
                .replace("{limit}", String.valueOf(max));

        for (String line : listFormat) {
            line = line.replace("{header}", header)
                    .replace("{players}", playerListStr)
                    .replace("{limit}", String.valueOf(max))
                    .replace("{online}", String.valueOf(online));

            if (hasPlaceholderAPI && sender instanceof Player) {
                try {
                    line = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders((Player) sender, line);
                } catch (Throwable ignored) {
                }
            }

            sender.sendMessage(color(line));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("ranklist.reload")) {
                if ("reload".startsWith(args[0].toLowerCase())) {
                    return Collections.singletonList("reload");
                }
            }
        }
        return Collections.emptyList();
    }

    private String formatPlayer(Player player) {
        String format = playerFormat;
        String prefix = getPrefix(player);
        if (prefix == null || prefix.isEmpty() || prefix.equalsIgnoreCase("null")) {
            prefix = defaultPrefix;
        }

        format = format.replace("{prefix}", prefix)
                .replace("{name}", player.getName());

        if (hasPlaceholderAPI) {
            try {
                format = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, format);
            } catch (Throwable ignored) {
            }
        }

        return color(format);
    }

    private String getPrefix(Player player) {
        if (luckPerms == null) {
            setupLuckPerms();
        }
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null && !prefix.isEmpty() && !prefix.equalsIgnoreCase("null")) {
                    return prefix;
                }
            }
        }
        return defaultPrefix;
    }

    private String getPrimaryGroup(Player player) {
        if (luckPerms == null) {
            setupLuckPerms();
        }
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String group = user.getPrimaryGroup();
                if (group != null && !group.isEmpty()) {
                    return group;
                }
            }
        }
        return "default";
    }

    public static String color(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
