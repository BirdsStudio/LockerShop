package glorydark.lockershop.items;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.network.protocol.PlayerSkinPacket;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.SerializedImage;
import glorydark.lockershop.MainClass;
import lombok.Data;

import java.util.*;

@Data
public class capeItem implements purchaseItem {
    String name;

    String displayName;

    String description;

    String category;

    SerializedImage skin;

    ElementButtonImageData iconData;

    double needMoney;

    boolean isAllowToUse; //可使用

    boolean isPurchasable; //可购买

    long duration;

    public capeItem(String name, String displayName, String description, String category, ElementButtonImageData iconData, SerializedImage skin, double needMoney, boolean isAllowToUse, boolean isPurchasable, long duration){
        this.name = name;
        this.description = description;
        this.displayName = displayName.replace("\\n", "\n");
        this.skin = skin;
        this.needMoney = needMoney;
        this.isAllowToUse = isAllowToUse;
        this.isPurchasable = isPurchasable;
        this.duration = duration;
        this.iconData = iconData;
        this.category = category;
    }

    public void equip(Player player){
        equip(player, true);
    }

    public void equip(Player player, boolean save){
        if(isAllowToUse) {
            setCapeSkin(player);
            if (save) {
                MainClass.setDisplayCape(player.getName(), this.getName());
            }
            MainClass.players.put(player, System.currentTimeMillis()+5000);
            player.sendMessage("§a已装备披风：" + this.getDisplayName());
        }else{
            player.sendMessage("§c§l[提示] 该披风暂时不可用！");
        }
    }

    public void remove(Player player){
        removeCapeSkin(player);
        MainClass.setDisplayCape(player.getName(), "");
        MainClass.players.put(player, System.currentTimeMillis()+5000);
        player.sendMessage("§a已卸载披风："+this.getDisplayName());
    }

    public boolean claim(String player, String claimer, boolean purchase, Object... params){
        if(MainClass.getUnlockedCapes(player).contains(this.getName())){
            Player p = Server.getInstance().getPlayer(player);
            if(p != null){
                p.sendMessage("§c§l[提示] 您已拥有此披风！");
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
            if(MainClass.economyAPI.myMoney(player) >= needMoney) {
                MainClass.economyAPI.reduceMoney(player, needMoney);
            }else{
                Player p = Server.getInstance().getPlayer(player);
                if(p != null){
                    p.sendMessage("§a§l[提示] 您的货币不足");
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
        config.set("capes."+name, map);
        config.save();
        Player p = Server.getInstance().getPlayer(player);
        if(p != null){
            p.sendMessage("§a§l[提示] 您获得了皮肤：§e"+getDisplayName()+"§a，快去使用吧！");
        }
        return true;
    }

    public static String getDate(){
        Calendar c1=Calendar.getInstance();
        return c1.get(Calendar.YEAR)+"年"+(c1.get(Calendar.MONTH)+1)+
                "月"+c1.get(Calendar.DAY_OF_MONTH)+"日 "+c1.get(Calendar.HOUR_OF_DAY)+
                ":"+c1.get(Calendar.MINUTE)+":"+c1.get(Calendar.SECOND);
    }

    public static String getDate(Long time){
        if(time == 0){
            return "永久";
        }
        Calendar c1=new Calendar.Builder().build();
        c1.setTimeInMillis(time);
        return c1.get(Calendar.YEAR)+"年"+(c1.get(Calendar.MONTH)+1)+
                "月"+c1.get(Calendar.DAY_OF_MONTH)+"日 "+c1.get(Calendar.HOUR_OF_DAY)+
                ":"+c1.get(Calendar.MINUTE)+":"+c1.get(Calendar.SECOND);
    }

    public void setCapeSkin(Player p){
        Skin modelSkin = new Skin();
        modelSkin.setSkinId(MainClass.defaultSkin.getSkinId());
        modelSkin.setSkinData(p.getSkin().getSkinData());
        modelSkin.setGeometryData(p.getSkin().getGeometryData());
        modelSkin.setGeometryDataEngineVersion(p.getSkin().getGeometryDataEngineVersion());
        modelSkin.setAnimationData(p.getSkin().getAnimationData());
        modelSkin.setSkinResourcePatch(p.getSkin().getSkinResourcePatch());
        modelSkin.setCapeId(UUID.randomUUID().toString());
        modelSkin.setCapeData(skin.data);
        modelSkin.setCapeOnClassic(true);
        modelSkin.setTrusted(true);
        p.setSkin(modelSkin);
        PlayerSkinPacket packet = new PlayerSkinPacket();
        packet.skin = modelSkin;
        packet.newSkinName = modelSkin.getSkinId();
        packet.oldSkinName = p.getSkin().getSkinId();
        packet.uuid = p.getUniqueId();
        HashSet<Player> players = new HashSet<>(p.getViewers().values());
        players.add(p);
        Server.broadcastPacket(players, packet);
    }

    public void removeCapeSkin(Player p){
        Skin modelSkin = new Skin();
        modelSkin.setSkinId(MainClass.defaultSkin.getSkinId());
        modelSkin.setSkinData(p.getSkin().getSkinData());
        modelSkin.setGeometryData(p.getSkin().getGeometryData());
        modelSkin.setGeometryDataEngineVersion(p.getSkin().getGeometryDataEngineVersion());
        modelSkin.setAnimationData(p.getSkin().getAnimationData());
        modelSkin.setSkinResourcePatch(p.getSkin().getSkinResourcePatch());
        modelSkin.setTrusted(true);
        modelSkin.setCapeId("");
        modelSkin.setCapeData("".getBytes());
        modelSkin.setCapeOnClassic(false);
        modelSkin.setTrusted(true);
        p.setSkin(modelSkin);
        PlayerSkinPacket packet = new PlayerSkinPacket();
        packet.skin = modelSkin;
        packet.newSkinName = modelSkin.getSkinId();
        packet.oldSkinName = p.getSkin().getSkinId();
        packet.uuid = p.getUniqueId();
        HashSet<Player> players = new HashSet<>(p.getViewers().values());
        players.add(p);
        Server.broadcastPacket(players, packet);
    }
}
