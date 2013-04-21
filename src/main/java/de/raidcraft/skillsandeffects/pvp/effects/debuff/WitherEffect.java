package de.raidcraft.skillsandeffects.pvp.effects.debuff;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EntityAttack;
import de.raidcraft.skills.api.combat.action.MagicalAttack;
import de.raidcraft.skills.api.combat.callback.EntityAttackCallback;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Wither",
        description = "Der standard Minecraft Wither Effekt.",
        types = {EffectType.MAGICAL, EffectType.DEBUFF, EffectType.HARMFUL, EffectType.DAMAGING, EffectType.PURGEABLE},
        elements = {EffectElement.DARK}
)
public class WitherEffect extends PeriodicExpirableEffect<Skill> {

    private EntityAttackCallback tickCallback;
    private RangedCallback deathCallback;
    private PotionEffect witherEffect;

    public WitherEffect(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        this.witherEffect = new PotionEffect(PotionEffectType.WITHER, (int) getDuration(), 0, false);
    }

    public void setTickCallback(EntityAttackCallback tickCallback) {

        this.tickCallback = tickCallback;
    }

    public void setDeathCallback(RangedCallback deathCallback) {

        this.deathCallback = deathCallback;
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        new MagicalAttack(getSource().getHero(), target, getDamage(), new EntityAttackCallback() {

            @Override
            public void run(EntityAttack attack) throws CombatException {

                if (attack.isCancelled() || attack.getDamage() <= 0) {
                    return;
                }
                if (attack.getTarget().getHealth() - attack.getDamage() <= 0) {
                    // target will die
                    if (deathCallback != null) {
                        deathCallback.run(attack.getTarget());
                    }
                } else {
                    if (tickCallback != null) {
                        tickCallback.run(attack);
                    }
                }
            }
        }).run();
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.getEntity().removePotionEffect(PotionEffectType.WITHER);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        target.getEntity().addPotionEffect(witherEffect);
    }
}
