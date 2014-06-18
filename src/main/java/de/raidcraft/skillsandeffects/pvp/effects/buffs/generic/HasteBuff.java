package de.raidcraft.skillsandeffects.pvp.effects.buffs.generic;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Haste Buff",
        description = "Erhöht die Bewegungsgeschwindigkeit.",
        types = {EffectType.HELPFUL, EffectType.MAGICAL, EffectType.BUFF, EffectType.PURGEABLE}
)
public class HasteBuff extends ExpirableEffect<Skill> {

    private final PotionEffect potionEffect;

    public HasteBuff(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        this.potionEffect = new PotionEffect(PotionEffectType.SPEED, (int) getDuration(), 3, true);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        target.getEntity().addPotionEffect(potionEffect);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.getEntity().removePotionEffect(PotionEffectType.SPEED);
    }
}
