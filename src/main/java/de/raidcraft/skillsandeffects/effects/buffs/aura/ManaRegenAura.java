package de.raidcraft.skillsandeffects.effects.buffs.aura;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.skills.buffs.Aura;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Mana Regain Aura",
        description = "Erhöht deine Mana Regeneration",
        types = {EffectType.AURA, EffectType.MAGICAL, EffectType.BUFF, EffectType.HELPFUL},
        priority = 1.0
)
public class ManaRegenAura extends AbstractAura {

    public static final String RESOURCE_NAME = "mana";

    private ConfigurationSection regen;
    private double oldManaRegen;

    public ManaRegenAura(Aura source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        regen = data.getConfigurationSection("mana-regain");
    }

    private double getManaIncrease() {

        return ConfigUtil.getTotalValue(getSource(), regen);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        Hero hero = getSource().getHero();
        oldManaRegen = getSource().getProfession().getResource(RESOURCE_NAME).getRegenPercent();
        double newManaRegen = oldManaRegen + oldManaRegen * getManaIncrease();
        getSource().getProfession().getResource(RESOURCE_NAME).setRegenPercent(newManaRegen);
        super.apply(target);
        hero.combatLog(this, "Mana Regeneration auf " + (int) (newManaRegen * 100) + "% erhöht.");
        hero.debug("increased mana regain " + oldManaRegen + "->" + newManaRegen + " - " + getName());
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        Hero hero = getSource().getHero();
        getSource().getProfession().getResource(RESOURCE_NAME).setRegenPercent(oldManaRegen);
        super.remove(target);
        hero.combatLog(this, "Mana Regeneration auf " + (int) (oldManaRegen * 100) + "% verringert.");
        hero.debug("resetted mana regain to default: " + oldManaRegen + " - " + getName());
    }
}
