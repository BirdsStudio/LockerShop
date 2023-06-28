package glorydark.lockershop;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChangeSkinEvent;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.nbt.stream.FastByteArrayOutputStream;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.SerializedImage;
import com.sun.istack.internal.NotNull;
import glorydark.lockershop.forms.FormCreator;
import glorydark.lockershop.forms.FormListener;
import glorydark.lockershop.forms.FormType;
import glorydark.lockershop.items.capeItem;
import glorydark.lockershop.items.particleItem;
import glorydark.lockershop.items.skinItem;
import me.onebone.economyapi.EconomyAPI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class MainClass extends PluginBase{

    public static String path;

    public static MainClass instance;

    public static EconomyAPI economyAPI;

    public static Skin defaultSkin;

    public static LinkedHashMap<String, skinItem> skins = new LinkedHashMap<>();

    public static LinkedHashMap<String, capeItem> capes = new LinkedHashMap<>();

    public static LinkedHashMap<String, particleItem> particles = new LinkedHashMap<>();

    public static List<skinItem> availableSkins = new ArrayList<>();

    public static List<capeItem> availableCapes = new ArrayList<>();

    public static List<particleItem> availableParticles = new ArrayList<>();

    public static HashMap<Player, Long> players = new HashMap<>();

    public static HashMap<Player, String> particlesCache = new HashMap<>();

    public static Config configs;

    public static boolean allow_change_skin;


    @Override
    public void onEnable() {
        instance = this;
        economyAPI = EconomyAPI.getInstance();
        path = this.getDataFolder().getPath();
        File originPath = new File(path);
        if(!originPath.exists()) {
            if(!originPath.mkdirs()){
                this.getLogger().warning("无法创建文件夹:"+path);
            }
        }
        File skinItemConfigPath = new File(getSkinItemConfigPath());
        if(!skinItemConfigPath.exists()) {
            if(!skinItemConfigPath.mkdirs()){
                this.getLogger().warning("无法创建文件夹:"+getSkinItemConfigPath());
            }
        }
        File CapeItemConfigPath = new File(getCapeItemConfigPath());
        if(!CapeItemConfigPath.exists()) {
            if(!CapeItemConfigPath.mkdirs()){
                this.getLogger().warning("无法创建文件夹:"+getCapeItemConfigPath());
            }
        }
        File ParticleItemConfigPath = new File(getParticleItemConfigPath());
        if(!ParticleItemConfigPath.exists()) {
            if(!ParticleItemConfigPath.mkdirs()){
                this.getLogger().warning("无法创建文件夹:"+getParticleItemConfigPath());
            }
        }
        this.saveDefaultConfig();
        this.saveResource("skins/default/skin.json", false);
        this.saveResource("skins/default/skin.png", false);
        this.saveResource("skins/default/skin.animation.json", false);
        defaultSkin = loadSkin("default");
        Config config = new Config(path+"/config.yml", Config.YAML);
        configs = config;
        allow_change_skin = config.getBoolean("allow_change_skin", false);
        this.getLogger().info("LockerShop Enabled");
        this.getServer().getCommandMap().register("", new Commands("locker"));
        this.getServer().getPluginManager().registerEvents(new EventsListener(), this);
        this.getServer().getPluginManager().registerEvents(new FormListener(), this);
        loadSkinItems();
        loadCapesItems();
        loadParticlesItems();
        skins.values().stream().filter(skinItem::isPurchasable).forEach(availableSkins::add);
        capes.values().stream().filter(capeItem::isPurchasable).forEach(availableCapes::add);
        particles.values().stream().filter(particleItem::isPurchasable).forEach(availableParticles::add);
        Server.getInstance().getScheduler().scheduleRepeatingTask(this, ()->{
            for(Player p: Server.getInstance().getOnlinePlayers().values()){
                if(MainClass.particlesCache.containsKey(p)){
                    String particle = MainClass.particlesCache.get(p);
                    if(particles.containsKey(particle)){
                        particles.get(particle).addCustomParticle(p);
                    }
                }
            }
        }, config.getInt("refresh_delay", 20));
    }

    public static void reload(){
        configs = new Config(path+"/config.yml", Config.YAML);
        skins.clear();
        capes.clear();
        particles.clear();
        availableSkins.clear();
        availableCapes.clear();
        availableParticles.clear();
        loadSkinItems();
        loadCapesItems();
        loadParticlesItems();
        skins.values().stream().filter(skinItem::isPurchasable).forEach(availableSkins::add);
        capes.values().stream().filter(capeItem::isPurchasable).forEach(availableCapes::add);
        particles.values().stream().filter(particleItem::isPurchasable).forEach(availableParticles::add);
    }


    public static class Commands extends Command {

        public Commands(String name) {
            super(name);
        }

        @Override
        public boolean execute(CommandSender commandSender, String s, String[] strings) {
            if(strings.length == 0){ return false; }
            switch (strings[0]){
                case "reload":
                    MainClass.reload();
                    commandSender.sendMessage("§a重新载入配置成功");
                    break;
                case "menu":
                    if(commandSender.isPlayer()){
                        Player player = (Player) commandSender;
                        if(strings.length == 1){
                            FormCreator.showMainMenu(player);
                        }else if(strings.length == 2){
                            switch (strings[1]){
                                case "myskin":
                                    FormCreator.showMySkinsMenu(player);
                                    break;
                                case "mycape":
                                    FormCreator.showMyCapesMenu(player);
                                    break;
                                case "myparticle":
                                    FormCreator.showMyParticlesMenu(player);
                                    break;
                                case "buyskin":
                                    FormCreator.showCategorySelectMenu(player, FormType.BuySkinCategory);
                                    break;
                                case "buycape":
                                    FormCreator.showCategorySelectMenu(player, FormType.BuyCapeCategory);
                                    break;
                                case "buyparticle":
                                    FormCreator.showCategorySelectMenu(player, FormType.BuyParticleCategory);
                                    break;
                            }
                        }
                    }
                    break;
                case "giveskin":
                    if(commandSender.isPlayer() && !commandSender.isOp()){ return false; }
                    if(strings.length == 4){
                        // 1 玩家  2 皮肤  3 时长
                        if(MainClass.skins.containsKey(strings[2])){
                            if(Server.getInstance().lookupName(strings[1]).isPresent()) {
                                if(MainClass.skins.get(strings[2]).claim(strings[1], "console", false, strings[3])) {
                                    commandSender.sendMessage("§a[提示] 给予玩家§e" + strings[1] + "§a名为§e" + strings[2] + "§a的皮肤成功，期限:"+getTimeString(Long.parseLong(strings[3])));
                                }else{
                                    commandSender.sendMessage("§c[提示] 玩家§e" + strings[1] + "§c已经拥有名为§e" + strings[2] + "§c的皮肤");
                                }
                            }else{
                                commandSender.sendMessage("§c[提示] 不存在名为§e"+strings[1]+"§c的玩家");
                            }
                        }else{
                            commandSender.sendMessage("§c[提示] 不存在名为§e"+strings[2]+"§c的皮肤");
                        }
                    }
                    break;
                case "givecape":
                    if(commandSender.isPlayer() && !commandSender.isOp()){ return false; }
                    if(strings.length == 4){
                        // 1 玩家  2 皮肤  3 时长
                        if(MainClass.capes.containsKey(strings[2])){
                            if(Server.getInstance().lookupName(strings[1]).isPresent()) {
                                if(MainClass.capes.get(strings[2]).claim(strings[1], "console", false, strings[3])) {
                                    commandSender.sendMessage("§a[提示] 给予玩家§e" + strings[1] + "§a名为§e" + strings[2] + "§a的披风成功，期限:"+getTimeString(Long.parseLong(strings[3])));
                                }else{
                                    commandSender.sendMessage("§c[提示] 玩家§e" + strings[1] + "§c已经拥有名为§e" + strings[2] + "§c的披风");
                                }
                            }else{
                                commandSender.sendMessage("§c[提示] 不存在名为§e"+strings[1]+"§c的玩家");
                            }
                        }else{
                            commandSender.sendMessage("§c[提示] 不存在名为§e"+strings[2]+"§c的披风");
                        }
                    }
                    break;
                case "giveparticle":
                    if(commandSender.isPlayer() && !commandSender.isOp()){ return false; }
                    if(strings.length == 4){
                        // 1 玩家  2 皮肤  3 时长
                        if(MainClass.particles.containsKey(strings[2])){
                            if(Server.getInstance().lookupName(strings[1]).isPresent()) {
                                if(MainClass.particles.get(strings[2]).claim(strings[1], "console", false, strings[3])) {
                                    commandSender.sendMessage("§a[提示] 给予玩家§e" + strings[1] + "§a名为§e" + strings[2] + "§a的粒子成功，期限:"+getTimeString(Long.parseLong(strings[3])));
                                }else{
                                    commandSender.sendMessage("§c[提示] 玩家§e" + strings[1] + "§c已经拥有名为§e" + strings[2] + "§c的粒子");
                                }
                            }else{
                                commandSender.sendMessage("§c[提示] 不存在名为§e"+strings[1]+"§c的玩家");
                            }
                        }else{
                            commandSender.sendMessage("§c[提示] 不存在名为§e"+strings[2]+"§c的粒子");
                        }
                    }
                    break;
                case "help":
                    commandSender.sendMessage("§e§lSkinShop 帮助");
                    if(commandSender.isOp() || !commandSender.isPlayer()){
                        commandSender.sendMessage("/skin menu  §a打开皮肤界面");
                        commandSender.sendMessage("/skin reload  §a重载配置");
                        commandSender.sendMessage("/skin giveskin 玩家名 皮肤名 时长(单位:毫秒, 1000毫秒=1秒，永久填0)  §a给予皮肤");
                        commandSender.sendMessage("/skin givecape 玩家名 皮肤名 时长(单位:毫秒, 1000毫秒=1秒，永久填0)  §a给予披风");
                    }else{
                        commandSender.sendMessage("/skin menu  §a打开皮肤界面");
                    }
                    break;
                case "statistics":
                    if(!commandSender.isPlayer()){
                        HashMap<String, Integer> skinCounts = new HashMap<>();
                        HashMap<String, Integer> capeCounts = new HashMap<>();
                        HashMap<String, Integer> particleCounts = new HashMap<>();
                        HashMap<String, List<String>> skinPls = new HashMap<>();
                        HashMap<String, List<String>> capePls = new HashMap<>();
                        HashMap<String, List<String>> particlePls = new HashMap<>();
                        File dic = new File(path+"/players/");
                        for(File file: Objects.requireNonNull(dic.listFiles())){
                            Config config = new Config(file, Config.YAML);
                            Set<String> s1 = config.getSection("skins").getKeys(false);
                            Set<String> s2 = config.getSection("capes").getKeys(false);
                            Set<String> s3 = config.getSection("particles").getKeys(false);
                            if(s1.size() > 0) {
                                s1.forEach(string1 -> {
                                    skinCounts.put(string1, skinCounts.getOrDefault(string1, 0) + 1);
                                    List<String> sl1 = skinPls.getOrDefault(string1, new ArrayList<>());
                                    sl1.add(file.getName().replace(".yml", ""));
                                    skinPls.put(string1, sl1);
                                });
                            }
                            if(s2.size() > 0) {
                                s2.forEach(string2 -> {
                                    capeCounts.put(string2, capeCounts.getOrDefault(string2, 0) + 1);
                                    List<String> sl2 = capePls.getOrDefault(string2, new ArrayList<>());
                                    sl2.add(file.getName().replace(".yml", ""));
                                    capePls.put(string2, sl2);
                                });
                            }
                            if(s3.size() > 0) {
                                s3.forEach(string3 -> {
                                    particleCounts.put(string3, particleCounts.getOrDefault(string3, 0) + 1);
                                    List<String> sl3 = particlePls.getOrDefault(string3, new ArrayList<>());
                                    sl3.add(file.getName().replace(".yml", ""));
                                    particlePls.put(string3, sl3);
                                });
                            }
                        }
                        commandSender.sendMessage("-------Statistics------");
                        commandSender.sendMessage("§a皮肤:");
                        if(skinPls.size() > 0) {
                            skinCounts.forEach((key, value) -> commandSender.sendMessage(key + ":" + value + "\n拥有者:" + skinPls.get(key).toString()));
                        }else{
                            commandSender.sendMessage("暂无数据");
                        }
                        commandSender.sendMessage("§a披风:");
                        if(capePls.size() > 0) {
                            capeCounts.forEach((key, value) -> commandSender.sendMessage(key + ":" + value + "\n拥有者:" + capePls.get(key).toString()));
                        }else{
                            commandSender.sendMessage("暂无数据");
                        }
                        commandSender.sendMessage("§a粒子:");
                        if(particlePls.size() > 0) {
                            particleCounts.forEach((key, value) -> commandSender.sendMessage(key + ":" + value + "\n拥有者:" + particlePls.get(key).toString()));
                        }else{
                            commandSender.sendMessage("暂无数据");
                        }
                        commandSender.sendMessage("-------Statistics------");
                    }
                    break;
            }
            return true;
        }
    }

    public static String getTimeString(long duration){
        if(duration == 0){
            return "永久";
        }
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
    public static class EventsListener implements Listener{

        public void checkPlayer(Player p){
            File file = new File(MainClass.path+"/players/"+p.getName()+".yml");
            if(!file.exists()){
                return;
            }
            Config config = new Config(MainClass.path+"/players/"+p.getName()+".yml");
            if(config.exists("skins")) {
                ConfigSection section = config.getSection("skins");
                for (String key : section.getKeys(false)) {
                    ConfigSection subSection = section.getSection(key);
                    if (subSection.getLong("due") <= System.currentTimeMillis() && subSection.getLong("due") != 0) {
                        skinItem skinItem = MainClass.skins.get(key);
                        if (skinItem != null) {
                            skinItem.remove(p);
                            p.sendMessage("§c[提示] 您的皮肤§e" + skinItem.getDisplayName() + "§c已过期！");
                        }
                        config.getSection("skins").remove(key);
                        config.save();
                        MainClass.setDisplaySkin(p.getName(), "");
                    }
                }
            }
            if(config.exists("capes")){
                ConfigSection section1 = config.getSection("capes");
                for(String key: section1.getKeys(false)){
                    ConfigSection subSection = section1.getSection(key);
                    if(subSection.getLong("due") <= System.currentTimeMillis() && subSection.getLong("due") != 0L){
                        capeItem skinItem = MainClass.capes.get(key);
                        if(skinItem != null) {
                            skinItem.remove(p);
                            p.sendMessage("§c[提示] 您的披风§e" + skinItem.getDisplayName()+"§c已过期！");
                        }
                        MainClass.instance.getLogger().warning("capes."+key);
                        config.getSection("capes").remove(key);
                        config.save();
                        MainClass.setDisplayCape(p.getName(), "");
                    }
                }
            }
            if(config.exists("particles")){
                ConfigSection section1 = config.getSection("particles");
                for(String key: section1.getKeys(false)){
                    ConfigSection subSection = section1.getSection(key);
                    if(subSection.getLong("due") <= System.currentTimeMillis() && subSection.getLong("due") != 0L){
                        capeItem skinItem = MainClass.capes.get(key);
                        if(skinItem != null) {
                            skinItem.remove(p);
                            p.sendMessage("§c[提示] 您的粒子§e" + skinItem.getDisplayName()+"§c已过期！");
                        }
                        MainClass.instance.getLogger().warning("particles."+key);
                        config.getSection("particles").remove(key);
                        config.save();
                        MainClass.setDisplayParticle(p.getName(), "");
                    }
                }
            }
        }

        @EventHandler
        public void PlayerChangeSkinEvent(PlayerChangeSkinEvent event){
            if(!allow_change_skin){
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void PlayerLocallyInitializedEvent(PlayerLocallyInitializedEvent event){
            Player player = event.getPlayer();
            checkPlayer(player);
            skinItem skinItem = MainClass.skins.getOrDefault(getDisplaySkin(event.getPlayer()), null);
            capeItem capeItem = MainClass.capes.getOrDefault(getDisplayCape(event.getPlayer()), null);
            if(skinItem != null || capeItem != null) {
                if (capeItem != null) {
                    if(getUnlockedCapes(player.getName()).contains(capeItem.getName())) {
                        capeItem.equip(player, false);
                    }else{
                        setDisplayCape(player.getName(), "");
                        player.setSkin(defaultSkin); //处理小概率离谱未清理事件
                    }
                }
                if (skinItem != null) {
                    if(getUnlockedSkins(player.getName()).contains(skinItem.getName())) {
                        skinItem.equip(player, false);
                    }else{
                        setDisplaySkin(player.getName(), "");
                        player.setSkin(defaultSkin); //处理小概率离谱未清理事件
                    }
                }
                player.sendMessage("§a已自动为您换上上次装扮的皮肤！");
            }else{
                if(!allow_change_skin) {
                    player.setSkin(defaultSkin);
                }
            }
            if(!getDisplayParticle(player).equals("")){
                particlesCache.put(player, getDisplayParticle(player));
            }
        }

        @EventHandler
        public void Quit(PlayerQuitEvent event){
            event.getPlayer().setSkin(defaultSkin);
        }
    }

    public static void loadSkinItems(){
        File file = new File(getSkinItemConfigPath());
        for(File skinConfig: Objects.requireNonNull(file.listFiles())){
            Config config = new Config(skinConfig, Config.YAML);
            String skinConfigName = skinConfig.getName().replace(".yml", "");
            String skinName = config.getString("skin", "default");
            File json = new File(getSkinResPath()+"/"+skinName+ "/skin.json");
            File data = new File(getSkinResPath()+"/"+skinName+ "/skin.png");
            File skinAnimation = new File(getSkinResPath()+"/"+skinName+"/skin.animation.json");
            if(json.exists() && data.exists()){
                Skin skin = loadSkin(skinName);
                if(skin != null){
                    if(skinAnimation.exists()){
                        skin.setAnimationData(readFile(skinAnimation));
                    }
                    ElementButtonImageData imageData = null;
                    String iconPath = config.getString("iconPath");
                    if(!iconPath.equals("null")){
                        if(iconPath.startsWith("url#")){
                            imageData = new ElementButtonImageData("url", iconPath.replaceFirst("url#", ""));
                        }
                        if(iconPath.startsWith("path#")){
                            imageData = new ElementButtonImageData("path", iconPath.replaceFirst("path#", ""));
                        }
                    }
                    skinItem skinItem = new skinItem(skinConfigName, config.getString("displayName"), config.getString("description"), config.getString("category"), imageData, skin, config.getDouble("needMoney"), config.getBoolean("isAllowToUse"), config.getBoolean("isPurchasable"), config.getLong("duration"));
                    skins.put(skinConfigName, skinItem);
                    MainClass.instance.getLogger().info("§a成功加载皮肤商品："+skinConfigName);
                }
            }else{
                MainClass.instance.getLogger().warning("无法找到对应皮肤："+skinName);
            }
        }
        MainClass.instance.getLogger().info("§a成功加载皮肤商品§e"+skins.keySet().size()+"个");
    }

    public static void loadCapesItems(){
        File file = new File(getCapeItemConfigPath());
        for(File skinConfig: Objects.requireNonNull(file.listFiles())){
            Config config = new Config(skinConfig, Config.YAML);
            String capeConfigName = skinConfig.getName().replace(".yml", "");
            String capeSkinName = config.getString("skin", "default");
            File data = new File(getCapeResPath()+"/"+capeSkinName+"/skin.png");
            if(data.exists()){
                SerializedImage skin;
                try {
                    skin = parseBufferedImage(ImageIO.read(data));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ElementButtonImageData imageData = null;
                String iconPath = config.getString("iconPath");
                if(!iconPath.equals("null")){
                    if(iconPath.startsWith("url#")){
                        imageData = new ElementButtonImageData("url", iconPath.replaceFirst("url#", ""));
                    }
                    if(iconPath.startsWith("path#")){
                        imageData = new ElementButtonImageData("path", iconPath.replaceFirst("path#", ""));
                    }
                }
                capeItem capeItem = new capeItem(capeConfigName, config.getString("displayName"), config.getString("description"), config.getString("category"), imageData, skin, config.getDouble("needMoney"), config.getBoolean("isAllowToUse"), config.getBoolean("isPurchasable"), config.getLong("duration"));
                capes.put(capeConfigName, capeItem);
                MainClass.instance.getLogger().info("§a成功加载披风商品："+capeItem.getName());
            }else{
                MainClass.instance.getLogger().warning("无法找到对应披风皮肤："+capeSkinName);
            }
        }
        MainClass.instance.getLogger().info("§a成功加载披风商品§e"+capes.keySet().size()+"个");
    }

    public static void loadParticlesItems(){
        File file = new File(getParticleItemConfigPath());
        for(File particleConfig: Objects.requireNonNull(file.listFiles())){
            Config config = new Config(particleConfig, Config.YAML);
            String particleConfigName = particleConfig.getName().replace(".yml", "");
            String particleIdentifier = config.getString("identifier", "minecraft:blue_flame");
            ElementButtonImageData imageData = null;
            String iconPath = config.getString("iconPath");
            if(!iconPath.equals("null")){
                if(iconPath.startsWith("url#")){
                    imageData = new ElementButtonImageData("url", iconPath.replaceFirst("url#", ""));
                }
                if(iconPath.startsWith("path#")){
                    imageData = new ElementButtonImageData("path", iconPath.replaceFirst("path#", ""));
                }
            }
            particleItem particleItem = new particleItem(particleConfigName, config.getString("displayName"), config.getString("description"), config.getString("category"), imageData, particleIdentifier, config.getDouble("needMoney"), config.getBoolean("isAllowToUse"), config.getBoolean("isPurchasable"), config.getLong("duration"));
            particles.put(particleConfigName, particleItem);
            MainClass.instance.getLogger().info("§a成功加载粒子商品："+particleItem.getName());
        }
        MainClass.instance.getLogger().info("§a成功加载粒子商品§e"+particles.keySet().size()+"个");
    }

    public static Skin loadSkin(String skinName){
        File skinDataFile = new File(getSkinResPath()+"/"+skinName+"/skin.png");
        File skinJsonFile = new File(getSkinResPath()+"/"+skinName+"/skin.json");
        if (skinDataFile.exists()) {
            Skin skin = new Skin();
            skin.setSkinId(skinName);
            try {
                skin.setSkinData(ImageIO.read(skinDataFile));
            } catch (Exception e) {
                MainClass.instance.getLogger().error("皮肤 " + skinName + " 读取错误，请检查图片格式或图片尺寸！", e);
            }

            //如果是4D皮肤
            if (skinJsonFile.exists()) {
                Map<String, Object> skinJson = (new Config(skinJsonFile, Config.JSON)).getAll();
                String geometryName = null;

                String formatVersion = (String) skinJson.getOrDefault("format_version", "1.10.0");
                skin.setGeometryDataEngineVersion(formatVersion); //设置皮肤版本，主流格式有1.16.0,1.12.0(Blockbench新模型),1.10.0(Blockbench Legacy模型),1.8.0
                switch (formatVersion) {
                    case "1.16.0":
                    case "1.12.0":
                        geometryName = getGeometryName(skinJsonFile);
                        if (geometryName.equals("nullvalue")) {
                            MainClass.instance.getLogger().error("暂不支持该版本格式的皮肤！请等待更新！");
                        } else {
                            skin.generateSkinId(skinName);
                            skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
                            skin.setGeometryName(geometryName);
                            skin.setGeometryData(readFile(skinJsonFile));
                            MainClass.instance.getLogger().info("皮肤 " + skinName + " 读取中");
                        }
                        break;
                    default:
                        MainClass.instance.getLogger().warning("[" + skinName + "] 的版本格式为：" + formatVersion + "，正在尝试加载！");
                    case "1.10.0":
                    case "1.8.0":
                        for (Map.Entry<String, Object> entry : skinJson.entrySet()) {
                            if (geometryName == null) {
                                if (entry.getKey().startsWith("geometry")) {
                                    geometryName = entry.getKey();
                                }
                            } else {
                                break;
                            }
                        }
                        skin.generateSkinId(skinName);
                        skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
                        skin.setGeometryName(geometryName);
                        skin.setGeometryData(readFile(skinJsonFile));
                        break;
                }
            }
            skin.setTrusted(true);

            if (skin.isValid()) {
                MainClass.instance.getLogger().info("皮肤 " + skinName + " 读取完成");
                return skin;
            }else {
                MainClass.instance.getLogger().error("皮肤 " + skinName + " 验证失败，请检查皮肤文件完整性！");
            }
        } else {
            MainClass.instance.getLogger().error("皮肤 " + skinName + " 错误的名称格式，请将皮肤文件命名为 skin.png 模型文件命名为 skin.json");
        }
        return null;
    }

    public static String readFile(@NotNull File file) {
        String content = "";
        try {
            content = cn.nukkit.utils.Utils.readFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static String getGeometryName(File file) {
        Config originGeometry = new Config(file, Config.JSON);
        if (!originGeometry.getString("format_version").equals("1.12.0") && !originGeometry.getString("format_version").equals("1.16.0")) {
            return "nullvalue";
        }
        //先读取minecraft:geometry下面的项目
        List<Map<String, Object>> geometryList = (List<Map<String, Object>>) originGeometry.get("minecraft:geometry");
        //不知道为何这里改成了数组，所以按照示例文件读取第一项
        Map<String, Object> geometryMain = geometryList.get(0);
        //获取description内的所有
        Map<String, Object> descriptions = (Map<String, Object>) geometryMain.get("description");
        return (String) descriptions.getOrDefault("identifier", "geometry.unknown"); //获取identifier
    }

    public static String getSkinResPath(){
        return path+"/skins/";
    }

    public static String getSkinItemConfigPath(){
        return path+"/items/skins/";
    }

    public static String getCapeItemConfigPath(){
        return path+"/items/capes/";
    }

    public static String getParticleItemConfigPath(){
        return path+"/items/particles/";
    }

    public static String getCapeResPath(){
        return path+"/capes/";
    }

    private static SerializedImage parseBufferedImage(BufferedImage image) {
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();

        for(int y = 0; y < image.getHeight(); ++y) {
            for(int x = 0; x < image.getWidth(); ++x) {
                Color color = new Color(image.getRGB(x, y), true);
                outputStream.write(color.getRed());
                outputStream.write(color.getGreen());
                outputStream.write(color.getBlue());
                outputStream.write(color.getAlpha());
            }
        }
        image.flush();
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new SerializedImage(image.getWidth(), image.getHeight(), outputStream.toByteArray());
    }

    public static List<String> getUnlockedSkins(String player){
        File file = new File(MainClass.path+"/players/"+player+".yml");
        if(!file.exists()){
            return new ArrayList<>();
        }
        Config config = new Config(file, Config.YAML);
        if(config.exists("skins")){
            return new ArrayList<>(config.getSection("skins").getKeys(false));
        }
        return new ArrayList<>();
    }

    public static List<String> getUnlockedCapes(String player){
        File file = new File(MainClass.path+"/players/"+player+".yml");
        if(!file.exists()){
            return new ArrayList<>();
        }
        Config config = new Config(file, Config.YAML);
        if(config.exists("capes")){
            return new ArrayList<>(config.getSection("capes").getKeys(false));
        }
        return new ArrayList<>();
    }

    public static List<String> getUnlockedParticles(String player){
        File file = new File(MainClass.path+"/players/"+player+".yml");
        if(!file.exists()){
            return new ArrayList<>();
        }
        Config config = new Config(file, Config.YAML);
        if(config.exists("particles")){
            return new ArrayList<>(config.getSection("particles").getKeys(false));
        }
        return new ArrayList<>();
    }

    public static void setDisplayCape(String player, String capeName){
        Config config = new Config(MainClass.path+"/players/"+player+".yml", Config.YAML);
        if(capeName.equals("")){
            config.getSection("settings").remove("cape");
        }else{
            config.set("settings.cape", capeName);
        }
        config.save();
    }

    public static void setDisplaySkin(String player, String skinName){
        Config config = new Config(MainClass.path+"/players/"+player+".yml", Config.YAML);
        if(skinName.equals("")){
            config.getSection("settings").remove("skin");
        }else{
            config.set("settings.skin", skinName);
        }
        config.save();
    }

    public static void setDisplayParticle(String player, String particleName){
        Config config = new Config(MainClass.path+"/players/"+player+".yml", Config.YAML);
        if(particleName.equals("")){
            config.getSection("settings").remove("skin");
        }else{
            config.set("settings.particle", particleName);
        }
        config.save();
    }

    public static String getDisplaySkin(Player player){
        File file = new File(MainClass.path+"/players/"+player.getName()+".yml");
        if(!file.exists()){
            return "";
        }
        Config config = new Config(file, Config.YAML);
        return config.getString("settings.skin", "");
    }

    public static String getDisplayCape(Player player){
        File file = new File(MainClass.path+"/players/"+player.getName()+".yml");
        if(!file.exists()){
            return "";
        }
        Config config = new Config(file, Config.YAML);
        return config.getString("settings.cape", "");
    }

    public static String getDisplayParticle(Player player){
        File file = new File(MainClass.path+"/players/"+player.getName()+".yml");
        if(!file.exists()){
            return "";
        }
        Config config = new Config(file, Config.YAML);
        return config.getString("settings.particle", "");
    }

    public static List<skinItem> getShopSkinsList(String player, String category){
        List<String> owned = getUnlockedSkins(player);
        List<skinItem> out = new ArrayList<>();
        availableSkins.stream().filter(skinItem->!owned.contains(skinItem.getName()) && skinItem.getCategory().equals(category)).forEach(out::add);
        return out;
    }

    public static List<capeItem> getShopCapesList(String player, String category){
        List<String> owned = getUnlockedCapes(player);
        List<capeItem> out = new ArrayList<>();
        availableCapes.stream().filter(capeItem->!owned.contains(capeItem.getName()) && capeItem.getCategory().equals(category)).forEach(out::add);
        return out;
    }

    public static List<particleItem> getShopParticlesList(String player, String category){
        List<String> owned = getUnlockedParticles(player);
        List<particleItem> out = new ArrayList<>();
        availableParticles.stream().filter(particleItem->!owned.contains(particleItem.getName()) && particleItem.getCategory().equals(category)).forEach(out::add);
        return out;
    }
}