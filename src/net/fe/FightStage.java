package net.fe;

import java.util.ArrayList;
import java.util.HashMap;

import chu.engine.Entity;
import chu.engine.Stage;

public class FightStage extends Stage {
	private Unit left, right;
	
	public FightStage() {
		super();
		// TODO: Beta testing stuff, delete later
		HashMap<String, Float> stats1 = new HashMap<String, Float>();
		stats1.put("Skl", 10f);
		stats1.put("Lck", 1f);
		stats1.put("HP", 15f);
		stats1.put("Str", 10f);
		stats1.put("Mag", 10f);
		stats1.put("Def", 10f);
		stats1.put("Res", 10f);
		stats1.put("Spd", 12f);
		stats1.put("Lvl", 10f);
		stats1.put("Mov", 3f);
		HashMap<String, Float> stats2 = new HashMap<String, Float>();
		stats2.put("Skl", 10f);
		stats2.put("Lck", 3f);
		stats2.put("HP", 15f);
		stats2.put("Str", 10f);
		stats2.put("Mag", 10f);
		stats2.put("Def", 10f);
		stats2.put("Res", 10f);
		stats2.put("Spd", 8f);
		stats2.put("Lvl", 10f);
		stats2.put("Mov", 3f);
		left = new Unit("Marth",Class.createClass("Sniper"), stats1, null);
		left.addToInventory(Weapon.createWeapon("bow"));
		left.equip(0);
		
		right = new Unit("Roy",Class.createClass(null), stats2, null);
		right.addToInventory(Weapon.createWeapon("lunce"));
		right.equip(0);
		
		calculate(2);
	}

	public void calculate(int range) {
		// Determine turn order
		ArrayList<Boolean> attackOrder = new ArrayList<Boolean>();
		if(left.getWeapon().range.contains(range))
			attackOrder.add(true);
		if(right.getWeapon().range.contains(range))
			attackOrder.add(false);
		if (left.get("Spd") >= right.get("Spd") + 4 && left.getWeapon().range.contains(range)) {
			attackOrder.add(true);
		}
		if (right.get("Spd") >= left.get("Spd") + 4 && right.getWeapon().range.contains(range)) {
			attackOrder.add(false);
		}
		
		System.out.println("Starting health | "+left.name+": "+left.getHp()
				+" | "+right.name+": "+right.getHp());
		for (Boolean i : attackOrder) {
			attack(i, true);
		}
		System.out.println("Ending health | "+left.name+": "+left.getHp()
				+" | "+right.name+": "+right.getHp());
	}

	public void attack(boolean dir, boolean skills) {
		Unit a, d;
		if (dir) {
			a = left;
			d = right;
		} else {
			a = right;
			d = left;
		}
		
		if(a.getHp() == 0 || d.getHp() == 0){
			return;
		}
		
		ArrayList<Trigger> aTriggers = a.getTriggers();
		ArrayList<Trigger> dTriggers = d.getTriggers();
		
		for(Trigger t: aTriggers){
			t.attempt(a);
		}
		for(Trigger t: dTriggers){
			t.attempt(a);
		}
		
		String animation = null;
		if(skills){
			boolean cancel = false;
			for(Trigger t: aTriggers){
				if(t.success && t.type.contains(Trigger.Type.PRE_ATTACK)){
					cancel = t.run(this, a, d) != 0;
					animation = t.getClass().getSimpleName();
				}
			}
			if(cancel) return;
		}
		if (!(RNG.get() < a.hit() - d.avoid()
				+ a.getWeapon().triMod(d.getWeapon()) * 10)) {
			// Miss
			addToAttackQueue(a, d, "Miss", 0);
			if(a.getWeapon().isMagic()) a.getWeapon().use(a);
			return;
		}
		int crit = 1;
		if (RNG.get() < a.crit() - d.dodge()) {
			crit = 3;
		}
		int damage;
		if(a.getWeapon().isMagic()){
			damage = a.get("Mag") + (a.getWeapon().mt + a.getWeapon().triMod(d.getWeapon())) 
					*(a.getWeapon().effective.contains(d.getTheClass())?3:1)
					- d.get("Res");
			//TODO Terrain modifier
		} else {
			damage = a.get("Str") + (a.getWeapon().mt + a.getWeapon().triMod(d.getWeapon())) 
					*(a.getWeapon().effective.contains(d.getTheClass())?3:1)
					- d.get("Res");
		}
		damage *= crit;
		
		for(Trigger t: dTriggers){
			if(t.success && t.type.contains(Trigger.Type.DAMAGE_MOD)){
				damage = t.run(damage);
			}
		}
		
		if(animation == null){
			animation = crit == 1? "Attack" : "Critical";
		}
		addToAttackQueue(a, d, animation, damage);
		d.setHp(d.getHp()-damage);
		a.clearTempMods();
		d.clearTempMods();
		if(skills){
			for(Trigger t: aTriggers){
				if(t.success && t.type.contains(Trigger.Type.POST_ATTACK)){
					t.run(this, a, d);
				}
			}
		}
	}

	public void addToAttackQueue(Unit a, Unit d, String animation, int damage) {
		System.out.print(animation + "! ");
		System.out.println(a.name + " hit " + d.name + " for " + damage + " damage!");
	}

	@Override
	public void beginStep() {
		for(Entity e : entities) {
			e.beginStep();
		}
		processAddStack();
		processRemoveStack();
	}

	@Override
	public void onStep() {
		for(Entity e : entities) {
			e.onStep();
		}
		processAddStack();
		processRemoveStack();
	}

	@Override
	public void endStep() {
		for(Entity e : entities) {
			e.endStep();
		}
		processAddStack();
		processRemoveStack();
	}

}
