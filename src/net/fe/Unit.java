package net.fe;

import java.util.ArrayList;
import java.util.HashMap;


public class Unit extends GriddedEntity {
	private HashMap<String, Float> stats;
	private int hp;
	private Class clazz;
	private HashMap<String, Integer> growths;
	private Weapon weapon;
	private ArrayList<Item> inventory;
	private HashMap<String, Integer> tempMods;
	public final String name;
	//TODO Rescue

	public Unit(String name, Class c, HashMap<String, Float> startingStats,
			HashMap<String, Integer> growths) {
		super(0, 0);
		stats = startingStats;
		hp = (int)(startingStats.get("HP").floatValue());
		this.growths = growths;
		inventory = new ArrayList<Item>();
		tempMods = new HashMap<String, Integer>();
		this.name = name;
		clazz = c;
	}
	
	@Override
	public void beginStep() {
		// TODO Auto-generated method stub
	}

	@Override
	public void endStep() {
		// TODO Auto-generated method stub

	}
	
	public void levelUp(){
		stats.put("Lvl", stats.get("Lvl") + 1);
		for(String stat: growths.keySet()){
			stats.put(stat, stats.get(stat) + (float)(growths.get(stat)/100.0));
		}
	}
	
	public void equip(int index){
		if(equippable(index))
			weapon = (Weapon) inventory.get(index);
		else
			throw new IllegalArgumentException("Cannot equip that item");
			
	}
	
	public boolean equippable(int index){
		if(inventory.get(index) instanceof Weapon){
			return clazz.usableWeapon.contains(((Weapon) inventory.get(index)).type);
		}
		return false;
	}
	
	public void clearTempMods(){
		tempMods.clear();
	}
	
	//TODO: getTriggers
	
	public ArrayList<Trigger> getTriggers(){
		ArrayList<Trigger> triggers = new ArrayList<Trigger>();
		if(clazz.masterSkill!=null)
			triggers.add(clazz.masterSkill);
		return triggers;
	}

	
	//Combat statistics
	public int hit(){
		return weapon.hit + get("Skl") + get("Lck")/2 +
				(tempMods.get("Hit")!=null?tempMods.get("Hit"):0);
	}
	
	public int avoid(){
		return get("Spd") + get("Lck")/2 +
				(tempMods.get("Avo")!=null?tempMods.get("Avo"):0);
		//TODO: terrain bonus
	}
	
	public int crit(){
		return weapon.crit + get("Skl")/2 + clazz.crit +
				(tempMods.get("Crit")!=null?tempMods.get("Crit"):0);
	}
	
	public int dodge(){ //Critical avoid
		return get("Lck")+
				(tempMods.get("Dodge")!=null?tempMods.get("Dodge"):0);
	}

	
	//Getter/Setter
	public Class getTheClass() {
		return clazz;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = Math.max(hp,0);
	}
	
	public int get(String stat){
		return stats.get(stat).intValue() + weapon.modifiers.get(stat) +
				(tempMods.get(stat)!=null?tempMods.get(stat):0);
	}
	
	public void setTempMod(String stat, int val){
		tempMods.put(stat, val);
	}
	
	public Weapon getWeapon(){
		return weapon;
	}
	
	public void addToInventory(Item item) {
		inventory.add(item);
	}
}
