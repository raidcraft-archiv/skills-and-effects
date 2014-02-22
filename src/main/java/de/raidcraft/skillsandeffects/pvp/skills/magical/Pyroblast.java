package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.debuff.FlamestrikeEffect;
import de.raidcraft.util.LocationUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Pyroblast",
        description = "Grosser Feuerball der mehr Schaden für jeden Flammenschlag Stack macht.",
        types = {EffectType.DAMAGING, EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.HARMFUL},
        elements = {EffectElement.FIRE}
)
public class Pyroblast extends AbstractSkill implements CommandTriggered {

    private ConfigurationSection damagePerStack;

    public Pyroblast(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        damagePerStack = data.getConfigurationSection("stack-damage");
    }

    public double getStackDamage() {

        return ConfigUtil.getTotalValue(this, damagePerStack);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        rangedAttack(ProjectileType.LARGE_FIREBALL, new RangedCallback() {
            @Override
            public void run(CharacterTemplate target) throws CombatException {

                if (LocationUtil.isSafeZone(target.getEntity().getLocation())) {
                    throw new CombatException(CombatException.Type.INVALID_TARGET);
                }
                if (target.hasEffect(FlamestrikeEffect.class)) {
                    int stacks = target.getEffect(FlamestrikeEffect.class).getStacks();
                    magicalAttack(target, (int) (stacks * getStackDamage()));
                    target.removeEffect(FlamestrikeEffect.class);
                }
            }
        });
    }
}
