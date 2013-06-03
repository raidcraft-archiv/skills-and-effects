package de.raidcraft.skillsandeffects.pvp.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.blocks.BlockID;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.LocationCallback;
import de.raidcraft.skills.api.effect.common.QueuedRangedAttack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Vine Arrow",
        description = "Lässt eine Ranke am Einschlagsort entstehen.",
        types = {EffectType.PHYSICAL, EffectType.HELPFUL, EffectType.MOVEMENT}
)
public class VineArrow extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection maxBlocks;

    public VineArrow(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        maxBlocks = data.getConfigurationSection("max-blocks");
    }

    public int getMaxBlocks() {

        return (int) ConfigUtil.getTotalValue(this, maxBlocks);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void runCommand(CommandContext args) throws CombatException {

        QueuedRangedAttack<LocationCallback> attack = addEffect(getHolder(), QueuedRangedAttack.class);
        attack.addCallback(new LocationCallback() {
            @Override
            public void run(Location location) throws CombatException {

                int changedBlocks = 0;
                int maxBlocks = getMaxBlocks();
                Block block = location.getBlock();
                do {
                    block.setTypeId(BlockID.VINE, false);
                    block = block.getRelative(BlockFace.DOWN);
                } while ((maxBlocks < 1 || ++changedBlocks < maxBlocks) && block.getTypeId() == 0);
            }
        });
    }
}
