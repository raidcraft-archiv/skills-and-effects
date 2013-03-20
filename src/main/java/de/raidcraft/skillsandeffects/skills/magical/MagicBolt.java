package de.raidcraft.skillsandeffects.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.MagicalAttackType;
import de.raidcraft.skills.api.combat.action.EntityAttack;
import de.raidcraft.skills.api.combat.callback.EntityAttackCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.effects.disabling.Disarm;
import de.raidcraft.skills.effects.disabling.Interrupt;
import de.raidcraft.skills.effects.disabling.KnockBack;
import de.raidcraft.skills.effects.disabling.Stun;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.effects.armor.SunderingArmor;
import de.raidcraft.skillsandeffects.effects.damaging.Bleed;
import de.raidcraft.skillsandeffects.effects.damaging.Burn;
import de.raidcraft.skillsandeffects.effects.damaging.Poison;
import de.raidcraft.skillsandeffects.effects.disabling.Pigify;
import de.raidcraft.skillsandeffects.effects.potion.Slow;
import de.raidcraft.skillsandeffects.effects.potion.Weakness;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Magic Bolt",
        desc = "Greift ein Ziel mit Magie an.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.HARMFUL, EffectType.DAMAGING}
)
public class MagicBolt extends AbstractSkill implements CommandTriggered, Triggered {

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

    public MagicBolt(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

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
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        magicalAttack(MagicalAttackType.SMOKE, new EntityAttackCallback() {
            @Override
            public void run(EntityAttack attack) throws CombatException {

                if (knockBack) MagicBolt.this.addEffect(getHero().getEntity().getLocation(), attack.getTarget(), KnockBack.class);
                if (bleed) MagicBolt.this.addEffect(attack.getTarget(), Bleed.class);
                if (stun) MagicBolt.this.addEffect(attack.getTarget(), Stun.class);
                if (sunderArmor) MagicBolt.this.addEffect(attack.getTarget(), SunderingArmor.class);
                if (disarm) MagicBolt.this.addEffect(attack.getTarget(), Disarm.class);
                if (slow) MagicBolt.this.addEffect(attack.getTarget(), Slow.class);
                if (weaken) MagicBolt.this.addEffect(attack.getTarget(), Weakness.class);
                if (burn) MagicBolt.this.addEffect(attack.getTarget(), Burn.class);
                if (interrupt) MagicBolt.this.addEffect(attack.getTarget(), Interrupt.class);
                if (disable) MagicBolt.this.addEffect(attack.getTarget(), Pigify.class);
                if (poison) MagicBolt.this.addEffect(attack.getTarget(), Poison.class);
            }
        });
    }
}
