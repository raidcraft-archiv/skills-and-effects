package de.raidcraft.skillsandeffects.pvp.effects.buffs.generic;

import de.raidcraft.api.items.AttributeType;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Attribute;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.util.ConfigUtil;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;

@Data
public class AttributeBuff extends ExpirableEffect<Skill> {

    private String attribute;
    private double modifier;
    private boolean updateBase = true;
    private boolean updateCurrent = true;

    public AttributeBuff(Skill source, CharacterTemplate target, EffectData data) {
        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        setAttribute(data.getString("attribute"));
        setModifier(ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("amount")));
        setUpdateBase(data.getBoolean("update-base", true));
        setUpdateCurrent(data.getBoolean("update-current", true));
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {
        if (!(target instanceof Hero)) return;

        Attribute attribute = ((Hero) target).getAttribute(getAttribute());
        if (attribute == null) throw new CombatException("Unbekanntes attribut: " + getAttribute());

        combatLog(attribute.getFriendlyName() + " um " + (int) getModifier() + (getModifier() > 0 ? " erhöht." : " verringert."));
        if (isUpdateBase()) {
            attribute.updateBaseValue((int) getModifier(), isUpdateCurrent());
        } else {
            attribute.setCurrentValue((int) (attribute.getCurrentValue() + getModifier()));
        }
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {
        if (!(target instanceof Hero)) return;

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {
        if (!(target instanceof Hero)) return;

        Attribute attribute = ((Hero) target).getAttribute(getAttribute());
        if (attribute == null) throw new CombatException("Unbekanntes attribut: " + getAttribute());

        combatLog(attribute.getFriendlyName() + " um " + (int) getModifier() + (getModifier() < 0 ? " erhöht." : " verringert."));
        if (isUpdateBase()) {
            attribute.updateBaseValue((int) -getModifier(), isUpdateCurrent());
        } else {
            attribute.setCurrentValue((int) (attribute.getCurrentValue() - getModifier()));
        }
    }
}
