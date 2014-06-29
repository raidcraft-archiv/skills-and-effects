package de.raidcraft.skillsandeffects.pve.effects;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pve.skills.OreFinder;
import de.raidcraft.util.BlockUtil;
import org.bukkit.block.Block;

import java.util.Set;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Ore Finder",
        description = "Dein Gespür verrät dir ob Erze in der Nähe zu finden sind.",
        types = {EffectType.BUFF, EffectType.HELPFUL}
)
public class OreFinderEffect extends PeriodicEffect<OreFinder> {

    public OreFinderEffect(OreFinder source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        if (!getSource().canUseAbility()) {
            return;
        }
        Set<Block> blocks = BlockUtil.getBlocks(target.getEntity().getLocation().getBlock(),
                getSource().getTotalRange(), getSource().getBlockIds());
        if (blocks.isEmpty()) {
            return;
        }
        info(getSource().getFindMessage());
        getSource().substractUsageCost(new SkillAction(getSource()));
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {


    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {


    }
}
