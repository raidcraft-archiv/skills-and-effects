package de.raidcraft.skillsandeffects.pve.effects;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pve.skills.OreFinder;
import org.bukkit.block.Block;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Ore Finder",
        description = "Dein Gespür verrät dir ob Erze in der Nähe zu finden sind.",
        types = {EffectType.BUFF, EffectType.HELPFUL}
)
public class OreFinderEffect extends PeriodicEffect<OreFinder> {
    private int count;
    public OreFinderEffect(OreFinder source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {
        if(count > getSource().getMaxTime()) {
            info("Petrologie ist am Ende seiner Kraft");
            remove();
        }
        count++;
        int range = getSource().getTotalRange();
        if (!hasOre(getSource().getTotalRange(), target.getEntity().getLocation().getBlock())) {
            return;
        }
        info(getSource().getFindMessage());
        info("Petrologie ist am Ende seiner Kraft");
        getSource().substractUsageCost(new SkillAction(getSource()));
        remove();
    }

    private boolean hasOre(int range, final Block startBlock) {

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (getSource().getMatBlocks()
                            .contains(startBlock.getRelative(x, y, z).getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
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
