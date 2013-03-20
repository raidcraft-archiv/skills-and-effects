package de.raidcraft.skillsandeffects.pvp.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.MagicalAttack;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.PeriodicExpirableEffect;
import de.raidcraft.skills.api.effect.Stackable;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.magical.Flamestrike;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Flamestrike",
        description = "Verursacht Schaden mit jedem Stack.",
        types = {EffectType.DAMAGING, EffectType.HARMFUL, EffectType.DEBUFF},
        elements = {EffectElement.FIRE}
)
public class FlamestrikeEffect extends PeriodicExpirableEffect<Flamestrike> implements Stackable {

    private int maxStacks = 5;
    private int stacks = maxStacks;

    public FlamestrikeEffect(Flamestrike source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        maxStacks = data.getInt("max-stacks", 5);
    }

    @Override
    public double getPriority() {

        return super.getPriority() * getStacks();
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        if (stacks < 1) {
            remove();
            return;
        }
        stacks--;
        new MagicalAttack(getSource().getHero(), target, getDamage()).run();
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        this.stacks = maxStacks;
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        this.stacks = 0;
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        this.stacks = maxStacks;
    }

    @Override
    public int getStacks() {

        return stacks;
    }

    @Override
    public void setStacks(int stacks) {

        this.stacks = stacks;
    }

    @Override
    public int getMaxStacks() {

        return maxStacks;
    }
}
