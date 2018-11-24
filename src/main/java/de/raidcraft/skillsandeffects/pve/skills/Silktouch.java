package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Silktouch",
        description = "Ermöglicht es Blöcke ohne die Verzauberung Silktouch genauso abzubauen."
)
public class Silktouch extends AbstractSkill implements Triggered {

    private final List<Material> blocks = new ArrayList<>();
    private final ItemStack silkTouch = new ItemStack(Material.DIAMOND_PICKAXE);

    public Silktouch(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
        this.silkTouch.addEnchantment(Enchantment.SILK_TOUCH, 1);
    }

    @Override
    public void load(ConfigurationSection data) {

        for (String key : data.getStringList("blocks")) {
            Material item = ItemUtils.getItem(key);
            if (item == null) {
                RaidCraft.LOGGER.warning("Uknown item " + key + " in effect config of " + getName() + " for skill " + getName());
                continue;
            }
            blocks.add(item);
        }
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakTrigger trigger) {

        Block block = trigger.getEvent().getBlock();
        if (blocks.contains(block.getType())) {
            // simply break the block with a different tool and cancel the event
            block.breakNaturally(silkTouch);
            trigger.getEvent().setCancelled(true);
        }
    }
}
