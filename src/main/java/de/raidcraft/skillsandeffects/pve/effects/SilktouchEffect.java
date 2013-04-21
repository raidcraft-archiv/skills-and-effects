package de.raidcraft.skillsandeffects.pve.effects;

import com.sk89q.worldedit.blocks.ItemID;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.skillsandeffects.pve.skills.Silktouch;
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
@EffectInformation(
        name = "Silktouch",
        description = "Ermöglicht es Blöcke in ihrer Ursprungsform abzubauen."
)
public class SilktouchEffect extends ExpirableEffect<Silktouch> implements Triggered {

    private final List<Integer> blockIds = new ArrayList<>();
    private final ItemStack silkTouch = new ItemStack(ItemID.DIAMOND_PICKAXE);

    public SilktouchEffect(Silktouch source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        this.silkTouch.addEnchantment(Enchantment.SILK_TOUCH, 1);
    }

    @Override
    public void load(ConfigurationSection data) {

        for (String key : data.getStringList("blocks")) {
            Material item = ItemUtils.getItem(key);
            if (item == null) {
                RaidCraft.LOGGER.warning("Uknown item " + key + " in effect config of " + getName() + " for skill " + getSource().getName());
                continue;
            }
            blockIds.add(item.getId());
        }
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakTrigger trigger) {

        Block block = trigger.getEvent().getBlock();
        if (blockIds.contains(block.getTypeId())) {
            // simply break the block with a different tool and cancel the event
            block.breakNaturally(silkTouch);
            trigger.getEvent().setCancelled(true);
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
