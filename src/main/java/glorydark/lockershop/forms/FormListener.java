package glorydark.lockershop.forms;


import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import glorydark.lockershop.MainClass;
import glorydark.lockershop.items.capeItem;
import glorydark.lockershop.items.particleItem;
import glorydark.lockershop.items.purchaseItem;
import glorydark.lockershop.items.skinItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FormListener implements Listener {

    public static HashMap<Player, purchaseItem> playerPurchaseItemHashMap = new HashMap<>();
    public static HashMap<Player, purchaseItem> playerEquipItemHashMap = new HashMap<>();

    public static HashMap<Player, String> selectedCategory = new HashMap<>();

    @EventHandler
    public void PlayerFormRespondedEvent(PlayerFormRespondedEvent event) {
        Player p = event.getPlayer();
        FormWindow window = event.getWindow();
        if (p == null || window == null) {
            return;
        }
        FormType guiType = FormCreator.UI_CACHE.containsKey(p) ? FormCreator.UI_CACHE.get(p).get(event.getFormID()) : null;
        if(guiType == null){
            return;
        }
        FormCreator.UI_CACHE.get(p).remove(event.getFormID());
        if (event.getResponse() == null) {
            return;
        }
        if (event.getWindow() instanceof FormWindowSimple) {
            this.onSimpleClick(p, (FormResponseSimple) event.getResponse(), guiType);
        }
        if (event.getWindow() instanceof FormWindowModal){
            this.onModalClick(p, (FormResponseModal) event.getResponse(), guiType);
        }
    }

    public void onSimpleClick(Player p, FormResponseSimple simple, FormType type) {
        switch (type){
            case MainMenu:
                switch (simple.getClickedButtonId()){
                    case 0:
                        FormCreator.showCategorySelectMenu(p, FormType.BuySkinCategory);
                        break;
                    case 1:
                        FormCreator.showCategorySelectMenu(p, FormType.BuyCapeCategory);
                        break;
                    case 2:
                        FormCreator.showCategorySelectMenu(p, FormType.BuyParticleCategory);
                        break;
                    case 3:
                        FormCreator.showMySkinsMenu(p);
                        break;
                    case 4:
                        FormCreator.showMyCapesMenu(p);
                        break;
                    case 5:
                        FormCreator.showMyParticlesMenu(p);
                        break;
                    case 6:
                        if(MainClass.players.getOrDefault(p, 0L) > System.currentTimeMillis()){
                            p.sendMessage("§c§l[提示] 请不要过快刷新皮肤，每次刷新有5秒冷却时间！");
                        }else {
                            MainClass.setDisplayCape(p.getName(), "");
                            MainClass.setDisplaySkin(p.getName(), "");
                            MainClass.players.put(p, System.currentTimeMillis() + 5000);
                            p.setSkin(MainClass.defaultSkin);
                            p.sendMessage("§a[提示] 一键卸下成功！");
                        }
                        break;
                    case 7:
                        if(MainClass.players.getOrDefault(p, 0L) > System.currentTimeMillis()) {
                            p.sendMessage("§c§l[提示] 请不要过快刷新皮肤，每次刷新有5秒冷却时间！");
                        }else {
                            Server.getInstance().updatePlayerListData(p.getUniqueId(), p.getId(), p.getName(), p.getSkin());
                        }
                }
                break;
            case BuyCapeCategory:
                List<String> categories = new ArrayList<>(MainClass.configs.getSection("cape_category").getKeys(false));
                String category = categories.get(simple.getClickedButtonId());
                FormCreator.showPurchasableCapesMenu(p, category);
                selectedCategory.put(p, category);
                break;
            case BuySkinCategory:
                categories = new ArrayList<>(MainClass.configs.getSection("skin_category").getKeys(false));
                category = categories.get(simple.getClickedButtonId());
                FormCreator.showPurchasableSkinsMenu(p, categories.get(simple.getClickedButtonId()));
                selectedCategory.put(p, category);
                break;
            case BuyParticleCategory:
                categories = new ArrayList<>(MainClass.configs.getSection("particle_category").getKeys(false));
                category = categories.get(simple.getClickedButtonId());
                FormCreator.showPurchasableParticlesMenu(p, categories.get(simple.getClickedButtonId()));
                selectedCategory.put(p, category);
                break;
            case ShopMenuCape:
                capeItem capeItem = MainClass.getShopCapesList(p.getName(), selectedCategory.get(p)).get(simple.getClickedButtonId());
                FormCreator.showBuyMenu(p, capeItem);
                playerPurchaseItemHashMap.put(p, capeItem);
                break;
            case ShopMenuSkin:
                skinItem skinItem = MainClass.getShopSkinsList(p.getName(), selectedCategory.get(p)).get(simple.getClickedButtonId());
                FormCreator.showBuyMenu(p, skinItem);
                playerPurchaseItemHashMap.put(p, skinItem);
                break;
            case ShopMenuParticle:
                particleItem particleItem = MainClass.getShopParticlesList(p.getName(), selectedCategory.get(p)).get(simple.getClickedButtonId());
                FormCreator.showBuyMenu(p, particleItem);
                playerPurchaseItemHashMap.put(p, particleItem);
                break;
            case MyMenuCape:
                String myCapeItemName = MainClass.getUnlockedCapes(p.getName()).get(simple.getClickedButtonId());
                capeItem myCapeItem = MainClass.capes.get(myCapeItemName);
                if(myCapeItem != null) {
                    FormCreator.showEquipMenu(p, myCapeItem);
                    playerEquipItemHashMap.put(p, myCapeItem);
                }else{
                    p.sendMessage("该披风暂时无法查看！");
                }
                break;
            case MyMenuSkin:
                String mySkinItemName = MainClass.getUnlockedSkins(p.getName()).get(simple.getClickedButtonId());
                skinItem mySkinItem = MainClass.skins.get(mySkinItemName);
                if(mySkinItem != null) {
                    FormCreator.showEquipMenu(p, mySkinItem);
                    playerEquipItemHashMap.put(p, mySkinItem);
                }else{
                    p.sendMessage("该皮肤暂时无法查看！");
                }
                break;
            case MyMenuParticle:
                String myParticleItemName = MainClass.getUnlockedParticles(p.getName()).get(simple.getClickedButtonId());
                particleItem myParticleItem = MainClass.particles.get(myParticleItemName);
                if(myParticleItem != null) {
                    FormCreator.showEquipMenu(p, myParticleItem);
                    playerEquipItemHashMap.put(p, myParticleItem);
                }else{
                    p.sendMessage("该皮肤暂时无法查看！");
                }
                break;
            case FuckMenu:
                FormCreator.showMainMenu(p);
                break;
        }
    }

    public void onModalClick(Player p, FormResponseModal simple, FormType type) {
        switch (type){
            case EquipMenu:
                if(simple.getClickedButtonId() == 0){
                    if(MainClass.players.getOrDefault(p, 0L) > System.currentTimeMillis()){
                        p.sendMessage("§c§l[提示] 请不要过快刷新皮肤，每次刷新有5秒冷却时间！");
                        return;
                    }
                    playerEquipItemHashMap.get(p).equip(p);
                }else{
                    if(playerEquipItemHashMap.get(p) instanceof capeItem){
                        FormCreator.showMyCapesMenu(p);
                    }
                    if(playerEquipItemHashMap.get(p) instanceof particleItem){
                        FormCreator.showMyParticlesMenu(p);
                    }
                    if(playerEquipItemHashMap.get(p) instanceof skinItem){
                        FormCreator.showMySkinsMenu(p);
                    }
                }
                break;
            case UnEquipMenu:
                if(simple.getClickedButtonId() == 0){
                    if(MainClass.players.getOrDefault(p, 0L) > System.currentTimeMillis()){
                        p.sendMessage("§c§l[提示] 请不要过快刷新皮肤，每次刷新有5秒冷却时间！");
                        return;
                    }
                    playerEquipItemHashMap.get(p).remove(p);
                }else{
                    if(playerEquipItemHashMap.get(p) instanceof capeItem){
                        FormCreator.showMyCapesMenu(p);
                    }
                    if(playerEquipItemHashMap.get(p) instanceof particleItem){
                        FormCreator.showMyParticlesMenu(p);
                    }
                    if(playerEquipItemHashMap.get(p) instanceof skinItem){
                        FormCreator.showMySkinsMenu(p);
                    }
                }
                break;
            case BuyMenu:
                if(simple.getClickedButtonId() == 0){
                    playerPurchaseItemHashMap.get(p).claim(p.getName(), p.getName(), true);
                    playerEquipItemHashMap.remove(p);
                    playerPurchaseItemHashMap.remove(p);
                }else{
                    if(playerPurchaseItemHashMap.get(p) instanceof capeItem){
                        FormCreator.showPurchasableCapesMenu(p, selectedCategory.get(p));
                    }
                    if(playerEquipItemHashMap.get(p) instanceof particleItem){
                        FormCreator.showPurchasableParticlesMenu(p, selectedCategory.get(p));
                    }
                    if(playerEquipItemHashMap.get(p) instanceof skinItem){
                        FormCreator.showPurchasableSkinsMenu(p, selectedCategory.get(p));
                    }
                }
                break;
        }
    }
}
