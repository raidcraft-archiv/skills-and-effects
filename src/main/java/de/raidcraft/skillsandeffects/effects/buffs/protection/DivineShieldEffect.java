package de.raidcraft.skillsandeffects.effects.buffs.protection;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.skills.protection.DivineShield;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Divine Shield",
        description = "Verhindert allen Schaden",
        types = {EffectType.BUFF, EffectType.PURGEABLE, EffectType.PROTECTION, EffectType.HELPFUL}
)
public class DivineShieldEffect extends ExpirableEffect<DivineShield> implements Triggered {

    public DivineShieldEffect(DivineShield source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOWEST)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        throw new CombatException(CombatException.Type.IMMUNE);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOWEST)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        throw new CombatException("Du kannst momentan keine Angriffe ausführen.");
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.YELLOW).withFlicker().build();

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
