package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.EntityAttack;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.combat.callback.EntityAttackCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.disabling.Disarm;
import de.raidcraft.skills.effects.disabling.Interrupt;
import de.raidcraft.skills.effects.disabling.KnockBack;
import de.raidcraft.skills.effects.disabling.Stun;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skills.api.effect.common.SunderingArmor;
import de.raidcraft.skills.effects.Bleed;
import de.raidcraft.skills.effects.Burn;
import de.raidcraft.skills.effects.Poison;
import de.raidcraft.skillsandeffects.pvp.effects.disabling.Pigify;
import de.raidcraft.skills.effects.Slow;
import de.raidcraft.skills.effects.Weakness;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Area Cast",
        description = "Standard Attacke um den Zauberer herum.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.HARMFUL, EffectType.AREA}
)
public class AreaCast extends AbstractSkill implements CommandTriggered {

    private boolean knockBack = false;
    private boolean bleed = false;
    private boolean stun = false;
    private boolean sunderArmor = false;
    private boolean disarm = false;
    private boolean slow = false;
    private boolean weaken = false;
    private boolean burn = false;
    private boolean interrupt = false;
    private boolean disable = false;
    private boolean poison = false;
    private boolean isLifeLeech = false;
    private ConfigurationSection lifeLeech;

    public AreaCast(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        addElements(EffectElement.fromString(data.getString("element")));
        knockBack = data.getBoolean("knockback", false);
        bleed = data.getBoolean("bleed", false);
        stun = data.getBoolean("stun", false);
        sunderArmor = data.getBoolean("sunder-armor", false);
        disarm = data.getBoolean("disarm", false);
        slow = data.getBoolean("slow", false);
        weaken = data.getBoolean("weaken", false);
        burn = data.getBoolean("burn", false);
        interrupt = data.getBoolean("interrupt", false);
        disable = data.getBoolean("disable", false);
        poison = data.getBoolean("poison", false);
        lifeLeech = data.getConfigurationSection("life-leech");
        isLifeLeech = lifeLeech != null;
    }

    private double getLifeLeechPercentage() {

        if (!isLifeLeech) {
            return 0.0;
        }
        return ConfigUtil.getTotalValue(this, lifeLeech);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        for (CharacterTemplate target : getSafeNearbyTargets()) {

            magicalAttack(target, new EntityAttackCallback() {
                @Override
                public void run(EntityAttack attack) throws CombatException {

                    if (knockBack) AreaCast.this.addEffect(getHolder().getEntity().getLocation(), attack.getTarget(), KnockBack.class);
                    if (bleed) AreaCast.this.addEffect(attack.getTarget(), Bleed.class);
                    if (stun) AreaCast.this.addEffect(attack.getTarget(), Stun.class);
                    if (sunderArmor) AreaCast.this.addEffect(attack.getTarget(), SunderingArmor.class);
                    if (disarm) AreaCast.this.addEffect(attack.getTarget(), Disarm.class);
                    if (slow) AreaCast.this.addEffect(attack.getTarget(), Slow.class);
                    if (weaken) AreaCast.this.addEffect(attack.getTarget(), Weakness.class);
                    if (burn) AreaCast.this.addEffect(attack.getTarget(), Burn.class);
                    if (interrupt) AreaCast.this.addEffect(attack.getTarget(), Interrupt.class);
                    if (disable) AreaCast.this.addEffect(attack.getTarget(), Pigify.class);
                    if (poison) AreaCast.this.addEffect(attack.getTarget(), Poison.class);
                    if (isLifeLeech) {
                        new HealAction<>(this, getHolder(), (int) (attack.getDamage() * getLifeLeechPercentage())).run();
                    }
                }
            }).run();
        }
    }
}
