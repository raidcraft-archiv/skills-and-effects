package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.BlockUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Summon Block",
        description = "Beschwört einen Block an dem Ziel herauf.",
        types = {EffectType.HELPFUL}
)
public class SummonBlock extends AbstractSkill implements CommandTriggered {

    private int blockId;
    private short blockData;

    private Location lastBlock;

    public SummonBlock(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        Material block = ItemUtils.getItem(data.getString("block"));
        if (block == null) {
            RaidCraft.LOGGER.warning("Unknown block defined in the config of " + getName());
            return;
        }
        blockId = block.getId();
        blockData = ItemUtils.getItemData(data.getString("block"));
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (blockId == 0) {
            throw new CombatException("Unbekannter Block definitiert! Bitte melde dies als Bug an das Raid-Craft Team!");
        }
        if (lastBlock != null) {
            Block block = lastBlock.getBlock();
            if (block instanceof InventoryHolder) {
                // drop all items in the inventory
                for (ItemStack itemStack : ((InventoryHolder) block).getInventory().getContents()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                }
                ((InventoryHolder) block).getInventory().clear();
            }
            block.setTypeId(0, true);
            lastBlock = null;
            getHolder().sendMessage(ChatColor.RED + "Dein letzter beschworener Block wurde entfernt.");
        }
        // dont store the block directly because this will keep the chunk loaded
        Block block = findBlock(getTargetBlock().getBlock());
        block.setTypeIdAndData(blockId, (byte) blockData, true);
        lastBlock = block.getLocation();
    }

    private Block findBlock(Block block) throws CombatException {

        // search in a radius of 3 for a non solid block
        Block relative;
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 3; y++) {
                    relative = block.getRelative(x, y, z);
                    if (BlockUtil.TRANSPARENT_BLOCKS.contains((byte) relative.getTypeId())) {
                        return relative;
                    }
                    relative = block.getRelative(-x, y, -z);
                    if (BlockUtil.TRANSPARENT_BLOCKS.contains((byte) relative.getTypeId())) {
                        return relative;
                    }
                }
            }
        }
        throw new CombatException("Es gibt keinen freien Platz um den Block am Ziel zu platzieren!");
    }
}
