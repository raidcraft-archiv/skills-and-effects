package de.raidcraft.skillsandeffects.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.PhysicalAttack;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.damaging.Bleed;
import de.raidcraft.skills.effects.disabling.KnockBack;
import de.raidcraft.skills.tables.THeroSkill;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "strike",
        desc = "Schlägt das Ziel mit voller Wucht und stößt es zurück.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL, EffectType.INTERRUPT},
        triggerCombat = true
)
public class Strike extends AbstractLevelableSkill implements CommandTriggered {

    private boolean knockBack = true;
    private boolean bleed = false;

    public Strike(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        knockBack = data.getBoolean("knockback", true);
        bleed = data.getBoolean("bleed", false);
    }

    @Override
    public void apply() {}

    @Override
    public void remove() {}

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        new PhysicalAttack(getHero(), getTarget(), getTotalDamage(), new Callback() {
            @Override
            public void run(final CharacterTemplate target) throws CombatException {

                if (knockBack) addEffect(getHero().getEntity().getLocation(), target, KnockBack.class);
                if (bleed) addEffect(target, Bleed.class);
            }
        }).run();
    }
}
