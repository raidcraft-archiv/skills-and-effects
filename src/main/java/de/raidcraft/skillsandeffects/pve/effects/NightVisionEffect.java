package de.raidcraft.skillsandeffects.pve.effects;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skillsandeffects.pve.skills.NightVision;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Night Vision",
        description = "Verleit dem Träger Nachtsicht.",
        types = {EffectType.MAGICAL, EffectType.PURGEABLE, EffectType.BUFF, EffectType.HELPFUL}
)
public class NightVisionEffect extends ExpirableEffect<NightVision> {

    private final PotionEffect effect;

    public NightVisionEffect(NightVision source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        this.effect = new PotionEffect(PotionEffectType.NIGHT_VISION, (int) getDuration(), 1, true);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        renew(target);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        target.getEntity().addPotionEffect(effect, true);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.getEntity().removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
}
