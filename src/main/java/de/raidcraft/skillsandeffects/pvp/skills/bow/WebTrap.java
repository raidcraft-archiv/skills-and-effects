package de.raidcraft.skillsandeffects.pvp.skills.bow;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.misc.TrapEffect;
import de.raidcraft.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Web Trap",
        description = "Lässt beim Aufprall des Pfeils Spinnennetze entstehen.",
        types = {EffectType.MOVEMENT, EffectType.HARMFUL},
        queuedAttack = true
)
public class WebTrap extends AbstractBowTrap {

    public WebTrap(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    protected void runTrap(Location target) throws CombatException {

        Set<Block> blocks = BlockUtil.replaceNonSolidSurfaceBlocks(target.getBlock(), Material.WEB, width, length, height, true);
        addEffect(getHolder(), TrapEffect.class).setChangedBlocks(blocks);
    }
}
