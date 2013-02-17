package de.raidcraft.skillsandeffects.skills.potion.splash;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.common.QueuedSplashPotion;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skillsandeffects.effects.potion.Blind;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PotionSplashTrigger;
import de.raidcraft.skills.util.HeroUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Smoke Bomb",
        desc = "Hüllt alle am Aufschlagsort in Rausch ein und blendet diese.",
        types = {EffectType.HARMFUL, EffectType.DEBUFF}
)
public class SmokeBomb extends AbstractLevelableSkill implements CommandTriggered {

    public SmokeBomb(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Potion potion = new Potion(PotionType.SLOWNESS);
        potion.setSplash(true);
        ItemStack item = potion.toItemStack(1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setDisplayName("Rauchbombe");
        item.setItemMeta(meta);

        getHero().getPlayer().getInventory().addItem(item);

        addEffect(getHero(), QueuedSplashPotion.class).addCallback(new Callback<PotionSplashTrigger>() {
            @Override
            public void run(PotionSplashTrigger trigger) throws CombatException {

                for (CharacterTemplate character : HeroUtil.toCharacters(trigger.getEvent().getAffectedEntities())) {

                    SmokeBomb.this.addEffect(character, Blind.class);
                }
            }
        });
    }
}
