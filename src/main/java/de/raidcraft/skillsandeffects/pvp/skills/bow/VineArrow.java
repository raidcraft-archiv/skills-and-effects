package de.raidcraft.skillsandeffects.pvp.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.blocks.BlockID;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.callback.LocationCallback;
import de.raidcraft.skills.api.effect.common.QueuedProjectile;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Vine Arrow",
        description = "Lässt eine Ranke am Einschlagsort entstehen.",
        types = {EffectType.PHYSICAL, EffectType.HELPFUL, EffectType.MOVEMENT}
)
public class VineArrow extends AbstractSkill implements CommandTriggered {

    private boolean persistant = false;
    private ConfigurationSection maxBlocks;
    private ConfigurationSection duration;
    private List<Block> affectedBlocks = new ArrayList<>();

    public VineArrow(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        persistant = data.getBoolean("persistant", false);
        maxBlocks = data.getConfigurationSection("max-blocks");
        duration = data.getConfigurationSection("duration");
    }

    public int getMaxBlocks() {

        return (int) ConfigUtil.getTotalValue(this, maxBlocks);
    }

    public long getDuration() {

        return (long) (ConfigUtil.getTotalValue(this, duration) * 20);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        clearVines();
        affectedBlocks.clear();
        addEffect(getHolder(), QueuedProjectile.class).addCallback(new LocationCallback() {
            @Override
            public void run(Location location) throws CombatException {

                int changedBlocks = 0;
                int maxBlocks = getMaxBlocks();
                Block block = location.getBlock();
                do {
                    block.setTypeId(BlockID.VINE, false);
                    block = block.getRelative(BlockFace.DOWN);
                    if (!persistant) {
                        affectedBlocks.add(block);
                    }
                } while ((maxBlocks < 1 || ++changedBlocks < maxBlocks) && block.getTypeId() == 0);
            }
        }, ProjectileType.ARROW);
        // clear the vines after the end of the duration
        Bukkit.getScheduler().runTaskLater(RaidCraft.getComponent(SkillsPlugin.class), new Runnable() {
            @Override
            public void run() {

                clearVines();
            }
        }, getDuration());
    }

    private void clearVines() {

        // remove the old vine
        if (!persistant) {
            for (Block block : affectedBlocks) {
                block.setTypeId(0, true);
            }
        }
    }
}
