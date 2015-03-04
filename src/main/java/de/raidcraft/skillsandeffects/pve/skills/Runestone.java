package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.tooltip.NameTooltip;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author mdoering
 */
@SkillInformation(
        name = "Runestone",
        description = "Ermöglicht es Runensteine zur Teleportation zu erstellen."
)
public class Runestone extends AbstractSkill implements CommandTriggered {

    public static final int RUNESTONE_ID = 2;
    public static final Pattern LOCATION_PATTERN = Pattern.compile("^([\\w\\d]+) - x:(\\d+) - y:(\\d+) - z:(\\d+)$");

    private String name;
    private ItemQuality quality;
    private int uses;

    public Runestone(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        name = data.getString("name", "Runenstein");
        uses = data.getInt("uses", 1);
        quality = ItemQuality.fromString(data.getString("quality", "COMMON"));
    }

    @Override
    public void runCommand(CommandContext commandContext) throws CombatException {

        Location location = getHolder().getPlayer().getLocation();
        ItemStack runestone = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemMeta meta = runestone.getItemMeta();
        meta.setDisplayName(new NameTooltip(RUNESTONE_ID, name, quality.getColor()).getTooltip()[0]);
        List<String> lore = new ArrayList<>();
        lore.add(new NameTooltip(uses, "", ChatColor.AQUA).getTooltip()[0]);
        lore.add(ChatColor.GRAY + location.getWorld().getName() + ChatColor.ITALIC +
                " - x:" + location.getBlockX() + " - y:" + location.getBlockY() + " - z:" + location.getBlockZ());
        lore.add(ChatColor.GREEN + "Benutzen: Teleportiert euch zu den Koordinaten an denen dieser Runenstein erstellt wurde." +
                " (" + uses + (uses > 1 ? " Aufladungen" : " Aufladung") + " verbleibend.)");
        meta.setLore(lore);
        runestone.setItemMeta(meta);
        InventoryUtils.addOrDropItems(getHolder().getPlayer(), runestone);
    }

    public static ItemStack updateLore(ItemStack runestone, int uses) {

        ItemMeta itemMeta = runestone.getItemMeta();
        List<String> lore = itemMeta.getLore();
        lore.set(0, new NameTooltip(uses, "", ChatColor.AQUA).getTooltip()[0]);
        lore.set(2, ChatColor.GREEN + "Benutzen: Teleportiert euch zu den Koordinaten an denen dieser Runenstein erstellt wurde." +
                " (" + uses + (uses > 1 ? " Aufladungen" : " Aufladung") + " verbleibend.)");
        itemMeta.setLore(lore);
        runestone.setItemMeta(itemMeta);
        return runestone;
    }
}
