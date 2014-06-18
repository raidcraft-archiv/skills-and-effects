package de.raidcraft.skillsandeffects.pvp.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.magical.LivingBomb;
import de.raidcraft.util.EffectUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Living Bomb",
        description = "Sprengt Gegner im Umkreis nach Ablauf der Zeit.",
        types = {EffectType.DAMAGING, EffectType.DEBUFF, EffectType.HARMFUL, EffectType.MAGICAL, EffectType.PURGEABLE},
        elements = {EffectElement.FIRE}
)
public class LivingBombEffect extends ExpirableEffect<LivingBomb> {

    private int blastRadius;
    private int hit = 0;

    public LivingBombEffect(LivingBomb source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    public void load(ConfigurationSection data) {

        blastRadius = data.getInt("blast-radius", 3);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        hit = 0;
        target.getEntity().setFireTicks((int) getDuration());
        target.getEntity().setNoDamageTicks((int) getDuration());
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        for (final CharacterTemplate victim : target.getNearbyTargets(blastRadius)) {

            if (!victim.equals(target)) {
                getSource().magicalAttack(victim, getDamage());
                hit++;
            }
        }
        if (hit > 0) {
            // launch the target high into the air
            target.getEntity().setVelocity(new Vector(0, 3 + hit, 0));
            EffectUtil.fakeExplosion(target.getEntity().getLocation());
        }
    }
}
