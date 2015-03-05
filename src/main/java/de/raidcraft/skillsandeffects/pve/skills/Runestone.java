package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.tooltip.SingleLineTooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.tables.TRunestone;
import de.raidcraft.util.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
@SkillInformation(
        name = "Runestone",
        description = "Ermöglicht es Runensteine zur Teleportation zu erstellen."
)
public class Runestone extends AbstractSkill implements CommandTriggered {

    private CustomItem runestone;
    private int uses;

    public Runestone(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        try {
            runestone = RaidCraft.getCustomItem(data.getInt("item"));
            uses = data.getInt("uses", 1);
        } catch (CustomItemException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
    }

    @Override
    public void runCommand(CommandContext commandContext) throws CombatException {

        if (this.runestone == null) {
            throw new CombatException("Invalid custom item defined in " + getName());
        }
        Location location = getHolder().getPlayer().getLocation();
        CustomItemStack runestone = this.runestone.createNewItem();
        runestone.setTooltip(new SingleLineTooltip(TooltipSlot.MISC, "Welt: " + location.getWorld().getName() + " " +
                "- x: " + location.getBlockX() +
                "- y: " + location.getBlockY() +
                "- z: " + location.getBlockZ()
        ));
        TRunestone.createRunestone(runestone, uses, uses, location);
        InventoryUtils.addOrDropItems(getHolder().getPlayer(), runestone);
        getHolder().sendMessage(runestone.getItem().getName() + " wurde erfolgreich hergestellt.");
    }
}