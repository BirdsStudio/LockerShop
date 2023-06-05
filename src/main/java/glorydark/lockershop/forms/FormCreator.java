package glorydark.lockershop.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import glorydark.lockershop.MainClass;
import glorydark.lockershop.items.capeItem;
import glorydark.lockershop.items.particleItem;
import glorydark.lockershop.items.purchaseItem;
import glorydark.lockershop.items.skinItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormCreator {
    public static final HashMap<Player, HashMap<Integer, FormType>> UI_CACHE = new HashMap<>();

    public static void showFormWindow(Player player, FormWindow window, FormType guiType) {
        UI_CACHE.computeIfAbsent(player, i -> new HashMap<>()).put(player.showFormWindow(window), guiType);
    }

    public static void showMainMenu(Player player) {
        FormWindowSimple simple = new FormWindowSimple("皮肤商店", "");
        simple.addButton(new ElementButton("购买皮肤"));
        simple.addButton(new ElementButton("购买披风"));
        simple.addButton(new ElementButton("购买行走粒子"));
        simple.addButton(new ElementButton("我的皮肤"));
        simple.addButton(new ElementButton("我的披风"));
        simple.addButton(new ElementButton("我的行走粒子"));
        simple.addButton(new ElementButton("一键卸下"));
        simple.addButton(new ElementButton("解决服装未生效"));
        showFormWindow(player, simple, FormType.MainMenu);
    }

    public static void showPurchasableSkinsMenu(Player player, String category) {
        FormWindowSimple simple = new FormWindowSimple("购买皮肤(未拥有)", "");
        List<skinItem> stringList = MainClass.getShopSkinsList(player.getName(), category);
        if(stringList.size() == 0){
            simple.setContent("暂未有您未拥有的皮肤");
            simple.addButton(new ElementButton("返回"));
            showFormWindow(player, simple, FormType.FuckMenu);
            return;
        }
        for(skinItem skinItem: stringList){
            if(skinItem.getIconData() != null) {
                simple.addButton(new ElementButton(skinItem.getDisplayName(), skinItem.getIconData()));
            }else{
                simple.addButton(new ElementButton(skinItem.getDisplayName()));
            }
        }
        showFormWindow(player, simple, FormType.ShopMenuSkin);
    }

    public static void showPurchasableCapesMenu(Player player, String category) {
        FormWindowSimple simple = new FormWindowSimple("购买披风(未拥有)", "");
        List<capeItem> stringList = MainClass.getShopCapesList(player.getName(), category);
        if(stringList.size() == 0){
            simple.setContent("暂未有您未拥有的披风");
            simple.addButton(new ElementButton("返回"));
            showFormWindow(player, simple, FormType.FuckMenu);
            return;
        }
        for(capeItem capeItem: MainClass.getShopCapesList(player.getName(), category)){
            if(capeItem.getIconData() != null) {
                simple.addButton(new ElementButton(capeItem.getDisplayName(), capeItem.getIconData()));
            }else{
                simple.addButton(new ElementButton(capeItem.getDisplayName()));
            }
        }
        showFormWindow(player, simple, FormType.ShopMenuCape);
    }

    public static void showPurchasableParticlesMenu(Player player, String category) {
        FormWindowSimple simple = new FormWindowSimple("购买粒子(未拥有)", "");
        List<particleItem> stringList = MainClass.getShopParticlesList(player.getName(), category);
        if(stringList.size() == 0){
            simple.setContent("暂未有您未拥有的粒子");
            simple.addButton(new ElementButton("返回"));
            showFormWindow(player, simple, FormType.FuckMenu);
            return;
        }
        for(particleItem particleItem: MainClass.getShopParticlesList(player.getName(), category)){
            if(particleItem.getIconData() != null) {
                simple.addButton(new ElementButton(particleItem.getDisplayName(), particleItem.getIconData()));
            }else{
                simple.addButton(new ElementButton(particleItem.getDisplayName()));
            }
        }
        showFormWindow(player, simple, FormType.ShopMenuParticle);
    }

    public static void showCategorySelectMenu(Player player, FormType type){
        FormWindowSimple simple = new FormWindowSimple("我的皮肤 - 选择类别", "");
        switch (type){
            case BuySkinCategory:
                List<String> categories = new ArrayList<>(MainClass.configs.getSection("skin_category").getKeys(false));
                for(String category: categories){
                    ConfigSection section = MainClass.configs.getSection("skin_category").getSection(category);
                    simple.addButton(new ElementButton(section.getString("displayName")));
                }
                break;
            case BuyParticleCategory:
                categories = new ArrayList<>(MainClass.configs.getSection("particle_category").getKeys(false));
                for(String category: categories){
                    ConfigSection section = MainClass.configs.getSection("particle_category").getSection(category);
                    simple.addButton(new ElementButton(section.getString("displayName")));
                }
                break;
            case BuyCapeCategory:
                categories = new ArrayList<>(MainClass.configs.getSection("cape_category").getKeys(false));
                for(String category: categories){
                    ConfigSection section = MainClass.configs.getSection("cape_category").getSection(category);
                    simple.addButton(new ElementButton(section.getString("displayName")));
                }
                break;
        }
        showFormWindow(player, simple, type);
    }

    public static void showMySkinsMenu(Player player) {
        FormWindowSimple simple = new FormWindowSimple("我的皮肤", "");
        List<String> skins = MainClass.getUnlockedSkins(player.getName());
        if(skins.size() == 0){
            simple.setContent("您还没有任何一个皮肤！");
            simple.addButton(new ElementButton("返回"));
            showFormWindow(player, simple, FormType.FuckMenu);
            return;
        }
        for(String skinName: skins){
            skinItem skinItem = MainClass.skins.getOrDefault(skinName, null);
            if(skinItem == null){
                simple.addButton(new ElementButton(skinName+"\n§c[无法使用]"));
                continue;
            }
            if(skinItem.getIconData() != null) {
                simple.addButton(new ElementButton(skinItem.getDisplayName(), skinItem.getIconData()));
            }else{
                simple.addButton(new ElementButton(skinItem.getDisplayName()));
            }
        }
        showFormWindow(player, simple, FormType.MyMenuSkin);
    }

    public static void showMyCapesMenu(Player player) {
        FormWindowSimple simple = new FormWindowSimple("我的披风", "");
        List<String> skins = MainClass.getUnlockedCapes(player.getName());
        if(skins.size() == 0){
            simple.setContent("您还没有任何一个披风！");
            simple.addButton(new ElementButton("返回"));
            showFormWindow(player, simple, FormType.FuckMenu);
            return;
        }
        for(String capeName: skins){
            capeItem capeItem = MainClass.capes.getOrDefault(capeName, null);
            if(capeItem == null){
                simple.addButton(new ElementButton(capeName+"\n§c[无法使用]"));
                continue;
            }
            if(capeItem.getIconData() != null) {
                simple.addButton(new ElementButton(capeItem.getDisplayName(), capeItem.getIconData()));
            }else{
                simple.addButton(new ElementButton(capeItem.getDisplayName()));
            }
        }
        showFormWindow(player, simple, FormType.MyMenuCape);
    }

    public static void showMyParticlesMenu(Player player) {
        FormWindowSimple simple = new FormWindowSimple("我的粒子", "");
        List<String> particles = MainClass.getUnlockedParticles(player.getName());
        if(particles.size() == 0){
            simple.setContent("您还没有任何一个粒子！");
            simple.addButton(new ElementButton("返回"));
            showFormWindow(player, simple, FormType.FuckMenu);
            return;
        }
        for(String particle: particles){
            particleItem particleItem = MainClass.particles.getOrDefault(particle, null);
            if(particleItem == null){
                simple.addButton(new ElementButton(particle+"\n§c[无法使用]"));
                continue;
            }
            if(particleItem.getIconData() != null) {
                simple.addButton(new ElementButton(particleItem.getDisplayName(), particleItem.getIconData()));
            }else{
                simple.addButton(new ElementButton(particleItem.getDisplayName()));
            }
        }
        showFormWindow(player, simple, FormType.MyMenuParticle);
    }

    public static void showBuyMenu(Player player, purchaseItem item){
        FormWindowModal modal = null;
        if(item instanceof skinItem){
            modal = new FormWindowModal("购买皮肤 - 确认界面", "购买皮肤:"+item.getDisplayName()+"\n介绍:"+item.getDescription()+"\n价格:"+ item.getNeedMoney()+"\n使用时间"+(item.getDuration() == 0L? "永久": getTimeString(item.getDuration())), "购买", "返回");
        }else if(item instanceof capeItem) {
            modal = new FormWindowModal("购买披风 - 确认界面", "购买披风:"+item.getDisplayName()+"\n"+"介绍:"+item.getDescription()+"\n价格:"+ item.getNeedMoney()+"\n使用时间"+(item.getDuration() == 0L? "永久": getTimeString(item.getDuration())), "购买", "返回");
        }else if(item instanceof particleItem){
            modal = new FormWindowModal("购买粒子 - 确认界面", "购买粒子:"+item.getDisplayName()+"\n"+"介绍:"+item.getDescription()+"\n价格:"+ item.getNeedMoney()+"\n使用时间"+(item.getDuration() == 0L? "永久": getTimeString(item.getDuration())), "购买", "返回");
        }
        if(modal != null){
            showFormWindow(player, modal, FormType.BuyMenu);
        }
    }

    public static void showEquipMenu(Player player, purchaseItem item){
        FormWindowModal modal;
        if(item instanceof skinItem){
            if(MainClass.getDisplaySkin(player).equals(item.getName())){
                modal = new FormWindowModal("设置皮肤 - 确认界面", "设置皮肤:" + item.getDisplayName() + "\n介绍:" + item.getDescription() + "\n期限:" + new Config(MainClass.path+"/players/"+player.getName()+".yml").getSection("skins").getString(item.getName()+".dueString"), "卸下", "返回");
                showFormWindow(player, modal, FormType.UnEquipMenu);
            }else {
                modal = new FormWindowModal("设置皮肤 - 确认界面", "设置皮肤:" + item.getDisplayName() + "\n" + "介绍:" + item.getDescription() + "\n期限:" + new Config(MainClass.path+"/players/"+player.getName()+".yml").getSection("skins").getString(item.getName()+".dueString"), "装备", "返回");
                showFormWindow(player, modal, FormType.EquipMenu);
            }
        }else if (item instanceof capeItem) {
            if(MainClass.getDisplayCape(player).equals(item.getName())){
                modal = new FormWindowModal("设置披风 - 确认界面", "设置披风:"+item.getDisplayName()+"\n"+"介绍:"+item.getDescription() + "\n期限:" + new Config(MainClass.path+"/players/"+player.getName()+".yml").getSection("capes").getString(item.getName()+".dueString"), "卸下", "返回");
                showFormWindow(player, modal, FormType.UnEquipMenu);
            }else{
                modal = new FormWindowModal("设置披风 - 确认界面", "设置披风:"+item.getDisplayName()+"\n"+"介绍:"+item.getDescription() + "\n期限:" + new Config(MainClass.path+"/players/"+player.getName()+".yml").getSection("capes").getString(item.getName()+".dueString"), "装备", "返回");
                showFormWindow(player, modal, FormType.EquipMenu);
            }
        } else if (item instanceof particleItem) {
            if(MainClass.getDisplayCape(player).equals(item.getName())){
                modal = new FormWindowModal("设置粒子 - 确认界面", "设置粒子:"+item.getDisplayName()+"\n"+"介绍:"+item.getDescription() + "\n期限:" + new Config(MainClass.path+"/players/"+player.getName()+".yml").getSection("particles").getString(item.getName()+".dueString"), "卸下", "返回");
                showFormWindow(player, modal, FormType.UnEquipMenu);
            }else{
                modal = new FormWindowModal("设置粒子 - 确认界面", "设置粒子:"+item.getDisplayName()+"\n"+"介绍:"+item.getDescription() + "\n期限:" + new Config(MainClass.path+"/players/"+player.getName()+".yml").getSection("particles").getString(item.getName()+".dueString"), "装备", "返回");
                showFormWindow(player, modal, FormType.EquipMenu);
            }
        }
    }

    public static String getTimeString(long duration){
        if(duration >= 60000){
            if(duration >= 3600000){
                if(duration >= 86400000){
                    return duration/86400000+"天";
                }else{
                    return duration/3600000+"小时";
                }
            }else{
                return duration/60000+"分钟";
            }
        }else{
            return duration/1000+"秒";
        }
    }
}
