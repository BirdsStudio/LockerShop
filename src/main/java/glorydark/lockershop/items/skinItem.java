package glorydark.lockershop.items;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.utils.Config;
import glorydark.lockershop.MainClass;
import lombok.Data;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Data
public class skinItem implements purchaseItem {
    String name;

    String displayName;
    String description;

    String category;

    ElementButtonImageData iconData;
    Skin skin;
    double needMoney;
    boolean isAllowToUse; //可使用
    boolean isPurchasable; //可购买
    long duration;

    public skinItem(String name, String displayName, String description, String category, ElementButtonImageData iconData, Skin skin, double needMoney, boolean isAllowToUse, boolean isPurchasable, long duration){
        this.name = name;
        this.displayName = displayName.replace("\\n", "\n");
        this.description = description;
        this.iconData = iconData;
        this.skin = skin;
        this.needMoney = needMoney;
        this.isAllowToUse = isAllowToUse;
        this.isPurchasable = isPurchasable;
        this.duration = duration;
        this.category = category;
    }

    public void equip(Player player){
        equip(player, true);
    }

    public void equip(Player player, boolean save){
        if(isAllowToUse) {
            if (skin.getCapeData() != null) {
                setSkinData(player);
                if(save) {
                    MainClass.setDisplaySkin(player.getName(), this.getName());
                }
                player.sendMessage("§a已装备皮肤："+this.getDisplayName());
                MainClass.players.put(player, System.currentTimeMillis()+5000);
            }else{
                player.sendMessage("§c§l[提示] 该皮肤暂时不可用！");
            }
        }else{
            player.sendMessage("§c§l[提示] 该皮肤暂时不可用！");
        }
    }

    public void unequip(Player player){
        removeSkinData(player);
        player.sendMessage("§a已卸载皮肤："+this.getDisplayName());
        player.getSkin().setSkinData(MainClass.defaultSkin.getSkinData());
        MainClass.setDisplaySkin(player.getName(), "");
        MainClass.players.put(player, System.currentTimeMillis()+5000);
    }

    public boolean claim(String player, String claimer, boolean purchase, Object... params){
        if(MainClass.getUnlockedSkins(player).contains(this.getName())){
            Player p = Server.getInstance().getPlayer(player);
            if(p != null){
                p.sendMessage("§c§l[提示] 您已拥有此皮肤！");
            }
            return false;
        }
        if(purchase){
            if(!isPurchasable) {
                Player p = Server.getInstance().getPlayer(player);
                if (p != null) {
                    p.sendMessage("§c§l[提示] 该皮肤暂时不可购买！");
                }
                return false;
            }
            if(MainClass.economyAPI.myMoney(player) >= needMoney){
                MainClass.economyAPI.reduceMoney(player, needMoney);
            }else{
                Player p = Server.getInstance().getPlayer(player);
                if(p != null){
                    p.sendMessage("§c§l[提示] 您的货币不足");
                }
                return false;
            }
        }
        Config config = new Config(MainClass.path+"/players/"+player+".yml", Config.YAML);
        Map<String, Object> map = new HashMap<>();
        map.put("reducedMoney", needMoney);
        map.put("date", getDate());
        map.put("claimer", claimer);
        if (duration == 0) {
            map.put("due", 0);
        } else {
            long end;
            if(params.length != 1) {
                end = System.currentTimeMillis() + duration;
            }else{
                if(params[0].toString().equals("0")) {
                    end = 0;
                }else{
                    end = System.currentTimeMillis() + Long.parseLong(params[0].toString());
                }
            }
            map.put("due", end);
            map.put("dueString", getDate(end));
        }
        config.set("skins."+name, map);
        config.save();
        Player p = Server.getInstance().getPlayer(player);
        if(p != null){
            p.sendMessage("§a§l[提示] 您获得了皮肤：§e"+getDisplayName()+"§a，快去使用吧！");
        }
        return true;
    }

    public static String getDate(){
        Calendar c1=Calendar.getInstance();
        return c1.get(Calendar.YEAR)+"年"+replace(c1.get(Calendar.MONTH)+1)+
                "月"+replace(c1.get(Calendar.DAY_OF_MONTH))+"日 "+replace(c1.get(Calendar.HOUR_OF_DAY))+
                ":"+replace(c1.get(Calendar.MINUTE))+":"+replace(c1.get(Calendar.SECOND));
    }

    public static String getDate(Long time){
        Calendar c1=new Calendar.Builder().build();
        c1.setTimeInMillis(time);
        return c1.get(Calendar.YEAR)+"年"+replace(c1.get(Calendar.MONTH)+1)+
                "月"+replace(c1.get(Calendar.DAY_OF_MONTH))+"日 "+replace(c1.get(Calendar.HOUR_OF_DAY))+
                ":"+replace(c1.get(Calendar.MINUTE))+":"+replace(c1.get(Calendar.SECOND));
    }

    public static String replace(int a){
        if(a < 10){
            return "0"+a;
        }else{
            return String.valueOf(a);
        }
    }

    public void setSkinData(Player p){
        Skin modelSkin = new Skin();
        modelSkin.setSkinData(skin.getSkinData());
        modelSkin.setGeometryData(skin.getGeometryData());
        modelSkin.setGeometryDataEngineVersion(skin.getGeometryDataEngineVersion());
        modelSkin.setAnimationData(skin.getAnimationData());
        modelSkin.setSkinResourcePatch(skin.getSkinResourcePatch());
        modelSkin.setCapeId(p.getSkin().getCapeId());
        modelSkin.setCapeData(p.getSkin().getCapeData());
        modelSkin.setCapeOnClassic(true);
        modelSkin.setTrusted(true);
        p.setSkin(modelSkin);
    }

    public void removeSkinData(Player p){
        Skin modelSkin = new Skin();
        modelSkin.setSkinData(MainClass.defaultSkin.getSkinData());
        modelSkin.setGeometryData(MainClass.defaultSkin.getGeometryData());
        modelSkin.setGeometryDataEngineVersion(MainClass.defaultSkin.getGeometryDataEngineVersion());
        modelSkin.setAnimationData(MainClass.defaultSkin.getAnimationData());
        modelSkin.setSkinResourcePatch(MainClass.defaultSkin.getSkinResourcePatch());
        modelSkin.setCapeId(p.getSkin().getCapeId());
        modelSkin.setCapeData(p.getSkin().getCapeData());
        modelSkin.setCapeOnClassic(true);
        modelSkin.setTrusted(true);
        p.setSkin(modelSkin);
    }
}
