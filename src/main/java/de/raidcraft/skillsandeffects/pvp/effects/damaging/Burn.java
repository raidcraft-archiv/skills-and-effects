package de.raidcraft.skillsandeffects.pvp.effects.damaging;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EffectDamage;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.util.EffectUtil;
import org.bukkit.Location;
import org.bukkit.Sound;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Burn",
        description = "Verbrennt das Ziel",
        types = {EffectType.HARMFUL, EffectType.DAMAGING, EffectType.DEBUFF},
        elements = {EffectElement.FIRE}
)
public class Burn extends PeriodicExpirableEffect<Skill> {

    public Burn(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void apply(CharacterTemplate target) {

        Location location = target.getEntity().getLocation();
        location.getWorld().playSound(location, Sound.FIRE_IGNITE, 10F, 1F);
        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) {


    }

    @Override
    protected void renew(CharacterTemplate target) {

        EffectUtil.fakeParticles(EffectUtil.Particle.FLAME, target.getEntity().getLocation(), 10);
    }

    @Override
    protected void tick(CharacterTemplate target) {

        try {
            renew(target);
            new EffectDamage(this, getDamage()).run();
        } catch (CombatException e) {
            warn(e.getMessage());
        }
    }
}
