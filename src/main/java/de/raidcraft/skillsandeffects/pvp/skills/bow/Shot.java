package de.raidcraft.skillsandeffects.pvp.skills.bow;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.effect.common.QueuedRangedAttack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.Bleed;
import de.raidcraft.skills.effects.Burn;
import de.raidcraft.skills.effects.disabling.Disarm;
import de.raidcraft.skills.effects.disabling.KnockBack;
import de.raidcraft.skills.effects.disabling.Silence;
import de.raidcraft.skills.effects.disabling.Stun;
import de.raidcraft.skills.effects.Slow;
import de.raidcraft.skills.effects.Weakness;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.api.effect.common.SunderingArmor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shot",
        description = "Schiesst einen Pfeil mit extra Fähigkeiten ab.",
        types = {EffectType.DAMAGING, EffectType.PHYSICAL, EffectType.HARMFUL},
        queuedAttack = true,
        configUsage = {
            "knockback[bool]",
            "bleed[bool]",
            "stun[bool]",
            "sunder-armor[bool]",
            "disarm[bool]",
            "slow[bool]",
            "weaken[bool]",
            "burn[bool]",
            "heal[bool]",
            "silence[bool]"
        },
        effects = {
            KnockBack.class,
            Bleed.class,
            Stun.class,
            SunderingArmor.class,
            Disarm.class,
            Slow.class,
            Weakness.class,
            Burn.class,
            Silence.class
        }
)
public class Shot extends AbstractLevelableSkill implements CommandTriggered {

    private boolean knockBack = false;
    private boolean bleed = false;
    private boolean stun = false;
    private boolean sunderArmor = false;
    private boolean disarm = false;
    private boolean slow = false;
    private boolean weaken = false;
    private boolean burn = false;
    private boolean heal = false;
    private boolean silence = false;

    public Shot(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        knockBack = data.getBoolean("knockback", false);
        bleed = data.getBoolean("bleed", false);
        stun = data.getBoolean("stun", false);
        sunderArmor = data.getBoolean("sunder-armor", false);
        disarm = data.getBoolean("disarm", false);
        slow = data.getBoolean("slow", false);
        weaken = data.getBoolean("weaken", false);
        burn = data.getBoolean("burn", false);
        heal = data.getBoolean("heal", false);
        silence = data.getBoolean("silence", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(QueuedRangedAttack.class).addCallback(new RangedCallback() {
            @Override
            public void run(CharacterTemplate target) throws CombatException {

                if (knockBack) Shot.this.addEffect(getHolder().getEntity().getLocation(), target, KnockBack.class);
                if (bleed) Shot.this.addEffect(target, Bleed.class);
                if (stun) Shot.this.addEffect(target, Stun.class);
                if (sunderArmor) Shot.this.addEffect(target, SunderingArmor.class);
                if (disarm) Shot.this.addEffect(target, Disarm.class);
                if (slow) Shot.this.addEffect(target, Slow.class);
                if (weaken) Shot.this.addEffect(target, Weakness.class);
                if (burn) Shot.this.addEffect(target, Burn.class);
                if (silence) Shot.this.addEffect(target, Silence.class);
                if (heal && target.isFriendly(getHolder())) {
                    new HealAction<>(Shot.this, target, getTotalDamage()).run();
                }
            }
        });
    }
}