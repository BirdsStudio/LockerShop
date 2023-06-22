package glorydark.lockershop.items;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.SpawnParticleEffectPacket;
import cn.nukkit.utils.Config;
import glorydark.lockershop.MainClass;
import lombok.Data;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Data
public class particleItem implements purchaseItem{
    String name;

    String displayName;

    String description;

    String identifier;

    String category;

    ElementButtonImageData iconData;

    double needMoney;

    boolean isAllowToUse; //可使用

    boolean isPurchasable; //可购买

    long duration;

    public particleItem(String name, String displayName, String description, String category, ElementButtonImageData iconData, String identifier, double needMoney, boolean isAllowToUse, boolean isPurchasable, long duration){
        this.name = name;
        this.description = description;
        this.displayName = displayName.replace("\\n", "\n");
        this.identifier = identifier;
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
            if (save) {
                MainClass.setDisplayParticle(player.getName(), this.getName());
            }
            MainClass.players.put(player, System.currentTimeMillis()+5000);
            MainClass.particlesCache.put(player, MainClass.getDisplayParticle(player));
            player.sendMessage("§a已装备粒子：" + this.getDisplayName());
        }else{
            player.sendMessage("§c§l[提示] 该粒子暂时不可用！");
        }
    }

    public void remove(Player player){
        //移除
        MainClass.particlesCache.remove(player);
        MainClass.setDisplayParticle(player.getName(), "");
        player.sendMessage("§a已卸载粒子："+this.getDisplayName());
    }

    public boolean claim(String player, String claimer, boolean purchase, Object... params){
        if(MainClass.getUnlockedCapes(player).contains(this.getName())){
            Player p = Server.getInstance().getPlayer(player);
            if(p != null){
                p.sendMessage("§c§l[提示] 您已拥有此粒子！");
            }
            return false;
        }
        if(purchase){
            if(!isPurchasable) {
                Player p = Server.getInstance().getPlayer(player);
                if (p != null) {
                    p.sendMessage("§c§l[提示] 该粒子暂时不可购买！");
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
        config.set("particles."+name, map);
        config.save();
        Player p = Server.getInstance().getPlayer(player);
        if(p != null){
            p.sendMessage("§a§l[提示] 您获得了粒子：§e"+getDisplayName()+"§a，快去使用吧！");
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

    // @author: wode490390
    public void addCustomParticle(Player p){
        if(isAllowToUse) {
            SpawnParticleEffectPacket pk = new SpawnParticleEffectPacket(); // 此数据包用于调用客户端的颗粒效果
            pk.position = new Vector3f((float) p.getX(), (float) p.getY(), (float) p.getZ()); // 生成颗粒效果的位置
            pk.identifier = this.getIdentifier(); // 颗粒效果定义符, 必须和材质包内设定的一样, 否则不会显示
            pk.dimensionId = 0; // 维度ID, 填玩家所在世界维度的即可, 默认为 0 (0: 主世界, 1: 地狱, 2: 末地)
            pk.uniqueEntityId = -1; // 某实体的UUID, 目前无需理会, 默认为 -1
            p.dataPacket(pk);
        }
    }
}
