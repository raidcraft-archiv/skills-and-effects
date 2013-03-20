package de.raidcraft.skillsandeffects.pvp.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EntityAttack;
import de.raidcraft.skills.api.combat.action.MagicalAttack;
import de.raidcraft.skills.api.combat.callback.EntityAttackCallback;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pvp.skills.magical.LivingBomb;
import de.raidcraft.util.EffectUtil;
import org.bukkit.Location;
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
    public void load(ConfigurationSection data) {

        blastRadius = data.getInt("blast-radius", 3);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        for (final CharacterTemplate victim : target.getNearbyTargets(blastRadius)) {

            if (!victim.isFriendly(getSource().getHero())) {
                target.damage(new MagicalAttack(getSource().getHero(), target, getDamage(), new EntityAttackCallback() {
                    @Override
                    public void run(EntityAttack attack) throws CombatException {

                        // create an explosion that does not damage
                        Location location = attack.getTarget().getEntity().getLocation();
                        EffectUtil.fakeExplosion(location);
                        // and launch the target into the air
                        attack.getTarget().getEntity().setVelocity(new Vector(0, 1, 0));
                        hit++;
                    }
                }));
            }
        }
        if (hit > 0) {
            // launch the target high into the air
            target.getEntity().setVelocity(new Vector(0, 3, 0));
        }
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        hit = 0;
        target.getEntity().setFireTicks((int) getDuration());
        target.getEntity().setNoDamageTicks((int) getDuration());
    }
}
