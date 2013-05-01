package de.raidcraft.skillsandeffects.pvp.skills.bow;

import de.raidcraft.skills.api.combat.EffectElement;
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
        name = "Fire Trap",
        description = "Lässt am Aufschlag des Pfeils eine Feuerfalle entstehen.",
        types = {EffectType.HARMFUL, EffectType.DAMAGING, EffectType.AREA},
        elements = {EffectElement.FIRE},
        queuedAttack = true
)
public class FireTrap extends AbstractBowTrap {

    public FireTrap(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    protected void runTrap(Location target) throws CombatException {

        Set<Block> blocks = BlockUtil.replaceNonSolidSurfaceBlocks(target.getBlock(), Material.FIRE, width, length, height);
        addEffect(getHolder(), TrapEffect.class).setChangedBlocks(blocks);
    }
}
